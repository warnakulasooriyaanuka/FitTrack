package com.example.fittrack.reminders

import android.content.Context

data class ReminderSettings(
    val waterOn: Boolean = false,
    val waterEveryH: Int = 2,
    val sleepOn: Boolean = false,
    val sleepH: Int = 22,
    val sleepM: Int = 30,
    val workoutOn: Boolean = false,
    val workoutH: Int = 18,
    val workoutM: Int = 0
)

/** Reminder settings live on the device (the alarm receiver reads them without a signed-in session). */
object ReminderStore {
    private fun sp(c: Context) = c.getSharedPreferences("fittrack_reminders", Context.MODE_PRIVATE)

    fun load(c: Context): ReminderSettings {
        val p = sp(c)
        return ReminderSettings(
            waterOn = p.getBoolean("waterOn", false),
            waterEveryH = p.getInt("waterEveryH", 2),
            sleepOn = p.getBoolean("sleepOn", false),
            sleepH = p.getInt("sleepH", 22),
            sleepM = p.getInt("sleepM", 30),
            workoutOn = p.getBoolean("workoutOn", false),
            workoutH = p.getInt("workoutH", 18),
            workoutM = p.getInt("workoutM", 0)
        )
    }

    fun save(c: Context, s: ReminderSettings) {
        sp(c).edit()
            .putBoolean("waterOn", s.waterOn).putInt("waterEveryH", s.waterEveryH)
            .putBoolean("sleepOn", s.sleepOn).putInt("sleepH", s.sleepH).putInt("sleepM", s.sleepM)
            .putBoolean("workoutOn", s.workoutOn).putInt("workoutH", s.workoutH).putInt("workoutM", s.workoutM)
            .apply()
    }
}
