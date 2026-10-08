package app.lovable.luckyvpnmaster.vpn;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;

import com.tim.openvpn.configuration.OpenVPNConfig;
import com.tim.openvpn.service.OpenVPNService;

/**
 * Embedded OpenVPN connector using the tim06 OpenVPN library (OpenVPN3,
 * Apache-2.0). No separate "OpenVPN for Android" install needed — the
 * engine runs inside our own app via the library's VpnService.
 *
 * Flow:
 *   1. VpnService.prepare() -> system VPN consent dialog (once)
 *   2. Sanitize the .ovpn config (OvpnConfigSanitizer)
 *   3. OpenVPNService.Companion.startService() with the config
 *   4. Stop via Companion.stopService()
 */
public class EmbeddedOpenVpnConnector {
    private static final String TAG = "EmbeddedOpenVPN";

    public static final int REQ_VPN_PERMISSION = 7002;

    public interface StatusListener {
        void onState(String state, String message);
        void onError(String error);
    }

    private static EmbeddedOpenVpnConnector instance;

    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;
    private StatusListener listener;

    private EmbeddedOpenVpnConnector() {}

    public static synchronized EmbeddedOpenVpnConnector getInstance() {
        if (instance == null) instance = new EmbeddedOpenVpnConnector();
        return instance;
    }

    /** Returns an Intent for the system VPN-consent dialog, or null if granted. */
    public Intent prepareVpn(Context context) {
        return VpnService.prepare(context);
    }

    public void connect(Context context, String rawOvpnConfig, String serverName, StatusListener listener) {
        this.listener = listener;
        try {
            String config = OvpnConfigSanitizer.sanitize(rawOvpnConfig);
            if (config.isEmpty()) {
                notifyError("Empty VPN config");
                return;
            }

            acquireLocks(context);

            String host = extractHost(rawOvpnConfig);
            int port = extractPort(rawOvpnConfig);

            OpenVPNConfig vpnConfig = new OpenVPNConfig(
                    serverName != null ? serverName : "lucky-vpn",
                    host,
                    port,
                    "udp-client",
                    null, null, null, null, null, null,
                    config
            );

            OpenVPNService.Companion.startService(context, vpnConfig, null, new String[0]);
            Log.i(TAG, "OpenVPN service start requested for " + host + ":" + port);
            if (this.listener != null) this.listener.onState("CONNECTING", "Starting VPN…");
        } catch (Exception e) {
            Log.e(TAG, "connect failed", e);
            releaseLocks();
            notifyError("Failed to start VPN: " + e.getMessage());
        }
    }

    public void disconnect(Context context) {
        try {
            OpenVPNService.Companion.stopService(context);
            Log.i(TAG, "OpenVPN service stop requested");
        } catch (Exception e) {
            Log.e(TAG, "disconnect failed", e);
        } finally {
            releaseLocks();
            if (listener != null) listener.onState("DISCONNECTED", "Disconnected");
        }
    }

    // ---- helpers ----

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

    private void notifyError(String msg) {
        if (listener != null) listener.onError(msg);
    }

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
