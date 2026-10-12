package app.lovable.luckyvpnmaster;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import app.lovable.luckyvpnmaster.api.ApiClient;
import app.lovable.luckyvpnmaster.models.BlogPost;

/**
 * Blog post detail screen (P1).
 */
public class BlogDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "post_id";

    private TextView tvTitle, tvDate, tvContent;
    private ProgressBar progressBar;
    private ScrollView contentScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_detail);

        tvTitle = findViewById(R.id.tv_detail_title);
        tvDate = findViewById(R.id.tv_detail_date);
        tvContent = findViewById(R.id.tv_detail_content);
        progressBar = findViewById(R.id.progress_detail);
        contentScroll = findViewById(R.id.scroll_detail);
        ImageButton btnBack = findViewById(R.id.btn_detail_back);
        btnBack.setOnClickListener(v -> finish());

        int postId = getIntent().getIntExtra(EXTRA_POST_ID, 0);
        if (postId == 0) {
            Toast.makeText(this, getString(R.string.blog_load_error), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadPost(postId);
    }

    private void loadPost(int postId) {
        progressBar.setVisibility(View.VISIBLE);
        contentScroll.setVisibility(View.GONE);

        ApiClient.get(this, "/blog/posts/" + postId, new ApiClient.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                BlogPost post = extractPost(data);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (post == null) {
                        Toast.makeText(BlogDetailActivity.this,
                                getString(R.string.blog_load_error), Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    tvTitle.setText(post.title != null ? post.title : "");
                    tvDate.setText(post.getDateShort());
                    String content = post.content != null ? post.content : "";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        tvContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        //noinspection deprecation
                        tvContent.setText(Html.fromHtml(content));
                    }
                    contentScroll.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(BlogDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    /** Tolerates {success,data:{...}} and top-level post objects. */
    private BlogPost extractPost(JSONObject data) {
        try {
            Object raw = data.opt("data");
            if (raw instanceof JSONObject) return BlogPost.fromJson((JSONObject) raw);
            return BlogPost.fromJson(data);
        } catch (Exception e) {
            return null;
        }
    }
}
