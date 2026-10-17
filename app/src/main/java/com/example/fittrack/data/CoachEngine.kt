package com.example.fittrack.data

import java.time.LocalTime

/** A snapshot of the user's day, used to personalise the offline coach's answers. */
data class CoachSnapshot(
    val name: String,
    val steps: Int,
    val stepGoal: Int,
    val waterMl: Int,
    val waterGoal: Int,
    val caloriesEaten: Int,
    val calorieGoal: Int,
    val protein: Int,
    val sleepMin: Int,
    val sleepGoalMin: Int,
    val streak: Int,
    val bestStreak: Int,
    val workoutsWeek: Int,
    val minutesWeek: Int,
    val totalWorkouts: Int,
    val weightKg: Float?,
    val weightChange: Float?,
    val program: ProgramSettings,
    val today: DailyWorkout?
)

/**
 * The built-in coach. It runs entirely on the phone (no internet, no API key): it recognises what you
 * are asking about from keywords and answers using your own numbers. It is not a language model, so for
 * open-ended chat you can still add a Gemini key in Smart mode.
 */
object CoachEngine {

    private class Q(raw: String) {
        val text: String = raw.lowercase().replace("'", "").replace(Regex("[^a-z0-9]+"), " ").trim()
        val tokens: List<String> = text.split(" ").filter { it.isNotEmpty() }
        private val padded = " $text "
        fun has(w: String): Boolean =
            if (w.contains(' ')) padded.contains(" $w ") else tokens.any { it == w || (w.length >= 4 && it.startsWith(w)) }
        fun any(vararg ws: String) = ws.any { has(it) }
    }

    private class Intent(val words: Map<String, Int>, val answer: (CoachSnapshot, Q) -> String) {
        fun score(q: Q) = words.entries.sumOf { if (q.has(it.key)) it.value else 0 }
    }

    fun reply(question: String, s: CoachSnapshot): String {
        val q = Q(question)
        if (q.tokens.isEmpty()) return fallback()

        // safety first
        if (q.any("suicide", "suicidal", "kill myself", "end my life", "self harm", "hurt myself", "want to die")) return crisis()
        if (q.any("starve", "starving", "anorexia", "bulimia", "purge", "purging", "binge", "throw up", "stop eating", "not eating", "skip meals", "800 calories", "500 calories", "1000 calories")) return disorder()
        if (q.any("pain", "hurts", "injury", "injured", "dizzy", "faint", "fainting", "chest", "numb", "swollen", "sprain", "fracture", "broken", "pregnant", "pregnancy", "diabetes", "asthma", "blood pressure", "medication", "surgery")) return medical()

        // a specific exercise was mentioned
        findExercise(q)?.let { return exerciseAnswer(it) }

        // short social messages
        if (q.tokens.size <= 4 && q.any("hi", "hello", "hey", "yo", "hola", "sup", "morning", "evening")) return greeting(s)
        if (q.tokens.size <= 5 && q.any("thanks", "thank", "thx", "cheers", "awesome", "great")) return "Anytime! Keep showing up. What else can I help with?"

        val best = intents.maxByOrNull { it.score(q) }
        if (best != null && best.score(q) > 0) return best.answer(s, q)
        return fallback()
    }

    /* ───────────────────────── intents ───────────────────────── */

