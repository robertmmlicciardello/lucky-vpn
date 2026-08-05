# Android Studio နဲ့ APK / AAB Build လုပ်နည်း (Monetize VPN)

## 0. လိုအပ်ချက်
- Android Studio (Hedgehog 2023.1+)
- JDK 17 (Android Studio ထဲ built-in ရှိသည်)
- Android SDK 34 + Build Tools 34
- Internet (Gradle dependency download အတွက်)

---

## 1. Project ဖွင့်ခြင်း
1. Project ကို GitHub ကနေ clone / download လုပ်ပါ။
2. Android Studio → **File → Open** → `android/` folder ကို ရွေးပါ
   (root folder မဟုတ်၊ **`android` folder** ကိုသာ ရွေးရမည်)။
3. "Gradle sync" အလိုအလျောက် စမည် — မစရင် ⚡ **Sync Project with Gradle Files** ကို နှိပ်ပါ။
4. Gradle wrapper jar မရှိပါက Terminal မှာ:
   ```bash
   cd android
   gradle wrapper --gradle-version 8.6      # gradle install ထားရင်
   ```
   သို့မဟုတ် Android Studio က အလိုအလျောက် wrapper ကို ဖန်တီးပေးမည်။

---

## 2. Config ထည့်ခြင်း
`android/local.properties` ဖိုင်ကို ဖန်တီးပါ (`local.properties.example` ကို ကူးပါ):

```properties
sdk.dir=/Users/you/Library/Android/sdk        # Windows: C:\\Users\\you\\AppData\\Local\\Android\\Sdk
API_BASE_URL=https://api.yourdomain.com/api/v1
ADMOB_APP_ID=ca-app-pub-xxxxxxxx~xxxxxxxx
ADMOB_BANNER_ID=ca-app-pub-xxxxxxxx/xxxxxxxx
ADMOB_INTERSTITIAL_ID=ca-app-pub-xxxxxxxx/xxxxxxxx
ADMOB_REWARDED_ID=ca-app-pub-xxxxxxxx/xxxxxxxx
```
> `local.properties` ကို git ထဲ **မတင်ရ**။

---

## 3. Debug APK (test လုပ်ရန်)
- Menu: **Build → Build Bundle(s)/APK(s) → Build APK(s)**
- ဒါမှမဟုတ် Terminal:
  ```bash
  cd android
  ./gradlew assembleDebug
  ```
- ရလာမည့်နေရာ: `android/app/build/outputs/apk/debug/app-debug.apk`
- ဖုန်းချိတ်ပြီး ▶️ **Run** နှိပ်လျှင် တိုက်ရိုက် install ဖြစ်သည်။

---

## 4. Keystore ဖန်တီးခြင်း (release အတွက် တစ်ခါတည်း)
**Build → Generate Signed Bundle / APK → APK → Create new…**
သို့မဟုတ် terminal:
```bash
keytool -genkey -v -keystore monetizevpn.jks -keyalg RSA \
  -keysize 2048 -validity 10000 -alias monetizevpn
```
> `.jks` ဖိုင်နဲ့ password ကို ဘယ်တော့မှ မပျောက်ပါစေနှင့် — ပျောက်ရင် Play Store update မတင်နိုင်ပါ။

`android/keystore.properties` (git ထဲ မတင်ရ):
```properties
storeFile=../monetizevpn.jks
storePassword=xxxx
keyAlias=monetizevpn
keyPassword=xxxx
```

`app/build.gradle` ထဲ signingConfig ထည့်ရန်:
```gradle
def ksFile = rootProject.file("keystore.properties")
def ks = new Properties()
if (ksFile.exists()) ks.load(new FileInputStream(ksFile))

android {
    signingConfigs {
        release {
            if (ksFile.exists()) {
                storeFile file(ks['storeFile'])
                storePassword ks['storePassword']
                keyAlias ks['keyAlias']
                keyPassword ks['keyPassword']
            }
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

---

## 5. Release APK / AAB ထုတ်ခြင်း
GUI: **Build → Generate Signed Bundle / APK**
- Play Store တင်မယ်ဆိုရင် → **Android App Bundle (AAB)**
- ဖုန်းမှာ တိုက်ရိုက် ဖြန့်မယ်ဆိုရင် → **APK**
- Build variant: **release** → Finish

Terminal:
```bash
./gradlew assembleRelease     # APK
./gradlew bundleRelease       # AAB (Play Store)
```
ရလာမည့်နေရာ:
- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`

---

## 6. Build မအောင်မြင်ရင် အဖြေများ
| Error | ဖြေရှင်းချက် |
|---|---|
| `SDK location not found` | `local.properties` ထဲ `sdk.dir` ထည့်ပါ |
| `Could not find gradle wrapper` | `gradle wrapper --gradle-version 8.6` run ပါ |
| `Unsupported class file major version` | JDK 17 သုံးပါ (Settings → Gradle → Gradle JDK 17) |
| `Duplicate class` | dependency version တွေ တူညီစေပါ |
| Dependency download fail | VPN/Proxy ပိတ်ပြီး **File → Invalidate Caches / Restart** |
| Manifest merger failed | `AndroidManifest.xml` ထဲ activity name/package စစ်ပါ |

Clean build:
```bash
./gradlew clean
./gradlew --stop
./gradlew assembleRelease --stacktrace
```

---

## 7. APK ရပြီးရင် စစ်ရမည့်အချက်များ
- [ ] Register / Login (backend API ချိတ်မိလား)
- [ ] Server list တင်လာ + VPN connect ဖြစ်
- [ ] Banner / Interstitial / Rewarded ad ပြ
- [ ] Daily check-in points တိုး
- [ ] Payment screenshot upload → admin panel မှာ ပေါ်
- [ ] Language ၇ မျိုး ပြောင်းလို့ရ
- [ ] Airplane mode မှာ crash မဖြစ်
