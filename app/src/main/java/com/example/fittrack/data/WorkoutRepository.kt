package com.example.fittrack.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/** Room is the source of truth (works offline); Firestore is the remote backup/sync. */
class WorkoutRepository(
    private val dao: WorkoutDao,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun col(uid: String) = db.collection("users").document(uid).collection("workouts")

    fun observe(uid: String): Flow<List<Workout>> = dao.observe(uid)

    suspend fun add(w: Workout) {
        dao.upsert(w)
        appScope.launch { push(w) }
    }

    suspend fun delete(w: Workout) {
        dao.delete(w)
        appScope.launch { runCatching { col(w.userId).document(w.id).delete().await() } }
    }

    private suspend fun push(w: Workout) {
        runCatching {
            col(w.userId).document(w.id).set(
                hashMapOf(
                    "type" to w.type, "durationMin" to w.durationMin,
                    "calories" to w.calories, "date" to w.date
                )
            ).await()
            dao.upsert(w.copy(synced = true))
        }
    }

    /** Push anything created offline, then pull remote workouts into Room. */
    suspend fun sync(uid: String) = withContext(Dispatchers.IO) {
        runCatching {
            dao.unsynced(uid).forEach { push(it) }
            val remote = col(uid).get().await().documents.mapNotNull { d ->
                Workout(
                    id = d.id, userId = uid,
                    type = d.getString("type") ?: return@mapNotNull null,
                    durationMin = d.getLong("durationMin")?.toInt() ?: 0,
                    calories = d.getLong("calories")?.toInt() ?: 0,
                    date = d.getLong("date") ?: 0L,
                    synced = true
                )
            }
            dao.upsertAll(remote)
        }
    }
}
