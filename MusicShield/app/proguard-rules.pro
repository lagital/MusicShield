# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\PB_HOME\android-sdk_r24.4.1-windows\android-sdk-windows/tools/proguard/proguard-android.txt
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

-dontwarn **
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

-optimizations !code/simplification/arithmetic,!code/allocation/variable
-keep class **
-keepclassmembers class *{*;}
-keepattributes *

#This will not remove error log
-assumenosideeffects class android.util.Log {
   public static int v(...);
   public static int w(...);
   public static int d(...);
   public static int e(...);
}