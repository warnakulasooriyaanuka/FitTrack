package com.example.fittrack.data

import java.time.LocalDate
import java.time.temporal.IsoFields

/* ───────────── exercise library (all doable at home with bodyweight or a pair of dumbbells) ───────────── */

data class Exercise(
    val id: String,
    val name: String,
    val muscle: String,
    val equipment: String,
    val tip: String,
    val timed: Boolean = false
)

val Muscles = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio")

val ExerciseLibrary: List<Exercise> = listOf(
    // Chest
    Exercise("pushup", "Push-ups", "Chest", "Bodyweight", "Hands just wider than shoulders, body in one straight line. Lower until your chest is a fist from the floor."),
    Exercise("inclinepush", "Incline push-ups", "Chest", "Bodyweight", "Hands on a sturdy chair or sofa edge. An easier push-up that still works chest and triceps."),
    Exercise("floorpress", "Dumbbell floor press", "Chest", "Dumbbells", "Lie on your back, elbows at about 45°. Press the weights up, stop when your upper arms touch the floor."),
    Exercise("fly", "Dumbbell floor fly", "Chest", "Dumbbells", "Slight bend in the elbows, open your arms wide, then squeeze the weights back together."),
    Exercise("diamond", "Diamond push-ups", "Chest", "Bodyweight", "Hands form a diamond under your chest. Keep elbows close to hit the triceps hard."),
    // Back
    Exercise("dbrow", "One-arm dumbbell row", "Back", "Dumbbells", "Hand and knee on a bench or chair. Pull the weight to your hip, squeezing the shoulder blade."),
    Exercise("superman", "Superman hold", "Back", "Bodyweight", "Lie face down, lift arms and legs together and squeeze your lower back and glutes.", timed = true),
    Exercise("revfly", "Reverse fly", "Back", "Dumbbells", "Hinge forward with a flat back, raise the weights out to the sides like wings."),
    Exercise("bentrow", "Bent-over row", "Back", "Dumbbells", "Hinge at the hips, flat back, pull both weights to your ribs."),
    Exercise("invrow", "Inverted table row", "Back", "Bodyweight", "Lie under a very sturdy table, grip the edge and pull your chest up. Only use furniture that cannot tip or slide."),
    Exercise("snowangel", "Reverse snow angels", "Back", "Bodyweight", "Face down, sweep your arms from your sides up overhead and back, squeezing your shoulder blades."),
    // Legs
    Exercise("squat", "Bodyweight squat", "Legs", "Bodyweight", "Feet shoulder-width, sit back and down, chest up, knees tracking over toes."),
    Exercise("goblet", "Goblet squat", "Legs", "Dumbbells", "Hold one dumbbell at your chest and squat deep, keeping your torso tall."),
    Exercise("lunge", "Walking lunge", "Legs", "Bodyweight", "Long step forward, back knee almost touching the floor, push through the front heel."),
    Exercise("revlunge", "Reverse lunge", "Legs", "Bodyweight", "Step back instead of forward. Kinder on the knees and great for balance."),
    Exercise("glute", "Glute bridge", "Legs", "Bodyweight", "Feet flat, drive hips up and squeeze your glutes hard at the top."),
    Exercise("calf", "Calf raises", "Legs", "Bodyweight", "Rise onto your toes slowly, pause at the top, lower under control."),
    Exercise("wallsit", "Wall sit", "Legs", "Bodyweight", "Back flat on the wall, thighs parallel to the floor. Breathe and hold.", timed = true),
    Exercise("jumpsquat", "Jump squats", "Legs", "Bodyweight", "Squat down, then explode up. Land softly with bent knees."),
    // Shoulders
    Exercise("shoulderpress", "Dumbbell shoulder press", "Shoulders", "Dumbbells", "Press the weights overhead without arching your back. Ribs down, core tight."),
    Exercise("lateral", "Lateral raise", "Shoulders", "Dumbbells", "Lift the weights out to shoulder height with soft elbows. Lower slowly."),
    Exercise("pike", "Pike push-ups", "Shoulders", "Bodyweight", "Hips high in an upside-down V, lower your head toward the floor and press back up."),
    Exercise("frontraise", "Front raise", "Shoulders", "Dumbbells", "Raise the weights in front of you to eye level, no swinging."),
    // Arms
    Exercise("curl", "Dumbbell biceps curl", "Arms", "Dumbbells", "Elbows pinned to your sides. Curl up, squeeze, lower for a slow count of three."),
    Exercise("hammer", "Hammer curl", "Arms", "Dumbbells", "Palms facing each other. Works the biceps and forearms."),
    Exercise("towelcurl", "Towel isometric curl", "Arms", "Bodyweight", "Stand on a towel, pull up against it as hard as you can without it moving. Squeeze for the whole set."),
    Exercise("dips", "Chair triceps dips", "Arms", "Bodyweight", "Hands on a chair behind you, lower until elbows reach 90°, press back up."),
    Exercise("tricepext", "Overhead triceps extension", "Arms", "Dumbbells", "Hold one weight overhead with both hands, lower it behind your head, extend."),
    // Core
    Exercise("plank", "Plank", "Core", "Bodyweight", "Forearms down, body in one line from head to heels. Squeeze glutes and abs.", timed = true),
    Exercise("crunch", "Crunches", "Core", "Bodyweight", "Lift your shoulder blades off the floor using your abs, not your neck."),
    Exercise("bicycle", "Bicycle crunches", "Core", "Bodyweight", "Opposite elbow to opposite knee, slow and controlled."),
    Exercise("legraise", "Lying leg raises", "Core", "Bodyweight", "Keep your lower back pressed down as you raise and lower straight legs."),
    Exercise("mountain", "Mountain climbers", "Core", "Bodyweight", "From a high plank, drive your knees to your chest quickly.", timed = true),
    Exercise("russian", "Russian twists", "Core", "Bodyweight", "Lean back slightly, rotate your torso side to side. Hold a weight to make it harder."),
    Exercise("deadbug", "Dead bug", "Core", "Bodyweight", "On your back, lower opposite arm and leg while your lower back stays flat.", timed = true),
    // Cardio
    Exercise("jacks", "Jumping jacks", "Cardio", "Bodyweight", "Classic full-body warm-up that gets your heart rate up fast.", timed = true),
    Exercise("burpee", "Burpees", "Cardio", "Bodyweight", "Squat, kick back to plank, push-up optional, jump up. Pace yourself."),
    Exercise("highknees", "High knees", "Cardio", "Bodyweight", "Run on the spot driving your knees to hip height.", timed = true),
    Exercise("skater", "Skater hops", "Cardio", "Bodyweight", "Leap side to side, landing softly on one leg like a speed skater.", timed = true)
)

