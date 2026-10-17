package com.example.fittrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: String,
    val durationMin: Int,
    val calories: Int,
    val date: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
