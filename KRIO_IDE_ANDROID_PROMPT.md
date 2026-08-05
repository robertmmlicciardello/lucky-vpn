# Complete Android VPN App Development Prompt for Krio IDE AI

## Project Overview
Create a comprehensive Android VPN application called "Lucky VPN Master" with full backend integration, monetization features, and advanced VPN functionality. This app should connect to an existing Node.js backend API and include multiple revenue streams.

## Core App Information
- **App Name:** Lucky VPN Master  
- **Package Name:** app.lovable.luckyvpnmaster
- **Target SDK:** 34 (Android 14)
- **Min SDK:** 21 (Android 5.0)
- **Language:** Java
- **Architecture:** MVVM with Fragment-based navigation

## Backend API Integration
The app must connect to a Node.js backend running at:
- **Base URL:** `http://10.0.2.2:3000/api/v1` (for emulator)
- **Production:** `https://your-domain.com/api/v1`

### Required API Endpoints Integration:

#### Authentication Endpoints:
- `POST /auth/login` - User login
- `POST /auth/register` - User registration
- `POST /auth/logout` - User logout

#### User Management:
- `GET /user/profile` - Get user profile
- `PUT /user/update` - Update user profile
- `GET /user/stats` - User statistics and points

#### Server Management:
- `GET /servers` - Get all VPN servers
- `GET /servers/free` - Get free servers
- `GET /servers/premium` - Get premium servers
- `GET /servers/{id}/config` - Get server configuration

#### OneConnect Integration:
- `GET /oneconnect/config` - Get OneConnect configuration
- `PUT /oneconnect/config` - Update OneConnect settings
- `POST /oneconnect/sync` - Sync servers from OneConnect API

#### Rewards & Points System:
- `POST /rewards/daily-checkin` - Daily check-in reward
- `POST /rewards/watch-video` - Watch ad reward
- `POST /rewards/refer-friend` - Referral reward
- `GET /rewards/points` - Get user points
- `POST /rewards/redeem` - Redeem points

#### Payment System:
- `GET /subscription/plans` - Get subscription plans
- `POST /subscription/subscribe` - Subscribe to plan
- `POST /payments/submit` - Submit payment proof
- `GET /payments/accounts` - Get payment accounts (KPay, Wave, AYA)

#### Ad Revenue:
- `POST /ads/view` - Record ad view
- `POST /ads/click` - Record ad click
- `GET /ads/config` - Get ad configuration

## Core Features Implementation

### 1. Authentication System
```java
// AuthManager.java - Handle all authentication
public class AuthManager {
    // Login with email/password
    public void login(String email, String password, AuthCallback callback)
    
    // Register new user
    public void register(String name, String email, String password, AuthCallback callback)
    
    // Check if user is logged in
    public boolean isLoggedIn()
    
    // Get stored access token
    public String getAccessToken()
    
    // Logout and clear data
    public void logout()
}
```

### 2. VPN Core Service
```java
// VPNService.java - Main VPN functionality
public class VPNService extends VpnService {
    // Connect to VPN server
    public void connectToServer(Server server)
    
    // Disconnect from VPN
    public void disconnect()
    
    // Get connection status
    public boolean isConnected()
    
    // Handle multiple VPN protocols (OpenVPN, IKEv2, WireGuard)
    // Auto-reconnect functionality
    // Kill switch implementation
    // DNS leak protection
}
```

### 3. Server Management
```java
// ServerManager.java - Manage VPN servers
public class ServerManager {
    // Load servers from API
    public void loadServers(ServerCallback callback)
    
    // Get best server for user
    public Server getBestServer()
    
    // Filter servers by location/type
    public List<Server> filterServers(String location, String type)
    
    // Test server speed/ping
    public void testServerSpeed(Server server, SpeedCallback callback)
    
    // OneConnect integration
    public void syncOneConnectServers(OneConnectCallback callback)
    
    // Check OneConnect status
    public boolean isOneConnectEnabled()
}
```

### 4. Payment Integration
```java
// PaymentManager.java - Handle Myanmar payment systems
public class PaymentManager {
    // Submit KPay payment
    public void submitKPayPayment(String transactionId, double amount, PaymentCallback callback)
    
    // Submit Wave payment  
    public void submitWavePayment(String transactionId, double amount, PaymentCallback callback)
    
    // Submit AYA payment
    public void submitAYAPayment(String transactionId, double amount, PaymentCallback callback)
    
    // Verify payment status
    public void verifyPayment(String transactionId, PaymentCallback callback)
}
```

