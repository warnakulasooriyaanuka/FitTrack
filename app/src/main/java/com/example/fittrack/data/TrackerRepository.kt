package com.example.fittrack.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/**
 * Same pattern as WorkoutRepository: Room is the source of truth (offline-first),
 * Firestore (users/{uid}/activity|meals|sleep|plans) is the remote backup / multi-device sync.
 */
class TrackerRepository(
    private val dao: TrackerDao,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private fun col(uid: String, name: String) = db.collection("users").document(uid).collection(name)

    fun observeActivity(uid: String): Flow<List<DailyActivity>> = dao.observeActivity(uid)
    fun observeMeals(uid: String): Flow<List<Meal>> = dao.observeMeals(uid)
    fun observeSleep(uid: String): Flow<List<SleepLog>> = dao.observeSleep(uid)
    fun observePlans(uid: String): Flow<List<PlannedWorkout>> = dao.observePlans(uid)
    fun observeWater(uid: String): Flow<List<WaterLog>> = dao.observeWater(uid)
    fun observeBody(uid: String): Flow<List<BodyMetric>> = dao.observeBody(uid)

    // ───────────── daily activity (steps) ─────────────
    suspend fun addSteps(uid: String, day: String, delta: Int) {
        val id = "$uid|$day"
        val cur = dao.activityById(id)
        dao.upsertActivity(DailyActivity(id, uid, day, (cur?.steps ?: 0) + delta, synced = false))
    }

    /** Steps change constantly, so they're pushed in batches instead of on every step. */
    suspend fun flushActivity(uid: String) = withContext(Dispatchers.IO) {
        dao.unsyncedActivity(uid).forEach { pushActivity(it) }
        dao.unsyncedWater(uid).forEach { pushWater(it) }
    }

    private suspend fun pushActivity(a: DailyActivity) {
        runCatching {
            col(a.userId, "activity").document(a.day).set(hashMapOf("steps" to a.steps)).await()
            // only mark synced if no new steps arrived while we were uploading
            val latest = dao.activityById(a.id)
            if (latest != null && latest.steps == a.steps) dao.upsertActivity(latest.copy(synced = true))
        }
    }

    // ───────────── water ─────────────
    suspend fun addWater(uid: String, day: String, deltaMl: Int) {
        val id = "$uid|$day"
        val cur = dao.waterById(id)
        dao.upsertWater(WaterLog(id, uid, day, ((cur?.ml ?: 0) + deltaMl).coerceAtLeast(0), synced = false))
    }

    private suspend fun pushWater(w: WaterLog) {
        runCatching {
            col(w.userId, "water").document(w.day).set(hashMapOf("ml" to w.ml)).await()
            val latest = dao.waterById(w.id)
            if (latest != null && latest.ml == w.ml) dao.upsertWater(latest.copy(synced = true))
        }
    }

    // ───────────── body weight ─────────────
    suspend fun addWeight(uid: String, day: String, kg: Float) {
        val b = BodyMetric("$uid|$day", uid, day, kg, synced = false)
        dao.upsertBody(b)
        appScope.launch { pushBody(b) }
    }

    suspend fun deleteBody(b: BodyMetric) {
        dao.deleteBody(b)
        appScope.launch { runCatching { col(b.userId, "body").document(b.day).delete().await() } }
    }

    private suspend fun pushBody(b: BodyMetric) {
        runCatching {
            col(b.userId, "body").document(b.day).set(hashMapOf("weightKg" to b.weightKg.toDouble())).await()
            dao.upsertBody(b.copy(synced = true))
        }
    }

    // ───────────── meals ─────────────
    suspend fun addMeal(m: Meal) { dao.upsertMeal(m); appScope.launch { pushMeal(m) } }

    suspend fun deleteMeal(m: Meal) {
        dao.deleteMeal(m)
        appScope.launch { runCatching { col(m.userId, "meals").document(m.id).delete().await() } }
    }

    private suspend fun pushMeal(m: Meal) {
        runCatching {
            col(m.userId, "meals").document(m.id).set(
                hashMapOf(
                    "day" to m.day, "mealType" to m.mealType, "name" to m.name, "calories" to m.calories,
                    "protein" to m.protein, "carbs" to m.carbs, "fat" to m.fat, "createdAt" to m.createdAt
                )
            ).await()
            dao.upsertMeal(m.copy(synced = true))
        }
    }

    // ───────────── sleep ─────────────
    suspend fun addSleep(s: SleepLog) { dao.upsertSleep(s); appScope.launch { pushSleep(s) } }

    suspend fun deleteSleep(s: SleepLog) {
        dao.deleteSleep(s)
        appScope.launch { runCatching { col(s.userId, "sleep").document(s.id).delete().await() } }
    }

    private suspend fun pushSleep(s: SleepLog) {
        runCatching {
            col(s.userId, "sleep").document(s.id).set(
                hashMapOf("day" to s.day, "durationMin" to s.durationMin, "quality" to s.quality, "createdAt" to s.createdAt)
            ).await()
            dao.upsertSleep(s.copy(synced = true))
        }
    }

    // ───────────── workout plan ─────────────
    suspend fun addPlan(p: PlannedWorkout) { dao.upsertPlan(p); appScope.launch { pushPlan(p) } }

    suspend fun deletePlan(p: PlannedWorkout) {
        dao.deletePlan(p)
        appScope.launch { runCatching { col(p.userId, "plans").document(p.id).delete().await() } }
    }

    private suspend fun pushPlan(p: PlannedWorkout) {
        runCatching {
            col(p.userId, "plans").document(p.id).set(
                hashMapOf("dayOfWeek" to p.dayOfWeek, "type" to p.type, "durationMin" to p.durationMin, "note" to p.note)
            ).await()
            dao.upsertPlan(p.copy(synced = true))
        }
    }

    // ───────────── sync (push offline items, then pull remote) ─────────────
    suspend fun sync(uid: String) = withContext(Dispatchers.IO) {
        runCatching {
            dao.unsyncedActivity(uid).forEach { pushActivity(it) }
            dao.unsyncedMeals(uid).forEach { pushMeal(it) }
            dao.unsyncedSleep(uid).forEach { pushSleep(it) }
            dao.unsyncedPlans(uid).forEach { pushPlan(it) }
            dao.unsyncedWater(uid).forEach { pushWater(it) }
            dao.unsyncedBody(uid).forEach { pushBody(it) }
        }
        runCatching {
            col(uid, "water").get().await().documents.forEach { d ->
                val remoteMl = d.getLong("ml")?.toInt() ?: return@forEach
                val id = "$uid|${d.id}"
                val local = dao.waterById(id)
                if (local == null || remoteMl > local.ml) dao.upsertWater(WaterLog(id, uid, d.id, remoteMl, synced = true))
            }
        }
        runCatching {
            val bodies = col(uid, "body").get().await().documents.mapNotNull { d ->
                val kg = d.getDouble("weightKg")?.toFloat() ?: return@mapNotNull null
                BodyMetric("$uid|${d.id}", uid, d.id, kg, synced = true)
            }
            dao.upsertBodies(bodies)
        }
        runCatching {
            col(uid, "activity").get().await().documents.forEach { d ->
                val remoteSteps = d.getLong("steps")?.toInt() ?: return@forEach
                val id = "$uid|${d.id}"
                val local = dao.activityById(id)
                if (local == null || remoteSteps > local.steps) {
                    dao.upsertActivity(DailyActivity(id, uid, d.id, remoteSteps, synced = true))
                }
            }
        }
        runCatching {
            val meals = col(uid, "meals").get().await().documents.mapNotNull { d ->
                Meal(
                    id = d.id, userId = uid,
                    day = d.getString("day") ?: return@mapNotNull null,
                    mealType = d.getString("mealType") ?: "Snack",
                    name = d.getString("name") ?: return@mapNotNull null,
                    calories = d.getLong("calories")?.toInt() ?: 0,
                    protein = d.getLong("protein")?.toInt() ?: 0,
                    carbs = d.getLong("carbs")?.toInt() ?: 0,
                    fat = d.getLong("fat")?.toInt() ?: 0,
                    createdAt = d.getLong("createdAt") ?: 0L,
                    synced = true
                )
            }
            dao.upsertMeals(meals)
        }
        runCatching {
            val sleeps = col(uid, "sleep").get().await().documents.mapNotNull { d ->
                SleepLog(
                    id = d.id, userId = uid,
                    day = d.getString("day") ?: return@mapNotNull null,
                    durationMin = d.getLong("durationMin")?.toInt() ?: 0,
                    quality = d.getLong("quality")?.toInt() ?: 3,
                    createdAt = d.getLong("createdAt") ?: 0L,
                    synced = true
                )
            }
            dao.upsertSleeps(sleeps)
        }
        runCatching {
            val plans = col(uid, "plans").get().await().documents.mapNotNull { d ->
                PlannedWorkout(
                    id = d.id, userId = uid,
                    dayOfWeek = d.getLong("dayOfWeek")?.toInt() ?: return@mapNotNull null,
                    type = d.getString("type") ?: return@mapNotNull null,
                    durationMin = d.getLong("durationMin")?.toInt() ?: 0,
                    note = d.getString("note") ?: "",
                    synced = true
                )
            }
            dao.upsertPlans(plans)
        }
    }
}
