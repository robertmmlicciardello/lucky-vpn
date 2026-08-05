
package app.lovable.luckyvpnmaster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.models.RewardItem;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {
    private Context context;
    private List<RewardItem> rewards;
    private OnRewardClickListener listener;

    public interface OnRewardClickListener {
        void onRewardClick(RewardItem reward);
    }

    public RewardAdapter(Context context, OnRewardClickListener listener) {
        this.context = context;
        this.rewards = new ArrayList<>();
        this.listener = listener;
    }

    public void updateRewards(List<RewardItem> newRewards) {
        this.rewards.clear();
        this.rewards.addAll(newRewards);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        RewardItem reward = rewards.get(position);
        holder.bind(reward);
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    class RewardViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvTitle, tvDescription, tvPoints;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvPoints = itemView.findViewById(R.id.tv_points);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onRewardClick(rewards.get(getAdapterPosition()));
                }
            });
        }

        public void bind(RewardItem reward) {
            ivIcon.setImageResource(reward.iconResource);
            tvTitle.setText(reward.title);
            tvDescription.setText(reward.description);
            if (reward.points > 0) {
                tvPoints.setText("+" + reward.points + " points");
            } else {
                tvPoints.setText("Surprise points");
            }
        }
    }
}
