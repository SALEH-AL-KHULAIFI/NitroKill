# قواعد ProGuard/R8 الخاصة بمشروع NitroKill

# الحفاظ على كلاسات التطبيق الأساسية
-keep class com.isx3i.nitrokill.** { *; }

# قواعد Jetpack Compose الافتراضية يوفرها AGP تلقائياً
-dontwarn kotlinx.coroutines.**