    private val intents: List<Intent> = listOf(
        Intent(mapOf("today" to 3, "todays" to 3, "workout" to 1, "session" to 2, "train" to 1, "training" to 1, "exercise" to 1, "gym" to 1, "what should i do" to 3, "what do i do" to 2)) { s, _ -> todayWorkout(s) },
        Intent(mapOf("week" to 3, "weekly" to 3, "plan" to 2, "routine" to 2, "schedule" to 2, "split" to 2, "program" to 2, "days" to 1)) { s, _ -> weekPlan(s) },
        Intent(mapOf("quick" to 3, "short" to 2, "minute" to 2, "minutes" to 2, "no time" to 3, "busy" to 2, "express" to 3, "10" to 1, "15" to 1, "20" to 1, "workout" to 1, "home" to 1)) { s, _ -> quick(s) },
        Intent(mapOf("eat" to 3, "food" to 3, "meal" to 3, "meals" to 2, "diet" to 3, "hungry" to 3, "breakfast" to 3, "lunch" to 3, "dinner" to 3, "snack" to 3, "snacks" to 3, "calorie" to 2, "calories" to 2, "macros" to 3, "what should i eat" to 4)) { s, _ -> food(s) },
        Intent(mapOf("protein" to 4)) { s, _ -> protein(s) },
        Intent(mapOf("lose" to 3, "weight loss" to 4, "fat" to 2, "slim" to 3, "cut" to 2, "belly" to 3, "tummy" to 3, "abs" to 2, "burn fat" to 4, "deficit" to 3)) { s, _ -> loseFat(s) },
        Intent(mapOf("muscle" to 4, "bulk" to 4, "gain" to 2, "stronger" to 3, "strength" to 3, "mass" to 3, "bigger" to 3, "hypertrophy" to 4)) { s, _ -> buildMuscle(s) },
        Intent(mapOf("sleep" to 4, "insomnia" to 4, "bedtime" to 3, "nap" to 3, "tired" to 2, "exhausted" to 2, "wake up" to 2)) { s, _ -> sleep(s) },
        Intent(mapOf("water" to 4, "hydrate" to 4, "hydration" to 4, "thirsty" to 3, "drink" to 2)) { s, _ -> water(s) },
        Intent(mapOf("steps" to 4, "walk" to 3, "walking" to 3, "step" to 2, "pedometer" to 3)) { s, _ -> steps(s) },
        Intent(mapOf("motivation" to 4, "motivated" to 4, "lazy" to 4, "bored" to 3, "give up" to 4, "cant" to 2, "dont feel" to 3, "no energy" to 3, "discipline" to 3, "habit" to 3, "consistent" to 3, "consistency" to 3)) { s, _ -> motivation(s) },
        Intent(mapOf("sore" to 4, "soreness" to 4, "doms" to 4, "recover" to 3, "recovery" to 3, "stretch" to 3, "stretching" to 3, "stiff" to 3, "rest day" to 4)) { _, _ -> recovery() },
        Intent(mapOf("warm" to 3, "warmup" to 4, "warm up" to 4, "cooldown" to 4, "cool down" to 4, "mobility" to 3)) { _, _ -> warmup() },
        Intent(mapOf("beginner" to 4, "start" to 2, "starting" to 2, "new" to 1, "first" to 1, "just started" to 3, "where do i start" to 4)) { s, _ -> beginner(s) },
        Intent(mapOf("run" to 3, "running" to 4, "jog" to 3, "cardio" to 4, "hiit" to 3, "cycling" to 3, "bike" to 2, "swim" to 2, "endurance" to 3)) { _, _ -> cardio() },
        Intent(mapOf("reps" to 4, "sets" to 3, "how many" to 2, "repetitions" to 4, "rest between" to 5, "between" to 1, "heavy" to 2, "how heavy" to 3, "tempo" to 3)) { s, _ -> setsReps(s) },
        Intent(mapOf("supplement" to 4, "supplements" to 4, "creatine" to 5, "protein powder" to 5, "preworkout" to 4, "pre workout" to 4, "bcaa" to 4, "whey" to 4, "vitamins" to 3)) { _, _ -> supplements() },
        Intent(mapOf("plateau" to 5, "stuck" to 4, "stalled" to 4, "no progress" to 4, "not losing" to 4, "not gaining" to 4)) { _, _ -> plateau() },
        Intent(mapOf("weight" to 2, "scale" to 3, "bmi" to 4, "kg" to 2, "body weight" to 3, "weighing" to 3)) { s, _ -> weightLog(s) },
        Intent(mapOf("progress" to 3, "how am i doing" to 5, "stats" to 3, "summary" to 3, "streak" to 3, "doing" to 1, "how did i do" to 5, "report" to 2, "overview" to 3)) { s, _ -> progress(s) },
        Intent(mapOf("who are you" to 5, "what can you do" to 5, "help" to 3, "commands" to 3, "what can i ask" to 5, "what do you do" to 4)) { _, _ -> about() }
    )

    /* ───────────────────────── exercise lookup ───────────────────────── */

    private val generic = setOf("bodyweight", "dumbbell", "chair", "table", "towel", "isometric", "lying", "walking")

    /** Same word, ignoring a plural ending (squat/squats, crunch/crunches, dip/dips). No loose prefixes. */
    private fun same(a: String, b: String): Boolean =
        a == b || a.removeSuffix("s") == b.removeSuffix("s") || a.removeSuffix("es") == b.removeSuffix("es")

    /** Short names people actually type for moves whose full name has extra words. */
    private val aliases = mapOf(
        "dip" to "dips", "dips" to "dips", "curl" to "curl", "curls" to "curl", "bridge" to "glute", "bridges" to "glute",
        "calf" to "calf", "twist" to "russian", "twists" to "russian", "mountain" to "mountain", "climbers" to "mountain",
        "pike" to "pike", "fly" to "fly", "flies" to "fly"
    )

