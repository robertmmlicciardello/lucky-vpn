package app.lovable.luckyvpnmaster.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import app.lovable.luckyvpnmaster.api.APIConfig;

/**
 * Fetches the public app-config from the server on startup and enforces
 * the admin's Critical Controls: maintenance mode + force update.
 */
public class AppConfigChecker {

    public interface Callback {
        /** Called on the UI thread when the check passes (app may proceed). */
        void onProceed();
    }

    public static void check(Activity activity, Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(APIConfig.API_BASE_URL + "/app-config");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                int code = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                if (code != 200) {
                    proceed(activity, callback);
                    return;
                }
                JSONObject data = new JSONObject(sb.toString()).optJSONObject("data");
                if (data == null) data = new JSONObject();

                final boolean maintenance = data.optBoolean("maintenance_mode", false);
                final String maintenanceMsg = data.optString("maintenance_message",
                        "The app is under maintenance. Please try again later.");
                final String minVersion = data.optString("min_app_version", "1.0.0");
                final String updateUrl = data.optString("update_url", "");
                final String currentVersion = getAppVersion(activity);

                if (maintenance) {
                    activity.runOnUiThread(() -> showMaintenance(activity, maintenanceMsg));
                } else if (isOlder(currentVersion, minVersion)) {
                    activity.runOnUiThread(() -> showForceUpdate(activity, updateUrl));
                } else {
                    proceed(activity, callback);
                }
            } catch (Exception e) {
                Log.e("AppConfigChecker", "config check failed", e);
                proceed(activity, callback); // never block the app on a network error
            }
        }).start();
    }

    private static void proceed(Activity activity, Callback callback) {
        activity.runOnUiThread(callback::onProceed);
    }

    private static String getAppVersion(Activity activity) {
        try {
            return activity.getPackageManager()
                    .getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    /** True if current < required (numeric dot-separated comparison). */
    private static boolean isOlder(String current, String required) {
        try {
            String[] c = current.split("\\.");
            String[] r = required.split("\\.");
            int n = Math.max(c.length, r.length);
            for (int i = 0; i < n; i++) {
                int cv = i < c.length ? Integer.parseInt(c[i].replaceAll("\\D", "")) : 0;
                int rv = i < r.length ? Integer.parseInt(r[i].replaceAll("\\D", "")) : 0;
                if (cv < rv) return true;
                if (cv > rv) return false;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static void showMaintenance(Activity activity, String message) {
        new AlertDialog.Builder(activity)
                .setTitle("Maintenance")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Retry", (d, w) -> check(activity, () -> {
                }))
                .show();
    }

    private static void showForceUpdate(Activity activity, String updateUrl) {
        final boolean hasUrl = updateUrl != null && !updateUrl.isEmpty();
        AlertDialog.Builder b = new AlertDialog.Builder(activity)
                .setTitle("Update Required")
                .setMessage("A new version of Lucky VPN is available. Please update to continue.")
                .setCancelable(false)
                .setPositiveButton("Update", (d, w) -> {
                    try {
                        String url = hasUrl ? updateUrl
                                : "https://play.google.com/store/apps/details?id="
                                + activity.getPackageName();
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } catch (Exception ignored) {
                    }
                });
        // Allow "Later" only when no update URL is configured (misconfig safety);
        // otherwise the app must not proceed until updated.
        if (!hasUrl) {
            b.setNegativeButton("Later", (d, w) -> {
            });
        }
        AlertDialog dialog = b.show();
        dialog.setOnDismissListener(d -> {
            if (hasUrl) activity.finish();
        });
    }
}
