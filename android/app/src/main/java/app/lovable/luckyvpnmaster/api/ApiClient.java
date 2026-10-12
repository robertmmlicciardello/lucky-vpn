package app.lovable.luckyvpnmaster.api;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import app.lovable.luckyvpnmaster.auth.AuthManager;

/**
 * Small shared HTTP helper for the P1 feature screens.
 * Follows the existing ServerManager/AuthManager pattern:
 * HttpURLConnection + JSONObject, background thread, Bearer JWT.
 * Callbacks are invoked on the background thread — post to UI yourself.
 */
public class ApiClient {
    private static final String TAG = "ApiClient";

    public interface Callback {
        void onSuccess(JSONObject data);
        void onError(String error);
    }

    public static void get(Context context, String endpoint, Callback callback) {
        request(context, "GET", endpoint, null, callback);
    }

    public static void post(Context context, String endpoint, JSONObject body, Callback callback) {
        request(context, "POST", endpoint, body, callback);
    }

    public static void put(Context context, String endpoint, JSONObject body, Callback callback) {
        request(context, "PUT", endpoint, body, callback);
    }

    public static void delete(Context context, String endpoint, Callback callback) {
        request(context, "DELETE", endpoint, null, callback);
    }

    private static void request(Context context, String method, String endpoint,
                                JSONObject body, Callback callback) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(APIConfig.API_BASE_URL + endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                String token = new AuthManager(context.getApplicationContext()).getAccessToken();
                if (token != null && !token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                if (body != null) {
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    os.write(body.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();
                }

                int code = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                if (code >= 200 && code < 300 && json.optBoolean("success", code < 300)) {
                    callback.onSuccess(json);
                } else {
                    callback.onError(json.optString("message",
                            "Request failed (" + code + ")"));
                }
            } catch (Exception e) {
                Log.e(TAG, method + " " + endpoint + " error", e);
                callback.onError("Network error. Please check your connection.");
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
