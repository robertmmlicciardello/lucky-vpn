package app.lovable.luckyvpnmaster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.models.RewardTransaction;

/** History list for the Rewards tab: +points, type label, description, date. */
public class RewardTransactionAdapter
        extends RecyclerView.Adapter<RewardTransactionAdapter.TxnViewHolder> {
    private final Context context;
    private final List<RewardTransaction> txns = new ArrayList<>();

    public RewardTransactionAdapter(Context context) {
        this.context = context;
    }

    public void updateTransactions(List<RewardTransaction> newTxns) {
        txns.clear();
        txns.addAll(newTxns);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TxnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_reward_transaction, parent, false);
        return new TxnViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TxnViewHolder holder, int position) {
        holder.bind(txns.get(position));
    }

    @Override
    public int getItemCount() {
        return txns.size();
    }

    class TxnViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPoints, tvType, tvDesc, tvDate;

        TxnViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPoints = itemView.findViewById(R.id.tv_txn_points);
            tvType = itemView.findViewById(R.id.tv_txn_type);
            tvDesc = itemView.findViewById(R.id.tv_txn_desc);
            tvDate = itemView.findViewById(R.id.tv_txn_date);
        }

        void bind(RewardTransaction t) {
            String sign = t.points >= 0 ? "+" : "";
            tvPoints.setText(sign + t.points);
            int colorRes = t.points >= 0 ? R.color.vpn_green : R.color.vpn_red;
            tvPoints.setTextColor(ContextCompat.getColor(context, colorRes));

            tvType.setText(typeLabel(t.type));

            if (t.description == null || t.description.isEmpty()) {
                tvDesc.setVisibility(View.GONE);
            } else {
                tvDesc.setVisibility(View.VISIBLE);
                tvDesc.setText(t.description);
            }

            String date = DateFmt.fmt(t.createdAt);
            tvDate.setVisibility(date.isEmpty() ? View.GONE : View.VISIBLE);
            tvDate.setText(date);
        }

        private String typeLabel(String type) {
            if (type == null) return "";
            switch (type.toLowerCase(Locale.US)) {
                case "daily":    return context.getString(R.string.p1b_reward_type_daily);
                case "ad":       return context.getString(R.string.p1b_reward_type_ad);
                case "referral": return context.getString(R.string.p1b_reward_type_referral);
                case "bonus":    return context.getString(R.string.p1b_reward_type_bonus);
                default:
                    return type.isEmpty() ? "" :
                            type.substring(0, 1).toUpperCase(Locale.US) + type.substring(1);
            }
        }
    }
}