### 5. Rewards & Monetization
```java
// RewardsManager.java - Handle points and rewards
public class RewardsManager {
    // Daily check-in reward
    public void dailyCheckin(RewardCallback callback)
    
    // Watch video ad reward
    public void watchVideoAd(RewardCallback callback)
    
    // Referral system
    public void referFriend(String friendEmail, RewardCallback callback)
    
    // Redeem points for premium time
    public void redeemPoints(int points, RewardCallback callback)
}
```

### 6. Ad Integration (Multiple Networks)
```java
// AdManager.java - Multiple ad networks
public class AdManager {
    // Initialize ad networks (AdMob, Unity, AppLovin, etc.)
    public void initializeAds()
    
    // Show banner ads
    public void showBannerAd(ViewGroup container)
    
    // Show interstitial ads
    public void showInterstitialAd(AdCallback callback)
    
    // Show rewarded video ads
    public void showRewardedVideoAd(RewardedAdCallback callback)
    
    // Native ads for premium content
    public void loadNativeAd(NativeAdCallback callback)
}
```

## UI/UX Implementation

### 1. Main Activity with Bottom Navigation
- **Home Tab:** Connection status, quick connect, user stats
- **Servers Tab:** List of available servers with flags and ping
- **Rewards Tab:** Daily rewards, points balance, ad watching
- **Profile Tab:** User profile, subscription status, settings

### 2. Required Fragments
```java
// HomeFragment.java - Main dashboard
public class HomeFragment extends Fragment {
    // Connection button (large, prominent)
    // Current server display
    // Connection status and time
    // User points and level
    // Quick stats (data used, speed)
}

// ServersFragment.java - Server selection
public class ServersFragment extends Fragment {
    // RecyclerView of servers with country flags
    // Filter options (Free/Premium, Location)
    // Server ping and load indicators
    // Favorite servers functionality
}

// RewardsFragment.java - Monetization hub
public class RewardsFragment extends Fragment {
    // Daily check-in calendar
    // Watch video ads section
    // Points balance and history
    // Redemption options
    // Referral code sharing
}

// ProfileFragment.java - User account
public class ProfileFragment extends Fragment {
    // User info and subscription status
    // App settings and preferences
    // Payment history
    // Support and feedback
}
```

### 3. Additional Activities
```java
// LoginActivity.java - User authentication
// RegisterActivity.java - User registration  
// SubscriptionActivity.java - Premium plans
// PaymentActivity.java - Payment processing
// SettingsActivity.java - App configuration
```

## Advanced Features Implementation

### 1. VPN Protocols Support
- **OpenVPN:** Primary protocol with .ovpn file support
- **IKEv2/IPSec:** Built-in Android VPN support
- **WireGuard:** Modern, fast protocol
- **Auto-protocol selection** based on server and network

### 2. Security Features
- **Kill Switch:** Block internet if VPN disconnects
- **DNS Leak Protection:** Force traffic through VPN DNS
- **IPv6 Leak Protection:** Disable IPv6 when connected
- **Auto-connect:** Connect on untrusted networks
- **Split Tunneling:** Allow certain apps to bypass VPN

### 3. Performance Features
- **Speed Test:** Built-in speed testing
- **Auto-reconnect:** Seamless reconnection
- **Server Load Balancing:** Distribute users across servers
- **Bandwidth Monitoring:** Track data usage
- **Connection Optimization:** Adaptive connection settings

### 4. Monetization Features
- **Freemium Model:** Limited free servers, unlimited premium
- **Ad Revenue:** Multiple ad networks integration
- **Points System:** Earn points through activities
- **Referral Program:** Reward user referrals
- **In-app Purchases:** Premium subscriptions
- **Local Payment Methods:** KPay, Wave, AYA Bank integration

## Models and Data Structures

### 1. User Model
```java
public class User {
    public int id;
    public String name;
    public String email;
    public String plan; // "free", "premium", "vip"
    public int points;
    public String referralCode;
    public String subscriptionExpires;
    public boolean isActive;
    public String createdAt;
}
```

### 2. Server Model
```java
public class Server {
    public int id;
    public String name;
    public String country;
    public String countryCode;
    public String city;
    public String ip;
    public String port;
    public String protocol;
    public boolean isPremium;
    public int load; // Server load percentage
    public int ping; // Ping in milliseconds
    public String config; // VPN configuration
    public boolean isActive;
    public String provider; // "manual" or "oneconnect"
    public String oneConnectId; // OneConnect server ID
    public String lastSync; // Last sync time for OneConnect servers
}
```

