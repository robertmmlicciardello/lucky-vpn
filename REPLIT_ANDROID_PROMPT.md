# Replit မှာ Monetize VPN Android APK ထုတ်ရန် — Prompt + လိုအပ်ချက်များ

> ဒီ file ထဲမှာ (၁) Replit Agent ကို ပေးရမယ့် prompt အပြည့်အစုံ (၂) မင်းလိုအပ်တဲ့ account/key list (၃) APK build လုပ်နည်း ၃ မျိုး ပါဝင်ပါတယ်။

---

## ⚠️ အရင်ဆုံး သိထားရမယ့်အချက် (အရေးကြီး)

Replit ပေါ်မှာ **Android SDK + Gradle build** တိုက်ရိုက် run တာ တော်တော်ခက်ခဲပါတယ် (disk/memory limit ကြောင့်)။
အောင်မြင်ဆုံးလမ်းစဉ်က:

| လမ်းစဉ် | Replit အလုပ် | APK ထွက်တဲ့နေရာ | အဆင်ပြေမှု |
|--------|--------------|-----------------|-----------|
| **A (အကြံပြု)** | Android source code အားလုံး generate | **GitHub Actions** က APK/AAB build | ⭐⭐⭐⭐⭐ |
| B | Source generate + Codemagic/Bitrise ချိတ် | Cloud CI | ⭐⭐⭐⭐ |
| C | Source generate → local မှာ Android Studio နဲ့ build | ကိုယ့်စက် | ⭐⭐⭐ |

ဒါကြောင့် အောက်ပါ prompt မှာ **GitHub Actions workflow ပါ တောင်းထားပါတယ်** — Replit ကနေ push လိုက်ရင် APK အလိုအလျောက်ထွက်လာမယ်။

---

## 📋 Replit Agent ကို ပေးရမယ့် PROMPT (အောက်ကအားလုံး copy လုပ်ပါ)

