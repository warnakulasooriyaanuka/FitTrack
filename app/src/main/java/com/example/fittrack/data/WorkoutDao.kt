package com.example.fittrack.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE userId = :uid ORDER BY date DESC")
    fun observe(uid: String): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE userId = :uid AND synced = 0")
    suspend fun unsynced(uid: String): List<Workout>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(w: Workout)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<Workout>)

    @Delete
    suspend fun delete(w: Workout)
}
