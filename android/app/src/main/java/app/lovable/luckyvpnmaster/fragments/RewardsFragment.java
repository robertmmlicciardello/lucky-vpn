
package app.lovable.luckyvpnmaster.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.adapters.RewardAdapter;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.User;
import app.lovable.luckyvpnmaster.models.RewardItem;
import app.lovable.luckyvpnmaster.rewards.RewardsManager;

public class RewardsFragment extends Fragment {
    private TextView tvPoints;
    private Button btnDailyCheckin, btnWatchAd, btnLuckyWheel;
    private RecyclerView recyclerView;
    private RewardAdapter rewardAdapter;
    private AuthManager authManager;
    private RewardsManager rewardsManager;

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
        
        return view;
    }

    private void initViews(View view) {
        tvPoints = view.findViewById(R.id.tv_points);
        btnDailyCheckin = view.findViewById(R.id.btn_daily_checkin);
        btnWatchAd = view.findViewById(R.id.btn_watch_ad);
        btnLuckyWheel = view.findViewById(R.id.btn_lucky_wheel);
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    private void setupClickListeners() {
        btnDailyCheckin.setOnClickListener(v -> performDailyCheckin());
        btnWatchAd.setOnClickListener(v -> watchRewardedAd());
        btnLuckyWheel.setOnClickListener(v -> openLuckyWheel());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rewardAdapter = new RewardAdapter(getContext(), rewardItem -> {
            // Handle reward item selection
            Toast.makeText(getContext(), "Selected: " + rewardItem.title, Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(rewardAdapter);
    }

    private void loadUserPoints() {
        User user = authManager.getCurrentUser();
        if (user != null) {
            tvPoints.setText(String.valueOf(user.points));
        }
    }

    private void loadRewardItems() {
        List<RewardItem> items = new ArrayList<>();
        items.add(new RewardItem("Daily Check-in", "Get 100 points", 100, R.drawable.ic_calendar));
        items.add(new RewardItem("Watch Video", "Get 50 points", 50, R.drawable.ic_play));
        items.add(new RewardItem("Lucky Wheel", "Win up to 1000 points", 0, R.drawable.ic_wheel));
        items.add(new RewardItem("Share App", "Get 200 points", 200, R.drawable.ic_share));
        items.add(new RewardItem("Rate App", "Get 300 points", 300, R.drawable.ic_star));
        items.add(new RewardItem("Invite Friend", "Get 500 points", 500, R.drawable.ic_person_add));
        
        rewardAdapter.updateRewards(items);
    }

    private void performDailyCheckin() {
        rewardsManager.dailyCheckin(new RewardsManager.RewardCallback() {
            @Override
            public void onSuccess(int pointsEarned) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Daily check-in successful! +" + pointsEarned + " points", Toast.LENGTH_SHORT).show();
                        loadUserPoints();
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
        // Implement rewarded ad logic
        Toast.makeText(getContext(), "Opening rewarded ad...", Toast.LENGTH_SHORT).show();
        
        // Simulate ad reward
        rewardsManager.addPoints(50, "Watched rewarded video", new RewardsManager.RewardCallback() {
            @Override
            public void onSuccess(int pointsEarned) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Ad watched! +" + pointsEarned + " points", Toast.LENGTH_SHORT).show();
                        loadUserPoints();
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
        Toast.makeText(getContext(), "Lucky Wheel coming soon!", Toast.LENGTH_SHORT).show();
    }
}