```
You are a Senior Android Engineer. Build a COMPLETE, production-ready native
Android VPN application called "Monetize VPN" inside this Replit workspace.
Do NOT try to run Gradle/Android SDK here — instead generate the full source
tree plus a GitHub Actions workflow that builds the signed APK/AAB in CI.

=====================================================================
1. PROJECT SPEC
=====================================================================
- App name: Monetize VPN
- Application ID: app.monetizevpn.master
- Language: Java (Android SDK), MVVM architecture
- compileSdk 34, targetSdk 34, minSdk 21
- Gradle 8.x, AGP 8.2+, Kotlin DSL not required (use Groovy .gradle)
- UI: Material 3 + Fragment based Bottom Navigation
- Dark theme first, brand color = deep blue/cyan gradient, Shield logo

=====================================================================
2. BACKEND INTEGRATION (already built - Node.js/Express + MySQL)
=====================================================================
Base URL must be configurable via BuildConfig:
  debug   -> http://10.0.2.2:3000/api/v1
  release -> https://YOUR_DOMAIN.com/api/v1
Auth: JWT Bearer token in `Authorization` header, stored with EncryptedSharedPreferences.

Implement Retrofit interfaces for ALL of these endpoints:

AUTH
  POST /auth/login            {email,password} -> {token, user}
  POST /auth/register         {name,email,password,referralCode?}
  POST /auth/logout
  POST /auth/refresh
  POST /auth/forgot-password
  POST /auth/reset-password

USER
  GET  /user/profile
  PUT  /user/update
  GET  /user/stats
  DELETE /user/delete

SERVERS
  GET  /servers
  GET  /servers/free
  GET  /servers/premium
  GET  /servers/{id}/status
  POST /servers/{id}/connect
  POST /servers/{id}/disconnect

ONECONNECT (primary VPN provider)
  GET  /servers/oneconnect
  GET  /oneconnect/servers/{id}/config
  POST /oneconnect/servers/{id}/connect
  POST /oneconnect/servers/{id}/disconnect

REWARDS / POINTS
  POST /rewards/daily-checkin
  POST /rewards/watch-video
  POST /rewards/refer-friend
  GET  /rewards/points
  POST /rewards/redeem
  GET  /leaderboard

SUBSCRIPTION / PAYMENT (Myanmar manual verification)
  GET  /subscription/plans
  POST /subscription/subscribe
  GET  /payments/accounts        (KPay / Wave / AYA account numbers)
  POST /payments/submit         (multipart: transactionId, amount, method, screenshot)
  GET  /payments/history

ADS
  GET  /ads/config              (remote config: which network + unit ids + frequency)
  POST /ads/view
  POST /ads/click

OTHER
  GET  /notifications
  GET  /blog
  POST /support/tickets
  GET  /support/tickets

All server-side validation is authoritative: NEVER grant points/premium locally,
always confirm from the API response.

=====================================================================
3. FILES / CLASSES TO GENERATE
=====================================================================
app/src/main/java/app/monetizevpn/master/
  MonetizeVpnApp.java              (Application: init ads, language, Timber)
  ui/SplashActivity.java
  ui/LoginActivity.java  ui/RegisterActivity.java  ui/ForgotPasswordActivity.java
  ui/MainActivity.java             (BottomNavigationView + NavHostFragment)
  ui/SubscriptionActivity.java  ui/PaymentActivity.java  ui/SettingsActivity.java
  ui/fragments/HomeFragment.java   (big connect button, timer, speed, data used)
  ui/fragments/ServersFragment.java(RecyclerView + flags, ping, load, favorites, search)
  ui/fragments/RewardsFragment.java(daily check-in calendar, rewarded video, spin wheel, referral)
  ui/fragments/ProfileFragment.java(plan status, payment history, language, logout)
  data/api/ApiService.java, ApiClient.java (Retrofit+OkHttp+Gson, auth interceptor,
        401 -> refresh token, 3x retry with exponential backoff, 30s timeouts)
  data/repo/*Repository.java        (Auth, Server, Rewards, Payment, Ads)
  data/model/User, Server, SubscriptionPlan, Payment, RewardItem, AdConfig, ApiResponse<T>
  data/local/SecurePrefs.java       (EncryptedSharedPreferences)
  vpn/VpnCoreService.java           (extends VpnService)
  vpn/protocol/OpenVpnHandler.java, WireGuardHandler.java, Ikev2Handler.java
  vpn/OneConnectClient.java         (fetch config from backend, start tunnel)
  vpn/VpnStateBus.java              (LiveData connection state machine:
        IDLE/CONNECTING/CONNECTED/RECONNECTING/DISCONNECTING/ERROR)
  ads/AdManager.java                (AdMob primary; Facebook, Unity, AppLovin,
        AdColony, StartApp as waterfall fallbacks; banner/interstitial/rewarded/native;
        all unit IDs and enable flags come from GET /ads/config, cached 6h)
  util/LanguageManager.java         (en, my, zh, es, fr, ar, hi + RTL support)
  util/NetworkMonitor.java, util/Formatter.java

Server model fields: id, name, country, countryCode, city, ip, port, protocol,
  isPremium, load, ping, config, isActive, provider("manual"|"oneconnect"),
  oneConnectId, lastSync.

=====================================================================
4. VPN FEATURE REQUIREMENTS
=====================================================================
- One-tap connect / disconnect with animated shield state
- Protocols: OpenVPN (.ovpn), WireGuard, IKEv2 — auto-select best
- Kill switch, DNS leak protection, IPv6 block, split tunneling (per-app)
- Auto-reconnect with backoff, foreground service + persistent notification
- Battery optimisation: idle detection, partial WakeLock only while tunnel up,
  MTU 1400 on mobile data, no polling loops (use callbacks/WorkManager)
- Free users: free servers only + interstitial before connect
- Premium users: all servers, zero ads (check plan from /user/profile)

=====================================================================
5. LOCALISATION
=====================================================================
res/values/strings.xml plus values-my, values-zh, values-es, values-fr,
values-ar (RTL), values-hi. NO hardcoded strings anywhere in layouts or Java.
Myanmar Unicode font support, MMK currency formatting.

=====================================================================
6. SECURITY / RELEASE CONFIG
=====================================================================
- All keys read from local.properties via buildConfigField; commit only
  local.properties.example (never real keys)
- res/xml/network_security_config.xml: cleartext only for 10.0.2.2 in debug
- backup_rules.xml + data_extraction_rules.xml exclude tokens
- proguard-rules.pro: minify + shrink on release, strip Log.* calls,
  keep model classes, Retrofit, Gson, ad SDKs, VPN core
- signingConfigs.release reads keystore path/passwords from env vars so CI works

=====================================================================
7. GITHUB ACTIONS (must generate)
=====================================================================
.github/workflows/android.yml
  - triggers: push to main + workflow_dispatch
  - JDK 17, setup-android, gradle cache
  - decode base64 secret KEYSTORE_BASE64 to keystore.jks
  - env: KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD, API_BASE_URL, ADMOB_APP_ID
  - runs: ./gradlew assembleRelease bundleRelease
  - uploads app-release.apk and app-release.aab as artifacts
Also generate gradle wrapper files and a RELEASE.md explaining the required
GitHub Secrets and how to download the APK from the Actions tab.

=====================================================================
8. DELIVERABLES CHECKLIST
=====================================================================
[ ] Full compiling source tree (no TODO stubs in critical paths)
[ ] All layouts + drawables + themes (dark/light)
[ ] Retrofit layer covering every endpoint above
[ ] AndroidManifest with VPN/Internet/Network/Wake/Foreground/AD_ID permissions
[ ] .github/workflows/android.yml producing signed APK + AAB
[ ] local.properties.example, README.md, RELEASE.md
Work step by step, print the file tree when finished, and list anything I must
fill in manually.
```

