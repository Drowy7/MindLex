# Retrofit и kotlinx.serialization
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn org.conscrypt.**
-dontwarn kotlinx.serialization.**

# Модели, сериализуемые kotlinx.serialization
-keepclassmembers class ru.mindlex.data.remote.dto.** {
    *** Companion;
    <fields>;
}
-keepclasseswithmembers class ru.mindlex.data.remote.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}
