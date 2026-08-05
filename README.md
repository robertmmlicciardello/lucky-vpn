
# Monetize VPN - Complete VPN Solution with Monetization

A comprehensive VPN application with built-in monetization features, admin panel, and Android app ready for commercial use.

## 🚀 What You Get

- **Complete Admin Panel** - User management, server control, payment approval
- **Android VPN App** - Native Android app with multiple ad networks
- **Backend API** - Robust Node.js backend with security features
- **Monetization Ready** - Multiple ad networks and payment gateways
- **Production Ready** - Optimized for commercial deployment

## 📋 System Requirements

### Backend Requirements
- Node.js 16+ 
- MySQL 5.7+ or 8.0+
- 1GB+ RAM recommended
- SSL certificate for production

### Android Development
- Android Studio 4.2+
- JDK 11+
- Android SDK 28+
- Gradle 7.0+

## 🔧 Quick Setup Guide

### Step 1: Backend Setup

1. **Navigate to backend directory:**
```bash
cd backend
```

2. **Install dependencies:**
```bash
npm install
```

3. **Configure environment:**
```bash
cp .env.example .env
```

4. **Edit .env file with your settings:**
```env
# Database Configuration
DB_HOST=localhost
DB_NAME=monetize_vpn
DB_USER=your_username
DB_PASSWORD=your_password

# JWT Secret (Change this!)
JWT_SECRET=your-super-secure-jwt-secret-at-least-32-characters

# Payment Gateway APIs
KPAY_MERCHANT_ID=your_kpay_id
WAVE_MERCHANT_ID=your_wave_id
```

5. **Setup database:**
```bash
# Create database
mysql -u root -p -e "CREATE DATABASE monetize_vpn;"

# Run migrations (tables will be created automatically on first run)
npm start
```

6. **Start the server:**
```bash
# Development
npm run dev

# Production
npm start
```

### Step 2: Android App Setup

1. **Configure local.properties:**
```bash
cd android
cp local.properties.example local.properties
```

2. **Edit local.properties:**
```properties
# SDK Path
sdk.dir=/path/to/Android/Sdk

# AdMob Configuration
ADMOB_APP_ID="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"
ADMOB_BANNER_ID="ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
ADMOB_INTERSTITIAL_ID="ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
ADMOB_REWARDED_ID="ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"

# API Configuration
API_BASE_URL="https://your-domain.com/api/v1"
```

3. **Add Firebase Configuration:**
- Download `google-services.json` from Firebase Console
- Place it in `android/app/` directory

4. **Build the app:**
```bash
./gradlew build
```

5. **Run on device/emulator:**
```bash
./gradlew installDebug
```

## 💰 Monetization Configuration

### Ad Networks Setup