    private fun findExercise(q: Q): Exercise? {
        q.tokens.firstNotNullOfOrNull { aliases[it] }?.let { id -> exerciseById(id)?.let { return it } }
        val compact = q.tokens.joinToString("")
        var best: Exercise? = null
        var bestScore = 0.0
        for (ex in ExerciseLibrary) {
            val words = ex.name.lowercase().replace(Regex("[^a-z ]+"), " ").split(" ")
                .filter { it.length >= 3 && it !in generic }
            if (words.isEmpty()) continue
            val matched = words.count { w -> q.tokens.any { same(it, w) } }
            var ratio = matched.toDouble() / words.size
            val compactName = words.joinToString("")
            if (compactName.length >= 5 && (compact.contains(compactName) || compact.contains(compactName.removeSuffix("s")))) ratio = 1.0
            val strong = words.any { w -> w.length >= 4 && q.tokens.any { same(it, w) } } || ratio == 1.0
            if (ratio >= 0.6 && strong) {
                val score = matched + ratio
                if (score > bestScore) { best = ex; bestScore = score }
            }
        }
        return best
    }

    private fun exerciseAnswer(ex: Exercise): String =
        "${ex.name} (${ex.muscle}, ${ex.equipment.lowercase()})\n${ex.tip}\n\nKeep the reps slow and controlled, breathe out on the effort, and stop if anything hurts. You'll find it in the Exercise library too."

    /* ───────────────────────── answers ───────────────────────── */

    private fun hi(s: CoachSnapshot) = if (s.name.isBlank()) "" else " ${s.name}"

    private fun greeting(s: CoachSnapshot): String {
        val line = s.today?.let { "Today's session is ${it.title.lowercase()} (~${it.estMinutes} min)." } ?: "Today is a rest day on your plan."
        return "Hey${hi(s)}! 👋 $line You're at ${"%,d".format(s.steps)} steps and ${s.waterMl} ml of water so far. What would you like to work on?"
    }

    private fun about() = "I'm Coach Fit. I work offline, using your own data. Ask me things like:\n• What's my workout today?\n• Give me a quick 15 minute workout\n• What should I eat?\n• How can I sleep better?\n• How do I do push-ups?\n• How am I doing?\n\nFor open-ended chat, switch on Smart mode and add a free Gemini key."

    private fun todayWorkout(s: CoachSnapshot): String {
        val w = s.today ?: return "Today is a rest day on your ${s.program.days}-day plan. Good options: a 20 to 30 minute walk, light stretching or an easy mobility flow. Recovery is when your muscles actually get stronger. If you still want to move, ask me for a quick workout."
        val list = w.exercises.joinToString("\n") { "• ${it.exercise.name}: ${it.sets} x ${it.reps}" }
        return "Today: ${w.title} (~${w.estMinutes} min)\n$list\n\nRest ${w.exercises.first().restSec} s between sets. Tap Start on the Summary or Plan tab and I'll run the rest timer for you."
    }

    private fun weekPlan(s: CoachSnapshot): String {
        val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val week = (1..7).joinToString("\n") { "• ${names[it - 1]}: ${focusLabel(s.program, it) ?: "Rest"}" }
        return "Your ${s.program.days}-day plan (${s.program.goal.lowercase()}, ${s.program.level.lowercase()}, ${s.program.equipment.lowercase()}):\n$week\n\nYou can change goal, days, equipment or what to go easy on in Plan > My program."
    }

    private fun quick(s: CoachSnapshot): String {
        val moves = quickCircuit(s.program)
        val list = moves.joinToString("\n") { "• ${it.name}" }
        return "15 minute no-equipment circuit:\n$list\n\nDo each move for 40 seconds, rest 20 seconds, and go through the round 3 times. Finish with 2 minutes of easy stretching."
    }

    private fun food(s: CoachSnapshot): String {
        val left = s.calorieGoal - s.caloriesEaten
        val status = if (s.caloriesEaten == 0) "Your goal is ${s.calorieGoal} kcal." else if (left >= 0) "You've logged ${s.caloriesEaten} of ${s.calorieGoal} kcal, so about $left left." else "You're ${-left} kcal over your goal today. No stress, just balance tomorrow."
        val ideas = when (LocalTime.now().hour) {
            in 4..10 -> "Breakfast ideas: eggs with toast and fruit, Greek yogurt with oats and berries, or a protein smoothie."
            in 11..16 -> "Lunch ideas: chicken or tofu with rice and veg, a tuna or bean wrap, or lentil soup with a side salad."
            in 17..21 -> "Dinner ideas: fish or lean meat with potatoes and veg, a veg stir-fry with eggs or tofu, or a chickpea curry."
            else -> "Light option: yogurt with fruit, a banana with peanut butter, or cottage cheese."
        }
        val goal = when (s.program.goal) {
            "Lose fat" -> "Build plates around protein and vegetables, they keep you full on fewer calories."
            "Build muscle" -> "Add a carb source and protein to every meal, you need the fuel to grow."
            else -> "Aim for a mix of protein, carbs, healthy fats and plenty of colour."
        }
        return "$status\n$ideas\n$goal"
    }

