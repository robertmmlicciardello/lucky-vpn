package app.lovable.luckyvpnmaster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.models.SupportTicket;

public class SupportTicketAdapter extends RecyclerView.Adapter<SupportTicketAdapter.TicketViewHolder> {
    private Context context;
    private List<SupportTicket> tickets;

    public SupportTicketAdapter(Context context) {
        this.context = context;
        this.tickets = new ArrayList<>();
    }

    public void updateTickets(List<SupportTicket> newTickets) {
        this.tickets.clear();
        this.tickets.addAll(newTickets);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_support_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        holder.bind(tickets.get(position));
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    class TicketViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSubject, tvMessage, tvCategory, tvStatus, tvDate,
                tvAdminResponseLabel, tvAdminResponse;
        private LinearLayout layoutAdminResponse;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tv_ticket_subject);
            tvMessage = itemView.findViewById(R.id.tv_ticket_message);
            tvCategory = itemView.findViewById(R.id.tv_ticket_category);
            tvStatus = itemView.findViewById(R.id.tv_ticket_status);
            tvDate = itemView.findViewById(R.id.tv_ticket_date);
            tvAdminResponseLabel = itemView.findViewById(R.id.tv_admin_response_label);
            tvAdminResponse = itemView.findViewById(R.id.tv_admin_response);
            layoutAdminResponse = itemView.findViewById(R.id.layout_admin_response);
        }

        public void bind(SupportTicket t) {
            tvSubject.setText(t.subject != null ? t.subject : "");
            tvMessage.setText(t.message != null ? t.message : "");
            tvCategory.setText(t.category != null ? t.category : "");
            tvDate.setText(t.createdAt != null ? t.createdAt : "");

            // Status badge: open=orange, answered/in_progress=blue, closed=gray
            String status = t.status != null ? t.status.toLowerCase() : "open";
            int badgeRes;
            int labelRes;
            if (status.equals("answered") || status.equals("in_progress")) {
                badgeRes = R.drawable.bg_badge_blue;
                labelRes = status.equals("answered")
                        ? R.string.p1_status_answered : R.string.p1_status_in_progress;
            } else if (status.equals("closed")) {
                badgeRes = R.drawable.bg_badge_gray;
                labelRes = R.string.p1_status_closed;
            } else {
                badgeRes = R.drawable.bg_badge_orange;
                labelRes = R.string.p1_status_open;
            }
            tvStatus.setBackgroundResource(badgeRes);
            tvStatus.setText(labelRes);

            // Admin response shown only when present
            if (t.adminResponse != null && !t.adminResponse.isEmpty()) {
                layoutAdminResponse.setVisibility(View.VISIBLE);
                tvAdminResponseLabel.setText(R.string.p1_admin_response);
                tvAdminResponse.setText(t.adminResponse);
            } else {
                layoutAdminResponse.setVisibility(View.GONE);
            }
        }
    }
}
