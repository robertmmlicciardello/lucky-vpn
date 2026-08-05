
# Security Setup Guide for Lucky VPN Master

This guide explains how to configure the security features implemented in the Lucky VPN Master project.

## 🔒 Backend Security Configuration

### 1. Environment Variables Setup

**Step 1:** Copy the example environment file
```bash
cp backend/.env.example backend/.env
```

**Step 2:** Edit the `.env` file with your actual values:

#### Critical Security Keys (MUST CHANGE):
```env
# Use a strong, unique JWT secret (minimum 32 characters)
JWT_SECRET=your-super-secure-jwt-secret-key-at-least-32-characters-long

# Separate JWT secret for admin (different from user JWT)
JWT_ADMIN_SECRET=your-admin-jwt-secret-different-from-user-jwt

# Game validation secret (prevents cheating)
GAME_SECRET=your-game-validation-secret-key

# Payment validation secret
PAYMENT_SECRET=your-payment-validation-secret-key
```

#### Database Configuration:
```env
DB_HOST=your_database_host
DB_USER=your_database_username
DB_PASSWORD=your_secure_database_password
DB_NAME=lucky_vpn_master
```

#### Payment Gateway Configuration:
```env
# Configure only the payment gateways you will use
KPAY_MERCHANT_ID=your_kpay_merchant_id
KPAY_API_KEY=your_kpay_api_key
KPAY_SECRET_KEY=your_kpay_secret_key
```

### 2. Security Features Implemented

#### ✅ Authentication & Authorization
- JWT tokens with secure secret keys
- Separate admin authentication system
- Token expiration and validation
- Rate limiting on authentication endpoints (5 attempts per 15 minutes)

#### ✅ Input Validation
- Server-side validation for all user inputs
- SQL injection prevention
- XSS protection
- Email and password validation with strength requirements

#### ✅ Payment Security
- Server-side payment validation
- Transaction ID format validation
- Duplicate payment prevention
- Payment amount validation against plan types
- Secure payment hash generation

#### ✅ Game Anti-Cheat System
- Server-side game session validation
- Cryptographic game result verification
- Time-based session expiration
- Lucky wheel and scratch card result validation

#### ✅ Rate Limiting
- General API: 100 requests per 15 minutes
- Authentication: 5 attempts per 15 minutes
- Payment submissions: 10 per hour
- Admin actions: Enhanced monitoring

#### ✅ Security Headers
- Helmet.js for security headers
- CORS configuration
- XSS protection
- Content type validation
- Frame options protection

## 📱 Android App Security Configuration

### 1. Local Properties Setup

**Step 1:** Copy the example file
```bash
cp android/local.properties.example android/local.properties
```

**Step 2:** Configure your Ad Network IDs and API keys in `local.properties`:

```properties
# AdMob Configuration
ADMOB_APP_ID="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"
ADMOB_BANNER_ID="ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
ADMOB_INTERSTITIAL_ID="ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
ADMOB_REWARDED_ID="ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"

# API Configuration
API_BASE_URL="https://your-api-domain.com/api/v1"
```

### 2. Firebase Configuration

**Step 1:** Download your `google-services.json` from Firebase Console

**Step 2:** Place it in the `android/app/` directory

**⚠️ IMPORTANT:** Never commit `google-services.json` to version control

### 3. Code Obfuscation

The project includes ProGuard rules in `android/proguard-rules.pro` for:
- Code obfuscation to prevent reverse engineering
- Removal of debug logs in release builds
- Protection of sensitive classes and methods
- Ad network compatibility

### 4. Secure Data Storage

The Android app implements:
- EncryptedSharedPreferences for sensitive data
- Secure token storage
- Network security configuration
- Certificate pinning preparation

## 🛡️ Additional Security Recommendations

### 1. Database Security
- Use strong database passwords
- Enable database SSL connections
- Restrict database access to application servers only
- Regular database backups with encryption

### 2. Server Security
- Use HTTPS/SSL certificates
- Keep server OS and dependencies updated
- Configure firewall rules
- Enable fail2ban for SSH protection

### 3. Admin Panel Security
- Implement Two-Factor Authentication (2FA)
- Use strong admin passwords
- Monitor admin activities
- Restrict admin access by IP if possible

### 4. Production Deployment
- Set `NODE_ENV=production`
- Use a reverse proxy (Nginx)
- Enable server monitoring and logging
- Configure automated backups

## ⚠️ Critical Security Checklist

Before deploying to production:

- [ ] Changed all default JWT secrets
- [ ] Configured strong database passwords
- [ ] Set up HTTPS/SSL certificates
- [ ] Configured payment gateway credentials
- [ ] Set up proper logging and monitoring
- [ ] Enabled rate limiting
- [ ] Configured CORS for your domain only
- [ ] Set up database backups
- [ ] Configured Firebase for push notifications
- [ ] Added `google-services.json` to `.gitignore`
- [ ] Never commit `.env` files to version control
- [ ] Set up admin 2FA (recommended)

## 🔧 Files That Must Be Added to .gitignore

Add these lines to your `.gitignore` file:

```
# Environment files
backend/.env
backend/.env.local
backend/.env.production

# Android sensitive files
android/local.properties
android/app/google-services.json
android/app/*.jks
android/app/*.keystore

# Logs
backend/logs/
*.log

# Database
*.db
*.sqlite
```

## 📞 Support

If you need help with security configuration:
1. Check this documentation first
2. Review the example configuration files
3. Test in development environment before production
4. Keep all secrets secure and never share them publicly

## 🔄 Regular Security Maintenance

- Update dependencies monthly
- Rotate JWT secrets every 3-6 months
- Monitor logs for suspicious activities
- Review admin access logs regularly
- Update payment gateway configurations as needed
