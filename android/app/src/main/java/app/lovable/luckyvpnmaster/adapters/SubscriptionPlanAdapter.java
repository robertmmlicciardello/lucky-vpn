
package app.lovable.luckyvpnmaster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.models.SubscriptionPlan;

public class SubscriptionPlanAdapter extends RecyclerView.Adapter<SubscriptionPlanAdapter.PlanViewHolder> {
    private Context context;
    private List<SubscriptionPlan> plans;
    private OnPlanClickListener listener;

    public interface OnPlanClickListener {
        void onPlanClick(SubscriptionPlan plan);
    }

    public SubscriptionPlanAdapter(Context context, OnPlanClickListener listener) {
        this.context = context;
        this.plans = new ArrayList<>();
        this.listener = listener;
    }

    public void updatePlans(List<SubscriptionPlan> newPlans) {
        this.plans.clear();
        this.plans.addAll(newPlans);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subscription_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        SubscriptionPlan plan = plans.get(position);
        holder.bind(plan);
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    class PlanViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPlanName, tvDuration, tvPrice, tvFeatures;
        private Button btnSubscribe;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanName = itemView.findViewById(R.id.tv_plan_name);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvFeatures = itemView.findViewById(R.id.tv_features);
            btnSubscribe = itemView.findViewById(R.id.btn_subscribe);

            btnSubscribe.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onPlanClick(plans.get(getAdapterPosition()));
                }
            });
        }

        public void bind(SubscriptionPlan plan) {
            tvPlanName.setText(plan.name);
            tvDuration.setText(plan.duration);
            tvPrice.setText(String.format("%.0f %s", plan.price, plan.currency));
            
            StringBuilder features = new StringBuilder();
            for (int i = 0; i < plan.features.length; i++) {
                features.append("• ").append(plan.features[i]);
                if (i < plan.features.length - 1) {
                    features.append("\n");
                }
            }
            tvFeatures.setText(features.toString());
        }
    }
}
