
package app.lovable.luckyvpnmaster.api;

public class APIConfig {
    // Change this to your actual backend URL
    public static final String API_BASE_URL = "http://10.0.2.2:3000/api/v1"; // For Android emulator
    // For physical device, use: "http://YOUR_COMPUTER_IP:3000/api/v1"
    
    // API Endpoints
    public static final String LOGIN_ENDPOINT = "/auth/login";
    public static final String REGISTER_ENDPOINT = "/auth/register";
    public static final String FREE_SERVERS_ENDPOINT = "/servers/free";
    public static final String PREMIUM_SERVERS_ENDPOINT = "/servers/premium";
    public static final String DAILY_REWARD_ENDPOINT = "/rewards/daily";
    public static final String WATCH_AD_ENDPOINT = "/rewards/watch-ad";
    public static final String SUBMIT_PAYMENT_ENDPOINT = "/payments/submit";
    public static final String PAYMENT_ACCOUNTS_ENDPOINT = "/payments/accounts";
}
