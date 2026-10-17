package com.example.fittrack

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fittrack.data.estimateKcal
import com.example.fittrack.reminders.ReminderNotifier
import com.google.firebase.auth.FirebaseAuth
import com.example.fittrack.reminders.ReminderScheduler
import com.example.fittrack.data.workoutFor
import com.example.fittrack.ui.*
import java.time.LocalDate
import com.example.fittrack.ui.theme.FitColors
import com.example.fittrack.ui.theme.FitTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent { FitTrackTheme { Surface(color = FitColors.Bg) { FitTrackApp() } } }
    }
}

@Composable
fun FitTrackApp() {
    val authVm: AuthViewModel = viewModel()
    val user by authVm.user.collectAsState()
    val u = user
    if (u == null) LoginScreen(authVm)
    else SignedInApp(u.uid, u.email ?: "", onLogout = { authVm.logout() })
}

private val TabRoutes = setOf("home", "plan", "food", "sleep", "coach")
private fun isTab(route: String?) = route in TabRoutes

private val Tabs = listOf(
    TabItem("home", Icons.Rounded.DonutLarge, "Summary"),
    TabItem("plan", Icons.Rounded.CalendarMonth, "Plan"),
    TabItem("food", Icons.Rounded.Restaurant, "Fuel"),
    TabItem("sleep", Icons.Rounded.Bedtime, "Sleep"),
    TabItem("coach", Icons.Rounded.AutoAwesome, "Coach")
)

