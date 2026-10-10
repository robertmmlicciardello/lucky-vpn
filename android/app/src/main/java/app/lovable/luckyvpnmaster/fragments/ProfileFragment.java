package app.lovable.luckyvpnmaster.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.LoginActivity;
import app.lovable.luckyvpnmaster.SettingsActivity;
import app.lovable.luckyvpnmaster.SubscriptionActivity;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.User;

public class ProfileFragment extends Fragment {
    private TextView tvUserName, tvUserEmail;
    private Button btnUpgrade;
    private View rowHistory, rowSettings, rowSubscription, rowHelp, rowPrivacy, rowAbout, rowLogout;
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
        btnUpgrade = view.findViewById(R.id.btn_upgrade);
        rowHistory = view.findViewById(R.id.row_history);
        rowSettings = view.findViewById(R.id.row_settings);
        rowSubscription = view.findViewById(R.id.row_subscription);
        rowHelp = view.findViewById(R.id.row_help);
        rowPrivacy = view.findViewById(R.id.row_privacy);
        rowAbout = view.findViewById(R.id.row_about);
        rowLogout = view.findViewById(R.id.row_logout);
    }

    private void setupClickListeners() {
        btnUpgrade.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SubscriptionActivity.class));
        });

        rowSubscription.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SubscriptionActivity.class));
        });

        rowLogout.setOnClickListener(v -> {
            authManager.logout();
            startActivity(new Intent(getContext(), LoginActivity.class));
            if (getActivity() != null) getActivity().finish();
        });

        // Settings / history screens are wired by the features worker;
        // keep them as clearly-marked placeholders for now.
        View.OnClickListener comingSoon = v ->
                Toast.makeText(getContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
        rowHistory.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ConnectionHistoryFragment())
                        .addToBackStack(null).commit());
        rowSettings.setOnClickListener(v ->
                startActivity(new Intent(getContext(), SettingsActivity.class)));
        rowHelp.setOnClickListener(comingSoon);
        rowPrivacy.setOnClickListener(comingSoon);
        rowAbout.setOnClickListener(comingSoon);
    }

    private void loadUserData() {
        User user = authManager.getCurrentUser();
        if (user != null) {
            tvUserName.setText(user.name);
            tvUserEmail.setText(user.email);
        } else if (authManager.isGuest()) {
            tvUserName.setText("Guest");
            tvUserEmail.setText(getString(R.string.premium_upsell_subtitle));
        }
    }
}
