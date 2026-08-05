package app.lovable.luckyvpnmaster.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.OptimizedVPNService;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.User;
import app.lovable.luckyvpnmaster.models.Server;
import app.lovable.luckyvpnmaster.api.ServerManager;
import app.lovable.luckyvpnmaster.utils.ConnectionManager;

public class HomeFragment extends Fragment implements ConnectionManager.NetworkCallback {
    private TextView tvConnectionStatus, tvUserName, tvUserPlan, tvCurrentServer;
    private Button btnConnect;
    private ImageView ivConnectionIcon;
    private AuthManager authManager;
    private ServerManager serverManager;
    private boolean isConnected = false;
    private Server currentServer;
    private LinearLayout offlineLayout;
    private FrameLayout loadingContainer;
    private ImageView loadingAnimation;
    private LinearLayout connectionStatusLayout;
    private ConnectionManager connectionManager;
    private VPNConnectionReceiver vpnReceiver;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        authManager = new AuthManager(getContext());
        serverManager = new ServerManager(getContext());
        
        initializeNetworkManager();
        initializeViews(view);
        setupClickListeners();
        loadUserData();
        loadBestServer();
        
        return view;
    }
    
    private void initializeNetworkManager() {
        connectionManager = new ConnectionManager(getContext());
        connectionManager.addCallback(this);
        
        // Register VPN connection receiver
        vpnReceiver = new VPNConnectionReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("VPN_CONNECTION_ERROR");
        getContext().registerReceiver(vpnReceiver, filter);
    }
    
    private void initializeViews(View view) {
        tvConnectionStatus = view.findViewById(R.id.tv_connection_status);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserPlan = view.findViewById(R.id.tv_user_plan);
        tvCurrentServer = view.findViewById(R.id.tv_current_server);
        btnConnect = view.findViewById(R.id.btn_connect);
        ivConnectionIcon = view.findViewById(R.id.iv_connection_icon);
        
        offlineLayout = view.findViewById(R.id.offline_layout);
        loadingContainer = view.findViewById(R.id.loading_container);
        loadingAnimation = view.findViewById(R.id.iv_loading_animation);
        connectionStatusLayout = view.findViewById(R.id.connection_status_layout);
        
        Button retryButton = view.findViewById(R.id.btn_retry);
        if (retryButton != null) {
            retryButton.setOnClickListener(v -> checkNetworkAndRetry());
        }
    }
    
    private void setupClickListeners() {
        btnConnect.setOnClickListener(v -> {
            if (isConnected) {
                disconnectVPN();
            } else {
                connectVPN();
            }
        });
    }
    
    private void loadUserData() {
        User user = authManager.getCurrentUser();
        if (user != null) {
            tvUserName.setText("Welcome, " + user.name);
            tvUserPlan.setText(user.plan.toUpperCase() + " Plan");
        }
    }
    
    private void loadBestServer() {
        serverManager.getBestServer(new ServerManager.ServerCallback() {
            @Override
            public void onSuccess(Server server) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentServer = server;
                        tvCurrentServer.setText(server.country + " - " + server.city);
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvCurrentServer.setText("No server available");
                    });
                }
            }
        });
    }
    
    private void connectVPN() {
        if (currentServer == null) {
            loadBestServer();
            return;
        }
        
        if (!connectionManager.isNetworkAvailable()) {
            showOfflineState();
            return;
        }
        
        showLoadingState();
        
        Intent intent = new Intent(getContext(), OptimizedVPNService.class);
        intent.putExtra("server_ip", currentServer.ip);
        intent.putExtra("server_port", currentServer.port);
        intent.putExtra("server_config", currentServer.configFile);
        
        getContext().startService(intent);
        
        // Simulate connection delay
        new android.os.Handler().postDelayed(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> updateConnectionStatus(true));
            }
        }, 3000);
    }
    
    private void disconnectVPN() {
        showLoadingState();
        
        Intent intent = new Intent(getContext(), OptimizedVPNService.class);
        intent.putExtra("action", "disconnect");
        getContext().startService(intent);
        
        // Simulate disconnection delay
        new android.os.Handler().postDelayed(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> updateConnectionStatus(false));
            }
        }, 1500);
    }
    
    private void showLoadingState() {
        if (loadingContainer != null && connectionStatusLayout != null && offlineLayout != null) {
            loadingContainer.setVisibility(View.VISIBLE);
            connectionStatusLayout.setVisibility(View.GONE);
            offlineLayout.setVisibility(View.GONE);
            
            // Start loading animation
            if (loadingAnimation != null && loadingAnimation.getDrawable() instanceof AnimatedVectorDrawable) {
                AnimatedVectorDrawable animatedDrawable = (AnimatedVectorDrawable) loadingAnimation.getDrawable();
                animatedDrawable.start();
            }
        }
    }
    
    private void showOfflineState() {
        if (offlineLayout != null && connectionStatusLayout != null && loadingContainer != null) {
            offlineLayout.setVisibility(View.VISIBLE);
            connectionStatusLayout.setVisibility(View.GONE);
            loadingContainer.setVisibility(View.GONE);
        }
    }
    
    private void showConnectionState() {
        if (connectionStatusLayout != null && offlineLayout != null && loadingContainer != null) {
            connectionStatusLayout.setVisibility(View.VISIBLE);
            offlineLayout.setVisibility(View.GONE);
            loadingContainer.setVisibility(View.GONE);
        }
    }
    
    private void checkNetworkAndRetry() {
        if (connectionManager.isNetworkAvailable()) {
            showConnectionState();
            loadBestServer();
        } else {
            // Still offline, show message
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "Still no internet connection", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void updateConnectionStatus(boolean connected) {
        isConnected = connected;
        
        if (connected) {
            tvConnectionStatus.setText("CONNECTED");
            tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnConnect.setText("DISCONNECT");
            btnConnect.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            ivConnectionIcon.setImageResource(R.drawable.ic_shield_check);
        } else {
            tvConnectionStatus.setText("DISCONNECTED");
            tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnConnect.setText("CONNECT");
            btnConnect.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            ivConnectionIcon.setImageResource(R.drawable.ic_shield_off);
        }
    }
    
    @Override
    public void onNetworkAvailable() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (offlineLayout != null && offlineLayout.getVisibility() == View.VISIBLE) {
                    showConnectionState();
                    loadBestServer();
                }
            });
        }
    }
    
    @Override
    public void onNetworkLost() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                showOfflineState();
                if (isConnected) {
                    disconnectVPN();
                }
            });
        }
    }
    
    @Override
    public void onNetworkCapabilitiesChanged(boolean isWifi, boolean isMobile) {
        // Handle network type changes if needed
        if (getActivity() != null && tvCurrentServer != null) {
            getActivity().runOnUiThread(() -> {
                String networkType = isWifi ? " (WiFi)" : isMobile ? " (Mobile)" : "";
                if (currentServer != null) {
                    tvCurrentServer.setText(currentServer.country + " - " + currentServer.city + networkType);
                }
            });
        }
    }
    
    // VPN Connection Error Receiver
    private class VPNConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("VPN_CONNECTION_ERROR".equals(intent.getAction())) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateConnectionStatus(false);
                        android.widget.Toast.makeText(context, "VPN connection failed. Please try again.", android.widget.Toast.LENGTH_LONG).show();
                    });
                }
            }
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (connectionManager != null) {
            connectionManager.removeCallback(this);
            connectionManager.destroy();
        }
        
        if (vpnReceiver != null) {
            getContext().unregisterReceiver(vpnReceiver);
        }
    }
}
