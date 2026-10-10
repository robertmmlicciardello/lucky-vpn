package app.lovable.luckyvpnmaster.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import app.lovable.luckyvpnmaster.KillSwitchActivity;
import app.lovable.luckyvpnmaster.R;

/**
 * Handles the kill-switch reaction to an unexpected VPN disconnect:
 * raises a persistent notification and opens the kill-switch warning screen.
 */
public class KillSwitchManager {

    private static final String CHANNEL_ID = "kill_switch";
    private static final int NOTIF_ID = 9001;

    public static void onUnexpectedDisconnect(Context ctx) {
        if (ctx == null) return;
        Context app = ctx.getApplicationContext();
        if (!new SettingsManager(app).isKillSwitch()) return;

        createChannel(app);

        Intent intent = new Intent(app, KillSwitchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(app, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentTitle(app.getString(R.string.kill_switch_notif_title))
                .setContentText(app.getString(R.string.kill_switch_notif_text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setAutoCancel(false);

        try {
            NotificationManagerCompat.from(app).notify(NOTIF_ID, builder.build());
        } catch (SecurityException ignored) {
            // POST_NOTIFICATIONS not granted — continue with the activity only.
        }

        app.startActivity(intent);
    }

    public static void clear(Context ctx) {
        if (ctx == null) return;
        NotificationManagerCompat.from(ctx.getApplicationContext()).cancel(NOTIF_ID);
    }

    private static void createChannel(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm == null || nm.getNotificationChannel(CHANNEL_ID) != null) return;
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                ctx.getString(R.string.kill_switch_channel_name),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(ctx.getString(R.string.kill_switch_channel_desc));
        nm.createNotificationChannel(channel);
    }
}
