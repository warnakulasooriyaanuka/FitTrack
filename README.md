# FitTrack – HND Android Project (Jetpack Compose + Kotlin)

| Requirement (from whiteboard) | Where it is implemented |
|---|---|
| Jetpack Compose (Kotlin) | `ui/Screens.kt`, `MainActivity.kt` (Material 3, Navigation Compose) |
| Local DB | Room – `data/Workout.kt`, `WorkoutDao.kt`, `AppDatabase.kt` |
| Hardware features | Step counter sensor + runtime permission – `sensor/StepCounter.kt`, `HomeScreen` |
| Remote data source | Firebase Firestore sync – `data/WorkoutRepository.kt` |
| Authentication | Firebase Auth (email/password) – `AuthViewModel` in `ui/ViewModels.kt` |

## Setup
1. Open this folder in **Android Studio** (Ladybug or newer).
2. Go to https://console.firebase.google.com → create project → add Android app with package `com.example.fittrack`.
3. Download **google-services.json** and put it in `app/`.
4. In Firebase console: enable **Authentication → Email/Password** and create a **Firestore Database** (test mode while developing).
5. Sync Gradle and run on a phone/emulator (step sensor works best on a real device).

## How it works
- Offline-first: workouts save to Room instantly, then upload to Firestore (`users/{uid}/workouts`). Items show ☁ when synced, "(offline)" otherwise; unsynced items are pushed on next launch.
- Logging in on another device pulls the user's workouts from Firestore into Room.

## Ideas to extend (for extra marks)
GPS run tracking (location sensor), weekly charts, workout reminders (WorkManager notifications), public exercise API with Retrofit (e.g. wger), dark theme, unit tests for the repository.


## v2 features (dark/orange UI)
| Feature | Where |
|---|---|
| Daily activity tracking (steps/day, distance, goal ring, 7-day chart) | `HomeScreen`, `TrackerViewModel.startStepTracking`, `daily_activity` table |
| Nutrition tracking (calories + macros, meals by type, day switcher) | `ui/NutritionScreens.kt`, `meals` table |
| Sleep tracking (duration, quality, goal, 7-day chart) | `ui/SleepScreens.kt`, `sleep_logs` table |
| Rest timer (presets, ±15 s, beep, keeps screen on) | `ui/TimerScreen.kt` |
| Workout planner (weekly plan, "Mark done" logs a real workout) | `ui/PlannerScreens.kt`, `planned_workouts` table |
| Achievements & streaks (derived from your data) | `data/Insights.kt`, `ui/AchievementsScreen.kt` |
| AI fitness assistant (Gemini, personalised with your data) | `ai/AiClient.kt`, `ui/CoachScreen.kt` |

### Backend
- Room DB is now **version 2** (migration keeps existing workouts). New tables sync to Firestore under
  `users/{uid}/activity | meals | sleep | plans` (same offline-first pattern as workouts).
- **Update your Firestore rules** so the new sub-collections are readable/writable by the owner, e.g.
  `match /users/{uid}/{document=**} { allow read, write: if request.auth != null && request.auth.uid == uid; }`

### AI assistant setup
Get a free key at https://aistudio.google.com/apikey, then either paste it in the app (Coach tab -> API key)
or add `GEMINI_API_KEY=your_key` to `local.properties`. Model names change; they are listed in `ai/AiClient.kt`.


## v3 additions
| Feature | Where |
|---|---|
| Daily workout plan generated from your goal / level / days per week | `data/Programs.kt` (`workoutFor`), `ui/LibraryScreens.kt` (`ProgramScreen`) |
| Guided workout session (sets, reps, auto rest timer, saves to your log) | `ui/SessionScreen.kt` |
| Exercise library (36 home exercises with form tips) | `ui/LibraryScreens.kt` |
| Water tracking (+250 ml, editable goal) | Home card, `water_logs` table |
| Body weight tracking with trend chart | `ui/BodyScreen.kt`, `body_metrics` table |
| AI coach now also knows your water, weight and today's session | `TrackerViewModel.coachContext` |

Room DB is now **version 3** (migration 2 -> 3 keeps all existing data). Water and weight sync to Firestore under
`users/{uid}/water` and `users/{uid}/body`, covered by the same `users/{uid}/{document=**}` rule.


## v4: Apple Fitness look, profile, reminders
- **Theme:** true black, `#1C1C1E` flat cards, Move-red accent, green/cyan/indigo ring colors (`ui/theme/Theme.kt`).
  Home is now a "Summary" screen with three activity rings (Steps / Exercise / Water) and an avatar that opens the profile.
- **Profile page** (`ui/ProfileScreen.kt`): display name, stats, daily goals, training shortcuts, reminders, **Log out**.
- **Reminders** (`reminders/`): drink water (every 1-4 h between 08:00 and 21:00, skipped once the goal is met),
  bedtime reminder, workout reminder (training days only). Scheduled with `AlarmManager`, restored after reboot.
  Android 13+ asks for the notification permission when you switch a reminder on.
- Added `material-icons-extended` for the tab bar icons, plus `POST_NOTIFICATIONS` / `RECEIVE_BOOT_COMPLETED` permissions.


## v5: lime + black, setup quiz, 3D coach, keyless AI
- **Theme:** accent is now lime `#A5FF00` on pure black (`ui/theme/Theme.kt`). Text on lime is black. The in-app logo and
  launcher icons were recolored to lime.
- **Setup quiz** (`ui/OnboardingScreen.kt`, `data/Onboarding.kt`): 12 questions for new accounts (also: Profile > Redo setup quiz).
  Builds your program (goal, level, days, equipment, what to go easy on) and daily targets (calories, protein, water, steps, sleep).
- **Profile:** change photo (Android photo picker, stored privately on the device) and display name.
- **3D coach** (`app/src/main/assets/coach/`): a chibi character built from 3D shapes with three.js r149 (no external model),
  shown in a transparent WebView (`ui/Coach3D.kt`). Drag to turn, tap to wave. It talks while answering and cheers on your plan.
- **AI without an API key** (`data/CoachEngine.kt`): the built-in offline coach answers using your own data.
  Optional Smart mode (Gemini key) is still there; if it fails, the offline coach answers instead.
- Equipment and "go easy on" settings now change which exercises are picked (`data/Programs.kt`).
