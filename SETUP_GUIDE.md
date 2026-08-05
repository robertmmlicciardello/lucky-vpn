
# Monetize VPN - Complete Setup Guide for Codecanyon Buyers

## 🎯 Welcome Codecanyon Buyer!

Thank you for purchasing Monetize VPN! This guide will help you set up and deploy your VPN application quickly and efficiently.

## 📦 What's Included

```
monetize-vpn/
├── backend/                 # Node.js API server
├── android/                # Android VPN app
├── src/                    # Admin panel (React)
├── docs/                   # Documentation
├── SETUP_GUIDE.md          # This file
└── README.md               # Technical documentation
```

## ⚡ Quick Start (5 Minutes)

### Step 1: Extract Files
```bash
unzip monetize-vpn.zip
cd monetize-vpn
```

### Step 2: Backend Setup
```bash
cd backend
npm install
cp .env.example .env
# Edit .env with your database settings
npm start
```

### Step 3: Android Setup
```bash
cd android
cp local.properties.example local.properties
# Edit local.properties with your ad IDs
./gradlew build
```

### Step 4: Access Admin Panel
- Open: `http://localhost:3000/admin`
- Login: `admin@monetizevpn.com` / `admin123`
- **Change password immediately!**

## 🔧 Detailed Configuration

### Database Setup

#### Option 1: Local MySQL
```bash
# Install MySQL
sudo apt-get install mysql-server

# Create database
mysql -u root -p
CREATE DATABASE monetize_vpn;
exit
```

#### Option 2: Remote Database (Recommended)
Use services like:
- Amazon RDS
- Google Cloud SQL
- DigitalOcean Managed Databases

Update `.env`:
```env
DB_HOST=your-remote-host
DB_NAME=monetize_vpn
DB_USER=your-username
DB_PASSWORD=your-password
```

### Ad Networks Configuration

