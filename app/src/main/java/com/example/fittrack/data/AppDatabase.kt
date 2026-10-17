package com.example.fittrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Workout::class, DailyActivity::class, Meal::class, SleepLog::class, PlannedWorkout::class, WaterLog::class, BodyMetric::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun trackerDao(): TrackerDao

    companion object {
        /** v1 -> v2: adds the new tables. Existing workouts are kept untouched. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `daily_activity` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `day` TEXT NOT NULL, `steps` INTEGER NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `meals` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `day` TEXT NOT NULL, `mealType` TEXT NOT NULL, `name` TEXT NOT NULL, `calories` INTEGER NOT NULL, `protein` INTEGER NOT NULL, `carbs` INTEGER NOT NULL, `fat` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `sleep_logs` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `day` TEXT NOT NULL, `durationMin` INTEGER NOT NULL, `quality` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `planned_workouts` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `dayOfWeek` INTEGER NOT NULL, `type` TEXT NOT NULL, `durationMin` INTEGER NOT NULL, `note` TEXT NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        /** v2 -> v3: water + body weight tables. Nothing existing is touched. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `water_logs` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `day` TEXT NOT NULL, `ml` INTEGER NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `body_metrics` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `day` TEXT NOT NULL, `weightKg` REAL NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "fittrack.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
        }
    }
}
