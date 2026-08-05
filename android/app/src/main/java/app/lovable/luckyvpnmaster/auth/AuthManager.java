
package app.lovable.luckyvpnmaster.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import app.lovable.luckyvpnmaster.api.APIConfig;
import app.lovable.luckyvpnmaster.models.LoginResponse;
import app.lovable.luckyvpnmaster.models.User;

public class AuthManager {
    private static final String PREFS_NAME = "LuckyVPNAuth";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PLAN = "user_plan";
    private static final String KEY_USER_POINTS = "user_points";
    
    private Context context;
    private SharedPreferences prefs;

    public AuthManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public interface AuthCallback {
        void onSuccess(LoginResponse response);
        void onError(String error);
    }

    public void login(String email, String password, AuthCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(APIConfig.API_BASE_URL + APIConfig.LOGIN_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("password", password);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

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
                    String token = responseJson.getString("token");
                    JSONObject userData = responseJson.getJSONObject("data");
                    
                    saveUserData(token, userData);
                    
                    LoginResponse loginResponse = new LoginResponse();
                    loginResponse.success = true;
                    loginResponse.token = token;
                    loginResponse.user = parseUser(userData);
                    
                    callback.onSuccess(loginResponse);
                } else {
                    String errorMessage = responseJson.optString("message", "Login failed");
                    callback.onError(errorMessage);
                }

            } catch (Exception e) {
                Log.e("AuthManager", "Login error", e);
                callback.onError("Network error. Please check your connection.");
            }
        }).start();
    }

    public void register(String name, String email, String password, String referralCode, AuthCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(APIConfig.API_BASE_URL + APIConfig.REGISTER_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("name", name);
                json.put("email", email);
                json.put("password", password);
                if (!referralCode.isEmpty()) {
                    json.put("referralCode", referralCode);
                }

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

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
                
                if (responseCode == 201 && responseJson.getBoolean("success")) {
                    String token = responseJson.getString("token");
                    JSONObject userData = responseJson.getJSONObject("data");
                    
                    saveUserData(token, userData);
                    
                    LoginResponse loginResponse = new LoginResponse();
                    loginResponse.success = true;
                    loginResponse.token = token;
                    loginResponse.user = parseUser(userData);
                    
                    callback.onSuccess(loginResponse);
                } else {
                    String errorMessage = responseJson.optString("message", "Registration failed");
                    callback.onError(errorMessage);
                }

            } catch (Exception e) {
                Log.e("AuthManager", "Register error", e);
                callback.onError("Network error. Please check your connection.");
            }
        }).start();
    }

    private void saveUserData(String token, JSONObject userData) throws Exception {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.putInt(KEY_USER_ID, userData.getInt("id"));
        editor.putString(KEY_USER_NAME, userData.getString("name"));
        editor.putString(KEY_USER_EMAIL, userData.getString("email"));
        editor.putString(KEY_USER_PLAN, userData.optString("plan", "free"));
        editor.putInt(KEY_USER_POINTS, userData.optInt("points", 0));
        editor.apply();
    }

    private User parseUser(JSONObject userData) throws Exception {
        User user = new User();
        user.id = userData.getInt("id");
        user.name = userData.getString("name");
        user.email = userData.getString("email");
        user.plan = userData.optString("plan", "free");
        user.points = userData.optInt("points", 0);
        return user;
    }

    public boolean isLoggedIn() {
        return !getAccessToken().isEmpty();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, "");
    }

    public User getCurrentUser() {
        if (!isLoggedIn()) return null;
        
        User user = new User();
        user.id = prefs.getInt(KEY_USER_ID, 0);
        user.name = prefs.getString(KEY_USER_NAME, "");
        user.email = prefs.getString(KEY_USER_EMAIL, "");
        user.plan = prefs.getString(KEY_USER_PLAN, "free");
        user.points = prefs.getInt(KEY_USER_POINTS, 0);
        return user;
    }

    public void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}
