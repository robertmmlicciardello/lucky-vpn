package app.lovable.luckyvpnmaster.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import app.lovable.luckyvpnmaster.LoginActivity;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.adapters.NotificationAdapter;
import app.lovable.luckyvpnmaster.api.ApiClient;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.AppNotification;

public class NotificationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty, layoutLoginRequired;
    private TextView tvEmpty;
    private Button btnLogin;
    private NotificationAdapter adapter;
    private AuthManager authManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        authManager = new AuthManager(getContext());

        recyclerView = view.findViewById(R.id.recycler_notifications);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        layoutLoginRequired = view.findViewById(R.id.layout_login_required);
        tvEmpty = view.findViewById(R.id.tv_empty);
        btnLogin = view.findViewById(R.id.btn_login);

        tvEmpty.setText(R.string.p1_no_notifications);
        btnLogin.setText(R.string.p1_login);
        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(getContext(), LoginActivity.class)));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(getContext(), this::onNotificationTap);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (authManager == null) authManager = new AuthManager(getContext());
        if (authManager.isLoggedIn()) {
            layoutLoginRequired.setVisibility(View.GONE);
            loadNotifications();
        } else {
            // Guest / not logged in: show login-required empty state
            layoutLoginRequired.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void loadNotifications() {
        ApiClient.get(getContext(), "/notifications", new ApiClient.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                if (getActivity() == null) return;
                List<AppNotification> list = new ArrayList<>();
                JSONArray arr = data.optJSONArray("data");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.optJSONObject(i);
                        if (obj != null) list.add(AppNotification.fromJson(obj));
                    }
                }
                // Newest first (highest id first; falls back to createdAt)
                Collections.sort(list, (a, b) -> {
                    if (a.id != b.id) return Integer.compare(b.id, a.id);
                    return (b.createdAt != null ? b.createdAt : "")
                            .compareTo(a.createdAt != null ? a.createdAt : "");
                });
                getActivity().runOnUiThread(() -> {
                    adapter.updateNotifications(list);
                    recyclerView.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                    layoutEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void onNotificationTap(AppNotification n, int position) {
        adapter.toggleExpanded(n.id, position);
        if (!n.isRead) {
            // Mark read on the server; update locally regardless of round-trip failure
            ApiClient.put(getContext(), "/notifications/" + n.id + "/read",
                    new JSONObject(), new ApiClient.Callback() {
                @Override
                public void onSuccess(JSONObject data) {
                    // Already marked locally below
                }

                @Override
                public void onError(String error) {
                    // Local state stays read; server will resync next load
                }
            });
            n.isRead = true;
            adapter.notifyItemChanged(position);
        }
    }
}
