package app.lovable.luckyvpnmaster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.models.BlogPost;

/**
 * Blog / announcements list adapter (P1).
 */
public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(BlogPost post);
    }

    private final Context context;
    private final List<BlogPost> posts = new ArrayList<>();
    private final OnPostClickListener listener;

    public BlogAdapter(Context context, OnPostClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updatePosts(List<BlogPost> newPosts) {
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_blog, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class BlogViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvExcerpt, tvDate;

        BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_blog_title);
            tvExcerpt = itemView.findViewById(R.id.tv_blog_excerpt);
            tvDate = itemView.findViewById(R.id.tv_blog_date);
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onPostClick(posts.get(getAdapterPosition()));
                }
            });
        }

        void bind(BlogPost post) {
            tvTitle.setText(post.title != null ? post.title : "");
            tvExcerpt.setText(post.excerpt != null ? post.excerpt : "");
            tvDate.setText(post.getDateShort());
        }
    }
}
