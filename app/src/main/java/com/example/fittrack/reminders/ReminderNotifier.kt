package com.example.fittrack.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.fittrack.MainActivity
import com.example.fittrack.R
import com.example.fittrack.data.AppDatabase
import com.example.fittrack.data.ProgramSettings
import com.example.fittrack.data.todayKey
import com.example.fittrack.data.workoutFor
import java.time.LocalDate

object ReminderNotifier {

    fun ensureChannels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(ReminderScheduler.WATER, "Water reminders", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Nudges to drink water during the day" }
        )
        nm.createNotificationChannel(
            NotificationChannel(ReminderScheduler.SLEEP, "Sleep reminder", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "A daily reminder to wind down for bed" }
        )
        nm.createNotificationChannel(
            NotificationChannel(ReminderScheduler.WORKOUT, "Workout reminder", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "A reminder on your training days" }
        )
    }

    /** Builds the message from the signed-in user's own data and posts it (skips when there's nothing to say). */
    @SuppressLint("MissingPermission")
    suspend fun maybeNotify(ctx: Context, type: String) {
        val sp = ctx.getSharedPreferences("fittrack_prefs", Context.MODE_PRIVATE)
        val uid = sp.getString("last_uid", "") ?: ""
        if (uid.isBlank()) return // signed out

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val (title, text) = when (type) {
            ReminderScheduler.WATER -> {
                val goal = sp.getInt("${uid}_goal_water", 2500)
                val ml = AppDatabase.get(ctx).trackerDao().waterById("$uid|${todayKey()}")?.ml ?: 0
                if (ml >= goal) return // goal already reached, stay quiet
                "Time for some water 💧" to "You've had $ml of $goal ml today. Have a glass now."
            }
            ReminderScheduler.SLEEP -> {
                val hours = sp.getInt("${uid}_goal_sleep", 480) / 60
                "Time to wind down 🌙" to "Head to bed soon to get your ${hours}h of sleep."
            }
            ReminderScheduler.WORKOUT -> {
                val program = ProgramSettings(
                    sp.getString("${uid}_prog_goal", "Stay fit") ?: "Stay fit",
                    sp.getString("${uid}_prog_level", "Beginner") ?: "Beginner",
                    sp.getInt("${uid}_prog_days", 3),
                    sp.getString("${uid}_prog_equip", "Dumbbells or gym") ?: "Dumbbells or gym",
                    sp.getString("${uid}_prog_limit", "None") ?: "None"
                )
                val w = workoutFor(program, LocalDate.now()) ?: return // rest day
                "Workout time 💪" to "${w.title}: ${w.exercises.size} exercises, about ${w.estMinutes} min. Let's go!"
            }
            else -> return
        }

        ensureChannels(ctx)
        val open = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(ctx, type)
            .setSmallIcon(R.drawable.ic_stat_fittrack)
            .setColor(0xFFA5FF00.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(2000 + type.hashCode() % 100, notification)
    }
}