private val exerciseMap = ExerciseLibrary.associateBy { it.id }
fun exerciseById(id: String): Exercise? = exerciseMap[id]

/* ───────────── program settings & generator ───────────── */

data class ProgramSettings(
    val goal: String = "Stay fit",
    val level: String = "Beginner",
    val days: Int = 3,
    val equipment: String = "Dumbbells or gym",
    val limit: String = "None"
)

val ProgramGoals = listOf("Lose fat", "Build muscle", "Stay fit")
val ProgramLevels = listOf("Beginner", "Intermediate")
val ProgramDays = listOf(3, 4, 5)
val ProgramEquipment = listOf("No equipment", "Dumbbells or gym")
val ProgramLimits = listOf("None", "Knees / legs", "Lower back", "Shoulders")

data class PlannedExercise(val exercise: Exercise, val sets: Int, val reps: String, val restSec: Int)

data class DailyWorkout(
    val title: String,
    val focus: String,
    val emoji: String,
    /** One of the app's workout types (Gym / HIIT), used when the session is saved to the log. */
    val type: String,
    val exercises: List<PlannedExercise>
) {
    val totalSets: Int get() = exercises.sumOf { it.sets }
    val estMinutes: Int get() = 5 + exercises.sumOf { it.sets * (40 + it.restSec) } / 60
}

private fun focusFor(days: Int, dow: Int): String? = when (days) {
    3 -> when (dow) { 1, 3, 5 -> "Full Body"; else -> null }
    4 -> when (dow) { 1, 4 -> "Upper"; 2, 5 -> "Lower"; else -> null }
    else -> when (dow) { 1 -> "Push"; 2 -> "Pull"; 3 -> "Legs"; 4 -> "Core & Cardio"; 5 -> "Full Body"; else -> null }
}

private val pools = mapOf(
    "Full Body" to listOf("squat", "pushup", "dbrow", "lunge", "shoulderpress", "plank", "glute", "mountain"),
    "Upper" to listOf("pushup", "dbrow", "shoulderpress", "floorpress", "curl", "tricepext", "lateral", "superman", "diamond", "pike", "dips", "invrow", "inclinepush"),
    "Lower" to listOf("goblet", "lunge", "glute", "revlunge", "calf", "wallsit", "jumpsquat"),
    "Push" to listOf("pushup", "shoulderpress", "floorpress", "lateral", "dips", "pike", "fly", "diamond", "inclinepush"),
    "Pull" to listOf("dbrow", "bentrow", "revfly", "superman", "curl", "hammer", "deadbug", "invrow", "snowangel", "towelcurl"),
    "Legs" to listOf("goblet", "revlunge", "glute", "lunge", "calf", "wallsit", "jumpsquat"),
    "Core & Cardio" to listOf("burpee", "plank", "mountain", "bicycle", "highknees", "legraise", "jacks", "russian")
)

