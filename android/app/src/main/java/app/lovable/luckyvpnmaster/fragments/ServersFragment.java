
package app.lovable.luckyvpnmaster.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import app.lovable.luckyvpnmaster.LoginActivity;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.adapters.ServerAdapter;
import app.lovable.luckyvpnmaster.api.ServerManager;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.Server;

public class ServersFragment extends Fragment {
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private EditText etSearch;
    private ChipGroup chipRegionGroup;
    private ServerAdapter serverAdapter;
    private ServerManager serverManager;
    private final List<Server> allServers = new ArrayList<>();
    private String currentRegion = "all";
    private String currentQuery = "";

    private static final Map<String, String> COUNTRY_REGION = new HashMap<>();
    static {
        // Asia
        for (String c : new String[]{"Singapore","Malaysia","Thailand","Japan","South Korea","Korea",
                "India","Vietnam","Indonesia","Philippines","Hong Kong","Taiwan","China","Cambodia",
                "Laos","Myanmar","Bangladesh","Sri Lanka","Nepal","Pakistan","UAE","United Arab Emirates",
                "Saudi Arabia","Qatar","Turkey","Israel","Kazakhstan","Mongolia","Australia","New Zealand"}) {
            COUNTRY_REGION.put(c.toLowerCase(), "asia");
        }
        // Europe
        for (String c : new String[]{"Germany","Netherlands","United Kingdom","UK","France","Switzerland",
                "Sweden","Norway","Spain","Italy","Poland","Ukraine","Austria","Belgium","Denmark",
                "Finland","Ireland","Portugal","Greece","Czech Republic","Hungary","Romania","Bulgaria",
                "Croatia","Russia","Iceland","Luxembourg"}) {
            COUNTRY_REGION.put(c.toLowerCase(), "europe");
        }
        // Americas
        for (String c : new String[]{"United States","USA","US","Canada","Brazil","Mexico","Argentina",
                "Chile","Colombia","Peru"}) {
            COUNTRY_REGION.put(c.toLowerCase(), "americas");
        }
    }

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
        etSearch = view.findViewById(R.id.et_search);
        chipRegionGroup = view.findViewById(R.id.chip_region_group);

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    currentQuery = s.toString().trim().toLowerCase();
                    applyFilter();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (chipRegionGroup != null) {
            chipRegionGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.contains(R.id.chip_asia)) {
                    currentRegion = "asia";
                } else if (checkedIds.contains(R.id.chip_europe)) {
                    currentRegion = "europe";
                } else if (checkedIds.contains(R.id.chip_americas)) {
                    currentRegion = "americas";
                } else {
                    currentRegion = "all";
                }
                applyFilter();
            });
        }
    }

    /** Applies the current region + search filters to the loaded server list. */
    private void applyFilter() {
        if (serverAdapter == null) return;
        List<Server> filtered = new ArrayList<>();
        for (Server s : allServers) {
            if (!"all".equals(currentRegion)) {
                String region = s.country != null
                        ? COUNTRY_REGION.get(s.country.toLowerCase()) : null;
                if (!currentRegion.equals(region)) continue;
            }
            if (!currentQuery.isEmpty()) {
                String hay = ((s.name != null ? s.name : "") + " "
                        + (s.country != null ? s.country : "") + " "
                        + (s.city != null ? s.city : "")).toLowerCase();
                if (!hay.contains(currentQuery)) continue;
            }
            filtered.add(s);
        }
        serverAdapter.updateServers(filtered);
    }

    private void setServers(List<Server> servers) {
        allServers.clear();
        if (servers != null) allServers.addAll(servers);
        applyFilter();
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
        serverAdapter = new ServerAdapter(getContext(), (server, position) -> {
            // Handle server selection
            serverAdapter.setSelectedPosition(position);
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
                        setServers(servers);
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
                        setServers(servers);
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
