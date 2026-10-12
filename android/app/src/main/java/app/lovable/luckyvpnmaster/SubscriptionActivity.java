
package app.lovable.luckyvpnmaster;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import app.lovable.luckyvpnmaster.adapters.SubscriptionPlanAdapter;
import app.lovable.luckyvpnmaster.models.SubscriptionPlan;
import app.lovable.luckyvpnmaster.billing.BillingManager;

public class SubscriptionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SubscriptionPlanAdapter adapter;
    private TextView tvCurrentPlan;
    private android.widget.ImageButton btnBack;
    private BillingManager billingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        initViews();
        setupClickListeners();
        setupRecyclerView();
        loadSubscriptionPlans();
        setupGooglePlayBilling();
    }

    @Override
    protected void onDestroy() {
        if (billingManager != null) billingManager.destroy();
        super.onDestroy();
    }

    /** Google Play billing: shows the "Pay with Google Play" button only when
     *  billing is actually available; the manual KPay/Wave flow always stays. */
    private void setupGooglePlayBilling() {
        billingManager = new BillingManager(this,
                available -> runOnUiThread(() -> adapter.setGooglePlayVisible(available)),
                new BillingManager.PurchaseListener() {
                    @Override
                    public void onPurchaseSuccess(String orderId, String sku, double amount) {
                        // Report the Play purchase to OUR backend like a manual
                        // payment; admin review then activates premium.
                        new PaymentManager(SubscriptionActivity.this).submitPayment(
                                orderId, amount, "google_play",
                                BillingManager.planTypeForSku(sku),
                                BillingManager.durationDaysForSku(sku),
                                new PaymentManager.PaymentCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(SubscriptionActivity.this,
                                                    R.string.google_play_purchase_ok,
                                                    Toast.LENGTH_LONG).show();
                                            finish();
                                        });
                                    }

                                    @Override
                                    public void onError(String error) {
                                        runOnUiThread(() -> Toast.makeText(
                                                SubscriptionActivity.this, error,
                                                Toast.LENGTH_LONG).show());
                                    }
                                });
                    }

                    @Override
                    public void onPurchaseError(String message) {
                        runOnUiThread(() -> Toast.makeText(SubscriptionActivity.this,
                                R.string.google_play_purchase_failed,
                                Toast.LENGTH_SHORT).show());
                    }
                });
        billingManager.start();
    }

    /** Maps our plan ids to Play Console SUBS product ids (quarterly has none). */
    private String skuForPlan(String planId) {
        if ("monthly".equals(planId)) return BillingManager.SKU_MONTHLY;
        if ("yearly".equals(planId)) return BillingManager.SKU_YEARLY;
        return null; // TODO(owner): create a "premium_quarterly" SKU in Play Console if wanted
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        tvCurrentPlan = findViewById(R.id.tv_current_plan);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubscriptionPlanAdapter(this, plan -> {
            // Handle plan selection
            Toast.makeText(this, "Selected: " + plan.name, Toast.LENGTH_SHORT).show();
            // Open payment activity
            PaymentManager paymentManager = new PaymentManager(this);
            paymentManager.initiatePayment(plan.id, "kpay", plan.price);
        });
        adapter.setGooglePlayListener(plan -> {
            String sku = skuForPlan(plan.id);
            if (sku == null) {
                Toast.makeText(this, R.string.google_play_only_monthly_yearly,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (billingManager != null && billingManager.isSkuAvailable(sku)) {
                billingManager.launchPurchase(sku);
            } else {
                Toast.makeText(this, R.string.google_play_unavailable,
                        Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadSubscriptionPlans() {
        List<SubscriptionPlan> plans = new ArrayList<>();
        
        SubscriptionPlan monthly = new SubscriptionPlan();
        monthly.id = "monthly";
        monthly.name = "Monthly Premium";
        monthly.duration = "1 Month";
        monthly.price = 5000;
        monthly.currency = "MMK";
        monthly.features = new String[]{"Unlimited VPN", "Premium Servers", "No Ads", "24/7 Support"};
        
        SubscriptionPlan quarterly = new SubscriptionPlan();
        quarterly.id = "quarterly";
        quarterly.name = "Quarterly Premium";
        quarterly.duration = "3 Months";
        quarterly.price = 12000;
        quarterly.currency = "MMK";
        quarterly.features = new String[]{"Unlimited VPN", "Premium Servers", "No Ads", "24/7 Support", "20% Discount"};
        
        SubscriptionPlan yearly = new SubscriptionPlan();
        yearly.id = "yearly";
        yearly.name = "Yearly Premium";
        yearly.duration = "12 Months";
        yearly.price = 40000;
        yearly.currency = "MMK";
        yearly.features = new String[]{"Unlimited VPN", "Premium Servers", "No Ads", "24/7 Support", "33% Discount", "Priority Support"};
        
        plans.add(monthly);
        plans.add(quarterly);
        plans.add(yearly);
        
        adapter.updatePlans(plans);
    }
}