private fun titleFor(focus: String) = when (focus) {
    "Full Body" -> "Full body"
    "Upper" -> "Upper body"
    "Lower" -> "Lower body"
    "Push" -> "Push day"
    "Pull" -> "Pull day"
    "Legs" -> "Leg day"
    else -> "Core & cardio"
}

private fun emojiForFocus(focus: String) = when (focus) {
    "Full Body" -> "🔥"
    "Upper", "Pull" -> "💪"
    "Lower", "Legs" -> "🦵"
    "Push" -> "🏋️"
    else -> "⚡"
}

/** Name of the focus for a weekday (1 = Mon … 7 = Sun), or null on a rest day. */
fun focusLabel(p: ProgramSettings, dow: Int): String? = focusFor(p.days, dow)?.let { titleFor(it) }

/** Moves to skip when the user wants to go easy on an area (general guidance, not medical advice). */
private val avoidMap = mapOf(
    "Knees / legs" to setOf("jumpsquat", "lunge", "revlunge", "goblet", "squat", "wallsit", "burpee", "jacks", "highknees", "skater"),
    "Lower back" to setOf("bentrow", "superman", "legraise", "russian", "burpee", "jumpsquat"),
    "Shoulders" to setOf("pike", "shoulderpress", "lateral", "frontraise", "diamond", "dips", "fly", "burpee")
)

/** Gentle moves used to top up a day when filters remove too many options. */
private val fillers = listOf("glute", "plank", "deadbug", "bicycle", "crunch", "calf", "inclinepush", "squat", "superman", "legraise", "mountain", "jacks")

/** True if [id] fits the user's equipment and limitation settings. */
fun isAllowed(p: ProgramSettings, id: String): Boolean {
    val ex = exerciseById(id) ?: return false
    if (p.equipment == "No equipment" && ex.equipment != "Bodyweight") return false
    if (avoidMap[p.limit]?.contains(id) == true) return false
    return true
}

/** A short bodyweight circuit (4 moves) that respects the user's settings. */
fun quickCircuit(p: ProgramSettings): List<Exercise> =
    listOf("squat", "pushup", "mountain", "glute", "plank", "jacks", "bicycle", "inclinepush", "deadbug", "calf")
        .filter { isAllowed(p, it) && exerciseById(it)?.equipment == "Bodyweight" }
        .take(4)
        .mapNotNull { exerciseById(it) }

/** The workout for [date] under [p], or null on a rest day. Deterministic, with a small weekly rotation for variety. */
fun workoutFor(p: ProgramSettings, date: LocalDate): DailyWorkout? {
    val focus = focusFor(p.days, date.dayOfWeek.value) ?: return null
    val pool = pools[focus] ?: return null
    val rotate = (date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) % 2) * 2
    val ordered = (pool.drop(rotate) + pool.take(rotate)).filter { isAllowed(p, it) }

    val count = if (p.level == "Intermediate") 6 else 5
    val ids = ordered.take(count).toMutableList()
    if (ids.size < 4) ids.addAll(fillers.filter { isAllowed(p, it) && it !in ids }.take(4 - ids.size))
    // fat-loss days finish with a cardio burst
    if (p.goal == "Lose fat" && focus != "Core & Cardio") {
        val first = if (date.dayOfWeek.value % 2 == 0) "burpee" else "highknees"
        listOf(first, "jacks", "mountain", "skater").firstOrNull { isAllowed(p, it) && it !in ids }?.let { ids.add(it) }
    }

    val sets = if (p.level == "Intermediate") 4 else 3
    val (reps, rest) = when (p.goal) {
        "Lose fat" -> 15 to 30
        "Build muscle" -> 10 to 75
        else -> 12 to 45
    }
    val timedReps = if (p.level == "Intermediate") "45 sec" else "30 sec"

    val exercises = ids.mapNotNull { exerciseById(it) }.map { ex ->
        PlannedExercise(ex, sets, if (ex.timed) timedReps else "$reps reps", rest)
    }
    return DailyWorkout(
        title = titleFor(focus),
        focus = focus,
        emoji = emojiForFocus(focus),
        type = if (focus == "Core & Cardio") "HIIT" else "Gym",
        exercises = exercises
    )
}
