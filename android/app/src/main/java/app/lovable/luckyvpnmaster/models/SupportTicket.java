package app.lovable.luckyvpnmaster.models;

import org.json.JSONObject;

public class SupportTicket {
    public int id;
    public String subject;
    public String message;
    public String category;
    public String status;
    public String adminResponse;
    public String createdAt;

    public static SupportTicket fromJson(JSONObject json) {
        SupportTicket t = new SupportTicket();
        t.id = json.optInt("id", 0);
        t.subject = json.optString("subject", "");
        t.message = json.optString("message", "");
        t.category = json.optString("category", "");
        t.status = json.optString("status", "open");
        t.adminResponse = json.isNull("admin_response") ? null : json.optString("admin_response", null);
        t.createdAt = json.optString("createdAt", "");
        return t;
    }
}
