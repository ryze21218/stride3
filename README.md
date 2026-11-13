# Stride

A step tracker for Android built with Kotlin and Jetpack Compose.

## Run it
1. Open this folder in Android Studio (Koala or newer). It will create the Gradle wrapper and sync.
2. Run on a **real phone**. Most emulators have no step sensor. (The emulator's Extended Controls > Virtual sensors can simulate one on some images.)
3. Grant the Physical activity permission, then walk.

## How it works
- `sensor/StepSensor.kt` reads the hardware `TYPE_STEP_COUNTER` (total steps since boot).
- `data/StepRepository.kt` converts that running total into "steps added today", handling reboots, and stores one row per day in Room.
- `sensor/StepSyncWorker.kt` takes a reading about every 15 minutes in the background via WorkManager.
- `ui/` holds the Compose screen: goal ring, stats, 7-day chart, and the goal/height/weight dialog.

## Known limits
- The very first reading after install only sets a baseline, so steps taken before it aren't counted.
- Steps are credited to the day a reading arrives, so a walk spanning midnight can be off by up to ~15 minutes.
- Streaks compare past days against your current goal.
- Some manufacturers (Xiaomi, Samsung, Huawei...) restrict background work; users may need to exempt the app from battery optimisation.

## Ideas for next
Widget (Jetpack Glance), Health Connect sync, friends/challenges, Wear OS companion, launcher icon.
