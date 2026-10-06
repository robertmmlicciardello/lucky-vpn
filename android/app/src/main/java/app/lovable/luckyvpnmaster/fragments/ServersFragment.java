
package app.lovable.luckyvpnmaster.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import java.util.List;
import app.lovable.luckyvpnmaster.LoginActivity;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.adapters.ServerAdapter;
import app.lovable.luckyvpnmaster.api.ServerManager;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.Server;

public class ServersFragment extends Fragment {
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private ServerAdapter serverAdapter;
    private ServerManager serverManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_servers, container, false);
        
        serverManager = new ServerManager(getContext());
        
        initViews(view);
        setupTabs();
        setupRecyclerView();
        loadFreeServers(); // Load free servers by default
        
        return view;
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Free Servers"));
        tabLayout.addTab(tabLayout.newTab().setText("Premium Servers"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadFreeServers();
                        break;
                    case 1:
                        loadPremiumServers();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        serverAdapter = new ServerAdapter(getContext(), server -> {
            // Handle server selection
            Toast.makeText(getContext(), "Selected: " + server.name, Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(serverAdapter);
    }

    private void loadFreeServers() {
        serverManager.getFreeServers(new ServerManager.ServersCallback() {
            @Override
            public void onSuccess(List<Server> servers) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        serverAdapter.updateServers(servers);
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

    private void loadPremiumServers() {
        AuthManager authManager = new AuthManager(getContext());
        if (!authManager.isLoggedIn()) {
            promptLoginForPremium();
            // fall back to the free tab instead of showing an empty list
            if (tabLayout != null && tabLayout.getTabAt(0) != null) {
                tabLayout.getTabAt(0).select();
            }
            return;
        }
        serverManager.getPremiumServers(new ServerManager.ServersCallback() {
            @Override
            public void onSuccess(List<Server> servers) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        serverAdapter.updateServers(servers);
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

    private void promptLoginForPremium() {
        if (getActivity() == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(getActivity())
            .setTitle("Premium servers")
            .setMessage("Please log in or create an account to use premium servers. Free servers work without an account.")
            .setPositiveButton("Log In", (d, w) -> {
                new AuthManager(getContext()).setGuestMode(false);
                startActivity(new Intent(getContext(), LoginActivity.class));
            })
            .setNegativeButton("Later", null)
            .show();
    }
}
