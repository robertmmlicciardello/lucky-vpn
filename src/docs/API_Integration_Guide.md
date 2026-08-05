
# VPN Admin Panel - API Integration Guide

This guide explains how to integrate your Android VPN app with the admin panel using the provided API endpoints.

## Overview

The admin panel provides a comprehensive API that allows your Android app to:
- Authenticate users
- Fetch VPN server configurations
- Manage user points and rewards
- Handle subscriptions and payments
- Track user activities and analytics

## Base Configuration

### API Base URL
```
https://your-api-domain.com/api/v1
```

### Authentication
All protected endpoints require Bearer token authentication:
```
Authorization: Bearer {access_token}
```

## Core Integration Steps

### 1. User Authentication

#### Login
```javascript
POST /auth/login
{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJ...",
  "refresh_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJ...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "user@example.com",
    "plan": "premium",
    "points": 1250
  }
}
```

#### Register
```javascript
POST /auth/register
{
  "name": "John Doe",
  "email": "user@example.com",
  "password": "password123",
  "password_confirmation": "password123"
}
```

### 2. Server Management

#### Get Available Servers
```javascript
GET /servers
GET /servers/free      // Free servers only
GET /servers/premium   // Premium servers only

Response:
{
  "success": true,
  "servers": [
    {
      "id": 1,
      "name": "US East",
      "country": "United States",
      "city": "New York",
      "ip": "192.168.1.100",
      "port": "1194",
      "protocol": "OpenVPN",
      "type": "premium",
      "status": "online",
      "load": 45,
      "users": 234,
      "config_file": "base64_encoded_ovpn_config"
    }
  ]
}
```

#### Connect to Server
```javascript
POST /servers/{id}/connect
{
  "device_id": "unique_device_identifier"
}

Response:
{
  "success": true,
  "connection_id": "uuid",
  "server_config": "base64_encoded_ovpn_config",
  "credentials": {
    "username": "generated_username",
    "password": "generated_password"
  }
}
```

### 3. Points & Rewards System

#### Get User Points
```javascript
GET /rewards/points

Response:
{
  "success": true,
  "points": 1250,
  "rank": 15,
  "daily_checkin_status": {
    "checked_in_today": false,
    "streak": 5,
    "next_reward": 20
  }
}
```

#### Daily Check-in
```javascript
POST /rewards/daily-checkin

Response:
{
  "success": true,
  "points_earned": 15,
  "total_points": 1265,
  "streak": 6,
  "next_reward": 25
}
```

#### Watch Rewarded Video
```javascript
POST /rewards/watch-video
{
  "ad_id": "admob_ad_unit_id",
  "duration": 30,
  "completed": true
}

Response:
{
  "success": true,
  "points_earned": 50,
  "total_points": 1315
}
```

### 4. Subscription Management

#### Get Available Plans
```javascript
GET /subscription/plans

Response:
{
  "success": true,
  "plans": [
    {
      "id": 1,
      "name": "1 Month Premium",
      "duration": "1 month",
      "price": 9.99,
      "currency": "USD",
      "features": ["Unlimited bandwidth", "Premium servers", "No ads"],
      "google_play_product_id": "premium_1_month"
    }
  ]
}
```

#### Verify In-App Purchase
```javascript
POST /subscription/verify-purchase
{
  "purchase_token": "google_play_purchase_token",
  "product_id": "premium_1_month",
  "transaction_id": "unique_transaction_id"
}

Response:
{
  "success": true,
  "subscription": {
    "plan_id": 1,
    "expires_at": "2024-07-12T10:30:00Z",
    "auto_renew": true
  }
}
```

### 5. Games & Activities

#### Lucky Wheel Spin
```javascript
POST /games/lucky-wheel/spin
{
  "bet_points": 100
}

Response:
{
  "success": true,
  "result": {
    "prize": "points",
    "amount": 500,
    "sector": 3
  },
  "points_balance": 1650
}
```

