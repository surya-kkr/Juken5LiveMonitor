# Juken 5 Live Monitor - ProGuard/R8 Rules for Android Auto

# Protobuf - Keep generated classes and methods
-keep class com.kkr.juken5livemonitor.aap.protocol.proto.** { *; }
-dontwarn com.kkr.juken5livemonitor.aap.protocol.proto.**

# Conscrypt - Keep SSL provider
-keep class org.conscrypt.** { *; }
-dontwarn org.conscrypt.**

# MediaSession - Keep metadata and session classes
-keep class android.support.v4.media.** { *; }
-keep class androidx.media.** { *; }

# JNI - Keep native method names and calling classes
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.kkr.juken5livemonitor.aap.** {
    native <methods>;
}

# Glide - Standard rules
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule
-keep class com.bumptech.glide.** { *; }

# Compose - Core rules (usually handled by AGP, but keeping for safety)
-keep class androidx.compose.runtime.Recomposer { *; }

# App Log and Debugging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
