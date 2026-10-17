package com.example.fittrack.data

import kotlin.math.roundToInt

/** Answers collected by the setup quiz. */
data class OnboardingAnswers(
    val name: String = "",
    val goal: String = "Stay fit",
    val level: String = "Beginner",
    val days: Int = 3,
    val equipment: String = "Dumbbells or gym",
    val limit: String = "None",
    val age: Int = 25,
    val sex: String = "Prefer not to say",
    val heightCm: Int = 170,
    val weightKg: Float = 70f,
    val activity: String = "Lightly active",
    val sleepNow: String = "7–8 h"
)

/** Everything the app suggests from the quiz. */
data class SuggestedPlan(
    val program: ProgramSettings,
    val calories: Int,
    val proteinG: Int,
    val waterMl: Int,
    val steps: Int,
    val sleepMin: Int,
    val notes: List<String>
)

private fun roundTo(v: Double, step: Int): Int = ((v / step).roundToInt() * step)

/**
 * Turns the quiz answers into a program and daily targets.
 * Calories use the Mifflin-St Jeor estimate x an activity factor, then a modest adjustment for the goal.
 * These are general starting points, not medical advice.
 */
fun buildPlan(a: OnboardingAnswers): SuggestedPlan {
    val w = a.weightKg.toDouble()
    val sexTerm = when (a.sex) { "Male" -> 5.0; "Female" -> -161.0; else -> -78.0 }
    val bmr = 10.0 * w + 6.25 * a.heightCm - 5.0 * a.age + sexTerm
    val factor = when (a.activity) { "Mostly sitting" -> 1.2; "Very active" -> 1.55; else -> 1.375 }
    val tdee = bmr * factor

    val minor = a.age < 18
    val adjust = if (minor) 0 else when (a.goal) { "Lose fat" -> -400; "Build muscle" -> 250; else -> 0 }
    val calories = roundTo(tdee + adjust, 50).coerceIn(1400, 4000)

    val perKg = when (a.goal) { "Lose fat", "Build muscle" -> 1.8; else -> 1.4 }
    val protein = roundTo(w * perKg, 5).coerceIn(50, 220)
    val water = roundTo(w * 35.0, 250).coerceIn(1500, 4000)

    var steps = when (a.activity) { "Mostly sitting" -> 6000; "Very active" -> 10000; else -> 8000 }
    if (a.goal == "Lose fat") steps += 1000

    val lowSleep = a.sleepNow == "Less than 6 h"
    val sleepMin = if (lowSleep) 420 else if (minor) 540 else 480

    val program = ProgramSettings(a.goal, a.level, a.days, a.equipment, a.limit)

    val notes = mutableListOf<String>()
    if (a.limit != "None") notes += "We left out moves that load your ${a.limit.lowercase()}. Stop anything that hurts, and see a doctor or physio if pain continues."
    if (a.equipment == "No equipment") notes += "Every workout is bodyweight only. A backpack with books makes moves harder as you get stronger."
    if (a.level == "Beginner") notes += "For the first two weeks, focus on good form. Then add one rep per set each week."
    when (a.goal) {
        "Lose fat" -> notes += "Aim for slow, steady progress of about 0.25 to 0.5 kg a week. Protein and sleep help you keep muscle."
        "Build muscle" -> notes += "Eat a little above maintenance and add reps or weight each week to keep progressing."
        else -> notes += "Consistency beats intensity. Hitting your week matters more than any single session."
    }
    if (lowSleep) notes += "You sleep under 6 hours, so your first target is 7 hours. Add 15 minutes each week."
    if (minor) notes += "Because you're under 18, calories are set to maintenance with no deficit."
    notes += "These are general starting points, not medical advice. Check with a doctor first if you have a health condition."

    return SuggestedPlan(program, calories, protein, water, steps, sleepMin, notes)
}