@Composable
fun SignedInApp(uid: String, email: String, onLogout: () -> Unit) {
    val app = LocalContext.current.applicationContext as Application
    // Activity-scoped so step counting and data survive navigation between screens
    val wvm: WorkoutViewModel = viewModel(key = "w_$uid", factory = WorkoutViewModel.factory(app, uid))
    val tvm: TrackerViewModel = viewModel(key = "t_$uid", factory = TrackerViewModel.factory(app, uid))
    val coach: CoachViewModel = viewModel(key = "c_$uid")

    // let the reminder receiver know who is signed in, and (re)arm any enabled reminders
    LaunchedEffect(uid) {
        app.getSharedPreferences("fittrack_prefs", Context.MODE_PRIVATE).edit().putString("last_uid", uid).apply()
        ReminderNotifier.ensureChannels(app)
        ReminderScheduler.rescheduleAll(app)
    }

    // Setup quiz: shown to brand-new accounts until finished (or skipped), and on request from the profile
    val prefs = remember { app.getSharedPreferences("fittrack_prefs", Context.MODE_PRIVATE) }
    val isNewAccount = remember(uid) {
        FirebaseAuth.getInstance().currentUser?.metadata
            ?.let { it.lastSignInTimestamp - it.creationTimestamp < 120_000L } ?: false
    }
    var onboarded by remember(uid) { mutableStateOf(prefs.getBoolean("${uid}_onboarded", false)) }
    var redoQuiz by remember(uid) { mutableStateOf(false) }

    fun finishQuiz() {
        prefs.edit().putBoolean("${uid}_onboarded", true).apply()
        onboarded = true
        redoQuiz = false
    }

    if ((isNewAccount && !onboarded) || redoQuiz) {
        OnboardingScreen(
            initialName = tvm.displayName.value,
            onFinish = { answers, plan ->
                tvm.applyPlan(answers.name, plan, answers.weightKg)
                finishQuiz()
            },
            onSkip = { finishQuiz() }
        )
        return
    }

    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val imeUp = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val showBar = Tabs.any { it.route == route } && !imeUp

    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val barInset by animateDpAsState(if (showBar) 84.dp + navBottom else 0.dp, tween(320), label = "barInset")

    CompositionLocalProvider(LocalBarInset provides barInset) {
    Box(Modifier.fillMaxSize().background(FitColors.Bg)) {
        Box(
            Modifier.fillMaxSize().then(
                if (showBar) Modifier.consumeWindowInsets(WindowInsets.navigationBars) else Modifier
            )
        ) {
            NavHost(
                nav, startDestination = "home",
                enterTransition = {
                    if (isTab(initialState.destination.route) && isTab(targetState.destination.route))
                        fadeIn(tween(260, delayMillis = 60)) + scaleIn(tween(340, easing = FastOutSlowInEasing), initialScale = 0.94f)
                    else slideInHorizontally(tween(380, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(260))
                },
                exitTransition = {
                    if (isTab(initialState.destination.route) && isTab(targetState.destination.route))
                        fadeOut(tween(160))
                    else slideOutHorizontally(tween(380, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(tween(260))
                },
                popEnterTransition = {
                    if (isTab(initialState.destination.route) && isTab(targetState.destination.route))
                        fadeIn(tween(260, delayMillis = 60)) + scaleIn(tween(340, easing = FastOutSlowInEasing), initialScale = 0.94f)
                    else slideInHorizontally(tween(380, easing = FastOutSlowInEasing)) { -it / 4 } + fadeIn(tween(260))
                },
                popExitTransition = {
                    if (isTab(initialState.destination.route) && isTab(targetState.destination.route))
                        fadeOut(tween(160))
                    else slideOutHorizontally(tween(380, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(260))
                }
            ) {
                composable("home") {
                    HomeScreen(
                        email, wvm, tvm,
                        onAdd = { nav.navigate("add") },
                        onProfile = { nav.navigate("profile") },
                        onTimer = { nav.navigate("timer") },
                        onAchievements = { nav.navigate("achievements") },
                        onStartWorkout = { nav.navigate("session") },
                        onBody = { nav.navigate("body") }
                    )
                }
                composable("plan") {
                    PlannerScreen(
                        tvm,
                        onAdd = { dow -> nav.navigate("addPlan/$dow") },
                        onTimer = { nav.navigate("timer") },
                        onStartWorkout = { nav.navigate("session") },
                        onProgram = { nav.navigate("program") },
                        onLibrary = { nav.navigate("library") }
                    )
                }
                composable("food") { NutritionScreen(tvm, onAddMeal = { day -> nav.navigate("addMeal/$day") }) }
                composable("sleep") { SleepScreen(tvm, onAdd = { nav.navigate("addSleep") }) }
                composable("coach") { CoachScreen(coach, tvm) }

                composable("session") {
                    val program by tvm.program.collectAsState()
                    val workout = remember(program) { workoutFor(program, LocalDate.now()) }
                    if (workout == null) {
                        LaunchedEffect(Unit) { nav.popBackStack() }
                    } else {
                        WorkoutSessionScreen(
                            workout,
                            onSave = { minutes ->
                                wvm.add(workout.type, minutes, estimateKcal(workout.type, minutes))
                                nav.popBackStack()
                            },
                            onBack = { nav.popBackStack() }
                        )
                    }
                }
                composable("profile") {
                    ProfileScreen(
                        email, tvm,
                        onBack = { nav.popBackStack() },
                        onProgram = { nav.navigate("program") },
                        onBody = { nav.navigate("body") },
                        onLibrary = { nav.navigate("library") },
                        onAchievements = { nav.navigate("achievements") },
                        onRedoQuiz = { redoQuiz = true },
                        onLogout = onLogout
                    )
                }
                composable("library") { ExerciseLibraryScreen(onBack = { nav.popBackStack() }) }
                composable("program") { ProgramScreen(tvm, onBack = { nav.popBackStack() }) }
                composable("body") { BodyScreen(tvm, onBack = { nav.popBackStack() }) }
                composable("add") { AddWorkoutScreen(wvm, onDone = { nav.popBackStack() }) }
                composable("timer") { RestTimerScreen(onBack = { nav.popBackStack() }) }
                composable("achievements") { AchievementsScreen(tvm, onBack = { nav.popBackStack() }) }
                composable("addSleep") { AddSleepScreen(tvm, onDone = { nav.popBackStack() }) }
                composable("addPlan/{dow}", arguments = listOf(navArgument("dow") { type = NavType.IntType })) { entry ->
                    AddPlanScreen(entry.arguments?.getInt("dow") ?: 1, tvm, onDone = { nav.popBackStack() })
                }
                composable("addMeal/{day}", arguments = listOf(navArgument("day") { type = NavType.StringType })) { entry ->
                    AddMealScreen(entry.arguments?.getString("day") ?: "", tvm, onDone = { nav.popBackStack() })
                }
            }
        }
        AnimatedVisibility(
            visible = showBar,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(380, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(300)),
            exit = slideOutVertically(tween(260)) { it } + fadeOut(tween(200))
        ) {
            FitBottomBar(Tabs, route) { target ->
                nav.navigate(target) {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }
    }
}
