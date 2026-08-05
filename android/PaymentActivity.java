
package app.lovable.luckyvpnmaster;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {
    private String paymentMethod;
    private String accountNumber;
    private String accountName;
    private String planId;
    private double amount;
    
    private TextView tvPaymentMethod;
    private TextView tvAccountNumber;
    private TextView tvAccountName;
    private TextView tvAmount;
    private EditText etTransactionId;
    private Button btnCopyAccount;
    private Button btnOpenApp;
    private Button btnSubmitProof;
    private ImageView ivQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        
        // Get payment details from intent
        Intent intent = getIntent();
        paymentMethod = intent.getStringExtra("payment_method");
        accountNumber = intent.getStringExtra("account_number");
        accountName = intent.getStringExtra("account_name");
        planId = intent.getStringExtra("plan_id");
        amount = intent.getDoubleExtra("amount", 0.0);
        
        initViews();
        setupPaymentDetails();
        setupClickListeners();
    }

    private void initViews() {
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvAccountNumber = findViewById(R.id.tv_account_number);
        tvAccountName = findViewById(R.id.tv_account_name);
        tvAmount = findViewById(R.id.tv_amount);
        etTransactionId = findViewById(R.id.et_transaction_id);
        btnCopyAccount = findViewById(R.id.btn_copy_account);
        btnOpenApp = findViewById(R.id.btn_open_app);
        btnSubmitProof = findViewById(R.id.btn_submit_proof);
        ivQRCode = findViewById(R.id.iv_qr_code);
    }

    private void setupPaymentDetails() {
        tvPaymentMethod.setText(paymentMethod.toUpperCase());
        tvAccountNumber.setText(accountNumber);
        tvAccountName.setText(accountName);
        tvAmount.setText(String.format("%.0f MMK", amount));
        
        // Load QR code if available
        if ("kpay".equals(paymentMethod.toLowerCase())) {
            // Generate or load KPay QR code
            loadKPayQR();
        } else if ("wave".equals(paymentMethod.toLowerCase())) {
            // Generate or load Wave Money QR code
            loadWaveQR();
        }
    }

    private void setupClickListeners() {
        btnCopyAccount.setOnClickListener(v -> copyAccountNumber());
        btnOpenApp.setOnClickListener(v -> openPaymentApp());
        btnSubmitProof.setOnClickListener(v -> submitPaymentProof());
    }

    private void copyAccountNumber() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Account Number", accountNumber);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Account number copied!", Toast.LENGTH_SHORT).show();
    }

    private void openPaymentApp() {
        try {
            Intent intent = null;
            String packageName = "";
            
            switch (paymentMethod.toLowerCase()) {
                case "kpay":
                    packageName = "com.kbzbank.kpaycustomer";
                    intent = getPackageManager().getLaunchIntentForPackage(packageName);
                    break;
                case "wave":
                    packageName = "com.wavemoney.userapp";
                    intent = getPackageManager().getLaunchIntentForPackage(packageName);
                    break;
                case "aya":
                    packageName = "mm.com.ayabank.ayapay";
                    intent = getPackageManager().getLaunchIntentForPackage(packageName);
                    break;
                case "cb":
                    packageName = "mm.com.cbbank.cbpay";
                    intent = getPackageManager().getLaunchIntentForPackage(packageName);
                    break;
            }
            
            if (intent != null) {
                startActivity(intent);
            } else {
                // Open Play Store if app not installed
                Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
                playStoreIntent.setData(Uri.parse("market://details?id=" + packageName));
                startActivity(playStoreIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Could not open payment app", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitPaymentProof() {
        String transactionId = etTransactionId.getText().toString().trim();
        
        if (transactionId.isEmpty()) {
            Toast.makeText(this, "Please enter transaction ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Submit payment proof via API
        PaymentManager paymentManager = new PaymentManager(this);
        paymentManager.submitPaymentProof(planId, paymentMethod, transactionId);
        
        Toast.makeText(this, "Payment proof submitted! Please wait for approval.", Toast.LENGTH_LONG).show();
        
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void loadKPayQR() {
        // In a real app, you would generate or load the actual QR code
        // For now, we'll use a placeholder
        ivQRCode.setImageResource(R.drawable.kpay_qr_placeholder);
    }

    private void loadWaveQR() {
        // In a real app, you would generate or load the actual QR code
        // For now, we'll use a placeholder
        ivQRCode.setImageResource(R.drawable.wave_qr_placeholder);
    }
}
