
package app.lovable.luckyvpnmaster.rewards;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import app.lovable.luckyvpnmaster.api.APIConfig;
import app.lovable.luckyvpnmaster.auth.AuthManager;

public class RewardsManager {
    private Context context;
    private AuthManager authManager;

    public RewardsManager(Context context) {
        this.context = context;
        this.authManager = new AuthManager(context);
    }

    public interface RewardCallback {
        void onSuccess(int pointsEarned);
        void onError(String error);
    }

    public void dailyCheckin(RewardCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(APIConfig.API_BASE_URL + APIConfig.DAILY_REWARD_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + authManager.getAccessToken());

                int responseCode = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JSONObject responseJson = new JSONObject(response.toString());
                if (responseCode == 200 && responseJson.getBoolean("success")) {
                    int pointsEarned = responseJson.getInt("points");
                    callback.onSuccess(pointsEarned);
                } else {
                    String errorMessage = responseJson.optString("message", "Daily check-in failed");
                    callback.onError(errorMessage);
                }

            } catch (Exception e) {
                Log.e("RewardsManager", "Error with daily check-in", e);
                callback.onError("Network error");
            }
        }).start();
    }

    public void watchAdReward(RewardCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(APIConfig.API_BASE_URL + APIConfig.WATCH_AD_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + authManager.getAccessToken());

                int responseCode = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JSONObject responseJson = new JSONObject(response.toString());
                if (responseCode == 200 && responseJson.getBoolean("success")) {
                    int pointsEarned = responseJson.getInt("points");
                    callback.onSuccess(pointsEarned);
                } else {
                    String errorMessage = responseJson.optString("message", "Ad reward failed");
                    callback.onError(errorMessage);
                }

            } catch (Exception e) {
                Log.e("RewardsManager", "Error with ad reward", e);
                callback.onError("Network error");
            }
        }).start();
    }
}
