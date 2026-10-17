package com.example.fittrack.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.fittrack.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.fittrack.ai.AiClient
import com.example.fittrack.ai.ChatMsg
import com.example.fittrack.sensor.stepsSinceStart
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user
    val error = MutableStateFlow<String?>(null)
    val loading = MutableStateFlow(false)
    /** Positive message shown on the login screen (e.g. "reset email sent"). */
    val info = MutableStateFlow<String?>(null)

    init { auth.addAuthStateListener { _user.value = it.currentUser } }

    /** Sends Firebase's password-reset email to [email]. */
    fun resetPassword(email: String) {
        val e = email.trim()
        if (e.isBlank()) {
            info.value = null
            error.value = "Type your email above first, then tap Forgot password."
            return
        }
        viewModelScope.launch {
            loading.value = true; error.value = null; info.value = null
            try {
                auth.sendPasswordResetEmail(e).await()
                info.value = "Reset link sent to $e. Check your inbox (and spam folder)."
            } catch (ex: Exception) {
                error.value = ex.message ?: "Couldn't send the reset email"
            }
            loading.value = false
        }
    }

    fun login(email: String, pass: String) = run { auth.signInWithEmailAndPassword(email.trim(), pass).await() }
    fun register(email: String, pass: String) = run { auth.createUserWithEmailAndPassword(email.trim(), pass).await() }
    fun logout() = auth.signOut()

    private fun run(block: suspend () -> Unit) = viewModelScope.launch {
        loading.value = true; error.value = null; info.value = null
        try { block() } catch (e: Exception) { error.value = e.message ?: "Something went wrong" }
        loading.value = false
    }
}

class WorkoutViewModel(app: Application, private val uid: String) : AndroidViewModel(app) {
    private val repo = WorkoutRepository(AppDatabase.get(app).workoutDao())

    val workouts: StateFlow<List<Workout>> =
        repo.observe(uid).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { viewModelScope.launch { repo.sync(uid) } }

    fun add(type: String, minutes: Int, calories: Int) =
        viewModelScope.launch { repo.add(Workout(userId = uid, type = type, durationMin = minutes, calories = calories)) }

    fun delete(w: Workout) = viewModelScope.launch { repo.delete(w) }

    companion object {
        fun factory(app: Application, uid: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = WorkoutViewModel(app, uid) as T
        }
    }
}