    private fun protein(s: CoachSnapshot): String {
        val kg = s.weightKg ?: 70f
        val per = if (s.program.goal == "Stay fit") 1.4f else 1.8f
        val target = (kg * per / 5f).toInt() * 5
        val basis = if (s.weightKg == null) " (log your weight and I'll personalise this)" else ""
        return "A good target for you is about $target g of protein a day$basis. Today you're at ${s.protein} g.\nGood sources: eggs, Greek yogurt, chicken, fish, tofu, lentils, beans and milk. Spread it over 3 or 4 meals."
    }

    private fun loseFat(s: CoachSnapshot): String =
        "Fat loss comes from a small calorie deficit plus protein, strength training, daily steps and good sleep.\n• Calories: stay near ${s.calorieGoal} kcal\n• Steps: aim for ${"%,d".format(s.stepGoal)}\n• Lift 3+ times a week to keep muscle\n• Sleep 7 to 9 hours\nYou can't spot-reduce belly fat with crunches, it comes off gradually all over. Aim for 0.25 to 0.5 kg a week."

    private fun buildMuscle(s: CoachSnapshot): String =
        "To build muscle:\n• Train each muscle about twice a week (your plan does this)\n• Add a rep or a bit of weight each week\n• Eat enough: roughly 1.6 to 2.2 g of protein per kg, plus a small calorie surplus\n• Sleep 7 to 9 hours, that's when you grow\nYour goal is set to ${s.program.goal.lowercase()}, with ${s.workoutsWeek} workouts logged in the last 7 days."

    private fun sleep(s: CoachSnapshot): String {
        val last = if (s.sleepMin > 0) "Last night you logged ${s.sleepMin / 60}h ${s.sleepMin % 60}m (goal ${s.sleepGoalMin / 60}h). " else "You haven't logged sleep yet. "
        return "${last}To sleep better:\n• Keep the same wake-up time every day\n• Stop caffeine 8 hours before bed\n• Dim screens and lights an hour before sleep\n• Keep the room cool and dark\n• Do a short wind-down: stretch, read, breathe\nTurn on the bedtime reminder in Profile to help build the habit."
    }

    private fun water(s: CoachSnapshot): String {
        val pct = if (s.waterGoal > 0) s.waterMl * 100 / s.waterGoal else 0
        val status = if (s.waterMl >= s.waterGoal) "You've hit your water goal today, nice! 💧" else "You're at ${s.waterMl} of ${s.waterGoal} ml ($pct%). Have a glass now."
        return "$status\n• Drink a glass with every meal\n• Keep a bottle in sight\n• Drink more when it's hot or you train\nTurn on the water reminder in Profile and I'll nudge you."
    }

    private fun steps(s: CoachSnapshot): String {
        val left = (s.stepGoal - s.steps).coerceAtLeast(0)
        val status = if (left == 0) "You've reached your step goal today! 🎉" else "You're at ${"%,d".format(s.steps)} of ${"%,d".format(s.stepGoal)} steps. About ${"%,d".format(left)} to go, roughly ${left / 100} minutes of walking."
        return "$status\n• Take a 10 minute walk after meals\n• Use stairs and park further away\n• Walk during phone calls"
    }

    private fun motivation(s: CoachSnapshot): String {
        val streak = if (s.streak > 0) "You're on a ${s.streak}-day streak, don't break it now. " else "Today is a great day to start a streak. "
        return "${streak}Motivation follows action, not the other way round. Make it tiny: put on your workout clothes and do just 5 minutes. If you still want to stop after that, you can. Most days you won't. 💪"
    }

    private fun recovery() = "Muscle soreness 24 to 72 hours after training is normal. To feel better:\n• Keep moving gently, walk or stretch\n• Eat enough protein and drink water\n• Sleep well\n• Take a proper rest day each week\nSharp or joint pain isn't normal soreness. Stop that exercise and see a professional if it continues."

