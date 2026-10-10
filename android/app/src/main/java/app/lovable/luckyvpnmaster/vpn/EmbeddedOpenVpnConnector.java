package app.lovable.luckyvpnmaster.vpn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import com.tim.basevpn.IConnectionStateListener;
import com.tim.basevpn.IVPNService;
import com.tim.basevpn.state.ConnectionState;
import com.tim.openvpn.configuration.OpenVPNConfig;
import com.tim.openvpn.service.OpenVPNService;

/**
 * Embedded OpenVPN connector using the tim06 OpenVPN library (OpenVPN3,
 * Apache-2.0). No separate "OpenVPN for Android" install needed — the
 * engine runs inside our own app via the library's VpnService
 * (which lives in the library's ":openvpn" process).
 *
 * <p>State observation: after starting the service we BIND to it with an
 * explicit intent (no "android.net.VpnService" action — the library's
 * BindableVpnService.onBind returns null for that action) and register an
 * {@link IConnectionStateListener} via the AIDL {@link IVPNService}
 * interface. The library's OpenVPNThreadv3 pushes real
 * {@link ConnectionState} values (CONNECTING / CONNECTED / DISCONNECTED /
 * IDLE) as the tunnel actually changes state — CONNECTED is emitted only
 * when the tunnel is truly up.
 *
 * <p>Behaviors:
 * <ul>
 *   <li>30s connect timeout per attempt → onError("Connection timed out")</li>
 *   <li>Auto-reconnect on unexpected drop: up to 3 retries, backoff 2s/4s/8s,
 *       emitting RECONNECTING with attempt count</li>
 *   <li>User-initiated disconnect never triggers reconnect; disconnect() is
 *       idempotent</li>
 *   <li>Last state + server name persisted in SharedPreferences ("vpn_prefs")</li>
 *   <li>PARTIAL_WAKE_LOCK (30 min) + WIFI_MODE_FULL_HIGH_PERF wifi lock,
 *       released on every exit path</li>
 * </ul>
 */
public class EmbeddedOpenVpnConnector {
    private static final String TAG = "EmbeddedOpenVPN";

    public static final int REQ_VPN_PERMISSION = 7002;

    // UI-facing state strings (kept stable for HomeFragment)
    public static final String STATE_CONNECTING = "CONNECTING";
    public static final String STATE_CONNECTED = "CONNECTED";
    public static final String STATE_DISCONNECTED = "DISCONNECTED";
    public static final String STATE_RECONNECTING = "RECONNECTING";

    // SharedPreferences persistence (survives rotation)
    private static final String PREFS_NAME = "vpn_prefs";
    private static final String KEY_STATE = "vpn_state";
    private static final String KEY_SERVER = "vpn_server";

    // Timing / retry policy
    private static final long CONNECT_TIMEOUT_MS = 30_000L;
    private static final int MAX_RETRIES = 3;
    private static final long[] RETRY_BACKOFF_MS = {2_000L, 4_000L, 8_000L};

    public interface StatusListener {
        void onState(String state, String message);
        void onError(String error);
    }

    private static EmbeddedOpenVpnConnector instance;

    private final Object lock = new Object();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;
    private volatile StatusListener listener;

    // Session state (all guarded by `lock` unless noted volatile)
    private volatile String lastKnownState = STATE_DISCONNECTED;
    private volatile String currentServerName = "";
    private boolean sessionActive = false;
    private boolean userInitiatedDisconnect = false;
    private boolean everConnectedThisSession = false;
    private int retryCount = 0;
    /** Bumped on every restart/teardown so stale binder callbacks are ignored. */
    private long stateEpoch = 0;

    // Pending config for (re)start
    private String pendingConfig;
    private String pendingServerName;

    private Context appContext;

    // Library service binding
    private ServiceConnection serviceConnection;
    private IVPNService vpnService;
    private IConnectionStateListener.Stub stateCallback;
    private boolean bound = false;

    private Runnable timeoutRunnable;
    private Runnable reconnectRunnable;

    private EmbeddedOpenVpnConnector() {}

    public static synchronized EmbeddedOpenVpnConnector getInstance() {
        if (instance == null) instance = new EmbeddedOpenVpnConnector();
        return instance;
    }

    /** Returns an Intent for the system VPN-consent dialog, or null if granted. */
    public Intent prepareVpn(Context context) {
        return VpnService.prepare(context);
    }

    /** True when the tunnel is actually up (last observed library state). */
    public boolean isConnected() {
        return STATE_CONNECTED.equals(lastKnownState);
    }