### 3. Subscription Plan Model
```java
public class SubscriptionPlan {
    public int id;
    public String name;
    public String description;
    public double price;
    public int duration; // in days
    public String features[];
    public boolean isPopular;
    public String paymentMethods[];
}
```

## Key Android Components Required

### 1. Dependencies (build.gradle)
```gradle
dependencies {
    // Core Android
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.10.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.fragment:fragment:1.6.2'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    
    // Networking
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    
    // Google Play Services
    implementation 'com.google.android.gms:play-services-ads:22.6.0'
    implementation 'com.google.android.gms:play-services-location:21.0.1'
    
    // Additional Ad Networks
    implementation 'com.unity3d.ads:unity-ads:4.9.2'
    implementation 'com.applovin:applovin-sdk:11.11.3'
    
    // Image Loading
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    
    // Charts for statistics
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
}
```

### 2. Permissions (AndroidManifest.xml)
```xml
<!-- VPN Permissions -->
<uses-permission android:name="android.permission.BIND_VPN_SERVICE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<!-- Location for optimal server selection -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Ad Networks -->
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />
```

## Myanmar-Specific Features

### 1. Local Payment Integration
- **KPay API Integration:** For mobile payments
- **Wave Money API:** Popular mobile wallet
- **AYA Bank Integration:** Major bank payment system
- **Manual payment verification:** Upload payment screenshots

### 2. Myanmar Language Support
- **Unicode Myanmar font support**
- **Myanmar number formatting**
- **Local currency display (MMK)**
- **Cultural appropriate UI elements**

### 3. Local Server Infrastructure
- **Singapore servers** (closest to Myanmar)
- **Thailand servers** for regional content
- **India servers** for additional options
- **Auto-select best server** based on location

## Security & Privacy Implementation

### 1. Data Protection
- **No-logs policy implementation**
- **Secure token storage** using Android Keystore
- **Certificate pinning** for API communications
- **Data encryption** for sensitive information

### 2. VPN Security
- **Strong encryption** (AES-256)
- **Perfect Forward Secrecy**
- **Regular key rotation**
- **Secure DNS resolution**

## Performance Optimization

### 1. App Performance
- **Lazy loading** for server lists
- **Background threading** for network operations
- **Memory optimization** for smooth UI
- **Battery optimization** for VPN service

### 2. Network Optimization
- **Connection pooling** for API calls
- **Caching strategies** for server data
- **Retry mechanisms** with exponential backoff
- **Network quality detection**

## Analytics & Tracking

### 1. User Analytics
- **Connection success rates**
- **Server performance metrics**
- **User engagement tracking**
- **Revenue tracking**

### 2. Crash Reporting
- **Firebase Crashlytics integration**
- **Custom error reporting**
- **Performance monitoring**
- **User feedback collection**

## Deployment & Distribution

### 1. Google Play Store
- **Optimized app listing**
- **Screenshots and videos**
- **ASO (App Store Optimization)**
- **Staged rollout strategy**

### 2. Alternative Distribution
- **APK download from website**
- **QR code distribution**
- **Referral program integration**

## Testing Requirements

### 1. Manual Testing
- **VPN connection on different networks**
- **Payment flow testing**
- **Ad integration testing**
- **Performance testing on various devices**

### 2. Automated Testing
- **Unit tests for core functionality**
- **UI tests for main flows**
- **Integration tests for API calls**
- **Security testing**

## Implementation Priority

### Phase 1 (Core VPN)
1. Basic VPN connection functionality
2. Server list and selection
3. User authentication
4. Basic UI with connection status

### Phase 2 (Monetization)
1. Ad integration (banner, interstitial, rewarded)
2. Subscription plans and payment
3. Points and rewards system
4. Referral program

### Phase 3 (Advanced Features)
1. Advanced VPN protocols
2. Security enhancements
3. Performance optimizations
4. Analytics integration

### Phase 4 (Polish & Launch)
1. UI/UX improvements
2. Bug fixes and optimization
3. Store preparation
4. Marketing features

## Special Considerations for Myanmar Market

1. **Stable internet connectivity handling**
2. **Government censorship bypass capabilities**
3. **Local payment method integration**
4. **Regional server optimization**
5. **Offline functionality where possible**
6. **Low data usage modes**
7. **Support for older Android devices**

This comprehensive prompt should help Krio IDE AI create a complete, production-ready VPN application that integrates with your existing backend infrastructure and includes all necessary monetization and advanced features for the Myanmar market.