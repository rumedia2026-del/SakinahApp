# Sakinah App - ProGuard Rules

# Keep Compose classes
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }

# Keep Room entities and DAOs
-keep class com.sakinah.app.data.local.db.entities.** { *; }
-keep class com.sakinah.app.data.local.db.dao.** { *; }

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep data classes (for JSON serialization)
-keepclassmembers class com.sakinah.app.data.** {
    public <fields>;
    public <methods>;
}

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# Glance
-keep class androidx.glance.** { *; }
