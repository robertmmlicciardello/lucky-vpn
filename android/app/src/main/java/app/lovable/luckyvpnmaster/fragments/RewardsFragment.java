package app.lovable.luckyvpnmaster.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import app.lovable.luckyvpnmaster.LoginActivity;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.adapters.RewardAdapter;
import app.lovable.luckyvpnmaster.adapters.RewardTransactionAdapter;
import app.lovable.luckyvpnmaster.api.ApiClient;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.User;
import app.lovable.luckyvpnmaster.models.RewardItem;
import app.lovable.luckyvpnmaster.models.RewardTransaction;
import app.lovable.luckyvpnmaster.rewards.InviteDialog;
import app.lovable.luckyvpnmaster.rewards.LuckyWheelDialog;
import app.lovable.luckyvpnmaster.rewards.RewardsManager;
import app.lovable.luckyvpnmaster.ads.AdMobManager;

public class RewardsFragment extends Fragment {
    private TextView tvPoints;
    private Button btnDailyCheckin, btnWatchAd, btnLuckyWheel, btnRedeem;
    private RecyclerView recyclerView;
    private RewardAdapter rewardAdapter;
    private AuthManager authManager;
    private RewardsManager rewardsManager;

    // P1: reward history section (visible only when logged in)
    private View historySection;
    private RecyclerView recyclerHistory;
    private TextView tvHistoryEmpty;
    private RewardTransactionAdapter historyAdapter;

