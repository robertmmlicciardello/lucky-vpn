package app.lovable.luckyvpnmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import app.lovable.luckyvpnmaster.adapters.PaymentAdapter;
import app.lovable.luckyvpnmaster.api.ApiClient;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.PaymentRecord;

/** "My Payments" screen: GET /subscription/history, newest first. */
public class PaymentHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PaymentAdapter adapter;
    private View emptyState;
    private TextView tvEmptyTitle, tvEmptyDesc;
    private Button btnEmptyAction;
    private ImageButton btnBack;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        authManager = new AuthManager(this);

        recyclerView = findViewById(R.id.recycler_view);
        emptyState = findViewById(R.id.empty_state);
        tvEmptyTitle = findViewById(R.id.tv_empty_title);
        tvEmptyDesc = findViewById(R.id.tv_empty_desc);
        btnEmptyAction = findViewById(R.id.btn_empty_action);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentAdapter(this);
        recyclerView.setAdapter(adapter);

        if (!authManager.isLoggedIn()) {
            showLoginRequired();
        } else {
            loadHistory();
        }
    }

    private void showLoginRequired() {
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(R.string.p1b_login_required);
        tvEmptyDesc.setText(R.string.p1b_login_required_desc);
        btnEmptyAction.setVisibility(View.VISIBLE);
        btnEmptyAction.setText(R.string.p1b_go_to_login);
        btnEmptyAction.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void loadHistory() {
        ApiClient.get(this, "/subscription/history", new ApiClient.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                List<PaymentRecord> records = new ArrayList<>();
                JSONObject payload = data.optJSONObject("data");
                JSONArray arr = payload != null ? payload.optJSONArray("history") : null;
                if (arr == null) arr = data.optJSONArray("history");
                if (arr == null) arr = data.optJSONArray("data");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.optJSONObject(i);
                        if (o != null) records.add(PaymentRecord.fromJson(o));
                    }
                }
                // newest first (ISO dates sort lexicographically)
                Collections.sort(records, (a, b) -> {
                    String x = a.createdAt != null ? a.createdAt : "";
                    String y = b.createdAt != null ? b.createdAt : "";
                    return y.compareTo(x);
                });
                runOnUiThread(() -> {
                    adapter.updateRecords(records);
                    if (records.isEmpty()) {
                        showEmpty();
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(PaymentHistoryActivity.this, error,
                            Toast.LENGTH_SHORT).show();
                    showEmpty();
                });
            }
        });
    }

    private void showEmpty() {
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(R.string.p1b_payment_empty);
        tvEmptyDesc.setText("");
        btnEmptyAction.setVisibility(View.GONE);
    }
}
