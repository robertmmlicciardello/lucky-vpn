package app.lovable.luckyvpnmaster.ads;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import app.lovable.luckyvpnmaster.auth.AuthManager;

/**
 * Central helper for REAL AdMob ads (rewarded, interstitial).
 *
 * IMPORTANT: all ad unit IDs below are Google TEST IDs. They always serve
 * test ads — replace them with the REAL IDs from https://apps.admob.com
 * (marked TODO below) before the Play Store release, otherwise no revenue.
 *
 * Every entry point degrades gracefully: if an ad can't load or show
 * (no network, no fill, Play Services missing), the caller's fallback runs
 * so the feature never breaks.
 */
public final class AdMobManager {
    private static final String TAG = "AdMobManager";

    // ------------------------------------------------------------------
    // TODO(owner): replace these TEST unit IDs with the REAL ones from
    // AdMob (Apps > Lucky VPN > Ad units) before publishing.
    // ------------------------------------------------------------------
    /** TODO(owner): real rewarded ad unit ID (format ca-app-pub-XXXX/YYYY). */
    public static final String REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"; // TEST
    /** TODO(owner): real interstitial ad unit ID. */
    public static final String INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"; // TEST
    /** TODO(owner): real banner ad unit ID (used in fragment_servers.xml). */
    public static final String BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"; // TEST

    private static final String PREFS = "lucky_vpn_ads";
    private static final String KEY_LAST_INTERSTITIAL_MS = "last_interstitial_ms";
    /** Interstitial shown at most once per 5 minutes. */
    private static final long INTERSTITIAL_COOLDOWN_MS = 5 * 60 * 1000L;

    private static volatile boolean initialized = false;
    private static RewardedAd rewardedAd;
    private static InterstitialAd interstitialAd;

    private AdMobManager() {}

    /** Call once from MainActivity.onCreate(). Safe to call multiple times. */
    public static synchronized void initialize(Context context) {
        if (initialized) return;
        initialized = true;
        try {
            MobileAds.initialize(context, status -> {
                Log.d(TAG, "AdMob initialized");
                // Pre-load both fullscreen formats so they're ready when needed.
                preloadRewarded(context.getApplicationContext());
                preloadInterstitial(context.getApplicationContext());
            });
        } catch (Exception e) {
            Log.w(TAG, "MobileAds.initialize failed (Play Services missing?)", e);
        }
    }

    /** True for premium users — they never see ads. */
    public static boolean isPremium(Context context) {
        try {
            return new AuthManager(context).isPremium();
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Rewarded ads ("Watch Video")
    // ---------------------------------------------------------------

    public interface RewardListener {
        /** Fired ONLY from onUserEarnedReward — safe to grant the reward. */
        void onRewardEarned();
        /** Ad unavailable (load/show failed) — caller should run its fallback. */
        void onAdUnavailable();
    }

    private static void preloadRewarded(Context context) {
        RewardedAd.load(context, REWARDED_UNIT_ID, new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override public void onAdLoaded(RewardedAd ad) {
                        rewardedAd = ad;
                        Log.d(TAG, "Rewarded ad loaded");
                    }
                    @Override public void onAdFailedToLoad(LoadAdError error) {
                        Log.w(TAG, "Rewarded ad failed to load: " + error.getMessage());
                    }
                });
    }

    /**
     * Shows the rewarded ad. {@code listener.onRewardEarned()} is called only
     * when the user actually earned the reward; the caller grants the reward
     * there (POST /rewards/watch-ad). Any failure routes to
     * {@code listener.onAdUnavailable()} so the caller can fall back to the
     * simulated flow — the feature never breaks.
     */
    public static void showRewardedAd(Activity activity, RewardListener listener) {
        if (activity == null || activity.isFinishing()) {
            listener.onAdUnavailable();
            return;
        }
        RewardedAd ad = rewardedAd;
        if (ad == null) {
            preloadRewarded(activity.getApplicationContext());
            listener.onAdUnavailable();
            return;
        }
        rewardedAd = null; // one-shot; pre-load the next one
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                preloadRewarded(activity.getApplicationContext());
            }
            @Override public void onAdFailedToShowFullScreenContent(AdError error) {
                Log.w(TAG, "Rewarded ad failed to show: " + error.getMessage());
                preloadRewarded(activity.getApplicationContext());
                listener.onAdUnavailable();
            }
        });
        try {
            ad.show(activity, rewardItem -> listener.onRewardEarned());
        } catch (Exception e) {
            Log.w(TAG, "Rewarded ad show crashed", e);
            listener.onAdUnavailable();
        }
    }

    // ---------------------------------------------------------------
    // Interstitial ads (before VPN connect)
    // ---------------------------------------------------------------

    private static void preloadInterstitial(Context context) {
        InterstitialAd.load(context, INTERSTITIAL_UNIT_ID, new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override public void onAdLoaded(InterstitialAd ad) {
                        interstitialAd = ad;
                        Log.d(TAG, "Interstitial ad loaded");
                    }
                    @Override public void onAdFailedToLoad(LoadAdError error) {
                        Log.w(TAG, "Interstitial ad failed to load: " + error.getMessage());
                    }
                });
    }

    /**
     * Shows an interstitial before {@code proceed} runs, at most once per
     * 5 minutes. Premium users skip it. {@code proceed} ALWAYS runs —
     * on dismiss, on ad failure, or when the ad isn't ready — so the VPN
     * connect is never blocked by ads.
     */
    public static void showInterstitialIfDue(Activity activity, Runnable proceed) {
        if (activity == null || activity.isFinishing()) {
            proceed.run();
            return;
        }
        Context appCtx = activity.getApplicationContext();
        if (isPremium(appCtx)) {
            proceed.run();
            return;
        }
        SharedPreferences prefs = appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        if (now - prefs.getLong(KEY_LAST_INTERSTITIAL_MS, 0) < INTERSTITIAL_COOLDOWN_MS) {
            proceed.run(); // cooldown — connect straight away
            return;
        }
        InterstitialAd ad = interstitialAd;
        if (ad == null) {
            preloadInterstitial(appCtx);
            proceed.run();
            return;
        }
        interstitialAd = null; // one-shot; pre-load the next one
        prefs.edit().putLong(KEY_LAST_INTERSTITIAL_MS, now).apply();
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                preloadInterstitial(appCtx);
                proceed.run();
            }
            @Override public void onAdFailedToShowFullScreenContent(AdError error) {
                Log.w(TAG, "Interstitial failed to show: " + error.getMessage());
                preloadInterstitial(appCtx);
                proceed.run();
            }
        });
        try {
            ad.show(activity);
        } catch (Exception e) {
            Log.w(TAG, "Interstitial show crashed", e);
            proceed.run();
        }
    }
}
