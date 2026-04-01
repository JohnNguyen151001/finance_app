# FinanceApp — add rules when enabling minify for release.

-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** get*Database(...);
}
