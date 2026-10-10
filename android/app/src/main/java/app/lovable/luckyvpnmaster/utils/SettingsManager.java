package app.lovable.luckyvpnmaster.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * App settings (auto-connect, kill switch, notifications) in SharedPreferences.
 */
public class SettingsManager {

    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_AUTO_CONNECT = "auto_connect";
    private static final String KEY_KILL_SWITCH = "kill_switch";
    private static final String KEY_NOTIFICATIONS = "notifications";

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isAutoConnect() {
        return prefs.getBoolean(KEY_AUTO_CONNECT, false);
    }

    public void setAutoConnect(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_CONNECT, enabled).apply();
    }

    public boolean isKillSwitch() {
        return prefs.getBoolean(KEY_KILL_SWITCH, false);
    }

    public void setKillSwitch(boolean enabled) {
        prefs.edit().putBoolean(KEY_KILL_SWITCH, enabled).apply();
    }

    public boolean isNotifications() {
        return prefs.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public void setNotifications(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }
}
