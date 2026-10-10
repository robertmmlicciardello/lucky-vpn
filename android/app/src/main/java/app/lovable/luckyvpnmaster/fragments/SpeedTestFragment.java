package app.lovable.luckyvpnmaster.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import app.lovable.luckyvpnmaster.utils.SpeedTestHelper;

/**
 * Speed test screen. Uses the fixed view-ID contract in
 * layout/fragment_speedtest.xml (created by the UI worker).
 */
public class SpeedTestFragment extends Fragment {

    private static final String PREFS_NAME = "speed_prefs";
    private static final String KEY_LAST_DOWN = "last_down";
    private static final String KEY_LAST_UP = "last_up";
    private static final String KEY_LAST_PING = "last_ping";
    private static final String KEY_LAST_TIME = "last_time";
    private static final String KEY_HISTORY = "history";
    private static final int MAX_HISTORY = 10;
    private static final double GAUGE_MAX_MBPS = 200.0;

    private ProgressBar speedGauge;
    private TextView tvSpeedValue, tvSpeedUnit, tvDownloadSpeed, tvUploadSpeed,
            tvPingMs, tvTestServer;
    private Button btnTestAgain;
    private RecyclerView rvHistory;
    private SpeedTestHelper speedTestHelper;
    private HistoryAdapter historyAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_speedtest, container, false);

        speedGauge = view.findViewById(R.id.speed_gauge);
        tvSpeedValue = view.findViewById(R.id.tv_speed_value);
        tvSpeedUnit = view.findViewById(R.id.tv_speed_unit);
        tvDownloadSpeed = view.findViewById(R.id.tv_download_speed);
        tvUploadSpeed = view.findViewById(R.id.tv_upload_speed);
        tvPingMs = view.findViewById(R.id.tv_ping_ms);
        tvTestServer = view.findViewById(R.id.tv_test_server);
        btnTestAgain = view.findViewById(R.id.btn_test_again);
        rvHistory = view.findViewById(R.id.rv_speed_history);

        speedGauge.setMax(100);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new HistoryAdapter(loadHistory());
        rvHistory.setAdapter(historyAdapter);

        tvTestServer.setText("speed.cloudflare.com");

        btnTestAgain.setOnClickListener(v -> startTest());
        return view;
    }

    private void startTest() {
        resetUi();
        btnTestAgain.setEnabled(false);
        speedTestHelper = new SpeedTestHelper();
        speedTestHelper.start(new SpeedTestHelper.SpeedTestListener() {
            @Override
            public void onPingResult(long ms) {
                runOnUi(() -> tvPingMs.setText(ms + " ms"));
            }

            @Override
            public void onDownloadResult(double mbps) {
                runOnUi(() -> tvDownloadSpeed.setText(fmt(mbps) + " Mbps"));
            }

            @Override
            public void onProgress(String phase, double mbps) {
                runOnUi(() -> {
                    if ("download".equals(phase) || "upload".equals(phase)) {
                        tvSpeedValue.setText(fmt(mbps));
                        animateGauge(mbps);
                        if ("upload".equals(phase)) {
                            tvUploadSpeed.setText(fmt(mbps) + " Mbps");
                        }
                    }
                });
            }

            @Override
            public void onComplete(double downMbps, double upMbps, long pingMs) {
                runOnUi(() -> {
                    btnTestAgain.setEnabled(true);
                    tvUploadSpeed.setText(fmt(upMbps) + " Mbps");
                    tvSpeedValue.setText(fmt(downMbps));
                    animateGauge(downMbps);
                    saveResult(downMbps, upMbps, pingMs);
                });
            }

            @Override
            public void onError(String msg) {
                runOnUi(() -> {
                    btnTestAgain.setEnabled(true);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void resetUi() {
        tvSpeedValue.setText("0.0");
        tvDownloadSpeed.setText("—");
        tvUploadSpeed.setText("—");
        tvPingMs.setText("—");
        speedGauge.setProgress(0);
    }

    /** Maps 0–200 Mbps onto the 0–100 ProgressBar range with animation. */
    private void animateGauge(double mbps) {
        int progress = (int) Math.min(100, Math.max(0, (mbps / GAUGE_MAX_MBPS) * 100));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            speedGauge.setProgress(progress, true);
        } else {
            speedGauge.setProgress(progress);
        }
    }

    private void runOnUi(Runnable r) {
        if (getActivity() != null) getActivity().runOnUiThread(r);
    }

    private String fmt(double v) {
        return String.format(Locale.US, "%.1f", v);
    }

    private void saveResult(double down, double up, long ping) {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        prefs.edit()
                .putFloat(KEY_LAST_DOWN, (float) down)
                .putFloat(KEY_LAST_UP, (float) up)
                .putLong(KEY_LAST_PING, ping)
                .putLong(KEY_LAST_TIME, now)
                .apply();

        List<String> history = loadHistory();
        history.add(0, now + "|" + fmt(down) + "|" + fmt(up) + "|" + ping);
        while (history.size() > MAX_HISTORY) history.remove(history.size() - 1);
        prefs.edit().putString(KEY_HISTORY, String.join("\n", history)).apply();
        historyAdapter.setItems(history);
    }

    private List<String> loadHistory() {
        List<String> out = new ArrayList<>();
        if (getContext() == null) return out;
        String raw = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_HISTORY, "");
        if (!raw.isEmpty()) {
            for (String line : raw.split("\n")) {
                if (!line.trim().isEmpty()) out.add(line);
            }
        }
        return out;
    }

    @Override
    public void onDestroyView() {
        if (speedTestHelper != null) speedTestHelper.cancel();
        super.onDestroyView();
    }

    /** Small inner adapter for the last-10 results (layout/item_speed_result.xml). */
    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
        private List<String> items;

        HistoryAdapter(List<String> items) {
            this.items = new ArrayList<>(items);
        }

        void setItems(List<String> items) {
            this.items = new ArrayList<>(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_speed_result, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            String[] p = items.get(position).split("\\|");
            String time = "", down = "", up = "", ping = "";
            if (p.length >= 4) {
                time = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                        .format(new Date(Long.parseLong(p[0])));
                down = p[1]; up = p[2]; ping = p[3];
            }
            h.tvLine1.setText(time + "  ·  ↓ " + down + " Mbps  ·  ↑ " + up + " Mbps");
            h.tvLine2.setText("Ping " + ping + " ms");
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvLine1, tvLine2;
            VH(@NonNull View itemView) {
                super(itemView);
                tvLine1 = itemView.findViewById(R.id.tv_history_line1);
                tvLine2 = itemView.findViewById(R.id.tv_history_line2);
            }
        }
    }
}
