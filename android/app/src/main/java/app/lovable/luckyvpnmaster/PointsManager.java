
package app.lovable.luckyvpnmaster;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import app.lovable.luckyvpnmaster.api.APIConfig;
import app.lovable.luckyvpnmaster.auth.AuthManager;

public class PointsManager {
    private Context context;
    private AuthManager authManager;

    public PointsManager(Context context) {
        this.context = context;
        this.authManager = new AuthManager(context);
    }

    public void addPoints(int points, String reason) {
        new Thread(() -> {
            try {
                URL url = new URL(APIConfig.API_BASE_URL + "/rewards/add-points");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + authManager.getAccessToken());
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
                    Log.d("PointsManager", "Points added successfully: " + points);
                }

            } catch (Exception e) {
                Log.e("PointsManager", "Error adding points", e);
            }
        }).start();
    }
}