#### 1. AdMob (Google Ads)
1. Create account at [AdMob](https://admob.google.com)
2. Create app and ad units
3. Update `local.properties` with your AdMob IDs
4. Update `android/app/src/main/res/values/strings.xml`:
```xml
<string name="admob_app_id">ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX</string>
```

#### 2. Facebook Audience Network
1. Setup at [Facebook for Developers](https://developers.facebook.com)
2. Add your app and create placement IDs
3. Update `local.properties`:
```properties
FACEBOOK_APP_ID="XXXXXXXXXXXXXXXX"
```

#### 3. Unity Ads
1. Create project at [Unity Dashboard](https://dashboard.unity3d.com)
2. Get Game ID and Placement IDs
3. Update `local.properties`:
```properties
UNITY_GAME_ID="XXXXXXX"
```

### Payment Gateways

#### Supported Gateways:
- KPay (Myanmar)
- Wave Money (Myanmar)  
- AYA Pay (Myanmar)
- Bank Transfer
- Custom gateways

#### Configuration:
1. Get merchant accounts from payment providers
2. Update backend `.env` file:
```env
KPAY_MERCHANT_ID=your_merchant_id
KPAY_API_KEY=your_api_key
WAVE_MERCHANT_ID=your_wave_id
```

3. Configure in Admin Panel:
- Login to admin panel
- Go to Payment Management
- Add your payment accounts
- Set instructions for users

## 🎨 White-labeling Guide

### Change App Name
1. **Android App Name:**
```xml
<!-- android/app/src/main/res/values/strings.xml -->
<string name="app_name">Your VPN Name</string>
```

2. **Admin Panel Title:**
```tsx
<!-- src/pages/AdminPanel.tsx -->
<title>Your VPN Admin</title>
```

### Change Package Name
1. **Update build.gradle:**
```gradle
// android/app/build.gradle
android {
    namespace 'com.yourcompany.yourvpn'
    defaultConfig {
        applicationId "com.yourcompany.yourvpn"
    }
}
```

2. **Update Capacitor config:**
```ts
// capacitor.config.ts
const config: CapacitorConfig = {
  appId: 'com.yourcompany.yourvpn',
  appName: 'Your VPN Name',
};
```

### Change App Icon & Logo
1. **Android App Icon:**
- Replace files in `android/app/src/main/res/mipmap-*/`
- Use Android Studio's Image Asset Studio for best results

2. **Admin Panel Logo:**
- Replace logo in `src/pages/Landing.tsx`
- Update favicon in `public/favicon.ico`

### Change Colors & Theme
1. **Android Colors:**
```xml
<!-- android/app/src/main/res/values/colors.xml -->
<color name="primary">#YOUR_PRIMARY_COLOR</color>
<color name="accent">#YOUR_ACCENT_COLOR</color>
```

2. **Web Admin Colors:**
```css
/* src/index.css */
:root {
  --primary: YOUR_PRIMARY_COLOR;
  --accent: YOUR_ACCENT_COLOR;
}
```

## 🔐 Security Configuration

### Important Security Steps:

1. **Change Default Passwords:**
```env
# Backend .env
JWT_SECRET=your-unique-secret-key-32-chars-min
DB_PASSWORD=your-secure-database-password
```

2. **Default Admin Login:**
- Email: `admin@monetizevpn.com`
- Password: `admin123`
- ⚠️ **Change this immediately in production!**

3. **SSL Certificate:**
- Required for production
- Configure in your web server (nginx/apache)

## 🗄️ Database Schema

The app automatically creates these tables:
- Users (user accounts)
- Servers (VPN servers)
- Payments (payment submissions)
- Rewards (user rewards/points)
- Notifications (push notifications)
- Support tickets
- Blog posts
- User connections (usage tracking)

## 📱 Features Overview

### Admin Panel Features:
- ✅ User Management
- ✅ VPN Server Management
- ✅ Payment Approval System
- ✅ Ad Network Configuration
- ✅ Rewards System
- ✅ Push Notifications
- ✅ Support Tickets
- ✅ Blog Management
- ✅ Analytics Dashboard

### Android App Features:
- ✅ One-tap VPN Connection
- ✅ Multiple Server Locations
- ✅ User Registration/Login
- ✅ Subscription Management
- ✅ Rewards & Games
- ✅ Multi-language Support
- ✅ Multiple Ad Networks
- ✅ In-app Purchases

## 🚀 Deployment Guide

### Backend Deployment:
1. **VPS/Dedicated Server:**
```bash
# Install Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Install PM2 for process management
npm install -g pm2

# Deploy your app
pm2 start npm --name "vpn-backend" -- start
```

2. **Configure Nginx:**
```nginx
server {
    listen 80;
    server_name yourdomain.com;
    
    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### Android App Deployment:
1. **Generate Signed APK:**
```bash
# Create keystore
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias

# Build release APK
./gradlew assembleRelease
```

2. **Upload to Play Store:**
- Follow Google Play Console guidelines
- Complete store listing
- Upload APK/AAB file

## 📞 Support & Customization

### Getting Help:
1. Check documentation first
2. Review example configurations
3. Test in development environment
4. Contact support for advanced customizations

### Customization Services:
- Custom payment gateway integration
- Additional ad networks
- Custom VPN protocols
- UI/UX modifications
- White-label branding

## 📄 License

This project is licensed for commercial use. You can:
- ✅ Use in commercial projects
- ✅ Sell on app stores
- ✅ Modify and customize
- ✅ White-label for clients

## 🔄 Updates & Maintenance

### Regular Updates Include:
- Security patches
- New ad network integrations
- Performance improvements
- Bug fixes
- New features

### Maintenance Tips:
- Keep dependencies updated
- Monitor server performance
- Regular database backups
- Monitor payment gateways
- Update ad network SDKs

---

## 🎯 Quick Start Checklist

- [ ] Backend setup complete
- [ ] Database configured
- [ ] Android app built successfully
- [ ] Firebase configured
- [ ] Ad networks configured
- [ ] Payment gateways configured
- [ ] Admin panel accessible
- [ ] Default passwords changed
- [ ] SSL certificate installed
- [ ] App tested on device

**Ready to deploy your Monetize VPN solution!** 🚀
