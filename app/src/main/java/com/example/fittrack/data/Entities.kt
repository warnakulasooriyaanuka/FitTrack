package com.example.fittrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/** One row per user per day. id = "$userId|$day"; Firestore doc id = day (yyyy-MM-dd). */
@Entity(tableName = "daily_activity")
data class DailyActivity(
    @PrimaryKey val id: String,
    val userId: String,
    val day: String,
    val steps: Int,
    val synced: Boolean = false
)

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val day: String,
    val mealType: String,
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val day: String,
    val durationMin: Int,
    val quality: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

/** dayOfWeek: 1 = Monday ... 7 = Sunday (java.time.DayOfWeek.value). */
@Entity(tableName = "planned_workouts")
data class PlannedWorkout(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val dayOfWeek: Int,
    val type: String,
    val durationMin: Int,
    val note: String = "",
    val synced: Boolean = false
)

/** One row per user per day. id = "$userId|$day"; Firestore doc id = day. */
@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey val id: String,
    val userId: String,
    val day: String,
    val ml: Int,
    val synced: Boolean = false
)

/** Body weight, one entry per day. id = "$userId|$day"; Firestore doc id = day. */
@Entity(tableName = "body_metrics")
data class BodyMetric(
    @PrimaryKey val id: String,
    val userId: String,
    val day: String,
    val weightKg: Float,
    val synced: Boolean = false
)
