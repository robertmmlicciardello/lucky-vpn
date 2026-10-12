package app.lovable.luckyvpnmaster.models;

import org.json.JSONObject;

/** One row from GET /rewards/ (user reward transaction history). */
public class RewardTransaction {
    public int id;
    /** daily | ad | referral | bonus */
    public String type = "";
    public int points = 0;
    public String description = "";
    public String createdAt = "";

    public static RewardTransaction fromJson(JSONObject o) {
        RewardTransaction t = new RewardTransaction();
        t.id = o.optInt("id");
        t.type = o.optString("type", "");
        t.points = o.optInt("points", 0);
        t.description = o.optString("description", "");
        t.createdAt = o.optString("created_at", o.optString("createdAt", ""));
        return t;
    }
}
