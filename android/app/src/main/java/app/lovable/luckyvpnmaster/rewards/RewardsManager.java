
package app.lovable.luckyvpnmaster.rewards;

import android.content.Context;
import android.content.SharedPreferences;
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

    /** Client-side wheel cooldown (matches the server's 1/hour rule). */
    private static final String REWARDS_PREFS = "LuckyVPNRewards";
    private static final String KEY_LAST_WHEEL_SPIN = "last_wheel_spin";
    private static final long WHEEL_COOLDOWN_MS = 60L * 60L * 1000L;

    public RewardsManager(Context context) {
        this.context = context;
        this.authManager = new AuthManager(context);
    }

    public interface RewardCallback {
        void onSuccess(int pointsEarned);
        void onError(String error);
    }

    public interface RedeemCallback {
        void onSuccess(int newPointsBalance, String message, JSONObject data);
        void onError(String error);
    }

    public interface ReferralCodeCallback {
        void onResult(String referralCode);
        void onError(String error);
    }

    /** Shared POST helper: background thread + HttpURLConnection + JSONObject. */
    private void postJson(String endpoint, JSONObject body, RewardCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(APIConfig.API_BASE_URL + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + authManager.getAccessToken());
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                if (body != null) {
                    conn.setDoOutput(true);
                    byte[] bytes = body.toString().getBytes("UTF-8");
                    conn.getOutputStream().write(bytes);
                    conn.getOutputStream().close();
                }

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
                if (responseCode >= 200 && responseCode < 300 && responseJson.optBoolean("success", false)) {
                    callback.onSuccess(responseJson.optInt("points", 0));
                } else {
                    callback.onError(responseJson.optString("message",
                            "Request failed (" + responseCode + ")"));
                }

            } catch (Exception e) {
                Log.e("RewardsManager", "Error posting to " + endpoint, e);
                callback.onError("Network error");
            }
        }).start();
    }

    /** Lucky wheel spin — server picks a random 10–1000 pts, once per hour. */
    public void wheelSpin(RewardCallback callback) {
        postJson(APIConfig.WHEEL_REWARD_ENDPOINT, null, callback);
    }

    /** Share-app reward — 200 pts, once per day. */
    public void shareReward(RewardCallback callback) {
        postJson(APIConfig.SHARE_REWARD_ENDPOINT, null, callback);
    }

    /** Rate-app reward — 300 pts, once ever. */
    public void rateReward(RewardCallback callback) {
        postJson(APIConfig.RATE_REWARD_ENDPOINT, null, callback);
    }

    /** Redeem 1000 pts for 7 days of premium. */
    public void redeemPremium(RedeemCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(APIConfig.API_BASE_URL + APIConfig.REDEEM_REWARD_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + authManager.getAccessToken());
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("reward", "premium_7d");
                byte[] bytes = body.toString().getBytes("UTF-8");
                conn.getOutputStream().write(bytes);
                conn.getOutputStream().close();

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
                if (responseCode >= 200 && responseCode < 300 && responseJson.optBoolean("success", false)) {
                    int balance = responseJson.optInt("points", 0);
                    String message = responseJson.optString("message", "");
                    JSONObject data = responseJson.optJSONObject("data");
                    if (data == null) data = new JSONObject();
                    callback.onSuccess(balance, message, data);
                } else {
                    callback.onError(responseJson.optString("message",
                            "Redeem failed (" + responseCode + ")"));
                }
            } catch (Exception e) {
                Log.e("RewardsManager", "Error redeeming premium", e);
                callback.onError("Network error");
            }
        }).start();
    }

    /**
     * Fetch the user's referral_code from GET /users/profile.
     * GET variant of the same pattern (used for the Invite screen).
     */
    public void fetchReferralCode(ReferralCodeCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(APIConfig.API_BASE_URL + APIConfig.PROFILE_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + authManager.getAccessToken());
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);

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
                if (responseCode >= 200 && responseCode < 300) {
                    JSONObject u = responseJson.optJSONObject("data");
                    if (u == null) u = responseJson;
                    String code = u.optString("referral_code", "");
                    if (code.isEmpty()) code = u.optString("referralCode", "");
                    if (!code.isEmpty()) {
                        authManager.saveReferralCode(code);
                        callback.onResult(code);
                    } else {
                        callback.onError("Referral code not available");
                    }
                } else {
                    callback.onError(responseJson.optString("message",
                            "Request failed (" + responseCode + ")"));
                }
            } catch (Exception e) {
                Log.e("RewardsManager", "Error fetching referral code", e);
                callback.onError("Network error");
            }
        }).start();
    }

    /** Milliseconds remaining before the wheel can spin again (0 = ready). */
    public long getWheelCooldownRemainingMs() {
        long last = context.getSharedPreferences(REWARDS_PREFS, Context.MODE_PRIVATE)
                .getLong(KEY_LAST_WHEEL_SPIN, 0);
        long elapsed = System.currentTimeMillis() - last;
        return elapsed >= WHEEL_COOLDOWN_MS ? 0 : WHEEL_COOLDOWN_MS - elapsed;
    }

    /** Record a successful wheel spin to drive the local cooldown. */
    public void recordWheelSpin() {
        context.getSharedPreferences(REWARDS_PREFS, Context.MODE_PRIVATE)
                .edit().putLong(KEY_LAST_WHEEL_SPIN, System.currentTimeMillis()).apply();
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

    public void watchAdReward(RewardCallback callback) {        new Thread(() -> {
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

    /** Generic points grant (e.g. rewarded video). Posts to the ad-reward endpoint. */
    public void addPoints(int points, String reason, RewardCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(APIConfig.API_BASE_URL + APIConfig.WATCH_AD_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + authManager.getAccessToken());
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("points", points);
                body.put("reason", reason);
                byte[] bytes = body.toString().getBytes("UTF-8");
                conn.getOutputStream().write(bytes);
                conn.getOutputStream().close();

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
                    callback.onSuccess(responseJson.optInt("points", points));
                } else {
                    callback.onError(responseJson.optString("message", "Reward failed"));
                }
            } catch (Exception e) {
                Log.e("RewardsManager", "Error adding points", e);
                callback.onError("Network error");
            }
        }).start();
    }
}