    /** Name of the server for the current/last session ("" if none). */
    public String getCurrentServerName() {
        String name = currentServerName;
        return name != null ? name : "";
    }

    public void connect(Context context, String rawOvpnConfig, String serverName, StatusListener listener) {
        synchronized (lock) {
            this.listener = listener;

            String config;
            try {
                config = OvpnConfigSanitizer.sanitize(rawOvpnConfig);
            } catch (Exception e) {
                Log.e(TAG, "sanitize failed", e);
                postError("Invalid VPN config");
                return;
            }
            if (config == null || config.isEmpty()) {
                postError("Empty VPN config");
                return;
            }

            // Clean slate: a stop+start cycle (e.g. HomeFragment.onDestroy
            // disconnect followed by a new connect) must not corrupt state.
            fullTeardownLocked(context, true);

            appContext = context.getApplicationContext();
            pendingConfig = config;
            pendingServerName = serverName != null ? serverName : "lucky-vpn";

            sessionActive = true;
            userInitiatedDisconnect = false;
            everConnectedThisSession = false;
            retryCount = 0;
            stateEpoch++;

            acquireLocks(appContext);

            currentServerName = pendingServerName;
            setStateLocked(STATE_CONNECTING, pendingServerName);
            postState(STATE_CONNECTING, "Starting VPN…");

            startTunnelLocked();
        }
    }

    /**
     * Idempotent, safe to call when already disconnected. Never triggers
     * auto-reconnect.
     */
    public void disconnect(Context context) {
        synchronized (lock) {
            userInitiatedDisconnect = true;
            Context ctx = context != null ? context.getApplicationContext() : appContext;
            fullTeardownLocked(ctx, true);
            setStateLocked(STATE_DISCONNECTED, currentServerName);
            postState(STATE_DISCONNECTED, "Disconnected");
            Log.i(TAG, "disconnected (user-initiated)");
        }
    }

    // ---- tunnel lifecycle (call with lock held) ----

    private void startTunnelLocked() {
        try {
            String host = extractHost(pendingConfig);
            int port = extractPort(pendingConfig);
            OpenVPNConfig vpnConfig = new OpenVPNConfig(
                    pendingServerName,
                    host,
                    port,
                    "udp-client",
                    null, null, null, null, null, null,
                    pendingConfig
            );
            OpenVPNService.Companion.startService(appContext, vpnConfig, null, new String[0]);
            Log.i(TAG, "OpenVPN service start requested for " + host + ":" + port);
            bindToServiceLocked();
            armTimeoutLocked();
        } catch (Exception e) {
            Log.e(TAG, "startTunnel failed", e);
            fullTeardownLocked(appContext, true);
            postError("Failed to start VPN: " + e.getMessage());
        }
    }

    /**
     * Binds to the library's OpenVPNService and registers a state callback.
     * IMPORTANT: the intent must NOT carry the "android.net.VpnService"
     * action — the library's BindableVpnService.onBind returns null for that
     * action; a plain explicit intent returns the IVPNService binder.
     */
    private void bindToServiceLocked() {
        unbindLocked(); // drop any stale binding first
        final long epoch = stateEpoch;
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                synchronized (lock) {
                    if (epoch != stateEpoch || !sessionActive) return;
                    try {
                        vpnService = IVPNService.Stub.asInterface(binder);
                        stateCallback = new IConnectionStateListener.Stub() {
                            @Override
                            public void stateChanged(ConnectionState state) {
                                // Binder pool thread → hop to main before touching state.
                                mainHandler.post(() -> onLibraryState(state, epoch));
                            }

                            @Override
                            public void trafficUpdate(long in, long out, long diffIn, long diffOut) {
                                // Not needed for connection state.
                            }
                        };
                        vpnService.registerCallback(stateCallback);
                        bound = true;
                        Log.i(TAG, "bound to OpenVPNService, callback registered");
                        // Catch up in case state changed between start and bind.
                        try {
                            ConnectionState current = vpnService.getState();
                            if (current != null) onLibraryState(current, epoch);
                        } catch (RemoteException re) {
                            Log.w(TAG, "getState failed", re);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "registerCallback failed", e);
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                synchronized (lock) {
                    vpnService = null;
                    bound = false;
                    stateCallback = null;
                }
                // Binder died unexpectedly → treat like a tunnel drop.
                mainHandler.post(() -> {
                    synchronized (lock) {
                        if (epoch == stateEpoch && sessionActive && !userInitiatedDisconnect) {
                            onUnexpectedDropLocked("VPN service disconnected");
                        }
                    }
                });
            }
        };
        try {
            Intent bindIntent = new Intent(appContext, OpenVPNService.class);
            // BIND_AUTO_CREATE keeps the remote :openvpn process bound while we observe.
            boolean ok = appContext.bindService(bindIntent, serviceConnection,
                    Context.BIND_AUTO_CREATE);
            Log.i(TAG, "bindService -> " + ok);
        } catch (Exception e) {
            Log.e(TAG, "bindService failed", e);
            serviceConnection = null;
        }
    }

