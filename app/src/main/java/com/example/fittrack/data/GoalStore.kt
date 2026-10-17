package com.example.fittrack.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Per-user daily goals, kept in SharedPreferences. */
class GoalStore(context: Context, uid: String) {
    private val sp = context.getSharedPreferences("fittrack_prefs", Context.MODE_PRIVATE)
    private val kSteps = "${uid}_goal_steps"
    private val kCal = "${uid}_goal_cal"
    private val kSleep = "${uid}_goal_sleep"
    private val kWater = "${uid}_goal_water"
    private val kName = "${uid}_display_name"
    private val kAvatar = "${uid}_avatar_path"
    private val kPGoal = "${uid}_prog_goal"
    private val kPLevel = "${uid}_prog_level"
    private val kPDays = "${uid}_prog_days"
    private val kPEquip = "${uid}_prog_equip"
    private val kPLimit = "${uid}_prog_limit"

    private val _goals = MutableStateFlow(
        Goals(sp.getInt(kSteps, 10_000), sp.getInt(kCal, 2_200), sp.getInt(kSleep, 480), sp.getInt(kWater, 2_500))
    )
    val goals: StateFlow<Goals> = _goals

    fun setSteps(v: Int) { sp.edit().putInt(kSteps, v).apply(); _goals.value = _goals.value.copy(steps = v) }
    fun setCalories(v: Int) { sp.edit().putInt(kCal, v).apply(); _goals.value = _goals.value.copy(calories = v) }
    fun setWater(v: Int) { sp.edit().putInt(kWater, v).apply(); _goals.value = _goals.value.copy(waterMl = v) }

    private val _program = MutableStateFlow(
        ProgramSettings(sp.getString(kPGoal, "Stay fit") ?: "Stay fit", sp.getString(kPLevel, "Beginner") ?: "Beginner", sp.getInt(kPDays, 3),
            sp.getString(kPEquip, "Dumbbells or gym") ?: "Dumbbells or gym", sp.getString(kPLimit, "None") ?: "None")
    )
    val program: StateFlow<ProgramSettings> = _program
    fun setProgram(p: ProgramSettings) {
        sp.edit().putString(kPGoal, p.goal).putString(kPLevel, p.level).putInt(kPDays, p.days)
            .putString(kPEquip, p.equipment).putString(kPLimit, p.limit).apply()
        _program.value = p
    }

    private val _avatar = MutableStateFlow(sp.getString(kAvatar, "") ?: "")
    val avatarPath: StateFlow<String> = _avatar
    /** Saves the new photo path and removes the previous file. Pass "" to remove the photo. */
    fun setAvatar(path: String) {
        val old = _avatar.value
        sp.edit().putString(kAvatar, path).apply()
        _avatar.value = path
        if (old.isNotBlank() && old != path) AvatarStore.delete(old)
    }

    private val _name = MutableStateFlow(sp.getString(kName, "") ?: "")
    val displayName: StateFlow<String> = _name
    fun setName(v: String) { sp.edit().putString(kName, v).apply(); _name.value = v }

    fun setSleepMin(v: Int) { sp.edit().putInt(kSleep, v).apply(); _goals.value = _goals.value.copy(sleepMin = v) }
}

/** Stores the Gemini API key entered inside the app (falls back to the BuildConfig value). */
object AiKeyStore {
    private const val KEY = "ai_api_key"
    fun get(context: Context): String {
        val saved = context.getSharedPreferences("fittrack_prefs", Context.MODE_PRIVATE).getString(KEY, "") ?: ""
        return saved.ifBlank { com.example.fittrack.BuildConfig.GEMINI_API_KEY }
    }
    fun set(context: Context, key: String) {
        context.getSharedPreferences("fittrack_prefs", Context.MODE_PRIVATE).edit().putString(KEY, key.trim()).apply()
    }
}
