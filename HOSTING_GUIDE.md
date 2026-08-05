# 🚀 Complete Hosting Setup Guide - VPN App Deployment

## 📋 ကြိုတင်ပြင်ဆင်ရမည့်အရာများ

### လိုအပ်သော hosting requirements:
- **cPanel/aaPanel hosting** (Node.js support ပါရှိရမည်)
- **MySQL Database** access
- **SSL Certificate** (Let's Encrypt free SSL)
- **Domain/Subdomain** for admin panel
- **File Manager** access

---

## 🔧 Phase 1: Backend Setup on Hosting

### Step 1: Upload Backend Files
```bash
# Local မှာ backend folder ကို zip လုပ်ပါ
zip -r backend.zip backend/

# cPanel File Manager မှာ public_html/api folder ဖန်တီးပြီး upload လုပ်ပါ
# သို့မဟုတ် FTP သုံးပြီး upload လုပ်ပါ
```

### Step 2: Database Setup
```sql
-- cPanel phpMyAdmin မှာ database အသစ်ဖန်တီးပါ
CREATE DATABASE your_hosting_database_name;

-- backend/database/setup.sql file ကို run လုပ်ပါ
-- Import SQL file မှ phpMyAdmin မှာ import လုပ်နိုင်ပါတယ်
```

### Step 3: Environment Configuration
```bash
# public_html/api/.env file ဖန်တီးပါ
DB_HOST=localhost
DB_PORT=3306
DB_NAME=your_hosting_database_name
DB_USER=your_database_username
DB_PASSWORD=your_database_password

JWT_SECRET=your-super-secure-jwt-secret-key-change-this
FRONTEND_URL=https://yourdomain.com
PORT=3000
NODE_ENV=production

# Payment Gateway Settings (optional)
KPAY_MERCHANT_ID=your_kpay_id
WAVE_MERCHANT_ID=your_wave_id
AYA_MERCHANT_ID=your_aya_id
```

### Step 4: Install Dependencies
```bash
# cPanel Terminal မှာ (သို့မဟုတ် SSH access ရှိရင်)
cd public_html/api
npm install

# Node.js version check လုပ်ပါ (16+ လိုအပ်တယ်)
node --version
```

### Step 5: Start Backend Service
```bash
# cPanel မှာ Node.js App ဖန်တီးပါ
# Application Root: public_html/api
# Application URL: yourdomain.com/api
# Application Startup File: server.js

# Manual start (SSH access ရှိရင်)
cd public_html/api
npm start

# PM2 သုံးရင် (recommended for production)
npm install -g pm2
pm2 start server.js --name "vpn-backend"
pm2 startup
pm2 save
```

---

## 🌐 Phase 2: Frontend Admin Panel Setup

### Step 1: Build Frontend
```bash
# Local computer မှာ
npm install
npm run build

# dist folder ကို zip လုပ်ပါ
zip -r admin-panel.zip dist/
```

### Step 2: Upload to Hosting
```bash
# cPanel File Manager မှာ
# public_html/admin folder ဖန်တီးပြီး dist folder contents ကို upload လုပ်ပါ
# သို့မဟုတ် public_html မှာတိုက်ရိုက် upload လုပ်လို့ရပါတယ်
```

### Step 3: Configure API Connection
```javascript
// Upload မလုပ်ခင် src/api/client.ts မှာ API URL ကို update လုပ်ပါ
const API_BASE_URL = 'https://yourdomain.com/api/v1';
```

### Step 4: Setup SSL & Domain
```bash
# cPanel SSL/TLS မှာ Let's Encrypt SSL install လုပ်ပါ
# Domain DNS pointing ကို hosting IP သို့ point လုပ်ပါ

# .htaccess file ဖန်တီးပါ (public_html/.htaccess)
RewriteEngine On
RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule . /index.html [L]

# Force HTTPS
RewriteCond %{HTTPS} off
RewriteRule ^(.*)$ https://%{HTTP_HOST}%{REQUEST_URI} [L,R=301]
```

---

## 📱 Phase 3: Android App Build & Release

### Step 1: Configure for Production
```gradle
// android/app/build.gradle မှာ
android {
    defaultConfig {
        applicationId "com.yourdomain.vpnmaster"
        versionCode 1
        versionName "1.0.0"
        
        buildConfigField "String", "API_BASE_URL", "\"https://yourdomain.com/api/v1\""
    }
    
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.release
        }
    }
}
```

### Step 2: Generate Keystore
```bash
# Release keystore ဖန်တီးပါ
keytool -genkey -v -keystore release-key.keystore -alias vpn-master -keyalg RSA -keysize 2048 -validity 10000

# Keystore ကို android/app/ folder မှာထားပါ
```

### Step 3: Configure Signing
```properties
# android/keystore.properties file ဖန်တီးပါ
storePassword=your_keystore_password
keyPassword=your_key_password
keyAlias=vpn-master
storeFile=release-key.keystore
```

```gradle
// android/app/build.gradle မှာ add လုပ်ပါ
def keystorePropertiesFile = rootProject.file("keystore.properties")
def keystoreProperties = new Properties()
keystoreProperties.load(new FileInputStream(keystorePropertiesFile))

android {
    signingConfigs {
        release {
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
        }
    }
}
```

### Step 4: Build Release APK
```bash
cd android
./gradlew assembleRelease

# APK ကို android/app/build/outputs/apk/release/ မှာရရှိမည်
```

### Step 5: Upload to Play Store
1. Google Play Console မှာ account ဖန်တီးပါ
2. App အသစ်ဖန်တီးပါ
3. Release APK ကို upload လုပ်ပါ
4. Store listing information ဖြည့်ပါ
5. Review process ကို ခံပါ

---

## 🔧 Phase 4: Final Configuration & Testing

### Step 1: Update API Endpoints
```bash
# Backend running ဖြစ်မဖြစ် test လုပ်ပါ
curl https://yourdomain.com/api/v1/status

# Expected response:
{
    "success": true,
    "message": "Monetize VPN API is running",
    "version": "1.0.0"
}
```

### Step 2: Admin Panel Access
```bash
# Browser မှာ https://yourdomain.com/admin သွားပါ
# Default login credentials:
# Email: admin@luckyvpn.com
# Password: password

# ⚠️ Password ကို အမြန်ဆုံးပြောင်းပါ!
```

### Step 3: Test Android App
```bash
# APK ကို phone မှာ install လုပ်ပြီး test လုပ်ပါ
# API connection ကောင်းမကောင်း check လုပ်ပါ
# Registration/Login စမ်းကြည့်ပါ
```

---

## 🛠️ Troubleshooting Common Issues

### Backend မ start နိုင်ရင်:
```bash
# Node.js version check
node --version  # 16+ လိုအပ်တယ်

# Dependencies reinstall
rm -rf node_modules package-lock.json
npm install

# Log file check
tail -f logs/error.log
```

### Database connection error ရရင်:
```bash
# Database credentials ကို ပြန်စစ်ပါ
# Hosting မှာ remote database access allow လုပ်ထားရမည်
# Database name prefix ရှိမရှိ check လုပ်ပါ (hosting ပေါ်မူတည်)
```

### Android APK build error ရရင်:
```bash
# Clean build
./gradlew clean
./gradlew assembleRelease

# Java version check (JDK 8 or 11)
java -version
```

### SSL issues ရရင်:
```bash
# Force HTTPS redirect
# Check SSL certificate validity
# DNS propagation check လုပ်ပါ
```

---

## 📝 Post-Launch Checklist

### Security:
- [ ] Default admin password ပြောင်းပြီးပြီလား?
- [ ] SSL certificate active ဖြစ်ပြီးလား?
- [ ] Database credentials secure လား?
- [ ] API rate limiting active လား?

### Functionality:
- [ ] User registration/login working လား?
- [ ] Payment gateway configured လား?
- [ ] VPN servers list loading လား?
- [ ] Push notifications working လား?

### Monitoring:
- [ ] Error logging setup လုပ်ပြီးပြီလား?
- [ ] Database backup schedule ရှိလား?
- [ ] Server monitoring setup လုပ်ပြီးပြီလား?

---

## 🎯 Success! 

ဒီ guide ကို လိုက်လုပ်ပြီးရင် သင့်ရဲ့ VPN app ကို hosting မှာ successfully deploy လုပ်နိုင်ပြီး production ready ဖြစ်သွားပါပြီ!

### 📞 Support:
ပြဿနာရှိရင် hosting provider ရဲ့ technical support နဲ့ဆက်သွယ်နိုင်ပါတယ်။ Error logs ကို အမြဲစစ်ကြည့်ပါ။

**Happy Launching! 🚀**