    private void unbindLocked() {
        if (stateCallback != null && vpnService != null) {
            try {
                vpnService.unregisterCallback(stateCallback);
            } catch (Exception e) {
                Log.w(TAG, "unregisterCallback failed", e);
            }
        }
        stateCallback = null;
        vpnService = null;
        if (bound && serviceConnection != null && appContext != null) {
            try {
                appContext.unbindService(serviceConnection);
            } catch (Exception e) {
                Log.w(TAG, "unbindService failed", e);
            }
        }
        bound = false;
        serviceConnection = null;
    }

    /** Full session teardown: timers, binding, service, locks. Always lock-held. */
    private void fullTeardownLocked(Context context, boolean stopService) {
        sessionActive = false;
        stateEpoch++; // invalidate in-flight binder callbacks
        cancelTimersLocked();
        unbindLocked();
        if (stopService) {
            Context ctx = context != null ? context : appContext;
            if (ctx != null) {
                try {
                    OpenVPNService.Companion.stopService(ctx);
                } catch (Exception e) {
                    Log.w(TAG, "stopService failed", e);
                }
            }
        }
        releaseLocks();
        pendingConfig = null;
    }

    // ---- library state handling (main thread) ----

    private void onLibraryState(ConnectionState state, long epoch) {
        synchronized (lock) {
            if (epoch != stateEpoch || !sessionActive || userInitiatedDisconnect) return;
            if (state == null) return;
            switch (state) {
                case CONNECTED:
                    cancelTimeoutLocked();
                    everConnectedThisSession = true;
                    setStateLocked(STATE_CONNECTED, currentServerName);
                    postState(STATE_CONNECTED, "Connected to " + currentServerName);
                    Log.i(TAG, "tunnel CONNECTED");
                    break;
                case CONNECTING:
                    setStateLocked(STATE_CONNECTING, currentServerName);
                    postState(STATE_CONNECTING, "Connecting…");
                    break;
                case DISCONNECTED:
                    onUnexpectedDropLocked("Tunnel dropped");
                    break;
                case DISCONNECTING:
                    // Transient — the final DISCONNECTED (or our own
                    // disconnect) follows; nothing to report yet.
                    break;
                case PERMISSION_NOT_GRANTED:
                    fullTeardownLocked(appContext, true);
                    setStateLocked(STATE_DISCONNECTED, currentServerName);
                    postError("VPN permission not granted");
                    postState(STATE_DISCONNECTED, "Disconnected");
                    break;
                case IDLE:
                case READYFORCONNECT:
                    // Initial library states; ignore.
                    break;
            }
        }
    }

    /** Unexpected drop (not user-initiated): retry with backoff, then give up. */
    private void onUnexpectedDropLocked(String reason) {
        if (!sessionActive || userInitiatedDisconnect) return;
        Log.w(TAG, "unexpected drop: " + reason + " (retry " + retryCount + "/" + MAX_RETRIES + ")");
        if (retryCount >= MAX_RETRIES) {
            fullTeardownLocked(appContext, true);
            setStateLocked(STATE_DISCONNECTED, currentServerName);
            postError("Connection failed after " + MAX_RETRIES + " attempts");
            postState(STATE_DISCONNECTED, "Disconnected");
            return;
        }
        retryCount++;
        long backoff = RETRY_BACKOFF_MS[Math.min(retryCount - 1, RETRY_BACKOFF_MS.length - 1)];
        setStateLocked(STATE_RECONNECTING, currentServerName);
        String msg = everConnectedThisSession
                ? "Connection lost. Reconnecting… attempt " + retryCount + " of " + MAX_RETRIES
                : "Retrying connection… attempt " + retryCount + " of " + MAX_RETRIES;
        postState(STATE_RECONNECTING, msg);

        // Fresh 30s timeout for this attempt.
        armTimeoutLocked();
        cancelReconnectLocked();
        final long epoch = stateEpoch;
        reconnectRunnable = () -> {
            synchronized (lock) {
                if (epoch != stateEpoch || !sessionActive || userInitiatedDisconnect) return;
                restartTunnelLocked();
            }
        };
        mainHandler.postDelayed(reconnectRunnable, backoff);
    }

