# DisciplineLock: System Architecture & Memory

This document serves as the permanent memory bank for the DisciplineLock project. If an AI agent or developer is picking up this project in the future, **READ THIS FILE FIRST** to understand the constraints, architecture, and core logic.

## 1. Environment & Constraints
- **Android SDK Only:** This project was built locally without Android Studio. Compilation is done via `./gradlew.bat assembleDebug` and the emulator is launched via `./android-sdk/emulator/emulator.exe -avd DiscLockEmu`.
- **Tech Stack:** Kotlin, Jetpack Compose (Material 3), Dagger-Hilt (Dependency Injection), Room Database (SQLite), Preferences DataStore.
- **Minimum API:** 26
- **Target API:** 34

## 2. Core Mechanics

### 2.1 The Interceptor (Accessibility Service)
The core of the blocker is the `InstagramTrackingService.kt`.
- It relies on `AccessibilityService` to monitor `TYPE_WINDOW_STATE_CHANGED` events.
- If `com.instagram.android` enters the foreground, it logs the start time.
- If it exits the foreground, it calculates the duration, saves it to Room (`UsageDao`), and instantly broadcasts an update to the Home Screen Widget.
- **Crucial Rule:** If usage exceeds `dailyLimitMinutes`, the service instantly fires an Intent to launch `BlockerActivity` with `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`.

### 2.2 The Blocker Screen
- `BlockerActivity.kt` and `BlockerScreen.kt`.
- It is excluded from the recent apps menu (`excludeFromRecents="true"`, `noHistory="true"`).
- Contains an "Emergency Unlock" feature.

### 2.3 Emergency Unlock
- Handled by `DataStoreManager.useEmergencyUnlock()`.
- Logs the current date to prevent multiple uses per day.
- Adds +5 minutes to the day's limit.
- **Penalty:** Immediately deducts 10 points from the Discipline Score.

## 3. The Gamification Engine (Streak & Score)

To avoid unreliable background jobs (like WorkManager which gets killed by Doze mode), we use a **Catch-Up Evaluator** (`StreakEngine.kt`).

- **Trigger:** Evaluated whenever the `DashboardViewModel` loads OR whenever the `InstagramTrackingService` intercepts an event.
- **Logic:** It checks the `last_evaluated_date`. If there are missed days between the last evaluation and yesterday, it loops through them.
- If a missed day had usage <= limit: `streak++`, `score += 5` (max 100).
- If a missed day failed: `streak = 0`, `score -= 20` (min 0).

## 4. Hardcore Mode
- Managed in `SettingsViewModel.kt` and `DataStoreManager.kt`.
- **The Lock:** If `hardcore_mode_enabled` is true AND the user has already opened Instagram today (`usageSeconds > 0`), the settings menu becomes entirely read-only. The user cannot increase their limit or disable Hardcore mode until the next calendar day.

## 5. Home Screen Widget
- `DisciplineWidgetProvider.kt`.
- A traditional XML AppWidget (not Jetpack Glance).
- Displays the current limit and usage fraction.
- IPC: Uses standard Android Broadcasts (`ACTION_UPDATE_WIDGET`) sent by the `InstagramTrackingService` to instantly refresh the UI.

---
**Agent Directive:** Do not alter the `AccessibilityService` logic or the `StreakEngine` Catch-Up logic without thoroughly testing, as they are the backbone of the app's reliability. Avoid introducing heavy background polling tasks; rely on event-driven state changes.
