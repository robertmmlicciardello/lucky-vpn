
package app.lovable.luckyvpnmaster.models;

public class RewardItem {
    public String title;
    public String description;
    public int points;
    public String type;
    public int iconResource;
    public boolean available;

    public RewardItem() {}

    public RewardItem(String title, String description, int points, int iconResource) {
        this.title = title;
        this.description = description;
        this.points = points;
        this.iconResource = iconResource;
        this.available = true;
    }
}
