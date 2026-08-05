
# White-labeling Guide - Monetize VPN

## 🎨 Complete Branding Customization

This guide shows you how to completely rebrand the Monetize VPN app with your own identity.

## 📱 Android App Branding

### 1. Change App Name

#### App Display Name
```xml
<!-- android/app/src/main/res/values/strings.xml -->
<string name="app_name">Your VPN Name</string>
```

#### Different Language Support
```xml
<!-- android/app/src/main/res/values-es/strings.xml -->
<string name="app_name">Tu VPN Nombre</string>

<!-- android/app/src/main/res/values-zh/strings.xml -->
<string name="app_name">你的VPN名称</string>
```

### 2. Change Package Name

#### Update Build Configuration
```gradle
// android/app/build.gradle
android {
    namespace 'com.yourcompany.yourvpn'
    
    defaultConfig {
        applicationId "com.yourcompany.yourvpn"
        minSdkVersion 21
        targetSdkVersion 34
        versionCode 1
        versionName "1.0"
    }
}
```

#### Update Capacitor Configuration
```ts
// capacitor.config.ts
import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.yourcompany.yourvpn',
  appName: 'Your VPN Name',
  webDir: 'dist',
  server: {
    androidScheme: 'https'
  }
};

export default config;
```

### 3. Replace App Icon

#### Method 1: Using Android Studio
1. Open project in Android Studio
2. Right-click `app` folder
3. Select `New` > `Image Asset`
4. Choose `Launcher Icons (Adaptive and Legacy)`
5. Upload your icon (512x512 PNG recommended)
6. Generate all sizes

#### Method 2: Manual Replacement
Replace these files with your icon:
```
android/app/src/main/res/
├── mipmap-hdpi/ic_launcher.png          (72x72)
├── mipmap-mdpi/ic_launcher.png          (48x48)
├── mipmap-xhdpi/ic_launcher.png         (96x96)
├── mipmap-xxhdpi/ic_launcher.png        (144x144)
├── mipmap-xxxhdpi/ic_launcher.png       (192x192)
└── mipmap-anydpi-v26/ic_launcher.xml    (Vector/Adaptive)
```

### 4. Customize App Colors

#### Primary Colors
```xml
<!-- android/app/src/main/res/values/colors.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="primary">#FF6B35</color>
    <color name="primary_dark">#E85A2B</color>
    <color name="accent">#2E8B57</color>
    <color name="background">#FFFFFF</color>
    <color name="surface">#F5F5F5</color>
    <color name="error">#FF5722</color>
    <color name="success">#4CAF50</color>
    <color name="warning">#FF9800</color>
    <color name="text_primary">#212121</color>
    <color name="text_secondary">#757575</color>
    <color name="white">#FFFFFF</color>
    <color name="black">#000000</color>
</resources>
```

#### Dark Mode Colors
```xml
<!-- android/app/src/main/res/values-night/colors.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="primary">#FF6B35</color>
    <color name="primary_dark">#E85A2B</color>
    <color name="accent">#2E8B57</color>
    <color name="background">#121212</color>
    <color name="surface">#1E1E1E</color>
    <color name="text_primary">#FFFFFF</color>
    <color name="text_secondary">#AAAAAA</color>
</resources>
```

### 5. Update Splash Screen

#### Create Splash Screen
```xml
<!-- android/app/src/main/res/layout/activity_splash.xml -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:background="@color/primary">

    <ImageView
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:src="@drawable/ic_launcher"
        android:layout_marginBottom="24dp" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/app_name"
        android:textSize="28sp"
        android:textStyle="bold"
        android:textColor="@color/white" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Secure • Fast • Reliable"
        android:textSize="16sp"
        android:textColor="@color/white"
        android:alpha="0.8"
        android:layout_marginTop="8dp" />

</LinearLayout>
```

## 🌐 Admin Panel Branding

### 1. Update Landing Page

#### Company Information
```tsx
// src/pages/Landing.tsx
const COMPANY_INFO = {
  name: "Your Company Name",
  appName: "Your VPN Brand",
  tagline: "Secure, Fast, Reliable VPN Service",
  description: "Experience the next generation of VPN technology with our advanced security features and global server network.",
  website: "https://yourcompany.com",
  support: "support@yourcompany.com"
};
```

### 2. Update Admin Panel

#### Branding Configuration
```tsx
// src/pages/AdminPanel.tsx
const ADMIN_CONFIG = {
  title: "Your VPN Admin Panel",
  companyName: "Your Company",
  logo: "/your-logo.png",
  favicon: "/your-favicon.ico"
};
```

### 3. Update Colors & Theme

