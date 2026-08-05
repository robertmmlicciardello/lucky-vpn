
package app.lovable.luckyvpnmaster;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdManager {
    private static final String API_BASE_URL = "https://your-api-domain.com/api/v1";
    private Context context;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    private PointsManager pointsManager;

    public AdManager(Context context) {
        this.context = context;
        this.pointsManager = new PointsManager(context);
        
        // Initialize AdMob
        MobileAds.initialize(context, initializationStatus -> {
            Log.d("AdManager", "AdMob initialized");
        });
        
        loadAdConfiguration();
    }

    private void loadAdConfiguration() {
        new Thread(() -> {
            try {
                URL url = new URL(API_BASE_URL + "/ads/config");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JSONObject config = new JSONObject(response.toString());
                if (config.getBoolean("success")) {
                    JSONObject adConfig = config.getJSONObject("config");
                    
                    // Load different ad types based on configuration
                    if (adConfig.getBoolean("interstitial_enabled")) {
                        loadInterstitialAd(adConfig.getString("interstitial_id"));
                    }
                    
                    if (adConfig.getBoolean("rewarded_enabled")) {
                        loadRewardedAd(adConfig.getString("rewarded_id"));
                    }
                }

            } catch (Exception e) {
                Log.e("AdManager", "Error loading ad configuration", e);
            }
        }).start();
    }

    private void loadInterstitialAd(String adUnitId) {
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(adUnitId);
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void loadRewardedAd(String adUnitId) {
        RewardedAd.load(context, adUnitId, new AdRequest.Builder().build(),
            new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(RewardedAd ad) {
                    rewardedAd = ad;
                    Log.d("AdManager", "Rewarded ad loaded");
                }
            });
    }

    public void showInterstitialAd() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    public void showRewardedAd(RewardCallback callback) {
        if (rewardedAd != null) {
            rewardedAd.show(/* activity */, rewardItem -> {
                // User earned reward
                int points = rewardItem.getAmount();
                pointsManager.addPoints(points, "Watched rewarded video");
                callback.onRewarded(points);
            });
        }
    }

    public interface RewardCallback {
        void onRewarded(int points);
    }
}
