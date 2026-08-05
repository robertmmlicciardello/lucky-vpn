
package app.lovable.luckyvpnmaster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.models.Server;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder> {
    private Context context;
    private List<Server> servers;
    private OnServerClickListener listener;

    public interface OnServerClickListener {
        void onServerClick(Server server);
    }

    public ServerAdapter(Context context, OnServerClickListener listener) {
        this.context = context;
        this.servers = new ArrayList<>();
        this.listener = listener;
    }

    public void updateServers(List<Server> newServers) {
        this.servers.clear();
        this.servers.addAll(newServers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_server, parent, false);
        return new ServerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        Server server = servers.get(position);
        holder.bind(server);
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    class ServerViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivFlag, ivSignalStrength;
        private TextView tvServerName, tvLocation, tvLoad, tvPing;

        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFlag = itemView.findViewById(R.id.iv_flag);
            ivSignalStrength = itemView.findViewById(R.id.iv_signal_strength);
            tvServerName = itemView.findViewById(R.id.tv_server_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvLoad = itemView.findViewById(R.id.tv_load);
            tvPing = itemView.findViewById(R.id.tv_ping);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onServerClick(servers.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Server server) {
            tvServerName.setText(server.name);
            tvLocation.setText(server.country + " - " + server.city);
            tvLoad.setText(server.load + "%");
            tvPing.setText("< 100ms"); // You can implement actual ping calculation
            
            // Set flag icon based on country
            setFlagIcon(server.country);
            
            // Set signal strength based on load
            setSignalStrength(server.load);
        }

        private void setFlagIcon(String country) {
            // You would typically load country flags from resources or URL
            // For now, use a default flag icon
            ivFlag.setImageResource(R.drawable.ic_flag_default);
        }

        private void setSignalStrength(int load) {
            if (load < 30) {
                ivSignalStrength.setImageResource(R.drawable.ic_signal_strong);
            } else if (load < 70) {
                ivSignalStrength.setImageResource(R.drawable.ic_signal_medium);
            } else {
                ivSignalStrength.setImageResource(R.drawable.ic_signal_weak);
            }
        }
    }
}