#### 1. Google AdMob (Highest Revenue)
1. Create account: [admob.google.com](https://admob.google.com)
2. Add your app
3. Create ad units:
   - Banner Ad
   - Interstitial Ad
   - Rewarded Video Ad
4. Copy IDs to `local.properties`:
```properties
ADMOB_APP_ID="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"
ADMOB_BANNER_ID="ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
ADMOB_INTERSTITIAL_ID="ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
ADMOB_REWARDED_ID="ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
```

#### 2. Facebook Audience Network
1. Create account: [developers.facebook.com](https://developers.facebook.com)
2. Add your app
3. Create placement IDs
4. Update `local.properties`:
```properties
FACEBOOK_APP_ID="XXXXXXXXXXXXXXXX"
```

#### 3. Unity Ads
1. Create account: [dashboard.unity3d.com](https://dashboard.unity3d.com)
2. Create project
3. Get Game ID
4. Update `local.properties`:
```properties
UNITY_GAME_ID="XXXXXXX"
```

### Payment Gateway Setup

#### For Myanmar Market:
1. **KPay Integration:**
```env
KPAY_MERCHANT_ID=your_merchant_id
KPAY_API_KEY=your_api_key
```

2. **Wave Money:**
```env
WAVE_MERCHANT_ID=your_wave_id
```

#### For International Markets:
- Stripe (credit cards)
- PayPal
- Local payment methods

### Firebase Setup (Push Notifications)
1. Create project: [console.firebase.google.com](https://console.firebase.google.com)
2. Add Android app
3. Download `google-services.json`
4. Place in `android/app/` directory
5. Enable Cloud Messaging

## 🎨 Customization Guide

### Branding Your App

#### 1. Change App Name
```xml
<!-- android/app/src/main/res/values/strings.xml -->
<string name="app_name">Your VPN Brand</string>
```

#### 2. Update Package Name
```gradle
// android/app/build.gradle
android {
    namespace 'com.yourcompany.yourvpn'
    defaultConfig {
        applicationId "com.yourcompany.yourvpn"
    }
}
```

#### 3. Replace App Icon
- Use Android Studio's Image Asset Studio
- Replace icons in `android/app/src/main/res/mipmap-*/`

#### 4. Customize Colors
```xml
<!-- android/app/src/main/res/values/colors.xml -->
<color name="primary">#FF6B35</color>
<color name="accent">#2E8B57</color>
<color name="background">#FFFFFF</color>
```

### Admin Panel Customization
```tsx
// src/pages/Landing.tsx
const appName = "Your VPN Brand";
const companyName = "Your Company";
```

## 🚀 Production Deployment

### Backend Deployment

#### Option 1: VPS (DigitalOcean, Linode, etc.)
```bash
# Server setup
sudo apt update
sudo apt install nodejs npm mysql-server nginx

# Deploy app
git clone your-repo
cd backend
npm install
npm run build

# Use PM2 for process management
npm install -g pm2
pm2 start npm --name "vpn-backend" -- start
pm2 startup
pm2 save
```

#### Option 2: Cloud Platforms
- Heroku (easy deployment)
- AWS EC2 (scalable)
- Google Cloud Platform
- DigitalOcean App Platform

### SSL Certificate Setup
```bash
# Using Let's Encrypt
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com
```

### Nginx Configuration
```nginx
server {
    listen 443 ssl;
    server_name yourdomain.com;
    
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    
    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Android App Release

#### 1. Generate Keystore
```bash
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias
```

#### 2. Configure Signing
```gradle
// android/app/build.gradle
android {
    signingConfigs {
        release {
            storeFile file('my-release-key.jks')
            storePassword 'your-store-password'
            keyAlias 'my-key-alias'
            keyPassword 'your-key-password'
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

#### 3. Build Release APK
```bash
./gradlew assembleRelease
```

#### 4. Upload to Play Store
- Create developer account
- Complete store listing
- Upload APK/AAB
- Set pricing and distribution

## 💰 Monetization Strategy

### Ad Revenue Optimization
1. **Implement Mediation:**
   - Use AdMob mediation
   - Add multiple ad networks
   - Optimize eCPM rates

2. **Strategic Ad Placement:**
   - Interstitial ads after connection
   - Rewarded videos for premium servers
   - Banner ads in server list

3. **User Experience Balance:**
   - Limit ad frequency
   - Provide ad-free premium option
   - Reward users for watching ads

### Subscription Revenue
1. **Pricing Strategy:**
   - Free tier with ads
   - Premium monthly ($4.99)
   - Premium yearly ($39.99)

2. **Premium Features:**
   - Ad-free experience
   - Faster servers
   - Unlimited bandwidth
   - Priority support

## 🔧 Troubleshooting

### Common Issues

#### 1. Backend Won't Start
```bash
# Check Node.js version
node --version  # Should be 16+

# Check database connection
mysql -u root -p
```

#### 2. Android Build Errors
```bash
# Clean and rebuild
./gradlew clean
./gradlew build

# Check SDK configuration
echo $ANDROID_HOME
```

#### 3. AdMob Not Working
- Verify App ID in `strings.xml`
- Check Google Play Console setup
- Ensure test device is added

### Getting Support
1. Check logs first:
   - Backend: `backend/logs/`
   - Android: Android Studio Logcat
2. Review configuration files
3. Test with example values
4. Contact support with specific error messages

## 📈 Scaling Your Business

### Marketing Tips
1. **App Store Optimization (ASO):**
   - Use relevant keywords
   - Attractive screenshots
   - Positive reviews

2. **User Acquisition:**
   - Social media marketing
   - Influencer partnerships
   - Referral programs

3. **Retention Strategies:**
   - Push notifications
   - Loyalty rewards
   - Regular feature updates

### Technical Scaling
1. **Performance Monitoring:**
   - Server monitoring
   - App crash reporting
   - User analytics

2. **Infrastructure:**
   - Load balancers
   - CDN for static assets
   - Database optimization

## 🎯 Success Checklist

### Pre-Launch
- [ ] All ads networks configured
- [ ] Payment gateways tested
- [ ] App store listing ready
- [ ] Backend deployed with SSL
- [ ] Database backed up
- [ ] Admin panel secured

### Post-Launch
- [ ] Monitor app performance
- [ ] Track revenue metrics
- [ ] Respond to user feedback
- [ ] Regular security updates
- [ ] Marketing campaigns active

## 🔄 Maintenance & Updates

### Regular Tasks
- **Weekly:** Check server performance
- **Monthly:** Update dependencies
- **Quarterly:** Review ad performance
- **Yearly:** Renew SSL certificates

### Keeping Updated
- Monitor for security patches
- Update ad network SDKs
- Add new payment methods
- Improve user experience

---

## 🎉 Congratulations!

You now have a complete VPN solution ready for commercial use. Focus on providing great user experience and the revenue will follow!

**Need help?** Check the documentation or contact support with specific questions.

**Good luck with your VPN business!** 🚀