/** Daily activity, nutrition, sleep, planner, goals, streaks/achievements. Lives for the whole signed-in session. */
class TrackerViewModel(private val app: Application, private val uid: String) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val repo = TrackerRepository(db.trackerDao())
    private val workoutRepo = WorkoutRepository(db.workoutDao())
    private val goalStore = GoalStore(app, uid)

    val goals: StateFlow<Goals> = goalStore.goals
    val workouts: StateFlow<List<Workout>> =
        workoutRepo.observe(uid).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<Workout>())
    val activity: StateFlow<List<DailyActivity>> =
        repo.observeActivity(uid).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<DailyActivity>())
    val meals: StateFlow<List<Meal>> =
        repo.observeMeals(uid).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<Meal>())
    val sleep: StateFlow<List<SleepLog>> =
        repo.observeSleep(uid).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<SleepLog>())
    val plans: StateFlow<List<PlannedWorkout>> =
        repo.observePlans(uid).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<PlannedWorkout>())

    val water: StateFlow<List<WaterLog>> =
        repo.observeWater(uid).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<WaterLog>())
    val body: StateFlow<List<BodyMetric>> =
        repo.observeBody(uid).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<BodyMetric>())
    val program: StateFlow<ProgramSettings> = goalStore.program
    val displayName: StateFlow<String> = goalStore.displayName
    val avatarPath: StateFlow<String> = goalStore.avatarPath

    val insights: StateFlow<Insights> =
        combine(workouts, activity, meals, sleep, goals) { w, a, m, s, g -> computeInsights(w, a, m, s, g) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, Insights())

    val hasStepSensor: Boolean = stepsSinceStart(app) != null
    private var stepJob: Job? = null

    init {
        viewModelScope.launch { repo.sync(uid) }
        // push accumulated steps to Firestore every 30s
        viewModelScope.launch { while (isActive) { delay(30_000); runCatching { repo.flushActivity(uid) } } }
    }

    /** Counts steps into today's record while the app process is alive. Safe to call repeatedly. */
    fun startStepTracking() {
        if (stepJob?.isActive == true) return
        val flow = stepsSinceStart(app) ?: return
        stepJob = viewModelScope.launch {
            var last = 0
            flow.collect { session ->
                val delta = session - last
                last = session
                if (delta > 0) repo.addSteps(uid, todayKey(), delta)
            }
        }
    }

    fun setStepGoal(v: Int) = goalStore.setSteps(v)
    fun setCalorieGoal(v: Int) = goalStore.setCalories(v)
    fun setSleepGoalMin(v: Int) = goalStore.setSleepMin(v)
    fun setWaterGoal(v: Int) = goalStore.setWater(v)
    fun setDisplayName(v: String) = goalStore.setName(v)

    /** Saves a picked photo (off the main thread) and shows it as the profile picture. */
    fun setAvatarFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            AvatarStore.save(app, uid, uri)?.let { goalStore.setAvatar(it) }
        }
    }
    fun clearAvatar() = goalStore.setAvatar("")

    /** Applies the plan suggested by the setup quiz. */
    fun applyPlan(name: String, plan: SuggestedPlan, weightKg: Float) {
        if (name.isNotBlank()) goalStore.setName(name)
        goalStore.setProgram(plan.program)
        goalStore.setSteps(plan.steps)
        goalStore.setCalories(plan.calories)
        goalStore.setWater(plan.waterMl)
        goalStore.setSleepMin(plan.sleepMin)
        viewModelScope.launch { repo.addWeight(uid, todayKey(), weightKg) }
    }

    /** What the offline coach needs to know about today. */
    fun coachSnapshot(): CoachSnapshot {
        val i = insights.value
        val g = goals.value
        val today = todayKey()
        val b = body.value
        val weekAgo = System.currentTimeMillis() - 7L * 24 * 3600 * 1000
        val week = workouts.value.filter { it.date >= weekAgo }
        return CoachSnapshot(
            name = displayName.value,
            steps = i.todaySteps, stepGoal = g.steps,
            waterMl = water.value.firstOrNull { it.day == today }?.ml ?: 0, waterGoal = g.waterMl,
            caloriesEaten = i.todayCalories, calorieGoal = g.calories,
            protein = meals.value.filter { it.day == today }.sumOf { it.protein },
            sleepMin = i.lastSleepMin, sleepGoalMin = g.sleepMin,
            streak = i.streak, bestStreak = i.bestStreak,
            workoutsWeek = week.size, minutesWeek = week.sumOf { it.durationMin }, totalWorkouts = i.totalWorkouts,
            weightKg = b.firstOrNull()?.weightKg,
            weightChange = if (b.size >= 2) b.first().weightKg - b.last().weightKg else null,
            program = program.value,
            today = workoutFor(program.value, java.time.LocalDate.now())
        )
    }
    fun setProgram(p: ProgramSettings) = goalStore.setProgram(p)

    fun addWater(deltaMl: Int) = viewModelScope.launch { repo.addWater(uid, todayKey(), deltaMl) }
    fun addWeight(kg: Float) = viewModelScope.launch { repo.addWeight(uid, todayKey(), kg) }
    fun deleteBody(b: BodyMetric) = viewModelScope.launch { repo.deleteBody(b) }

    fun addMeal(day: String, type: String, name: String, kcal: Int, protein: Int, carbs: Int, fat: Int) =
        viewModelScope.launch {
            repo.addMeal(Meal(userId = uid, day = day, mealType = type, name = name.trim(), calories = kcal, protein = protein, carbs = carbs, fat = fat))
        }
    fun deleteMeal(m: Meal) = viewModelScope.launch { repo.deleteMeal(m) }

    fun addSleep(day: String, minutes: Int, quality: Int) =
        viewModelScope.launch { repo.addSleep(SleepLog(userId = uid, day = day, durationMin = minutes, quality = quality)) }
    fun deleteSleep(s: SleepLog) = viewModelScope.launch { repo.deleteSleep(s) }

    fun addPlan(dow: Int, type: String, minutes: Int, note: String) =
        viewModelScope.launch { repo.addPlan(PlannedWorkout(userId = uid, dayOfWeek = dow, type = type, durationMin = minutes, note = note.trim())) }
    fun deletePlan(p: PlannedWorkout) = viewModelScope.launch { repo.deletePlan(p) }

    /** "Mark done" on a planned workout logs it as a real workout. */
    fun completePlan(p: PlannedWorkout) = viewModelScope.launch {
        workoutRepo.add(Workout(userId = uid, type = p.type, durationMin = p.durationMin, calories = estimateKcal(p.type, p.durationMin)))
    }

    /** A snapshot of the user's data, given to the AI coach as context. */
    fun coachContext(): String {
        val i = insights.value
        val g = goals.value
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val today = todayKey()
        val todayMeals = meals.value.filter { it.day == today }
        val recent = workouts.value.take(5).joinToString("; ") { "${it.type} ${it.durationMin}min (${dateOf(it.date)})" }.ifBlank { "none yet" }
        val plan = plans.value.joinToString("; ") { "${dayNames[(it.dayOfWeek - 1).coerceIn(0, 6)]}: ${it.type} ${it.durationMin}min" }.ifBlank { "no plan yet" }
        val waterToday = water.value.firstOrNull { it.day == today }?.ml ?: 0
        val latestWeight = body.value.firstOrNull()?.let { "${it.weightKg} kg (${it.day})" } ?: "not logged"
        val prog = program.value
        val todaysPlan = workoutFor(prog, java.time.LocalDate.now())?.let { w -> "${w.title}: " + w.exercises.joinToString(", ") { it.exercise.name } } ?: "rest day"
        val sleepLine = if (i.lastSleepMin > 0) "${i.lastSleepMin / 60}h ${i.lastSleepMin % 60}m, quality ${i.lastSleepQuality}/5 (goal ${g.sleepMin / 60}h)" else "not logged yet"
        return """
You are FitTrack Coach, a friendly, motivating fitness and wellness assistant inside the FitTrack app.
Use the user's data below to personalise answers.

User data (today is $today):
- Steps today: ${i.todaySteps} (goal ${g.steps})
- Eaten today: ${i.todayCalories} kcal of ${g.calories} goal; protein ${todayMeals.sumOf { it.protein }}g, carbs ${todayMeals.sumOf { it.carbs }}g, fat ${todayMeals.sumOf { it.fat }}g
- Last sleep: $sleepLine
- Streak: ${i.streak} days (best ${i.bestStreak}); total workouts ${i.totalWorkouts}, ${i.totalMinutes} min
- Recent workouts: $recent
- Weekly plan: $plan
- Water today: $waterToday ml (goal ${g.waterMl} ml)
- Latest body weight: $latestWeight
- Training program: ${prog.goal}, ${prog.level}, ${prog.days} days/week. Today's session: $todaysPlan

Rules:
- Be concise (under 150 words unless asked for more), practical and encouraging.
- Plain text only. Use short lines and "•" bullets. No markdown headings or bold.
- You are not a doctor. For pain, injuries, medical conditions, pregnancy, eating disorders or extreme diets, advise seeing a qualified professional.
- Never recommend extreme calorie restriction or dangerous training.
""".trimIndent()
    }

    companion object {
        fun factory(app: Application, uid: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = TrackerViewModel(app, uid) as T
        }
    }
}