---

## 🔑 မင်းလိုအပ်တဲ့အရာများ (Prompt run မတိုင်မီ/ပြီးနောက်)

### 1. Backend (ဒီ project ထဲမှာရှိပြီးသား)
- VPS သို့မဟုတ် cPanel/aaPanel hosting (Node.js 18+, MySQL 8+)
- Domain + **SSL (HTTPS မဖြစ်မနေလိုသည်)** → `https://api.yourdomain.com/api/v1`
- `.env` ထဲ `JWT_SECRET`, DB credentials, `ONECONNECT_API_KEY`

### 2. VPN Server
- **OneConnect** account + API key (admin panel → OneConnect Settings)
- ဒါမှမဟုတ် ကိုယ်ပိုင် OpenVPN/WireGuard VPS များ

### 3. Monetization Accounts
| လိုအပ်ချက် | ရယူရန် |
|-----------|--------|
| AdMob App ID + Banner/Interstitial/Rewarded unit ID | admob.google.com |
| (optional) Facebook Audience Network / Unity / AppLovin ID | ဆိုင်ရာ dashboard |
| KPay / Wave / AYA account နံပါတ်များ | admin panel → Payment Management |
| Google Play Developer account ($25 တစ်ခါတည်း) | play.google.com/console |

### 4. Signing Keystore (APK လက်မှတ်)
```bash
keytool -genkey -v -keystore monetizevpn.jks -keyalg RSA \
  -keysize 2048 -validity 10000 -alias monetizevpn
base64 -w0 monetizevpn.jks > keystore.b64   # GitHub Secret အတွက်
```
GitHub → Settings → Secrets → Actions မှာ ထည့်ရမည်:
`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `API_BASE_URL`, `ADMOB_APP_ID`

---

## 🚀 အစအဆုံး လုပ်ရမယ့်အစီအစဉ်

1. Backend ကို VPS/hosting ပေါ်တင် → HTTPS API လည်ပတ်ကြောင်း စစ်
2. Admin panel (`npm run build` → `dist/` upload) → admin ဝင်ကြည့်
3. Admin panel မှာ Servers / OneConnect / Ads / Payment accounts / Plans ထည့်
4. Replit workspace အသစ်ဖွင့် → အထက်ပါ prompt ကို Agent ကို ပေး
5. ထွက်လာတဲ့ source ကို GitHub repo ကို push
6. GitHub Secrets ၆ ခု ထည့် → Actions run → **app-release.apk / .aab download**
7. APK ကို ဖုန်းမှာ test (login, server list, connect, ad, payment)
8. AAB ကို Play Console တင် (Privacy Policy + Data Safety form လိုသည်)

---

## 🧪 APK ရပြီးရင် စစ်ရမယ့် Checklist

- [ ] Register / Login / Logout အလုပ်လုပ်
- [ ] Server list backend ကနေ တင်လာ (free/premium ခွဲပြ)
- [ ] VPN connect ဖြစ် + IP ပြောင်း (whatismyip နဲ့စစ်)
- [ ] Kill switch + auto-reconnect
- [ ] Banner / Interstitial / Rewarded ad ပြ + `/ads/view` record
- [ ] Daily check-in + points backend မှာ တိုး
- [ ] Payment screenshot upload → admin panel မှာ ပေါ်
- [ ] Premium ဖြစ်သွားရင် ad မပြ + premium server ရ
- [ ] Language ၇ မျိုး ပြောင်းလို့ရ (Myanmar အထူး)
- [ ] Airplane mode မှာ offline state ပြ (crash မဖြစ်)

---

## ⚠️ Play Store အတွက် သတိထားရန်

- VPN app များ **Privacy Policy URL** မဖြစ်မနေလိုသည်
- `VpnService` အသုံးပြုမှုကို Data Safety form မှာ ရှင်းပြရမည်
- "No-logs" policy ကို website မှာ ရေးထားရမည်
- Rewarded ad နဲ့ premium ပေးတာက policy ချိုးမဖောက်စေရန် "reward = points" အနေနဲ့သာ ဖော်ပြပါ
