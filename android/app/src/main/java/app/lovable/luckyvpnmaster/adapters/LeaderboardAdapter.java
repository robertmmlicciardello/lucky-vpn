package app.lovable.luckyvpnmaster.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.models.LeaderboardEntry;

/**
 * Points leaderboard adapter (P1). Medal colors for ranks 1-3;
 * the current user's row is tinted light blue.
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private static final int COLOR_GOLD = Color.parseColor("#D4A017");
    private static final int COLOR_SILVER = Color.parseColor("#9E9E9E");
    private static final int COLOR_BRONZE = Color.parseColor("#A9662E");

    private final Context context;
    private final List<LeaderboardEntry> entries = new ArrayList<>();
    /** Current user id, or -1 when guest/unknown (then no row is highlighted). */
    private int currentUserId = -1;

    public LeaderboardAdapter(Context context) {
        this.context = context;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public void updateEntries(List<LeaderboardEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        holder.bind(entries.get(position));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRank, tvName, tvPoints;

        LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvName = itemView.findViewById(R.id.tv_lb_name);
            tvPoints = itemView.findViewById(R.id.tv_lb_points);
        }

        void bind(LeaderboardEntry entry) {
            int displayRank = entry.rank > 0 ? entry.rank : getAdapterPosition() + 1;
            tvRank.setText("#" + displayRank);
            tvName.setText(entry.name != null ? entry.name : "");
            tvPoints.setText(context.getString(R.string.leaderboard_points_value, entry.points));

            // Medal colors for top 3
            if (displayRank == 1) {
                tvRank.setTextColor(COLOR_GOLD);
            } else if (displayRank == 2) {
                tvRank.setTextColor(COLOR_SILVER);
            } else if (displayRank == 3) {
                tvRank.setTextColor(COLOR_BRONZE);
            } else {
                tvRank.setTextColor(ContextCompat.getColor(context, R.color.vpn_text_secondary));
            }

            // Highlight current user's row
            if (entry.id == currentUserId && currentUserId != -1) {
                itemView.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.vpn_blue_light));
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }
}
