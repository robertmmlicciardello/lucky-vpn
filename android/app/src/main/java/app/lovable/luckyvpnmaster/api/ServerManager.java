
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

    public ServerManager(Context context) {
        this.context = context;
        this.authManager = new AuthManager(context);
    }

    public interface ServerCallback {
        void onSuccess(Server server);
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

    private Server parseServer(JSONObject json) throws Exception {
        Server server = new Server();
        server.id = json.getInt("id");
        server.name = json.getString("name");
        server.country = json.getString("country");
        server.city = json.optString("city", "");
        server.ip = json.getString("ip");
        server.port = json.getString("port");
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
