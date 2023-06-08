# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/huangyifei/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


-keep class com.github.mikephil.charting.** { *; }
-keep class de.blinkt.openvpn.model.** { *; }
-keep class de.blinkt.openvpn.model.ServerNodeBean { *; }
-keep class de.blinkt.openvpn.model.ServerBean { *; }
-keep class de.blinkt.openvpn.model.ZoneModel { *; }
-keep class de.blinkt.openvpn.model.ZoneBean { *; }
-keep class de.blinkt.openvpn.model.ZoneProfileModel { *; }
-keep class de.blinkt.openvpn.model.ZoneProfileBean { *; }
-dontwarn io.realm.**
# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation @interface com.google.gson.annotations.SerializedName
keepattributes *Annotation*
-keep class kotlin.** { *; }
-keep class org.jetbrains.** { *; }
-keepattributes Signature
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.**{*; }
