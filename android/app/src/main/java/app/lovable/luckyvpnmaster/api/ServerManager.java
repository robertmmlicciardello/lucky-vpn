
package app.lovable.luckyvpnmaster.api;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.Server;

public class ServerManager {
    private Context context;
    private AuthManager authManager;

    private static final String PREFS = "lucky_vpn_prefs";
    private static final String KEY_SELECTED_SERVER = "selected_server_json";

    public ServerManager(Context context) {
        this.context = context;
        this.authManager = new AuthManager(context);
    }

    /** Persist the user's chosen server so Home/Connect uses it. */
    public void saveSelectedServer(Server server) {
        try {
            JSONObject j = new JSONObject();
            j.put("id", server.id);
            j.put("name", server.name);
            j.put("country", server.country);
            j.put("city", server.city);
            j.put("ip", server.ip);
            j.put("port", server.port);
            j.put("protocol", server.protocol);
            j.put("type", server.type);
            j.put("status", server.status);
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putString(KEY_SELECTED_SERVER, j.toString()).apply();
        } catch (Exception e) {
            Log.e("ServerManager", "saveSelectedServer failed", e);
        }
    }

    /** The server the user picked in the Servers tab, or null if none. */
    public Server getSelectedServer() {
        try {
            String json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getString(KEY_SELECTED_SERVER, null);
            if (json == null) return null;
            return parseServer(new JSONObject(json));
        } catch (Exception e) {
            return null;
        }
    }

    public interface ServerCallback {
        void onSuccess(Server server);
        void onError(String error);
    }

    public interface ConfigCallback {
        void onSuccess(String ovpnConfig);
        void onError(String error);
    }

    public interface ServersCallback {
        void onSuccess(List<Server> servers);
        void onError(String error);
    }

    public void getFreeServers(ServersCallback callback) {
        getServers(APIConfig.FREE_SERVERS_ENDPOINT, callback);
    }

    public void getPremiumServers(ServersCallback callback) {
        getServers(APIConfig.PREMIUM_SERVERS_ENDPOINT, callback);
    }

    public void getBestServer(ServerCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(APIConfig.API_BASE_URL + APIConfig.FREE_SERVERS_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                String token = authManager.getAccessToken();
                if (token != null && !token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
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
                if (responseCode == 200 && responseJson.getBoolean("success")) {
                    JSONArray serversArray = responseJson.getJSONArray("data");
                    if (serversArray.length() > 0) {
                        JSONObject serverJson = serversArray.getJSONObject(0);
                        Server server = parseServer(serverJson);
                        callback.onSuccess(server);
                    } else {
                        callback.onError("No servers available");
                    }
                } else {
                    callback.onError("Failed to load servers");
                }

            } catch (Exception e) {
                Log.e("ServerManager", "Error getting best server", e);
                callback.onError("Network error");
            }
        }).start();
    }

    private void getServers(String endpoint, ServersCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(APIConfig.API_BASE_URL + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                String token = authManager.getAccessToken();
                if (token != null && !token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
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
                if (responseCode == 200 && responseJson.getBoolean("success")) {
                    JSONArray serversArray = responseJson.getJSONArray("data");
                    List<Server> servers = new ArrayList<>();
                    
                    for (int i = 0; i < serversArray.length(); i++) {
                        JSONObject serverJson = serversArray.getJSONObject(i);
                        Server server = parseServer(serverJson);
                        servers.add(server);
                    }
                    
                    callback.onSuccess(servers);
                } else {
                    callback.onError("Failed to load servers");
                }

            } catch (Exception e) {
                Log.e("ServerManager", "Error getting servers", e);
                callback.onError("Network error");
            }
        }).start();
    }

    /** Download the inline .ovpn config for a server (GET /servers/{id}/config). */
    public void getServerConfig(int serverId, ConfigCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = String.format(APIConfig.SERVER_CONFIG_TEMPLATE, serverId);
                URL url = new URL(APIConfig.API_BASE_URL + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                String token = authManager.getAccessToken();
                if (token != null && !token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
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
                if (responseCode == 200 && responseJson.getBoolean("success")) {
                    String config = responseJson.getJSONObject("data").optString("config_file", "");
                    if (!config.isEmpty()) callback.onSuccess(config);
                    else callback.onError("No VPN config for this server");
                } else {
                    callback.onError("Failed to load VPN config");
                }
            } catch (Exception e) {
                Log.e("ServerManager", "Error getting server config", e);
                callback.onError("Network error");
            }
        }).start();
    }

    private Server parseServer(JSONObject json) throws Exception {
        Server server = new Server();
        server.id = json.getInt("id");
        server.name = json.optString("name", "");
        server.country = json.optString("country", "");
        server.city = json.optString("city", "");
        server.ip = json.optString("ip", "");
        server.port = json.optString("port", "");
        server.protocol = json.optString("protocol", "OpenVPN");
        server.type = json.optString("type", "free");
        server.status = json.optString("status", "online");
        server.load = json.optInt("load", 0);
        server.users = json.optInt("users", 0);
        server.configFile = json.optString("config_file", "");
        server.provider = json.optString("provider", "manual");
        return server;
    }
}
