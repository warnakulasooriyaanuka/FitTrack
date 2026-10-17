package com.example.fittrack.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Schedules one pending alarm per reminder type. Each time an alarm fires, the receiver posts the
 * notification and schedules the next one. Uses inexact alarms, so no special permission is needed
 * (a reminder may arrive a few minutes late when the phone is in deep sleep).
 */
object ReminderScheduler {
    const val WATER = "water"
    const val SLEEP = "sleep"
    const val WORKOUT = "workout"
    const val EXTRA_TYPE = "type"

    private const val WATER_FROM_H = 8
    private const val WATER_UNTIL_H = 21

    fun rescheduleAll(c: Context) {
        val s = ReminderStore.load(c)
        apply(c, WATER, s.waterOn)
        apply(c, SLEEP, s.sleepOn)
        apply(c, WORKOUT, s.workoutOn)
    }

    fun cancelAll(c: Context) {
        listOf(WATER, SLEEP, WORKOUT).forEach { cancel(c, it) }
    }

    private fun apply(c: Context, type: String, on: Boolean) = if (on) scheduleNext(c, type) else cancel(c, type)

    fun scheduleNext(c: Context, type: String) {
        val s = ReminderStore.load(c)
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val at = when (type) {
            WATER -> nextWater(now, s.waterEveryH)
            SLEEP -> nextDaily(now, s.sleepH, s.sleepM)
            else -> nextDaily(now, s.workoutH, s.workoutM)
        }
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toInstant().toEpochMilli(), pending(c, type))
    }

    private fun cancel(c: Context, type: String) {
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending(c, type))
    }

    /** Every [everyH] hours, but only between 08:00 and 21:00. */
    private fun nextWater(now: ZonedDateTime, everyH: Int): ZonedDateTime {
        var t = now.plusHours(everyH.coerceAtLeast(1).toLong()).withSecond(0).withNano(0)
        if (t.hour >= WATER_UNTIL_H) t = t.plusDays(1).withHour(WATER_FROM_H).withMinute(0)
        else if (t.hour < WATER_FROM_H) t = t.withHour(WATER_FROM_H).withMinute(0)
        return t
    }

    private fun nextDaily(now: ZonedDateTime, h: Int, m: Int): ZonedDateTime {
        var t = now.withHour(h).withMinute(m).withSecond(0).withNano(0)
        if (!t.isAfter(now)) t = t.plusDays(1)
        return t
    }

    private fun pending(c: Context, type: String): PendingIntent {
        val code = when (type) { WATER -> 1001; SLEEP -> 1002; else -> 1003 }
        val intent = Intent(c, ReminderReceiver::class.java).putExtra(EXTRA_TYPE, type)
        return PendingIntent.getBroadcast(c, code, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
