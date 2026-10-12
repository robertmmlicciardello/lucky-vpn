package app.lovable.luckyvpnmaster.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import app.lovable.luckyvpnmaster.BlogDetailActivity;
import app.lovable.luckyvpnmaster.LoginActivity;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.adapters.BlogAdapter;
import app.lovable.luckyvpnmaster.api.ApiClient;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.BlogPost;

/**
 * Blog / announcements feed (P1). Requires login.
 */
public class BlogFragment extends Fragment {

    private RecyclerView recyclerView;
    private BlogAdapter adapter;
    private ProgressBar progressBar;
    private View loginRequiredLayout;
    private TextView tvEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog, container, false);

        recyclerView = view.findViewById(R.id.rv_blog);
        progressBar = view.findViewById(R.id.progress_blog);
        loginRequiredLayout = view.findViewById(R.id.layout_login_required);
        tvEmpty = view.findViewById(R.id.tv_blog_empty);
        Button btnLogin = view.findViewById(R.id.btn_go_login);
        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(getContext(), LoginActivity.class)));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BlogAdapter(getContext(), post -> {
            Intent intent = new Intent(getContext(), BlogDetailActivity.class);
            intent.putExtra(BlogDetailActivity.EXTRA_POST_ID, post.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        if (!new AuthManager(requireContext()).isLoggedIn()) {
            loginRequiredLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {
            loadPosts();
        }
        return view;
    }

    private void loadPosts() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.get(requireContext(), "/blog/posts", new ApiClient.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                List<BlogPost> posts = extractPosts(data);
                runOnUi(() -> {
                    progressBar.setVisibility(View.GONE);
                    adapter.updatePosts(posts);
                    tvEmpty.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
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

    /** Tolerates {success,data:[...]}, {success,data:{posts:[...]}} shapes. */
    private List<BlogPost> extractPosts(JSONObject data) {
        List<BlogPost> out = new ArrayList<>();
        try {
            JSONArray arr = null;
            Object raw = data.opt("data");
            if (raw instanceof JSONArray) {
                arr = (JSONArray) raw;
            } else if (raw instanceof JSONObject) {
                JSONObject d = (JSONObject) raw;
                if (d.has("posts")) arr = d.optJSONArray("posts");
                else if (d.has("items")) arr = d.optJSONArray("items");
                else if (d.has("data")) arr = d.optJSONArray("data");
            }
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    out.add(BlogPost.fromJson(arr.getJSONObject(i)));
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
