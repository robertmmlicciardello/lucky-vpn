
package app.lovable.luckyvpnmaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PointsManager {
    private static final String PREFS_NAME = "VPNMasterPoints";
    private static final String POINTS_KEY = "user_points";
    private static final String API_BASE_URL = "https://your-api-domain.com/api/v1";
    
    private Context context;
    private SharedPreferences prefs;

    public PointsManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getLocalPoints() {
        return prefs.getInt(POINTS_KEY, 0);
    }

    public void setLocalPoints(int points) {
        prefs.edit().putInt(POINTS_KEY, points).apply();
    }

    public void addPoints(int points, String reason) {
        new Thread(() -> {
            try {
                URL url = new URL(API_BASE_URL + "/rewards/add-points");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + getAccessToken());
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("points", points);
                json.put("reason", reason);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONObject responseJson = new JSONObject(response.toString());
                    if (responseJson.getBoolean("success")) {
                        int totalPoints = responseJson.getInt("total_points");
                        setLocalPoints(totalPoints);
                        Log.d("PointsManager", "Points added successfully: " + totalPoints);
                    }
                }

            } catch (Exception e) {
                Log.e("PointsManager", "Error adding points", e);
            }
        }).start();
    }

    public void dailyCheckIn() {
        new Thread(() -> {
            try {
                URL url = new URL(API_BASE_URL + "/rewards/daily-checkin");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + getAccessToken());

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONObject responseJson = new JSONObject(response.toString());
                    if (responseJson.getBoolean("success")) {
                        int pointsEarned = responseJson.getInt("points_earned");
                        int totalPoints = responseJson.getInt("total_points");
                        setLocalPoints(totalPoints);
                        Log.d("PointsManager", "Daily check-in successful: +" + pointsEarned + " points");
                    }
                }

            } catch (Exception e) {
                Log.e("PointsManager", "Error with daily check-in", e);
            }
        }).start();
    }

    private String getAccessToken() {
        return prefs.getString("access_token", "");
    }
}
