package app.lovable.luckyvpnmaster.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks VPN connection history in SharedPreferences.
 * Keeps the most recent 50 entries as a JSON array.
 */
public class ConnectionHistoryManager {

    private static final String PREFS_NAME = "history_prefs";
    private static final String KEY_HISTORY = "connection_history";
    private static final int MAX_ENTRIES = 50;

    private final SharedPreferences prefs;

    public ConnectionHistoryManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Records the start of a new connection. Returns the start time millis. */
    public long recordConnect(String serverName, String country) {
        long now = System.currentTimeMillis();
        List<ConnectionRecord> history = getHistory();
        history.add(0, new ConnectionRecord(serverName, country, now, 0L, true));
        trim(history);
        save(history);
        return now;
    }

    /**
     * Marks the most recent open record as disconnected and stamps its duration.
     * Call on both expected and unexpected disconnects.
     */
    public void recordDisconnect(long durationSec) {
        List<ConnectionRecord> history = getHistory();
        if (!history.isEmpty()) {
            ConnectionRecord latest = history.get(0);
            if (latest.connected) {
                latest.connected = false;
                latest.durationSec = durationSec;
            }
        }
        save(history);
    }

    public List<ConnectionRecord> getHistory() {
        List<ConnectionRecord> out = new ArrayList<>();
        String json = prefs.getString(KEY_HISTORY, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                out.add(new ConnectionRecord(
                        o.optString("serverName", ""),
                        o.optString("country", ""),
                        o.optLong("startTimeMillis", 0L),
                        o.optLong("durationSec", 0L),
                        o.optBoolean("connected", false)));
            }
        } catch (JSONException ignored) {
            // Corrupted prefs -> return empty list.
        }
        return out;
    }

    public void clear() {
        prefs.edit().remove(KEY_HISTORY).apply();
    }

    private void trim(List<ConnectionRecord> history) {
        while (history.size() > MAX_ENTRIES) {
            history.remove(history.size() - 1);
        }
    }

    private void save(List<ConnectionRecord> history) {
        JSONArray arr = new JSONArray();
        for (ConnectionRecord r : history) {
            try {
                JSONObject o = new JSONObject();
                o.put("serverName", r.serverName);
                o.put("country", r.country);
                o.put("startTimeMillis", r.startTimeMillis);
                o.put("durationSec", r.durationSec);
                o.put("connected", r.connected);
                arr.put(o);
            } catch (JSONException ignored) {
            }
        }
        prefs.edit().putString(KEY_HISTORY, arr.toString()).apply();
    }

    public static class ConnectionRecord {
        public String serverName;
        public String country;
        public long startTimeMillis;
        public long durationSec;
        public boolean connected;

        public ConnectionRecord(String serverName, String country,
                                long startTimeMillis, long durationSec, boolean connected) {
            this.serverName = serverName;
            this.country = country;
            this.startTimeMillis = startTimeMillis;
            this.durationSec = durationSec;
            this.connected = connected;
        }
    }
}
