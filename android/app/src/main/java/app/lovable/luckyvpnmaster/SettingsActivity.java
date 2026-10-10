package app.lovable.luckyvpnmaster;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import app.lovable.luckyvpnmaster.utils.SettingsManager;

/**
 * Settings screen. Uses the fixed view-ID contract in
 * layout/activity_settings.xml (created by the UI worker).
 */
public class SettingsActivity extends AppCompatActivity {

    private static final int REQ_POST_NOTIFICATIONS = 1001;

    private SettingsManager settingsManager;
    private SwitchCompat switchAutoConnect, switchKillSwitch, switchNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings_title);
        }

        settingsManager = new SettingsManager(this);

        switchAutoConnect = findViewById(R.id.switch_auto_connect);
        switchKillSwitch = findViewById(R.id.switch_kill_switch);
        switchNotifications = findViewById(R.id.switch_notifications);
        TextView tvProtocolValue = findViewById(R.id.tv_protocol_value);
        TextView tvDnsValue = findViewById(R.id.tv_dns_value);

        switchAutoConnect.setChecked(settingsManager.isAutoConnect());
        switchKillSwitch.setChecked(settingsManager.isKillSwitch());
        switchNotifications.setChecked(settingsManager.isNotifications());

        // WireGuard not available in this build — protocol is OpenVPN (UDP) only.
        tvProtocolValue.setText(getString(R.string.settings_protocol_value));
        tvDnsValue.setText(getString(R.string.settings_dns_value));

        switchAutoConnect.setOnCheckedChangeListener((b, checked) ->
                settingsManager.setAutoConnect(checked));

        switchKillSwitch.setOnCheckedChangeListener((b, checked) ->
                settingsManager.setKillSwitch(checked));

        switchNotifications.setOnCheckedChangeListener((b, checked) -> {
            settingsManager.setNotifications(checked);
            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIFICATIONS);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIFICATIONS
                && (grantResults.length == 0
                    || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            // User denied — revert the switch so state stays truthful.
            settingsManager.setNotifications(false);
            switchNotifications.setChecked(false);
            Toast.makeText(this, R.string.notifications_permission_denied, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