    private fun warmup() = "Quick 5 minute warm-up:\n• 1 min easy jumping jacks or marching\n• 10 arm circles each way\n• 10 bodyweight squats\n• 10 hip circles each side\n• 5 slow push-ups\nCool down with 3 to 5 minutes of walking and gentle stretches for the muscles you used."

    private fun beginner(s: CoachSnapshot): String =
        "Great that you're starting! Keep it simple:\n• Train ${s.program.days} days a week with full-body sessions, your plan already does this\n• Learn the form first, weight comes later\n• Add one rep per set each week\n• Walk daily and sleep well\nOpen the Plan tab for today's session, and tap any exercise in the library for form tips."

    private fun cardio() = "For running, start with run-walk intervals: run 1 minute, walk 2 minutes, repeat 8 times, three days a week. Go at a pace where you can still talk. Add a minute of running each week. Cycling and swimming are great low-impact options, and HIIT 1 or 2 times a week is plenty."

    private fun setsReps(s: CoachSnapshot): String {
        val w = s.today?.exercises?.firstOrNull()
        val own = w?.let { "Your plan uses ${it.sets} sets of about ${it.reps} with ${it.restSec} s rest. " } ?: ""
        return "${own}As a rule: strength 3 to 6 reps with 2 to 3 minutes rest, muscle growth 8 to 12 reps with 60 to 90 seconds, endurance and fat loss 12 to 20 reps with 30 to 45 seconds. The last 2 reps of each set should feel hard but clean."
    }

    private fun supplements() = "You don't need supplements to make progress, food, sleep and consistent training matter far more. Protein powder is just a convenient protein source. Creatine monohydrate is one of the best-studied supplements, but check with a doctor first, especially if you have a health condition or take medication."

    private fun plateau() = "Plateaus are normal. Try:\n• Track your food and steps honestly for a week\n• Change one thing: add a set, a rep, or 1,000 steps\n• Sleep 7 to 9 hours\n• Take a lighter week, then push again\nGive any change 2 to 3 weeks before judging it."

    private fun weightLog(s: CoachSnapshot): String {
        val w = s.weightKg ?: return "You haven't logged your weight yet. Open Summary > Body weight and add it. Weigh yourself at the same time each morning, and look at the weekly trend, not daily ups and downs."
        val change = s.weightChange?.let { c -> if (c == 0f) "unchanged since your first entry" else (if (c > 0) "up " else "down ") + "%.1f kg since your first entry".format(kotlin.math.abs(c)) } ?: "just getting started"
        return "Your latest weight is ${"%.1f".format(w)} kg, $change. Weigh at the same time each week and focus on the trend, not daily changes."
    }

    private fun progress(s: CoachSnapshot): String =
        "Here's how you're doing:\n• Streak: ${s.streak} days (best ${s.bestStreak})\n• Last 7 days: ${s.workoutsWeek} workouts, ${s.minutesWeek} min\n• Today: ${"%,d".format(s.steps)} steps, ${s.waterMl} ml water, ${s.caloriesEaten} kcal\n• Total workouts: ${s.totalWorkouts}\n" +
            (if (s.workoutsWeek >= s.program.days) "You've hit your weekly target, great job! 🔥" else "${(s.program.days - s.workoutsWeek).coerceAtLeast(1)} more session(s) to hit this week's plan.")

    /* ───────────────────────── safety + fallback ───────────────────────── */

    private fun crisis() = "I'm really sorry you're feeling this way, and I'm glad you said it. You deserve support from a real person right now. If you might act on these thoughts or you're in danger, please call your local emergency number. You can also find a crisis line in your country at findahelpline.com, or tell someone you trust how bad it feels. I'm only an app coach, but I'm here to talk."

    private fun disorder() = "I can't help with eating very little or purging, your body needs fuel to be healthy. If food, weight or exercise feel stressful or out of control, a doctor or counsellor can really help, and you deserve that support. I'm happy to help with balanced meals and gentle workouts instead."

    private fun medical() = "I'm a fitness coach app, not a doctor. If you feel sudden chest pain, dizziness, trouble breathing or numbness, stop and get medical help right away. For ongoing pain, an injury, pregnancy or a health condition, please check with a doctor or physio before training. When you're cleared, I can adjust your plan: set what to go easy on in Plan > My program."

    private fun fallback() = "I'm not sure about that one. I can help with today's workout, your weekly plan, quick workouts, food and protein, sleep, water, steps, recovery, motivation and exercise form (try \"how do I do push-ups?\"). For open-ended chat, switch on Smart mode and add a free Gemini key."
}
