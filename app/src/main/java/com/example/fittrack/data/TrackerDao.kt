package com.example.fittrack.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {
    // ---- daily activity ----
    @Query("SELECT * FROM daily_activity WHERE userId = :uid ORDER BY day DESC")
    fun observeActivity(uid: String): Flow<List<DailyActivity>>

    @Query("SELECT * FROM daily_activity WHERE id = :id")
    suspend fun activityById(id: String): DailyActivity?

    @Query("SELECT * FROM daily_activity WHERE userId = :uid AND synced = 0")
    suspend fun unsyncedActivity(uid: String): List<DailyActivity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActivity(a: DailyActivity)

    // ---- meals ----
    @Query("SELECT * FROM meals WHERE userId = :uid ORDER BY createdAt DESC")
    fun observeMeals(uid: String): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE userId = :uid AND synced = 0")
    suspend fun unsyncedMeals(uid: String): List<Meal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeal(m: Meal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeals(list: List<Meal>)

    @Delete
    suspend fun deleteMeal(m: Meal)

    // ---- sleep ----
    @Query("SELECT * FROM sleep_logs WHERE userId = :uid ORDER BY day DESC, createdAt DESC")
    fun observeSleep(uid: String): Flow<List<SleepLog>>

    @Query("SELECT * FROM sleep_logs WHERE userId = :uid AND synced = 0")
    suspend fun unsyncedSleep(uid: String): List<SleepLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSleep(s: SleepLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSleeps(list: List<SleepLog>)

    @Delete
    suspend fun deleteSleep(s: SleepLog)

    // ---- workout plan ----
    @Query("SELECT * FROM planned_workouts WHERE userId = :uid ORDER BY dayOfWeek ASC")
    fun observePlans(uid: String): Flow<List<PlannedWorkout>>

    @Query("SELECT * FROM planned_workouts WHERE userId = :uid AND synced = 0")
    suspend fun unsyncedPlans(uid: String): List<PlannedWorkout>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlan(p: PlannedWorkout)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlans(list: List<PlannedWorkout>)

    @Delete
    suspend fun deletePlan(p: PlannedWorkout)

    // ---- water ----
    @Query("SELECT * FROM water_logs WHERE userId = :uid ORDER BY day DESC")
    fun observeWater(uid: String): Flow<List<WaterLog>>

    @Query("SELECT * FROM water_logs WHERE id = :id")
    suspend fun waterById(id: String): WaterLog?

    @Query("SELECT * FROM water_logs WHERE userId = :uid AND synced = 0")
    suspend fun unsyncedWater(uid: String): List<WaterLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWater(w: WaterLog)

    // ---- body weight ----
    @Query("SELECT * FROM body_metrics WHERE userId = :uid ORDER BY day DESC")
    fun observeBody(uid: String): Flow<List<BodyMetric>>

    @Query("SELECT * FROM body_metrics WHERE userId = :uid AND synced = 0")
    suspend fun unsyncedBody(uid: String): List<BodyMetric>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBody(b: BodyMetric)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBodies(list: List<BodyMetric>)

    @Delete
    suspend fun deleteBody(b: BodyMetric)
}
