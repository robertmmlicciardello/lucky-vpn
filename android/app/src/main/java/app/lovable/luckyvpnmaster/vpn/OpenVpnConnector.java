package app.lovable.luckyvpnmaster.vpn;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import de.blinkt.openvpn.api.IOpenVPNAPIService;
import de.blinkt.openvpn.api.IOpenVPNStatusCallback;

/**
 * Connects to real OpenVPN servers through the external API of
 * "OpenVPN for Android" (de.blinkt.openvpn, open-source, by Arne Schwabe).
 *
 * Flow:
 *   1. bind to de.blinkt.openvpn.api.ExternalOpenVPNService
 *   2. api.prepare(packageName)  -> permission Intent if the user has not
 *      allowed this app yet (shown once, remembered)
 *   3. api.prepareVPNService()   -> system VPN-consent dialog Intent if needed
 *   4. api.startVPN(inlineOvpnConfig)  (VPNGate configs are fully inlined)
 *   5. status updates arrive via IOpenVPNStatusCallback
 *
 * If the OpenVPN client app is not installed the caller should send the
 * user to the Play Store (see openPlayStore()).
 */
public class OpenVpnConnector {
    private static final String TAG = "OpenVpnConnector";

    public static final String OPENVPN_PACKAGE = "de.blinkt.openvpn";
    private static final String API_ACTION = "de.blinkt.openvpn.api.IOpenVPNAPIService";

    public static final int REQ_API_PERMISSION = 7001;
    public static final int REQ_VPN_SERVICE = 7002;

    public interface StatusListener {
        void onState(String state, String message);
        void onError(String error);
    }

    private static OpenVpnConnector instance;

    private IOpenVPNAPIService api;
    private boolean bound = false;
    private String pendingConfig;
    private StatusListener listener;
    private Activity activityRef;

    private final IOpenVPNStatusCallback statusCallback = new IOpenVPNStatusCallback.Stub() {
        @Override
        public void newStatus(String uuid, String state, String message, String level) {
            Log.d(TAG, "ovpn state=" + state + " msg=" + message);
            if (listener != null) listener.onState(state, message);
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            api = IOpenVPNAPIService.Stub.asInterface(service);
            bound = true;
            try {
                api.registerStatusCallback(statusCallback);
            } catch (RemoteException e) {
                Log.e(TAG, "registerStatusCallback failed", e);
            }
            proceedWithPermissionFlow();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            api = null;
            bound = false;
        }
    };

    private OpenVpnConnector() {}

    public static synchronized OpenVpnConnector getInstance() {
        if (instance == null) instance = new OpenVpnConnector();
        return instance;
    }

    /** True when "OpenVPN for Android" is installed. */
    public static boolean isClientInstalled(Context ctx) {
        try {
            ctx.getPackageManager().getPackageInfo(OPENVPN_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /** Open the Play Store page of "OpenVPN for Android". */
    public static void openPlayStore(Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + OPENVPN_PACKAGE)));
        } catch (Exception e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://play.google.com/store/apps/details?id=" + OPENVPN_PACKAGE)));
        }
    }

    /**
     * Start a real OpenVPN connection with the given inline .ovpn config.
     * Handles the whole permission flow; forwards activity results via
     * {@link #onActivityResult}.
     */
    public void connect(Activity activity, String ovpnConfig, StatusListener listener) {
        if (ovpnConfig == null || !ovpnConfig.contains("remote ")) {
            listener.onError("Invalid VPN config");
            return;
        }
        this.activityRef = activity;
        this.listener = listener;
        this.pendingConfig = ovpnConfig;

        if (bound && api != null) {
            proceedWithPermissionFlow();
            return;
        }
        Intent intent = new Intent(API_ACTION);
        intent.setPackage(OPENVPN_PACKAGE);
        try {
            activity.getApplicationContext().bindService(
                    intent, connection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(TAG, "bindService failed", e);
            listener.onError("Could not reach OpenVPN app: " + e.getMessage());
        }
    }

    /** Forward from the hosting Activity/Fragment. */
    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQ_API_PERMISSION || requestCode == REQ_VPN_SERVICE) {
            if (resultCode == Activity.RESULT_OK) {
                proceedWithPermissionFlow();
            } else if (listener != null) {
                listener.onError("Permission denied");
            }
        }
    }

    private void proceedWithPermissionFlow() {
        if (api == null || activityRef == null) return;
        try {
            // 1. external-API permission (one-time grant inside the OpenVPN app)
            Intent perm = api.prepare(activityRef.getPackageName());
            if (perm != null) {
                activityRef.startActivityForResult(perm, REQ_API_PERMISSION);
                return;
            }
            // 2. system VPN consent dialog
            Intent vpnPerm = api.prepareVPNService();
            if (vpnPerm != null) {
                activityRef.startActivityForResult(vpnPerm, REQ_VPN_SERVICE);
                return;
            }
            // 3. go
            api.startVPN(pendingConfig);
        } catch (RemoteException e) {
            Log.e(TAG, "API call failed", e);
            if (listener != null) listener.onError("OpenVPN error: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "permission flow failed", e);
            if (listener != null) listener.onError(e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (bound && api != null) api.disconnect();
        } catch (RemoteException e) {
            Log.e(TAG, "disconnect failed", e);
        }
    }

    public void release(Context ctx) {
        try {
            if (bound) {
                if (api != null) {
                    try { api.unregisterStatusCallback(statusCallback); }
                    catch (RemoteException ignored) {}
                }
                ctx.getApplicationContext().unbindService(connection);
            }
        } catch (Exception e) {
            Log.e(TAG, "unbind failed", e);
        } finally {
            bound = false;
            api = null;
            listener = null;
            activityRef = null;
            pendingConfig = null;
        }
    }

    /** Map raw OpenVPN states to a simple connected/disconnected verdict. */
    public static Boolean toConnected(String state) {
        if (state == null) return null;
        switch (state) {
            case "CONNECTED": return true;
            case "NOPROCESS":
            case "DISCONNECTED":
            case "EXITING":
                return false;
            default: return null; // transitional
        }
    }
}
