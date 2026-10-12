package app.lovable.luckyvpnmaster.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONObject;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.LoginActivity;
import app.lovable.luckyvpnmaster.PaymentHistoryActivity;
import app.lovable.luckyvpnmaster.SettingsActivity;
import app.lovable.luckyvpnmaster.SubscriptionActivity;
import app.lovable.luckyvpnmaster.api.ApiClient;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.User;

public class ProfileFragment extends Fragment {
    private TextView tvUserName, tvUserEmail, tvPremiumSubtitle;
    private Button btnUpgrade;
    private View rowHistory, rowSettings, rowSubscription, rowPayments, rowBlog,
            rowLeaderboard, rowHelp, rowPrivacy, rowAbout, rowLogout;
    private AuthManager authManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        authManager = new AuthManager(getContext());

        initViews(view);
        setupClickListeners();
        loadUserData();
        refreshProfileFromServer();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
        refreshProfileFromServer();
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvPremiumSubtitle = view.findViewById(R.id.tv_premium_subtitle);
        btnUpgrade = view.findViewById(R.id.btn_upgrade);
        rowHistory = view.findViewById(R.id.row_history);
        rowSettings = view.findViewById(R.id.row_settings);
        rowSubscription = view.findViewById(R.id.row_subscription);
        rowPayments = view.findViewById(R.id.row_payments);
        rowBlog = view.findViewById(R.id.row_blog);
        rowLeaderboard = view.findViewById(R.id.row_leaderboard);
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

        // P1: new menu rows
        rowPayments.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), PaymentHistoryActivity.class));
        });
        rowBlog.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,
                                new app.lovable.luckyvpnmaster.fragments.BlogFragment())
                        .addToBackStack(null).commit());
        rowLeaderboard.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,
                                new app.lovable.luckyvpnmaster.fragments.LeaderboardFragment())
                        .addToBackStack(null).commit());

        rowLogout.setOnClickListener(v -> {
            authManager.logout();
            startActivity(new Intent(getContext(), LoginActivity.class));
            if (getActivity() != null) getActivity().finish();
        });

        rowHistory.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ConnectionHistoryFragment())
                        .addToBackStack(null).commit());
        rowSettings.setOnClickListener(v ->
                startActivity(new Intent(getContext(), SettingsActivity.class)));

        // P1: Help opens the support screen (created by another worker).
        rowHelp.setOnClickListener(v -> startActivity(
                new Intent(getContext(),
                        app.lovable.luckyvpnmaster.SupportActivity.class)));

        // P1: privacy / about show dialogs instead of "coming soon".
        rowPrivacy.setOnClickListener(v -> showInfoDialog(
                R.string.p1b_privacy_title, R.string.p1b_privacy_text));
        rowAbout.setOnClickListener(v -> showInfoDialog(
                R.string.p1b_about_title, R.string.p1b_about_text));
    }

    private void showInfoDialog(int titleRes, int textRes) {
        if (getActivity() == null) return;
        new AlertDialog.Builder(getActivity())
                .setTitle(titleRes)
                .setMessage(textRes)
                .setPositiveButton(R.string.p1b_dialog_close, null)
                .show();
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

    /**
     * P1: refresh name/email/plan/points from the server. Local cached values
     * stay on screen if the API call fails.
     */
    private void refreshProfileFromServer() {
        if (!authManager.isLoggedIn() || !isAdded()) return;
        ApiClient.get(getContext(), "/users/profile", new ApiClient.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                JSONObject u = data.optJSONObject("data");
                if (u == null) u = data;
                final String name = u.optString("name", "");
                final String email = u.optString("email", "");
                final String plan = u.optString("plan", "free");
                final int points = u.optInt("points", 0);
                String expiry = u.optString("premium_expires_at", "");
                if (expiry.isEmpty()) expiry = u.optString("premiumExpiresAt", "");
                final boolean premiumActive = isPremiumActive(plan, expiry);

                // keep the shared cache consistent for other screens
                authManager.updateCachedProfile(plan, points);

                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    if (!name.isEmpty()) tvUserName.setText(name);
                    if (!email.isEmpty()) tvUserEmail.setText(email);
                    if (premiumActive) {
                        tvPremiumSubtitle.setText(R.string.p1b_premium_active);
                        btnUpgrade.setVisibility(View.GONE);
                    } else {
                        tvPremiumSubtitle.setText(R.string.premium_upsell_subtitle);
                        btnUpgrade.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String error) {
                // silent: keep showing the cached values
            }
        });
    }

    private boolean isPremiumActive(String plan, String expiry) {
        if (!"premium".equalsIgnoreCase(plan)) return false;
        if (expiry == null || expiry.isEmpty()) return true;
        String expDay = expiry.length() >= 10 ? expiry.substring(0, 10) : expiry;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        return expDay.compareTo(today) >= 0;
    }
}
