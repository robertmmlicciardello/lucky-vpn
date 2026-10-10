package app.lovable.luckyvpnmaster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import app.lovable.luckyvpnmaster.utils.KillSwitchManager;

/**
 * Full-screen kill-switch warning shown after an unexpected VPN disconnect.
 */
public class KillSwitchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kill_switch);

        Button btnReconnect = findViewById(R.id.btn_ks_reconnect);
        Button btnDismiss = findViewById(R.id.btn_ks_dismiss);

        btnReconnect.setOnClickListener(v -> {
            KillSwitchManager.clear(this);
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnDismiss.setOnClickListener(v -> {
            KillSwitchManager.clear(this);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Force an explicit choice via the buttons.
        super.onBackPressed();
    }
}
