package app.lovable.luckyvpnmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import app.lovable.luckyvpnmaster.adapters.SupportTicketAdapter;
import app.lovable.luckyvpnmaster.api.ApiClient;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.SupportTicket;

public class SupportActivity extends AppCompatActivity {
    private LinearLayout layoutContent, layoutLoginRequired;
    private EditText etSubject, etMessage;
    private Spinner spinnerCategory;
    private Button btnSubmit, btnLogin;
    private RecyclerView recyclerView;
    private android.widget.TextView tvEmptyTickets;
    private SupportTicketAdapter adapter;
    private AuthManager authManager;

    private final String[] categoryValues = {"general", "billing", "technical"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        authManager = new AuthManager(this);

        layoutContent = findViewById(R.id.layout_content);
        layoutLoginRequired = findViewById(R.id.layout_login_required);
        etSubject = findViewById(R.id.et_subject);
        etMessage = findViewById(R.id.et_message);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnSubmit = findViewById(R.id.btn_submit);
        btnLogin = findViewById(R.id.btn_login);
        recyclerView = findViewById(R.id.recycler_tickets);
        tvEmptyTickets = findViewById(R.id.tv_empty_tickets);

        String[] categoryLabels = {
                getString(R.string.p1_cat_general),
                getString(R.string.p1_cat_billing),
                getString(R.string.p1_cat_technical)
        };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryLabels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        btnSubmit.setOnClickListener(v -> submitTicket());
        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new SupportTicketAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (authManager.isLoggedIn()) {
            layoutLoginRequired.setVisibility(View.GONE);
            layoutContent.setVisibility(View.VISIBLE);
            loadTickets();
        } else {
            layoutLoginRequired.setVisibility(View.VISIBLE);
            layoutContent.setVisibility(View.GONE);
        }
    }

    private void submitTicket() {
        String subject = etSubject.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        String category = categoryValues[spinnerCategory.getSelectedItemPosition()];

        if (subject.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, R.string.p1_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        try {
            JSONObject body = new JSONObject();
            body.put("subject", subject);
            body.put("message", message);
            body.put("category", category);

            ApiClient.post(this, "/support/tickets", body, new ApiClient.Callback() {
                @Override
                public void onSuccess(JSONObject data) {
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        etSubject.setText("");
                        etMessage.setText("");
                        spinnerCategory.setSelection(0);
                        Toast.makeText(SupportActivity.this,
                                R.string.p1_ticket_sent, Toast.LENGTH_SHORT).show();
                        loadTickets();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        Toast.makeText(SupportActivity.this, error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            btnSubmit.setEnabled(true);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTickets() {
        ApiClient.get(this, "/support/tickets", new ApiClient.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                List<SupportTicket> list = new ArrayList<>();
                JSONArray arr = data.optJSONArray("data");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.optJSONObject(i);
                        if (obj != null) list.add(SupportTicket.fromJson(obj));
                    }
                }
                // Newest first
                Collections.sort(list, (a, b) -> Integer.compare(b.id, a.id));
                runOnUiThread(() -> {
                    adapter.updateTickets(list);
                    tvEmptyTickets.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(SupportActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