    // Share flow: credit the share reward when the chooser returns.
    private ActivityResultLauncher<Intent> shareLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shareLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> onShareReturned());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        authManager = new AuthManager(getContext());
        rewardsManager = new RewardsManager(getContext());

        initViews(view);
        setupClickListeners();
        setupRecyclerView();
        loadUserPoints();
        loadRewardItems();
        setupHistorySection();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserPoints();
        loadRewardHistory();
    }

    private void initViews(View view) {
        tvPoints = view.findViewById(R.id.tv_points);
        btnDailyCheckin = view.findViewById(R.id.btn_daily_checkin);
        btnWatchAd = view.findViewById(R.id.btn_watch_ad);
        btnLuckyWheel = view.findViewById(R.id.btn_lucky_wheel);
        btnRedeem = view.findViewById(R.id.btn_redeem);
        recyclerView = view.findViewById(R.id.recycler_view);
        // P1: history section views
        historySection = view.findViewById(R.id.history_section);
        recyclerHistory = view.findViewById(R.id.recycler_history);
        tvHistoryEmpty = view.findViewById(R.id.tv_history_empty);
    }

    private void setupClickListeners() {
        btnDailyCheckin.setOnClickListener(v -> performDailyCheckin());
        btnWatchAd.setOnClickListener(v -> watchRewardedAd());
        btnLuckyWheel.setOnClickListener(v -> openLuckyWheel());
        if (btnRedeem != null) {
            btnRedeem.setOnClickListener(v -> confirmRedeem());
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rewardAdapter = new RewardAdapter(getContext(), this::onRewardItemClicked);
        recyclerView.setAdapter(rewardAdapter);
    }

    /** Every reward tile does something real — no "coming soon" anywhere. */
    private void onRewardItemClicked(RewardItem item) {
        if (item == null || item.type == null) return;
        switch (item.type) {
            case "daily":
                performDailyCheckin();
                break;
            case "video":
                watchRewardedAd();
                break;
            case "wheel":
                openLuckyWheel();
                break;
            case "share":
                shareApp();
                break;
            case "rate":
                rateApp();
                break;
            case "invite":
                showInviteDialog();
                break;
            default:
                Toast.makeText(getContext(), item.title, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /** Rewards require a logged-in account (guests carry no points). */
    private boolean requireLogin() {
        if (authManager.isLoggedIn()) return true;
        if (getActivity() == null) return false;
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.p1b_login_required)
                .setMessage(R.string.login_to_earn)
                .setPositiveButton(R.string.p1b_go_to_login, (d, w) ->
                        startActivity(new Intent(getContext(), LoginActivity.class)))
                .setNegativeButton(R.string.p1b_dialog_close, null)
                .show();
        return false;
    }

    private void loadUserPoints() {
        User user = authManager.getCurrentUser();
        if (user != null) {
            tvPoints.setText(String.valueOf(user.points));
        }
    }

    /** Refresh points + plan from the server so the UI stays truthful. */
    private void refreshProfile() {
        if (!authManager.isLoggedIn()) return;
        ApiClient.get(getContext(), "/users/profile", new ApiClient.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                JSONObject u = data.optJSONObject("data");
                if (u == null) u = data;
                final String plan = u.optString("plan", "free");
                final int points = u.optInt("points", 0);
                authManager.updateCachedProfile(plan, points);
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    loadUserPoints();
                    loadRewardHistory();
                });
            }

            @Override
            public void onError(String error) {
                // keep cached values on failure
            }
        });
    }

    /** P1: history section setup — hidden for guests / logged-out users. */
    private void setupHistorySection() {
        if (historySection == null) return;
        if (!authManager.isLoggedIn()) {
            historySection.setVisibility(View.GONE);
            return;
        }
        historySection.setVisibility(View.VISIBLE);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new RewardTransactionAdapter(getContext());
        recyclerHistory.setAdapter(historyAdapter);
        loadRewardHistory();
    }

    /** P1: GET /rewards/ — user reward transaction history. */
    private void loadRewardHistory() {
        if (historyAdapter == null || !authManager.isLoggedIn()) return;
        ApiClient.get(getContext(), "/rewards/", new ApiClient.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                List<RewardTransaction> txns = new ArrayList<>();
                JSONArray arr = data.optJSONArray("data");
                if (arr == null) arr = data.optJSONArray("transactions");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.optJSONObject(i);
                        if (o != null) txns.add(RewardTransaction.fromJson(o));
                    }
                }
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    historyAdapter.updateTransactions(txns);
                    if (tvHistoryEmpty != null) {
                        tvHistoryEmpty.setVisibility(txns.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
            }

            @Override
            public void onError(String error) {
                // keep the section quiet on error; points/earn buttons still work
            }
        });
    }

    private void loadRewardItems() {
        List<RewardItem> items = new ArrayList<>();
        items.add(new RewardItem("daily",
                getString(R.string.reward_item_daily_title),
                getString(R.string.reward_item_daily_desc), 100, R.drawable.ic_calendar));
        items.add(new RewardItem("video",
                getString(R.string.reward_item_video_title),
                getString(R.string.reward_item_video_desc), 50, R.drawable.ic_play));
        items.add(new RewardItem("wheel",
                getString(R.string.reward_item_wheel_title),
                getString(R.string.reward_item_wheel_desc), 0, R.drawable.ic_wheel));
        items.add(new RewardItem("share",
                getString(R.string.reward_item_share_title),
                getString(R.string.reward_item_share_desc), 200, R.drawable.ic_share));
        items.add(new RewardItem("rate",
                getString(R.string.reward_item_rate_title),
                getString(R.string.reward_item_rate_desc), 300, R.drawable.ic_star));
        items.add(new RewardItem("invite",
                getString(R.string.reward_item_invite_title),
                getString(R.string.reward_item_invite_desc), 500, R.drawable.ic_person_add));

        rewardAdapter.updateRewards(items);
    }

    private void performDailyCheckin() {
        if (!requireLogin()) return;
        rewardsManager.dailyCheckin(new RewardsManager.RewardCallback() {
            @Override
            public void onSuccess(int pointsEarned) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Daily check-in successful! +" + pointsEarned + " points", Toast.LENGTH_SHORT).show();
                        loadUserPoints();
                        loadRewardHistory();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void watchRewardedAd() {
        if (!requireLogin()) return;
        if (getActivity() == null) return;
        // Implement rewarded ad logic
        Toast.makeText(getContext(), "Opening rewarded ad...", Toast.LENGTH_SHORT).show();

        // REAL AdMob rewarded ad. The reward (POST /rewards/watch-ad) is
        // granted ONLY inside onRewardEarned(). If the ad can't load/show,
        // fall back to the old simulated flow so the feature never breaks.
        AdMobManager.showRewardedAd(getActivity(), new AdMobManager.RewardListener() {
            @Override
            public void onRewardEarned() {
                rewardsManager.watchAdReward(new RewardsManager.RewardCallback() {
                    @Override
                    public void onSuccess(int pointsEarned) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Ad watched! +" + pointsEarned + " points", Toast.LENGTH_SHORT).show();
                                loadUserPoints();
                                loadRewardHistory();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            }

            @Override
            public void onAdUnavailable() {
                // Fallback: the old simulated flow (ad couldn't load/show).
                simulateAdReward();
            }
        });
    }

    /** Legacy simulated "watch video" reward — kept as the no-ad fallback. */
    private void simulateAdReward() {
        // Simulate ad reward
        rewardsManager.addPoints(50, "Watched rewarded video", new RewardsManager.RewardCallback() {
            @Override
            public void onSuccess(int pointsEarned) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Ad watched! +" + pointsEarned + " points", Toast.LENGTH_SHORT).show();
                        loadUserPoints();
                        loadRewardHistory();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void openLuckyWheel() {
        if (!requireLogin()) return;
        LuckyWheelDialog dialog = LuckyWheelDialog.newInstance();
        dialog.setOnPointsEarnedListener(points -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    refreshProfile();
                });
            }
        });
        dialog.show(getParentFragmentManager(), "lucky_wheel");
    }

    /** Share the app with the user's referral code; credit on return. */
    private void shareApp() {
        if (!requireLogin()) return;
        String cached = authManager.getReferralCode();
        if (cached != null && !cached.isEmpty()) {
            launchShareIntent(cached);
        } else {
            rewardsManager.fetchReferralCode(new RewardsManager.ReferralCodeCallback() {
                @Override
                public void onResult(String code) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> launchShareIntent(code));
                }

                @Override
                public void onError(String error) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private void launchShareIntent(String referralCode) {
        if (getContext() == null) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.share_text, referralCode));
        shareLauncher.launch(Intent.createChooser(intent, getString(R.string.share_title)));
    }

    /** Called when the share chooser returns — credit the share reward. */
    private void onShareReturned() {
        rewardsManager.shareReward(new RewardsManager.RewardCallback() {
            @Override
            public void onSuccess(int pointsEarned) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                getString(R.string.share_success, pointsEarned),
                                Toast.LENGTH_SHORT).show();
                        refreshProfile();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    /** Open the Play Store listing, then claim the rate reward (once ever). */
    private void rateApp() {
        if (!requireLogin()) return;
        if (getContext() == null) return;
        String pkg = getContext().getPackageName();
        Toast.makeText(getContext(), R.string.rate_opening, Toast.LENGTH_SHORT).show();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + pkg)));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + pkg)));
        }

        rewardsManager.rateReward(new RewardsManager.RewardCallback() {
            @Override
            public void onSuccess(int pointsEarned) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                getString(R.string.rate_success, pointsEarned),
                                Toast.LENGTH_SHORT).show();
                        refreshProfile();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void showInviteDialog() {
        if (!requireLogin()) return;
        InviteDialog.newInstance().show(getParentFragmentManager(), "invite");
    }

    private void confirmRedeem() {
        if (!requireLogin()) return;
        if (getActivity() == null) return;
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.redeem_confirm_title)
                .setMessage(R.string.redeem_confirm_msg)
                .setPositiveButton(R.string.dialog_confirm, (d, w) -> doRedeem())
                .setNegativeButton(R.string.p1b_dialog_close, null)
                .show();
    }

    /** Spend 1000 points for 7 days of premium. */
    private void doRedeem() {
        rewardsManager.redeemPremium(new RewardsManager.RedeemCallback() {
            @Override
            public void onSuccess(int newPointsBalance, String message, JSONObject data) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    // keep the shared profile cache truthful (plan + points)
                    String plan = data.optString("plan", "premium");
                    authManager.updateCachedProfile(plan, newPointsBalance);

                    String expiry = data.optString("premium_expires_at", "");
                    if (expiry.isEmpty()) expiry = data.optString("premiumExpiresAt", "");
                    String shown;
                    if (!expiry.isEmpty()) {
                        String day = expiry.length() >= 10 ? expiry.substring(0, 10) : expiry;
                        shown = getString(R.string.redeem_success, day);
                    } else if (!message.isEmpty()) {
                        shown = message;
                    } else {
                        shown = getString(R.string.redeem_success, "");
                    }
                    Toast.makeText(getContext(), shown, Toast.LENGTH_LONG).show();
                    refreshProfile();
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show());
            }
        });
    }
}
