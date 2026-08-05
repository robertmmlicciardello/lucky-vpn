
package app.lovable.luckyvpnmaster;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import app.lovable.luckyvpnmaster.adapters.SubscriptionPlanAdapter;
import app.lovable.luckyvpnmaster.models.SubscriptionPlan;

public class SubscriptionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SubscriptionPlanAdapter adapter;
    private TextView tvCurrentPlan;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        initViews();
        setupClickListeners();
        setupRecyclerView();
        loadSubscriptionPlans();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        tvCurrentPlan = findViewById(R.id.tv_current_plan);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubscriptionPlanAdapter(this, plan -> {
            // Handle plan selection
            Toast.makeText(this, "Selected: " + plan.name, Toast.LENGTH_SHORT).show();
            // Open payment activity
            PaymentManager paymentManager = new PaymentManager(this);
            paymentManager.initiatePayment(plan.id, "kpay", plan.price);
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadSubscriptionPlans() {
        List<SubscriptionPlan> plans = new ArrayList<>();
        
        SubscriptionPlan monthly = new SubscriptionPlan();
        monthly.id = "monthly";
        monthly.name = "Monthly Premium";
        monthly.duration = "1 Month";
        monthly.price = 5000;
        monthly.currency = "MMK";
        monthly.features = new String[]{"Unlimited VPN", "Premium Servers", "No Ads", "24/7 Support"};
        
        SubscriptionPlan quarterly = new SubscriptionPlan();
        quarterly.id = "quarterly";
        quarterly.name = "Quarterly Premium";
        quarterly.duration = "3 Months";
        quarterly.price = 12000;
        quarterly.currency = "MMK";
        quarterly.features = new String[]{"Unlimited VPN", "Premium Servers", "No Ads", "24/7 Support", "20% Discount"};
        
        SubscriptionPlan yearly = new SubscriptionPlan();
        yearly.id = "yearly";
        yearly.name = "Yearly Premium";
        yearly.duration = "12 Months";
        yearly.price = 40000;
        yearly.currency = "MMK";
        yearly.features = new String[]{"Unlimited VPN", "Premium Servers", "No Ads", "24/7 Support", "33% Discount", "Priority Support"};
        
        plans.add(monthly);
        plans.add(quarterly);
        plans.add(yearly);
        
        adapter.updatePlans(plans);
    }
}
