package app.lovable.luckyvpnmaster.models;

import org.json.JSONObject;

/**
 * Blog / announcement post model (P1).
 */
public class BlogPost {
    public int id;
    public String title;
    public String excerpt;
    public String featuredImage;
    public String createdAt;
    /** Full content, present only on the detail endpoint (may be HTML). */
    public String content;

    public static BlogPost fromJson(JSONObject o) {
        BlogPost p = new BlogPost();
        p.id = o.optInt("id", 0);
        p.title = o.optString("title", "");
        p.excerpt = o.optString("excerpt", "");
        p.featuredImage = o.optString("featured_image", "");
        p.createdAt = o.optString("createdAt",
                o.optString("created_at", ""));
        p.content = o.optString("content", "");
        return p;
    }

    /** Short YYYY-MM-DD representation of createdAt for list rows. */
    public String getDateShort() {
        if (createdAt == null) return "";
        return createdAt.length() >= 10 ? createdAt.substring(0, 10) : createdAt;
    }
}
