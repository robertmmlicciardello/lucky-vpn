package app.lovable.luckyvpnmaster.models;

import org.json.JSONObject;

/** One row from GET /subscription/history. */
public class PaymentRecord {
    public int id;
    /** Display-ready amount (may be "5000", "5000.00", "5,000 MMK", ...). */
    public String amount = "";
    public String planType = "";
    public String paymentMethod = "";
    public String transactionId = "";
    /** pending | approved | rejected */
    public String status = "pending";
    public String createdAt = "";

    public static PaymentRecord fromJson(JSONObject o) {
        PaymentRecord r = new PaymentRecord();
        r.id = o.optInt("id");
        Object amt = o.opt("amount");
        r.amount = amt != null ? String.valueOf(amt) : "";
        r.planType = o.optString("plan_type", o.optString("planType", ""));
        r.paymentMethod = o.optString("payment_method", o.optString("paymentMethod", ""));
        r.transactionId = o.optString("transaction_id", o.optString("transactionId", ""));
        r.status = o.optString("status", "pending");
        r.createdAt = o.optString("created_at", o.optString("createdAt", ""));
        return r;
    }
}
