
package app.lovable.luckyvpnmaster;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PaymentManager {
    private static final String API_BASE_URL = "http://your-backend-url.com/api/v1";
    private Context context;

    public PaymentManager(Context context) {
        this.context = context;
    }

    public void initiatePayment(String planId, String paymentMethod, double amount) {
        new Thread(() -> {
            try {
                // Get payment account details from backend
                URL url = new URL(API_BASE_URL + "/subscription/accounts");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + getAccessToken());

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JSONObject responseJson = new JSONObject(response.toString());
                if (responseJson.getBoolean("success")) {
                    JSONObject accounts = responseJson.getJSONObject("accounts");
                    
                    if (accounts.has(paymentMethod)) {
                        JSONObject account = accounts.getJSONObject(paymentMethod);
                        String accountNumber = account.getString("account_number");
                        String accountName = account.getString("account_name");
                        
                        // Show payment details to user
                        showPaymentDialog(paymentMethod, accountNumber, accountName, planId, amount);
                    }
                }

            } catch (Exception e) {
                Log.e("PaymentManager", "Error getting payment accounts", e);
            }
        }).start();
    }

    private void showPaymentDialog(String method, String accountNumber, String accountName, String planId, double amount) {
        Intent intent = new Intent(context, PaymentActivity.class);
        intent.putExtra("payment_method", method);
        intent.putExtra("account_number", accountNumber);
        intent.putExtra("account_name", accountName);
        intent.putExtra("plan_id", planId);
        intent.putExtra("amount", amount);
        context.startActivity(intent);
    }

    public void submitPaymentProof(String planId, String paymentMethod, String transactionId, double amount) {
        new Thread(() -> {
            try {
                URL url = new URL(API_BASE_URL + "/subscription/submit");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + getAccessToken());
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("transaction_id", transactionId);
                json.put("amount", amount);
                json.put("payment_method", paymentMethod);
                json.put("plan_type", planId);
                json.put("duration", getDurationByPlan(planId));

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 201) {
                    Log.d("PaymentManager", "Payment proof submitted successfully");
                }

            } catch (Exception e) {
                Log.e("PaymentManager", "Error submitting payment proof", e);
            }
        }).start();
    }

    private int getDurationByPlan(String planId) {
        switch (planId) {
            case "monthly": return 30;
            case "quarterly": return 90;
            case "yearly": return 365;
            default: return 30;
        }
    }

    private String getAccessToken() {
        return context.getSharedPreferences("VPNMasterPoints", Context.MODE_PRIVATE)
                .getString("access_token", "");
    }
}
