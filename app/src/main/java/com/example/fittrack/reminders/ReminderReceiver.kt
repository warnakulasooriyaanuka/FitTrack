package com.example.fittrack.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Fires when a reminder alarm goes off: post the notification, then schedule the next one. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(ReminderScheduler.EXTRA_TYPE) ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val s = ReminderStore.load(context)
                val on = when (type) {
                    ReminderScheduler.WATER -> s.waterOn
                    ReminderScheduler.SLEEP -> s.sleepOn
                    else -> s.workoutOn
                }
                if (on) {
                    runCatching { ReminderNotifier.maybeNotify(context, type) }
                    ReminderScheduler.scheduleNext(context, type)
                }
            } finally {
                pending.finish()
            }
        }
    }
}

/** Alarms are cleared on reboot / app update, so put them back. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.rescheduleAll(context)
    }
}
