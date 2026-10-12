package app.lovable.luckyvpnmaster.models;

import org.json.JSONObject;

public class AppNotification {
    public int id;
    public String title;
    public String message;
    public String type;
    public boolean isRead;
    public String createdAt;

    public static AppNotification fromJson(JSONObject json) {
        AppNotification n = new AppNotification();
        n.id = json.optInt("id", 0);
        n.title = json.optString("title", "");
        n.message = json.optString("message", "");
        n.type = json.optString("type", "");
        n.isRead = json.optBoolean("is_read", false);
        n.createdAt = json.optString("createdAt", "");
        return n;
    }
}
