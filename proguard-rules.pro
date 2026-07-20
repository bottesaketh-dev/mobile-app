# Add project specific ProGuard rules here.

# ============================================================
# PostgreSQL JDBC Driver
# ============================================================
# Keep the JDBC driver class so it can be loaded via Class.forName()
-keep class org.postgresql.Driver { *; }
-keep class org.postgresql.** { *; }

# Keep JDBC service loader entry
-keepnames class * implements java.sql.Driver

# ============================================================
# Room Database
# ============================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface *

# ============================================================
# Kotlin Serialization
# ============================================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }

# ============================================================
# Coroutines
# ============================================================
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# ============================================================
# General Android
# ============================================================
-dontwarn org.slf4j.**
-dontwarn org.checkerframework.**
