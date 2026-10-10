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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.models.Server;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder> {
    private Context context;
    private List<Server> servers;
    private OnServerClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    private static final Map<String, String> COUNTRY_ISO = new HashMap<>();
    static {
        COUNTRY_ISO.put("singapore", "SG");
        COUNTRY_ISO.put("malaysia", "MY");
        COUNTRY_ISO.put("thailand", "TH");
        COUNTRY_ISO.put("japan", "JP");
        COUNTRY_ISO.put("south korea", "KR");
        COUNTRY_ISO.put("korea", "KR");
        COUNTRY_ISO.put("india", "IN");
        COUNTRY_ISO.put("vietnam", "VN");
        COUNTRY_ISO.put("indonesia", "ID");
        COUNTRY_ISO.put("philippines", "PH");
        COUNTRY_ISO.put("hong kong", "HK");
        COUNTRY_ISO.put("taiwan", "TW");
        COUNTRY_ISO.put("china", "CN");
        COUNTRY_ISO.put("cambodia", "KH");
        COUNTRY_ISO.put("laos", "LA");
        COUNTRY_ISO.put("myanmar", "MM");
        COUNTRY_ISO.put("bangladesh", "BD");
        COUNTRY_ISO.put("united states", "US");
        COUNTRY_ISO.put("usa", "US");
        COUNTRY_ISO.put("us", "US");
        COUNTRY_ISO.put("canada", "CA");
        COUNTRY_ISO.put("brazil", "BR");
        COUNTRY_ISO.put("mexico", "MX");
        COUNTRY_ISO.put("germany", "DE");
        COUNTRY_ISO.put("netherlands", "NL");
        COUNTRY_ISO.put("united kingdom", "GB");
        COUNTRY_ISO.put("uk", "GB");
        COUNTRY_ISO.put("france", "FR");
        COUNTRY_ISO.put("switzerland", "CH");
        COUNTRY_ISO.put("sweden", "SE");
        COUNTRY_ISO.put("norway", "NO");
        COUNTRY_ISO.put("spain", "ES");
        COUNTRY_ISO.put("italy", "IT");
        COUNTRY_ISO.put("poland", "PL");
        COUNTRY_ISO.put("ukraine", "UA");
        COUNTRY_ISO.put("australia", "AU");
        COUNTRY_ISO.put("new zealand", "NZ");
        COUNTRY_ISO.put("turkey", "TR");
        COUNTRY_ISO.put("uae", "AE");
        COUNTRY_ISO.put("united arab emirates", "AE");
    }

    public interface OnServerClickListener {
        void onServerClick(Server server, int position);
    }

    public ServerAdapter(Context context, OnServerClickListener listener) {
        this.context = context;
        this.servers = new ArrayList<>();
        this.listener = listener;
    }

    public void updateServers(List<Server> newServers) {
        this.servers.clear();
        this.servers.addAll(newServers);
        this.selectedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int old = selectedPosition;
        selectedPosition = position;
        if (old != RecyclerView.NO_POSITION) notifyItemChanged(old);
        if (position != RecyclerView.NO_POSITION) notifyItemChanged(position);
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
        holder.bind(server, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    /** Converts a country name to a flag emoji via regional indicator symbols. */
    public static String countryToFlagEmoji(String country) {
        if (country == null) return "\uD83C\uDF10";
        String iso = COUNTRY_ISO.get(country.trim().toLowerCase());
        if (iso == null || iso.length() != 2) return "\uD83C\uDF10";
        StringBuilder sb = new StringBuilder();
        for (char c : iso.toCharArray()) {
            sb.appendCodePoint(0x1F1E6 + (c - 'A'));
        }
        return sb.toString();
    }

    class ServerViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFlagEmoji, tvServerName, tvLocation, tvLoad, tvPing;
        private ImageView ivSignal, ivSelected;

        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFlagEmoji = itemView.findViewById(R.id.tv_flag_emoji);
            ivSignal = itemView.findViewById(R.id.iv_signal);
            ivSelected = itemView.findViewById(R.id.iv_selected);
            tvServerName = itemView.findViewById(R.id.tv_server_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvLoad = itemView.findViewById(R.id.tv_load);
            tvPing = itemView.findViewById(R.id.tv_ping);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onServerClick(servers.get(getAdapterPosition()), getAdapterPosition());
                }
            });
        }

        public void bind(Server server, boolean selected) {
            tvServerName.setText(server.country != null ? server.country : server.name);
            tvLocation.setText(server.name != null ? server.name
                    : (server.country + " - " + server.city));
            tvLoad.setText(server.load + "%");
            tvPing.setText("< 100ms"); // You can implement actual ping calculation

            tvFlagEmoji.setText(countryToFlagEmoji(server.country));

            // Set signal strength based on load
            if (server.load < 30) {
                ivSignal.setImageResource(R.drawable.ic_signal_strong);
            } else if (server.load < 70) {
                ivSignal.setImageResource(R.drawable.ic_signal_medium);
            } else {
                ivSignal.setImageResource(R.drawable.ic_signal_weak);
            }

            ivSelected.setVisibility(selected ? View.VISIBLE : View.GONE);
        }
    }
}