#### Tic-Tac-Toe
```javascript
POST /games/tic-tac-toe/start

Response:
{
  "success": true,
  "game_id": "uuid",
  "board": [0,0,0,0,0,0,0,0,0],
  "player_symbol": "X",
  "ai_symbol": "O"
}

POST /games/tic-tac-toe/move
{
  "game_id": "uuid",
  "position": 4
}

Response:
{
  "success": true,
  "board": [0,0,0,0,1,0,0,0,2],
  "game_status": "ongoing|won|lost|draw",
  "points_earned": 30
}
```

### 6. App Configuration

#### Get App Config
```javascript
GET /app/config

Response:
{
  "success": true,
  "config": {
    "app_name": "VPN Master Pro",
    "app_version": "1.2.0",
    "force_update": false,
    "maintenance_mode": false,
    "ad_config": {
      "admob_enabled": true,
      "banner_ad_id": "ca-app-pub-xxx/xxx",
      "interstitial_ad_id": "ca-app-pub-xxx/xxx",
      "rewarded_ad_id": "ca-app-pub-xxx/xxx",
      "ad_frequency": 3,
      "reward_points": 50
    },
    "features": {
      "free_server_limit": 3,
      "auto_connect": true,
      "kill_switch": true,
      "connection_timeout": 30
    },
    "urls": {
      "privacy_policy": "https://vpnmaster.com/privacy",
      "terms_of_service": "https://vpnmaster.com/terms",
      "support": "https://vpnmaster.com/support"
    }
  }
}
```

## Android Implementation Examples

### 1. Retrofit API Service (Java/Kotlin)

```kotlin
interface VPNApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("servers")
    suspend fun getServers(@Header("Authorization") token: String): Response<ServersResponse>
    
    @POST("rewards/daily-checkin")
    suspend fun dailyCheckin(@Header("Authorization") token: String): Response<RewardResponse>
    
    @POST("games/lucky-wheel/spin")
    suspend fun spinWheel(@Header("Authorization") token: String, @Body request: SpinRequest): Response<SpinResponse>
}
```

### 2. Points Management

```kotlin
class PointsManager {
    suspend fun addPoints(reason: String, amount: Int) {
        try {
            val response = apiService.addPoints(
                token = "Bearer ${getAccessToken()}",
                request = AddPointsRequest(reason, amount)
            )
            if (response.isSuccessful) {
                updateLocalPoints(response.body()?.totalPoints ?: 0)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}
```

### 3. Server Connection

```kotlin
class VPNManager {
    suspend fun connectToServer(serverId: Int) {
        try {
            val response = apiService.connectServer(
                token = "Bearer ${getAccessToken()}",
                serverId = serverId,
                request = ConnectRequest(getDeviceId())
            )
            
            if (response.isSuccessful) {
                val config = response.body()?.serverConfig
                // Use OpenVPN library to connect with config
                connectWithOVPN(config)
            }
        } catch (e: Exception) {
            // Handle connection error
        }
    }
}
```

## Error Handling

### Standard Error Response
```javascript
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Email or password is incorrect",
    "details": {}
  }
}
```

### Common Error Codes
- `INVALID_CREDENTIALS` - Login failed
- `INSUFFICIENT_POINTS` - Not enough points for action
- `SERVER_UNAVAILABLE` - Selected server is offline
- `SUBSCRIPTION_EXPIRED` - Premium features require active subscription
- `RATE_LIMIT_EXCEEDED` - Too many requests
- `MAINTENANCE_MODE` - App is under maintenance
- `FORCE_UPDATE_REQUIRED` - App version too old

## Security Considerations

1. **Token Refresh**: Implement automatic token refresh
2. **SSL Pinning**: Pin SSL certificates for API calls
3. **Request Signing**: Sign critical requests
4. **Encryption**: Encrypt sensitive data in storage
5. **Obfuscation**: Obfuscate API endpoints and keys

## Testing

Use the provided admin panel to:
1. Create test users and servers
2. Configure point rewards for testing
3. Monitor API usage and errors
4. Test different app configurations

## Support

For integration support, contact: support@vpnmaster.com
```
