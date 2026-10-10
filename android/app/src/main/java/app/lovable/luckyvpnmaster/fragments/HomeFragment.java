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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.vpn.EmbeddedOpenVpnConnector;
// WireGuard temporarily disabled (see build.gradle)
// import app.lovable.luckyvpnmaster.vpn.WireGuardConnector;
import app.lovable.luckyvpnmaster.auth.AuthManager;
import app.lovable.luckyvpnmaster.models.User;
import app.lovable.luckyvpnmaster.models.Server;
import app.lovable.luckyvpnmaster.api.ServerManager;
import app.lovable.luckyvpnmaster.utils.ConnectionManager;
import app.lovable.luckyvpnmaster.utils.ConnectionHistoryManager;
import app.lovable.luckyvpnmaster.utils.KillSwitchManager;
import app.lovable.luckyvpnmaster.utils.SettingsManager;

public class HomeFragment extends Fragment implements ConnectionManager.NetworkCallback {
    private TextView tvConnectionStatus, tvUserName, tvUserPlan, tvCurrentServer;
    private TextView tvConnectionTimer, tvIpAddress, tvDownloadSpeed, tvUploadSpeed;
    private ImageButton btnConnect;
    private Button btnChangeServer;
    private ImageView ivConnectRing;
    private final android.os.Handler timerHandler = new android.os.Handler();
    private long connectStartTime = 0;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isConnected && tvConnectionTimer != null) {
                long elapsed = (System.currentTimeMillis() - connectStartTime) / 1000;
                tvConnectionTimer.setText(String.format(java.util.Locale.US, "%02d:%02d:%02d",
                        elapsed / 3600, (elapsed % 3600) / 60, elapsed % 60));
                timerHandler.postDelayed(this, 1000);
            }
        }
    };
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
    private String activeProtocol = null; // "openvpn" (wireguard planned)
    // Connection-history + kill-switch integration
    private ConnectionHistoryManager historyManager;
    private long connectStartSec = 0;
    private boolean userInitiatedDisconnect = false;
    private boolean autoConnectDone = false;
    private final EmbeddedOpenVpnConnector.StatusListener vpnStatusListener =
            new EmbeddedOpenVpnConnector.StatusListener() {
        @Override
        public void onState(String state, String message) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                boolean connected = "CONNECTED".equalsIgnoreCase(state);
                boolean disconnected = "DISCONNECTED".equalsIgnoreCase(state);
                if (connected || disconnected) {
                    updateConnectionStatus(connected);
                } else if (tvConnectionStatus != null && message != null && !message.isEmpty()) {
                    tvConnectionStatus.setText(state);
                }
                // ---- history + kill-switch ----
                if (getContext() == null) return;
                if (connected) {
                    if (historyManager == null) historyManager = new ConnectionHistoryManager(getContext());
                    String name = currentServer != null ? currentServer.name : "Unknown";
                    String country = currentServer != null ? currentServer.country : "";
                    historyManager.recordConnect(name, country);
                    connectStartSec = System.currentTimeMillis() / 1000;
                    KillSwitchManager.clear(getContext());
                } else if (disconnected) {
                    if (historyManager == null) historyManager = new ConnectionHistoryManager(getContext());
                    long dur = connectStartSec > 0
                            ? (System.currentTimeMillis() / 1000 - connectStartSec) : 0;
                    historyManager.recordDisconnect(dur);
                    connectStartSec = 0;
                    if (!userInitiatedDisconnect) {
                        KillSwitchManager.onUnexpectedDisconnect(getContext());
                    }
                }
            });
        }

        @Override
        public void onError(String error) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                updateConnectionStatus(false);
                android.widget.Toast.makeText(getContext(), error,
                        android.widget.Toast.LENGTH_LONG).show();
            });
        }
    };
    
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

        // Restore UI if the tunnel survived a rotation / tab switch.
        try {
            if (EmbeddedOpenVpnConnector.getInstance().isConnected()) {
                String srv = EmbeddedOpenVpnConnector.getInstance().getCurrentServerName();
                if (srv != null && tvCurrentServer != null) tvCurrentServer.setText(srv);
                updateConnectionStatus(true);
            }
        } catch (Exception ignored) {}

        return view;
    }
    
    private void initializeNetworkManager() {
        connectionManager = new ConnectionManager(getContext());
        connectionManager.addCallback(this);
        
        // Register VPN connection receiver
        // (Android 14+ requires RECEIVER_NOT_EXPORTED for non-system broadcasts)
        vpnReceiver = new VPNConnectionReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("VPN_CONNECTION_ERROR");
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            getContext().registerReceiver(vpnReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            getContext().registerReceiver(vpnReceiver, filter);
        }
    }
    
    private void initializeViews(View view) {
        tvConnectionStatus = view.findViewById(R.id.tv_connection_status);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserPlan = view.findViewById(R.id.tv_user_plan);
        tvCurrentServer = view.findViewById(R.id.tv_current_server);
        tvConnectionTimer = view.findViewById(R.id.tv_connection_timer);
        tvIpAddress = view.findViewById(R.id.tv_ip_address);
        tvDownloadSpeed = view.findViewById(R.id.tv_download_speed);
        tvUploadSpeed = view.findViewById(R.id.tv_upload_speed);
        btnConnect = view.findViewById(R.id.btn_connect);
        btnChangeServer = view.findViewById(R.id.btn_change_server);
        ivConnectRing = view.findViewById(R.id.iv_connect_ring);
        
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
                userInitiatedDisconnect = true;
                disconnectVPN();
            } else {
                userInitiatedDisconnect = false;
                connectVPN();
            }
        });

        if (btnChangeServer != null) {
            btnChangeServer.setOnClickListener(v -> {
                if (getActivity() != null) {
                    com.google.android.material.bottomnavigation.BottomNavigationView nav =
                            getActivity().findViewById(R.id.bottom_navigation);
                    if (nav != null) {
                        nav.setSelectedItemId(R.id.nav_servers);
                    }
                }
            });
        }
    }
    
    private void loadUserData() {
        User user = authManager.getCurrentUser();
        if (user != null) {
            tvUserName.setText(getString(R.string.hello_user) + " " + user.name);
            tvUserPlan.setText(user.plan.toUpperCase() + " Plan");
        } else if (authManager.isGuest()) {
            tvUserName.setText(getString(R.string.hello_user) + " Guest");
            tvUserPlan.setText("FREE Plan");
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
                        // Auto-connect on launch if the user enabled it.
                        if (!autoConnectDone && getContext() != null
                                && new SettingsManager(getContext()).isAutoConnect()
                                && !EmbeddedOpenVpnConnector.getInstance().isConnected()) {
                            autoConnectDone = true;
                            userInitiatedDisconnect = false;
                            connectVPN();
                        }
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

        // Embedded VPN — no separate app install needed.
        // System VPN permission first (one-time dialog).
        if (getActivity() != null) {
            Intent vpnIntent = android.net.VpnService.prepare(getActivity());
            if (vpnIntent != null) {
                startActivityForResult(vpnIntent, EmbeddedOpenVpnConnector.REQ_VPN_PERMISSION);
                return;
            }
        }

        startEmbeddedVpn();
    }

    /** Starts the embedded tunnel after VPN permission is granted. */
    private void startEmbeddedVpn() {
        if (currentServer == null || getActivity() == null) return;

        showLoadingState();

        // Download the inline .ovpn config for this server, then connect
        // via the embedded OpenVPN engine (no separate app needed).
        serverManager.getServerConfig(currentServer.id, new ServerManager.ConfigCallback() {
            @Override
            public void onSuccess(String vpnConfig) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    activeProtocol = "openvpn";
                    EmbeddedOpenVpnConnector.getInstance().connect(
                            getActivity(), vpnConfig, currentServer.name, vpnStatusListener);
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    showConnectionState();
                    android.widget.Toast.makeText(getContext(), error,
                            android.widget.Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void disconnectVPN() {
        showLoadingState();
        if (getActivity() != null) {
            EmbeddedOpenVpnConnector.getInstance().disconnect(getActivity());
        }
        activeProtocol = null;
        // Update optimistically so the UI never hangs on "loading".
        new android.os.Handler().postDelayed(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isConnected) showConnectionState();
                    updateConnectionStatus(false);
                });
            }
        }, 1500);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EmbeddedOpenVpnConnector.REQ_VPN_PERMISSION) {
            if (resultCode == android.app.Activity.RESULT_OK) {
                // VPN permission granted — start the tunnel now.
                startEmbeddedVpn();
            } else {
                showConnectionState();
                android.widget.Toast.makeText(getContext(),
                        "VPN permission is required to connect",
                        android.widget.Toast.LENGTH_LONG).show();
            }
        }
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
            tvConnectionStatus.setText(R.string.connected);
            tvConnectionStatus.setTextColor(getResources().getColor(R.color.vpn_green));
            ivConnectRing.setImageResource(R.drawable.ring_connect_active);
            btnConnect.setColorFilter(getResources().getColor(R.color.vpn_green));
            btnConnect.setContentDescription(getString(R.string.disconnect));
            // Start the connection timer
            connectStartTime = System.currentTimeMillis();
            if (tvConnectionTimer != null) {
                tvConnectionTimer.setVisibility(View.VISIBLE);
                tvConnectionTimer.setText("00:00:00");
                timerHandler.removeCallbacks(timerRunnable);
                timerHandler.post(timerRunnable);
            }
        } else {
            tvConnectionStatus.setText(R.string.disconnected);
            tvConnectionStatus.setTextColor(getResources().getColor(R.color.vpn_text_primary));
            ivConnectRing.setImageResource(R.drawable.ring_connect_idle);
            btnConnect.setColorFilter(getResources().getColor(R.color.vpn_blue));
            btnConnect.setContentDescription(getString(R.string.tap_to_connect));
            // Stop the connection timer
            timerHandler.removeCallbacks(timerRunnable);
            if (tvConnectionTimer != null) {
                tvConnectionTimer.setVisibility(View.GONE);
            }
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

        timerHandler.removeCallbacks(timerRunnable);

        if (connectionManager != null) {
            connectionManager.removeCallback(this);
            connectionManager.destroy();
        }
        
        if (vpnReceiver != null) {
            getContext().unregisterReceiver(vpnReceiver);
        }

        // NOTE: do NOT disconnect the tunnel here. The VPN must survive tab
        // switches and rotation; it stops only on explicit user Disconnect
        // (btnConnect) or process death.
    }
}
