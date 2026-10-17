package com.example.fittrack.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class Goals(val steps: Int = 10_000, val calories: Int = 2_200, val sleepMin: Int = 480, val waterMl: Int = 2_500)

data class Achievement(
    val id: String,
    val emoji: String,
    val title: String,
    val desc: String,
    val progress: Int,
    val target: Int
) {
    val unlocked: Boolean get() = progress >= target
}

data class Insights(
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val totalWorkouts: Int = 0,
    val totalMinutes: Int = 0,
    val totalBurned: Int = 0,
    val todaySteps: Int = 0,
    val todayCalories: Int = 0,
    val lastSleepMin: Int = 0,
    val lastSleepQuality: Int = 0,
    val weekSteps: List<Int> = emptyList(),
    val weekSleepMin: List<Int> = emptyList(),
    val weekActive: List<Boolean> = emptyList(),
    val weekLabels: List<String> = emptyList(),
    val achievements: List<Achievement> = emptyList()
)

fun todayKey(): String = LocalDate.now().toString()
fun dateOf(ms: Long): LocalDate = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()

/** Rough calories-per-minute by activity, used when a planned workout is marked done. */
fun estimateKcal(type: String, minutes: Int): Int {
    val perMin = when (type) {
        "Running" -> 10; "Cycling" -> 8; "Gym" -> 6; "Yoga" -> 3; "Swimming" -> 9; "HIIT" -> 12
        else -> 6
    }
    return perMin * minutes
}

/**
 * Everything on the Streaks & Achievements screen is derived from your data,
 * so it can never get out of sync. An "active day" = a logged workout OR hitting the step goal.
 */
fun computeInsights(
    workouts: List<Workout>,
    activity: List<DailyActivity>,
    meals: List<Meal>,
    sleep: List<SleepLog>,
    goals: Goals
): Insights {
    val today = LocalDate.now()
    val workoutDays = workouts.map { dateOf(it.date) }.toSet()
    val stepDays = activity.filter { it.steps >= goals.steps }.mapNotNull { runCatching { LocalDate.parse(it.day) }.getOrNull() }.toSet()
    val active = workoutDays + stepDays

    var streak = 0
    var d = if (today in active) today else today.minusDays(1)
    while (d in active) { streak++; d = d.minusDays(1) }

    var best = 0
    var run = 0
    var prev: LocalDate? = null
    for (day in active.sorted()) {
        run = if (prev != null && prev.plusDays(1) == day) run + 1 else 1
        if (run > best) best = run
        prev = day
    }

    val weekDays = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val stepsByDay = activity.associate { it.day to it.steps }
    val sleepByDay = sleep.groupBy { it.day }.mapValues { e -> e.value.sumOf { it.durationMin } }
    val lastSleep = sleep.maxWithOrNull(compareBy<SleepLog>({ it.day }, { it.createdAt }))

    val totalWorkouts = workouts.size
    val totalMinutes = workouts.sumOf { it.durationMin }
    val totalBurned = workouts.sumOf { it.calories }
    val maxSteps = activity.maxOfOrNull { it.steps } ?: 0
    val mealDays = meals.map { it.day }.toSet().size
    val restedNights = sleep.filter { it.durationMin >= 420 }.map { it.day }.toSet().size

    val achievements = listOf(
        Achievement("first", "🎯", "First rep", "Log your first workout", totalWorkouts, 1),
        Achievement("w5", "💪", "Getting warm", "Log 5 workouts", totalWorkouts, 5),
        Achievement("w25", "🏅", "Dedicated", "Log 25 workouts", totalWorkouts, 25),
        Achievement("s3", "🔥", "Spark", "Reach a 3-day streak", best, 3),
        Achievement("s7", "⚡", "Week warrior", "Reach a 7-day streak", best, 7),
        Achievement("s30", "👑", "Unstoppable", "Reach a 30-day streak", best, 30),
        Achievement("steps10k", "👟", "10K club", "Walk 10,000 steps in a day", maxSteps, 10_000),
        Achievement("min500", "⏱️", "Time in", "Train for 500 minutes in total", totalMinutes, 500),
        Achievement("burn5k", "🌋", "Furnace", "Burn 5,000 kcal from workouts", totalBurned, 5_000),
        Achievement("meals7", "🍎", "Fuel logger", "Log meals on 7 different days", mealDays, 7),
        Achievement("sleep5", "🌙", "Well rested", "Sleep 7h+ on 5 different nights", restedNights, 5)
    )

    return Insights(
        streak = streak,
        bestStreak = best,
        totalWorkouts = totalWorkouts,
        totalMinutes = totalMinutes,
        totalBurned = totalBurned,
        todaySteps = stepsByDay[today.toString()] ?: 0,
        todayCalories = meals.filter { it.day == today.toString() }.sumOf { it.calories },
        lastSleepMin = lastSleep?.durationMin ?: 0,
        lastSleepQuality = lastSleep?.quality ?: 0,
        weekSteps = weekDays.map { stepsByDay[it.toString()] ?: 0 },
        weekSleepMin = weekDays.map { sleepByDay[it.toString()] ?: 0 },
        weekActive = weekDays.map { it in active },
        weekLabels = weekDays.map { it.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) },
        achievements = achievements
    )
}
