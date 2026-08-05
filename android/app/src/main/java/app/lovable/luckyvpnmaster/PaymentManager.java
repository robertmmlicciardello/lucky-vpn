
package app.lovable.luckyvpnmaster;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import app.lovable.luckyvpnmaster.api.APIConfig;
import app.lovable.luckyvpnmaster.auth.AuthManager;

public class PaymentManager {
    private Context context;
    private AuthManager authManager;

    public PaymentManager(Context context) {
        this.context = context;
        this.authManager = new AuthManager(context);
    }

    public interface PaymentCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public void submitPayment(String transactionId, double amount, String paymentMethod, 
                            String planType, int duration, PaymentCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(APIConfig.API_BASE_URL + APIConfig.SUBMIT_PAYMENT_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + authManager.getAccessToken());
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("transaction_id", transactionId);
                json.put("amount", amount);
                json.put("payment_method", paymentMethod);
                json.put("plan_type", planType);
                json.put("duration", duration);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JSONObject responseJson = new JSONObject(response.toString());
                if (responseCode == 201 && responseJson.getBoolean("success")) {
                    callback.onSuccess("Payment submitted successfully");
                } else {
                    String errorMessage = responseJson.optString("message", "Payment submission failed");
                    callback.onError(errorMessage);
                }

            } catch (Exception e) {
                Log.e("PaymentManager", "Error submitting payment", e);
                callback.onError("Network error");
            }
        }).start();
    }
}
