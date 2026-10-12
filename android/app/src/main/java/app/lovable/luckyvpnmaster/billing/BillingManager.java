package app.lovable.luckyvpnmaster.billing;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google Play Billing wrapper (BillingClient 6.2.1, SUBS products).
 *
 * Flow: connect -> query SKU details -> launchBillingFlow -> onPurchasesUpdated
 * -> acknowledgePurchase -> caller reports the purchase to OUR backend via
 * PaymentManager.submitPayment(..., payment_method='google_play',
 * transaction_id=purchase.getOrderId(), ...).
 *
 * ---------------------------------------------------------------------------
 * TODO(owner) — Play Console setup required before this works in production:
 *   1. Play Console > Monetize > Products > Subscriptions: create TWO
 *      subscription products with EXACTLY these product IDs:
 *        - "premium_monthly" (1 month, ~5,000 MMK)
 *        - "premium_yearly"  (12 months, ~40,000 MMK)
 *      Status must be ACTIVE, and the app must be published (at least to an
 *      internal/closed testing track) under the SAME package + signing key.
 *   2. Play Console > Setup > Payments profile: link a Google payments
 *      merchant profile, or payouts are held.
 *   3. Server side: verify each purchase server-side with the Google Play
 *      Developer API (purchases.subscriptionsv2.get) before granting premium —
 *      the app currently trusts the client callback only.
 *   4. Add license-test accounts (Play Console > Setup > License testing) so
 *      test purchases don't charge real money.
 * ---------------------------------------------------------------------------
 *
 * Degrades gracefully: if the Play Store is missing or the BillingClient
 * can't connect, {@link ReadyListener#onBillingReady(boolean)} fires with
 * false and the caller hides the Google Play button (manual KPay/Wave flow
 * keeps working).
 */
public class BillingManager implements PurchasesUpdatedListener {
    private static final String TAG = "BillingManager";

    public static final String SKU_MONTHLY = "premium_monthly";
    public static final String SKU_YEARLY = "premium_yearly";
    private static final List<String> ALL_SKUS = Arrays.asList(SKU_MONTHLY, SKU_YEARLY);

    /** Plan id used by the app's own backend, per SKU. */
    public static String planTypeForSku(String sku) {
        return SKU_YEARLY.equals(sku) ? "yearly" : "monthly";
    }

    /** Duration in days sent to the backend, per SKU. */
    public static int durationDaysForSku(String sku) {
        return SKU_YEARLY.equals(sku) ? 365 : 30;
    }

    public interface ReadyListener {
        /** Fired once: true when billing is connected AND at least one SKU resolved. */
        void onBillingReady(boolean available);
    }

    public interface PurchaseListener {
        void onPurchaseSuccess(String orderId, String sku, double amount);
        void onPurchaseError(String message);
    }

    private final Activity activity;
    private final ReadyListener readyListener;
    private final PurchaseListener purchaseListener;
    private BillingClient billingClient;
    private final Map<String, SkuDetails> skuDetails = new HashMap<>();
    private boolean readyFired = false;

    public BillingManager(Activity activity, ReadyListener readyListener,
                          PurchaseListener purchaseListener) {
        this.activity = activity;
        this.readyListener = readyListener;
        this.purchaseListener = purchaseListener;
    }

