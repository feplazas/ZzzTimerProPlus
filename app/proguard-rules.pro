# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep data classes
-keep class com.felipeplazas.zzztimerpro.data.** { *; }

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Keep service classes
-keep class com.felipeplazas.zzztimerpro.services.** { *; }

# Keep widget classes
-keep class com.felipeplazas.zzztimerpro.ui.widget.** { *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve line number information for debugging stack traces (Essential for Play Console)
-keepattributes SourceFile,LineNumberTable

# Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Health Connect
-keep class androidx.health.connect.** { *; }

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Generic dontwarns to prevent build failure on missing references
-dontwarn com.github.mikephil.charting.**
-dontwarn androidx.health.connect.**
-dontwarn org.bouncycastle.**
-dontwarn org.junit.**
-dontwarn android.test.**
-dontwarn androidx.test.**