/** AI fitness assistant chat state (kept in memory for the session). */
class CoachViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx = app.applicationContext
    val messages = MutableStateFlow<List<ChatMsg>>(emptyList())
    val busy = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    val apiKey = MutableStateFlow(AiKeyStore.get(ctx))

    fun saveKey(key: String) {
        AiKeyStore.set(ctx, key)
        apiKey.value = AiKeyStore.get(ctx)
        error.value = null
    }

    fun clear() { messages.value = emptyList(); error.value = null }

    /**
     * With no API key the built-in offline coach answers instantly. With a key, Smart mode (Gemini) answers,
     * and if it can't be reached the offline coach steps in so the user always gets a reply.
     */
    fun send(text: String, systemPrompt: String, offlineReply: (String) -> String) {
        val t = text.trim()
        if (t.isEmpty() || busy.value) return
        messages.value = messages.value + ChatMsg(true, t)
        busy.value = true
        error.value = null
        viewModelScope.launch {
            if (apiKey.value.isBlank()) {
                delay(650) // a short beat so the coach feels like it is thinking
                messages.value = messages.value + ChatMsg(false, offlineReply(t))
            } else {
                AiClient.chat(apiKey.value, systemPrompt, messages.value).fold(
                    onSuccess = { messages.value = messages.value + ChatMsg(false, it) },
                    onFailure = {
                        messages.value = messages.value + ChatMsg(false, offlineReply(t) + "\n\n(Offline answer. Smart mode couldn't answer: ${it.message ?: "unknown error"})")
                    }
                )
            }
            busy.value = false
        }
    }
}
