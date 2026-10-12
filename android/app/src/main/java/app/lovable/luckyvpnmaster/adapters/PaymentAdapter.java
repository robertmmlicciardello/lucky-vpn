package app.lovable.luckyvpnmaster.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
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
import app.lovable.luckyvpnmaster.models.PaymentRecord;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {
    private final Context context;
    private final List<PaymentRecord> records = new ArrayList<>();

    public PaymentAdapter(Context context) {
        this.context = context;
    }

    public void updateRecords(List<PaymentRecord> newRecords) {
        records.clear();
        records.addAll(newRecords);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        holder.bind(records.get(position));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class PaymentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAmount, tvStatus, tvPlan, tvMethod, tvTxn, tvDate;

        PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tv_payment_amount);
            tvStatus = itemView.findViewById(R.id.tv_payment_status);
            tvPlan = itemView.findViewById(R.id.tv_payment_plan);
            tvMethod = itemView.findViewById(R.id.tv_payment_method);
            tvTxn = itemView.findViewById(R.id.tv_payment_txn);
            tvDate = itemView.findViewById(R.id.tv_payment_date);
        }

        void bind(PaymentRecord r) {
            tvAmount.setText(formatAmount(r.amount));
            tvPlan.setText(context.getString(R.string.p1b_payment_plan, labelOrDash(r.planType)));
            tvMethod.setText(context.getString(R.string.p1b_payment_method, labelOrDash(r.paymentMethod)));
            tvTxn.setText(context.getString(R.string.p1b_payment_txn, labelOrDash(r.transactionId)));
            tvDate.setText(DateFmt.fmt(r.createdAt));

            String status = r.status != null ? r.status.toLowerCase() : "pending";
            int colorRes;
            int labelRes;
            switch (status) {
                case "approved":
                    colorRes = R.color.vpn_green;
                    labelRes = R.string.p1b_status_approved;
                    break;
                case "rejected":
                    colorRes = R.color.vpn_red;
                    labelRes = R.string.p1b_status_rejected;
                    break;
                default:
                    colorRes = android.R.color.holo_orange_dark;
                    labelRes = R.string.p1b_status_pending;
                    break;
            }
            tvStatus.setText(context.getString(labelRes));
            tvStatus.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)));
        }

        private String labelOrDash(String s) {
            return (s == null || s.isEmpty()) ? "—" : s;
        }

        /** "5000" / "5000.0" -> "5,000"; leaves non-numeric amounts untouched. */
        private String formatAmount(String amount) {
            if (amount == null || amount.isEmpty()) return "—";
            try {
                double d = Double.parseDouble(amount.replace(",", "").trim());
                if (d == Math.rint(d)) {
                    return String.format("%,d", (long) d);
                }
                return String.format("%,.2f", d);
            } catch (NumberFormatException e) {
                return amount;
            }
        }
    }
}
