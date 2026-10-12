package app.lovable.luckyvpnmaster.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.adapters.LeaderboardAdapter;
import app.lovable.luckyvpnmaster.api.ApiClient;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.LeaderboardEntry;
import app.lovable.luckyvpnmaster.models.User;

/**
 * Points leaderboard (P1). Works for guests too; the "My rank" card
 * is hidden for guests, and no row is highlighted then.
 */
public class LeaderboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private ProgressBar progressBar;
    private CardView myRankCard;
    private TextView tvMyRank, tvEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        recyclerView = view.findViewById(R.id.rv_leaderboard);
        progressBar = view.findViewById(R.id.progress_leaderboard);
        myRankCard = view.findViewById(R.id.card_my_rank);
        tvMyRank = view.findViewById(R.id.tv_my_rank);
        tvEmpty = view.findViewById(R.id.tv_leaderboard_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter(getContext());

        AuthManager auth = new AuthManager(requireContext());
        User user = auth.getCurrentUser();
        if (user != null) {
            adapter.setCurrentUserId(user.id);
        }
        recyclerView.setAdapter(adapter);

        if (!auth.isLoggedIn()) {
            myRankCard.setVisibility(View.GONE);
            loadLeaderboard();
        } else {
            loadMyRank();
            loadLeaderboard();
        }
        return view;
    }

    private void loadMyRank() {
        ApiClient.get(requireContext(), "/leaderboard/my-rank", new ApiClient.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                final int rank = data.optInt("rank",
                        data.optJSONObject("data") != null
                                ? data.optJSONObject("data").optInt("rank", 0) : 0);
                final int points = data.optInt("points",
                        data.optJSONObject("data") != null
                                ? data.optJSONObject("data").optInt("points", 0) : 0);
                runOnUi(() -> {
                    myRankCard.setVisibility(View.VISIBLE);
                    tvMyRank.setText(getString(R.string.leaderboard_my_rank, rank, points));
                });
            }

            @Override
            public void onError(String error) {
                runOnUi(() -> myRankCard.setVisibility(View.GONE));
            }
        });
    }

    private void loadLeaderboard() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.get(requireContext(), "/leaderboard/points", new ApiClient.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                List<LeaderboardEntry> entries = extractEntries(data);
                runOnUi(() -> {
                    progressBar.setVisibility(View.GONE);
                    adapter.updateEntries(entries);
                    tvEmpty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(String error) {
                runOnUi(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /** Tolerates {success,data:[...]}, {success,data:{leaderboard:[...]}} shapes. */
    private List<LeaderboardEntry> extractEntries(JSONObject data) {
        List<LeaderboardEntry> out = new ArrayList<>();
        try {
            JSONArray arr = null;
            Object raw = data.opt("data");
            if (raw instanceof JSONArray) {
                arr = (JSONArray) raw;
            } else if (raw instanceof JSONObject) {
                JSONObject d = (JSONObject) raw;
                if (d.has("leaderboard")) arr = d.optJSONArray("leaderboard");
                else if (d.has("items")) arr = d.optJSONArray("items");
                else if (d.has("data")) arr = d.optJSONArray("data");
            }
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    LeaderboardEntry e = LeaderboardEntry.fromJson(arr.getJSONObject(i));
                    if (e.rank == 0) e.rank = i + 1;
                    out.add(e);
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    private void runOnUi(Runnable r) {
        if (getActivity() != null) getActivity().runOnUiThread(r);
    }
}