    /** Stop + start the library service again, keeping locks held. */
    private void restartTunnelLocked() {
        Log.i(TAG, "restarting tunnel (attempt " + retryCount + ")");
        stateEpoch++; // invalidate callbacks from the dying service instance
        unbindLocked();
        try {
            OpenVPNService.Companion.stopService(appContext);
        } catch (Exception e) {
            Log.w(TAG, "stopService during restart failed", e);
        }
        startTunnelLocked();
    }

    // ---- timeout ----

    private void armTimeoutLocked() {
        cancelTimeoutLocked();
        final long epoch = stateEpoch;
        timeoutRunnable = () -> {
            synchronized (lock) {
                if (epoch != stateEpoch || !sessionActive || userInitiatedDisconnect) return;
                Log.w(TAG, "connect timed out after " + CONNECT_TIMEOUT_MS + "ms");
                fullTeardownLocked(appContext, true);
                setStateLocked(STATE_DISCONNECTED, currentServerName);
                postError("Connection timed out");
                postState(STATE_DISCONNECTED, "Disconnected");
            }
        };
        mainHandler.postDelayed(timeoutRunnable, CONNECT_TIMEOUT_MS);
    }

    private void cancelTimeoutLocked() {
        if (timeoutRunnable != null) {
            mainHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    private void cancelReconnectLocked() {
        if (reconnectRunnable != null) {
            mainHandler.removeCallbacks(reconnectRunnable);
            reconnectRunnable = null;
        }
    }

    private void cancelTimersLocked() {
        cancelTimeoutLocked();
        cancelReconnectLocked();
    }

    // ---- state persistence + listener posts (main thread) ----

    private void setStateLocked(String state, String serverName) {
        lastKnownState = state;
        if (serverName != null) currentServerName = serverName;
        persistState();
    }

    private void persistState() {
        Context ctx = appContext;
        if (ctx == null) return;
        try {
            SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                    .putString(KEY_STATE, lastKnownState)
                    .putString(KEY_SERVER, currentServerName != null ? currentServerName : "")
                    .apply();
        } catch (Exception e) {
            Log.w(TAG, "persistState failed", e);
        }
    }

    private void postState(String state, String message) {
        mainHandler.post(() -> {
            StatusListener l = listener;
            if (l != null) {
                try {
                    l.onState(state, message);
                } catch (Exception e) {
                    Log.w(TAG, "listener.onState threw", e);
                }
            }
        });
    }

    private void postError(String error) {
        mainHandler.post(() -> {
            StatusListener l = listener;
            if (l != null) {
                try {
                    l.onError(error);
                } catch (Exception e) {
                    Log.w(TAG, "listener.onError threw", e);
                }
            }
        });
    }

    // ---- locks ----

    private void acquireLocks(Context context) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LuckyVPN::ovpn");
            wakeLock.acquire(30 * 60 * 1000L);
        } catch (Exception e) { Log.w(TAG, "wakelock failed", e); }
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "LuckyVPN::ovpn");
            wifiLock.acquire();
        } catch (Exception e) { Log.w(TAG, "wifilock failed", e); }
    }

    private void releaseLocks() {
        try { if (wakeLock != null && wakeLock.isHeld()) wakeLock.release(); } catch (Exception ignored) {}
        try { if (wifiLock != null && wifiLock.isHeld()) wifiLock.release(); } catch (Exception ignored) {}
        wakeLock = null; wifiLock = null;
    }

    // ---- config helpers (unchanged) ----

    private String extractHost(String config) {
        for (String line : config.split("\n")) {
            String t = line.trim();
            if (t.startsWith("remote ")) {
                String[] parts = t.split("\\s+");
                if (parts.length >= 2) return parts[1];
            }
        }
        return "";
    }

    private int extractPort(String config) {
        for (String line : config.split("\n")) {
            String t = line.trim();
            if (t.startsWith("remote ")) {
                String[] parts = t.split("\\s+");
                if (parts.length >= 3) {
                    try { return Integer.parseInt(parts[2]); } catch (NumberFormatException ignored) {}
                }
            }
        }
        return 1194;
    }
}
