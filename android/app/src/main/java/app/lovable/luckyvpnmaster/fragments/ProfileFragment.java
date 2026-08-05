
package app.lovable.luckyvpnmaster.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.LoginActivity;
import app.lovable.luckyvpnmaster.SubscriptionActivity;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.User;

public class ProfileFragment extends Fragment {
    private TextView tvUserName, tvUserEmail, tvUserPlan, tvUserPoints, tvReferralCode;
    private Button btnUpgrade, btnSupport, btnLogout;
    private AuthManager authManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        authManager = new AuthManager(getContext());
        
        initViews(view);
        setupClickListeners();
        loadUserData();
        
        return view;
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvUserPlan = view.findViewById(R.id.tv_user_plan);
        tvUserPoints = view.findViewById(R.id.tv_user_points);
        tvReferralCode = view.findViewById(R.id.tv_referral_code);
        btnUpgrade = view.findViewById(R.id.btn_upgrade);
        btnSupport = view.findViewById(R.id.btn_support);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void setupClickListeners() {
        btnUpgrade.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SubscriptionActivity.class));
        });
        
        btnSupport.setOnClickListener(v -> {
            // Open support activity
        });
        
        btnLogout.setOnClickListener(v -> {
            authManager.logout();
            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        });
    }

    private void loadUserData() {
        User user = authManager.getCurrentUser();
        if (user != null) {
            tvUserName.setText(user.name);
            tvUserEmail.setText(user.email);
            tvUserPlan.setText(user.plan.toUpperCase());
            tvUserPoints.setText(String.valueOf(user.points));
            // tvReferralCode.setText(user.referralCode); // Add this to User model
        }
    }
}
