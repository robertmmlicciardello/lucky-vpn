package app.lovable.luckyvpnmaster.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.models.AppNotification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private Context context;
    private List<AppNotification> notifications;
    private OnNotificationClickListener listener;
    private Set<Integer> expandedIds = new HashSet<>();

    public interface OnNotificationClickListener {
        void onNotificationClick(AppNotification notification, int position);
    }

    public NotificationAdapter(Context context, OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = new ArrayList<>();
        this.listener = listener;
    }

    public void updateNotifications(List<AppNotification> newNotifications) {
        this.notifications.clear();
        this.notifications.addAll(newNotifications);
        this.expandedIds.clear();
        notifyDataSetChanged();
    }

    public AppNotification getItem(int position) {
        return notifications.get(position);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        AppNotification n = notifications.get(position);
        holder.bind(n, expandedIds.contains(n.id));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvMessage, tvDate;
        private View viewDot;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notif_title);
            tvMessage = itemView.findViewById(R.id.tv_notif_message);
            tvDate = itemView.findViewById(R.id.tv_notif_date);
            viewDot = itemView.findViewById(R.id.view_unread_dot);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onNotificationClick(notifications.get(getAdapterPosition()),
                            getAdapterPosition());
                }
            });
        }

        public void bind(AppNotification n, boolean expanded) {
            tvTitle.setText(n.title != null ? n.title : "");
            tvMessage.setText(n.message != null ? n.message : "");
            tvDate.setText(n.createdAt != null ? n.createdAt : "");

            // Unread: bold title + blue dot
            tvTitle.setTypeface(null, n.isRead ? Typeface.NORMAL : Typeface.BOLD);
            tvMessage.setTypeface(null, n.isRead ? Typeface.NORMAL : Typeface.BOLD);
            viewDot.setVisibility(n.isRead ? View.INVISIBLE : View.VISIBLE);

            // Tap expands the message
            tvMessage.setMaxLines(expanded ? Integer.MAX_VALUE : 2);
        }
    }

    public void toggleExpanded(int id, int position) {
        if (expandedIds.contains(id)) {
            expandedIds.remove(id);
        } else {
            expandedIds.add(id);
        }
        notifyItemChanged(position);
    }
}