    /** True when the Google Play Store app is installed on this device. */
    public static boolean isPlayStoreInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo("com.android.vending", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /** Connects to Google Play. Safe to call from onCreate. */
    public void start() {
        if (!isPlayStoreInstalled(activity)) {
            Log.w(TAG, "Play Store not installed — Google Play billing unavailable");
            fireReady(false);
            return;
        }
        try {
            billingClient = BillingClient.newBuilder(activity)
                    .setListener(this)
                    // NOTE: the no-arg overload is deprecated in 6.x, but
                    // PendingPurchasesParams only exists in billing 7.0+.
                    .enablePendingPurchases()
                    .build();
        } catch (Exception e) {
            Log.w(TAG, "BillingClient build failed", e);
            fireReady(false);
            return;
        }
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult result) {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    querySkuDetails();
                    queryExistingPurchases(); // catch purchases completed while offline
                } else {
                    Log.w(TAG, "Billing setup failed: " + result.getDebugMessage());
                    fireReady(false);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected");
                fireReady(false);
            }
        });
    }

    private void querySkuDetails() {
        SkuDetailsParams params = SkuDetailsParams.newBuilder()
                .setSkusList(ALL_SKUS)
                .setType(BillingClient.SkuType.SUBS)
                .build();
        billingClient.querySkuDetailsAsync(params, (result, list) -> {
            if (result.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                for (SkuDetails details : list) {
                    skuDetails.put(details.getSku(), details);
                    Log.d(TAG, "SKU resolved: " + details.getSku() + " " + details.getPrice());
                }
            } else {
                Log.w(TAG, "querySkuDetails failed: " + result.getDebugMessage());
            }
            fireReady(!skuDetails.isEmpty());
        });
    }

    /** Acknowledge any already-purchased subs so entitlements aren't lost. */
    private void queryExistingPurchases() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                (result, purchases) -> {
                    if (result.getResponseCode() == BillingClient.BillingResponseCode.OK
                            && purchases != null) {
                        for (Purchase p : purchases) handlePurchase(p);
                    }
                });
    }

    /** True when a Google Play purchase can be launched for this SKU right now. */
    public boolean isSkuAvailable(String sku) {
        return billingClient != null && billingClient.isReady() && skuDetails.containsKey(sku);
    }

    /** Opens the Google Play purchase sheet for the given SKU. */
    public void launchPurchase(String sku) {
        SkuDetails details = skuDetails.get(sku);
        if (details == null || billingClient == null || !billingClient.isReady()) {
            purchaseListener.onPurchaseError("Google Play billing is not ready");
            return;
        }
        BillingFlowParams params = BillingFlowParams.newBuilder()
                .setSkuDetails(details)
                .build();
        BillingResult result = billingClient.launchBillingFlow(activity, params);
        if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "launchBillingFlow failed: " + result.getDebugMessage());
            purchaseListener.onPurchaseError("Could not open Google Play: "
                    + result.getDebugMessage());
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult result, List<Purchase> purchases) {
        int code = result.getResponseCode();
        if (code == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) handlePurchase(purchase);
        } else if (code == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled the Play purchase");
        } else {
            Log.w(TAG, "Purchase failed: " + result.getDebugMessage());
            purchaseListener.onPurchaseError("Google Play purchase failed");
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) {
            Log.d(TAG, "Ignoring non-purchased state: " + purchase.getPurchaseState());
            return;
        }
        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.acknowledgePurchase(params, result -> {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    reportSuccess(purchase);
                } else {
                    Log.w(TAG, "Acknowledge failed: " + result.getDebugMessage());
                    purchaseListener.onPurchaseError("Purchase could not be confirmed");
                }
            });
        } else {
            // Already acknowledged (e.g. restored via queryPurchasesAsync).
            reportSuccess(purchase);
        }
    }

    private void reportSuccess(Purchase purchase) {
        String sku = purchase.getSkus().isEmpty() ? "" : purchase.getSkus().get(0);
        SkuDetails details = skuDetails.get(sku);
        double amount = 0;
        if (details != null) {
            try {
                amount = details.getPriceAmountMicros() / 1_000_000.0;
            } catch (Exception ignored) {}
        }
        Log.d(TAG, "Play purchase success: sku=" + sku + " order=" + purchase.getOrderId());
        purchaseListener.onPurchaseSuccess(purchase.getOrderId(), sku, amount);
    }

    private void fireReady(boolean available) {
        if (readyFired) return;
        readyFired = true;
        readyListener.onBillingReady(available);
    }

    /** Call from Activity.onDestroy(). */
    public void destroy() {
        try {
            if (billingClient != null) billingClient.endConnection();
        } catch (Exception ignored) {}
    }
}
