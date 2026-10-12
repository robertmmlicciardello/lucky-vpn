package app.lovable.luckyvpnmaster.models;

import org.json.JSONObject;

/**
 * One row of the points leaderboard (P1).
 */
public class LeaderboardEntry {
    public int rank;
    public int id;
    public String name;
    public int points;

    public static LeaderboardEntry fromJson(JSONObject o) {
        LeaderboardEntry e = new LeaderboardEntry();
        e.rank = o.optInt("rank", 0);
        e.id = o.optInt("id", 0);
        e.name = o.optString("name", "");
        e.points = o.optInt("points", 0);
        return e;
    }
}
