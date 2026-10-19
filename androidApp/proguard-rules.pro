# Keep serialization metadata for cached/API payloads.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class et.matchup.app.data.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class et.matchup.app.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
