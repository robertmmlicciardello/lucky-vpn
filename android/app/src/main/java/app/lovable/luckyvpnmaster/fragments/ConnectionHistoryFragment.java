package app.lovable.luckyvpnmaster.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.utils.ConnectionHistoryManager;

/**
 * Shows VPN connection history (last 50 entries) from ConnectionHistoryManager.
 */
public class ConnectionHistoryFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connection_history, container, false);

        RecyclerView rvHistory = view.findViewById(R.id.rv_history);
        View emptyView = view.findViewById(R.id.tv_history_empty);

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        List<ConnectionHistoryManager.ConnectionRecord> records =
                getContext() != null
                        ? new ConnectionHistoryManager(getContext()).getHistory()
                        : new ArrayList<>();

        if (records.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            rvHistory.setAdapter(new HistoryAdapter(records));
        }
        return view;
    }

    private static class HistoryAdapter
            extends RecyclerView.Adapter<HistoryAdapter.VH> {

        private final List<ConnectionHistoryManager.ConnectionRecord> records;
        private final SimpleDateFormat dateFmt =
                new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());

        HistoryAdapter(List<ConnectionHistoryManager.ConnectionRecord> records) {
            this.records = records;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ConnectionHistoryManager.ConnectionRecord r = records.get(position);
            h.tvServerName.setText(r.serverName);
            h.tvCountry.setText(r.country);
            h.tvDate.setText(dateFmt.format(new Date(r.startTimeMillis)));
            h.tvDuration.setText(formatDuration(r.durationSec));
            h.tvStatus.setText(r.connected
                    ? h.itemView.getContext().getString(R.string.history_status_connected)
                    : h.itemView.getContext().getString(R.string.history_status_disconnected));
            h.tvStatus.setBackgroundResource(r.connected
                    ? R.drawable.badge_connected
                    : R.drawable.badge_disconnected);
        }

        @Override
        public int getItemCount() {
            return records.size();
        }

        private String formatDuration(long seconds) {
            if (seconds <= 0) return "--";
            long m = seconds / 60;
            long s = seconds % 60;
            if (m >= 60) {
                return (m / 60) + "h " + (m % 60) + "m";
            }
            return m + "m " + s + "s";
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvServerName, tvCountry, tvDate, tvDuration, tvStatus;
            VH(@NonNull View itemView) {
                super(itemView);
                tvServerName = itemView.findViewById(R.id.tv_item_server_name);
                tvCountry = itemView.findViewById(R.id.tv_item_country);
                tvDate = itemView.findViewById(R.id.tv_item_date);
                tvDuration = itemView.findViewById(R.id.tv_item_duration);
                tvStatus = itemView.findViewById(R.id.tv_item_status);
            }
        }
    }
}
