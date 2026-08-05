
# Monetize VPN - Enhanced ProGuard Rules for Production

# Core obfuscation settings
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5

# Keep application class and main components
-keep public class app.lovable.monetizevpn.** { *; }
-keep class app.lovable.monetizevpn.VPNService { *; }
-keep class app.lovable.monetizevpn.models.** { *; }
-keep interface app.lovable.monetizevpn.api.** { *; }

# Protect VPN core logic from reverse engineering
-keep class app.lovable.monetizevpn.vpn.** { *; }
-keep class app.lovable.monetizevpn.security.** { *; }
-keepclassmembers class app.lovable.monetizevpn.VPNService {
    private void runVPN();
    private void processPacket(...);
    private void establishTunnel(...);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Protect ad integration
-keep class app.lovable.monetizevpn.ads.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.facebook.ads.** { *; }
-keep class com.unity3d.ads.** { *; }
-keep class com.applovin.** { *; }
-keep class com.startapp.** { *; }

# Obfuscate payment and sensitive data classes
-keepclassmembers class app.lovable.monetizevpn.models.Payment {
    private java.lang.String transactionId;
    private java.lang.String paymentMethod;
}
-keepclassmembers class app.lovable.monetizevpn.models.User {
    private java.lang.String token;
    private java.lang.String apiKey;
}

# Remove debug logs in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Remove debug classes and methods
-assumenosideeffects class app.lovable.monetizevpn.utils.Debug {
    public static void log(...);
    public static void print(...);
}

# Encrypt string constants
-adaptclassstrings
-obfuscationdictionary dictionary.txt
-classobfuscationdictionary dictionary.txt
-packageobfuscationdictionary dictionary.txt

# Advanced obfuscation
-repackageclasses ''
-allowaccessmodification
-overloadaggressively

# Keep R class
-keep class **.R
-keep class **.R$* { <fields>; }

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Network security
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