#### CSS Variables
```css
/* src/index.css */
:root {
  /* Primary Brand Colors */
  --primary: #FF6B35;
  --primary-dark: #E85A2B;
  --accent: #2E8B57;
  
  /* Background Colors */
  --background: #FFFFFF;
  --surface: #F8F9FA;
  --card: #FFFFFF;
  
  /* Text Colors */
  --text-primary: #1A1A1A;
  --text-secondary: #6B7280;
  --text-muted: #9CA3AF;
  
  /* Status Colors */
  --success: #10B981;
  --warning: #F59E0B;
  --error: #EF4444;
  --info: #3B82F6;
}

/* Dark Mode */
@media (prefers-color-scheme: dark) {
  :root {
    --background: #0F172A;
    --surface: #1E293B;
    --card: #334155;
    --text-primary: #F1F5F9;
    --text-secondary: #CBD5E1;
    --text-muted: #94A3B8;
  }
}
```

## 🔧 Backend Branding

### 1. Update Server Configuration

#### Company Information
```js
// backend/config/company.js
module.exports = {
  name: 'Your Company Name',
  app: {
    name: 'Your VPN Brand',
    version: '1.0.0',
    description: 'Secure VPN Service'
  },
  contact: {
    email: 'support@yourcompany.com',
    website: 'https://yourcompany.com',
    phone: '+1-555-0123'
  },
  branding: {
    primaryColor: '#FF6B35',
    secondaryColor: '#2E8B57',
    logo: 'https://yourcompany.com/logo.png'
  }
};
```

### 2. Update Email Templates

#### Email Branding
```html
<!-- backend/templates/email-template.html -->
<!DOCTYPE html>
<html>
<head>
    <style>
        .header { background-color: #FF6B35; color: white; padding: 20px; }
        .logo { height: 40px; }
        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; }
    </style>
</head>
<body>
    <div class="header">
        <img src="https://yourcompany.com/logo-white.png" alt="Your VPN Brand" class="logo">
        <h1>Your VPN Brand</h1>
    </div>
    
    <div class="content">
        {{content}}
    </div>
    
    <div class="footer">
        <p>&copy; 2024 Your Company Name. All rights reserved.</p>
        <p>Email: support@yourcompany.com | Website: yourcompany.com</p>
    </div>
</body>
</html>
```

## 📄 Legal & Compliance

### 1. Update Privacy Policy

#### Create Privacy Policy
```markdown
# Privacy Policy - Your VPN Brand

Last updated: [Date]

## Information We Collect
- Account information (email, username)
- Usage statistics (connection logs, bandwidth)
- Device information (OS version, device type)

## How We Use Information
- Provide VPN services
- Improve service quality
- Customer support
- Security monitoring

## Data Retention
- Account data: Until account deletion
- Connection logs: 30 days maximum
- Usage statistics: 90 days

## Contact Information
Email: privacy@yourcompany.com
Address: Your Company Address
```

### 2. Update Terms of Service

#### Create Terms of Service
```markdown
# Terms of Service - Your VPN Brand

## Acceptance of Terms
By using Your VPN Brand, you agree to these terms.

## Service Description
Your VPN Brand provides virtual private network services.

## Acceptable Use
You agree not to use our service for:
- Illegal activities
- Spam or malware distribution
- Copyright infringement

## Limitation of Liability
Service is provided "as is" without warranties.

## Contact
support@yourcompany.com
```

## 🚀 Deployment Checklist

### Pre-Deployment
- [ ] App name changed everywhere
- [ ] Package name updated
- [ ] App icon replaced
- [ ] Colors customized
- [ ] Company information updated
- [ ] Legal documents prepared
- [ ] Email templates updated

### Testing
- [ ] Build Android app successfully
- [ ] Test all app functions
- [ ] Verify branding consistency
- [ ] Check admin panel
- [ ] Test payment flows
- [ ] Verify email templates

### Store Preparation
- [ ] App store screenshots
- [ ] App description written
- [ ] Keywords researched
- [ ] Privacy policy hosted
- [ ] Terms of service hosted
- [ ] Support email set up

## 🎯 Branding Best Practices

### 1. Consistency
- Use same colors across all platforms
- Maintain consistent typography
- Use consistent messaging
- Align visual elements

### 2. Professional Appearance
- High-quality app icon (vector format)
- Consistent color scheme
- Clean, modern UI
- Professional email templates

### 3. Legal Compliance
- Privacy policy must be accessible
- Terms of service must be clear
- Contact information must be valid
- App store guidelines followed

### 4. User Experience
- Intuitive navigation
- Clear call-to-actions
- Consistent branding
- Professional support

## 🔄 Ongoing Maintenance

### Regular Updates
- Update copyright years
- Refresh marketing materials
- Update contact information
- Maintain brand consistency

### Brand Evolution
- Gather user feedback
- Monitor brand perception
- Update visuals periodically
- Maintain professional image

---

## 📞 Support

Need help with white-labeling? Contact our support team with:
- Specific branding requirements
- Custom color schemes
- Logo placement questions
- Legal compliance help

**Your branded VPN app is ready for success!** 🚀
