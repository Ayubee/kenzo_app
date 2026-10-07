# Kenzo App

An offline Android planner with an Uzbek interface. Save today's tasks, choose an optional time, and keep earlier days in a separate history.

**Development status: Unreleased.** Current code adds priorities, repeat reminders, motion preferences and the official logo/splash. The distributed version stays **1.1.0 / 2** until the owner requests a new release. The existing 1.1.0 APK below contains the earlier feature set. [Current checks](docs/UNRELEASED-VERIFICATION.md) · [Security audit](docs/SECURITY-AUDIT.md).

[O'zbekcha README](README.uz.md) · [Verification](docs/VERIFICATION.md) · [Three-agent review](docs/REVIEW.md) · [Changelog](CHANGELOG.md)

## Screenshots

Actual Unreleased screens captured from the running debug app on the project's single Android 16 emulator. Sample tasks were created for verification; no mockups are used.

| Light | Dark |
| --- | --- |
| ![Kenzo App in light mode](docs/screenshots/unreleased-priority-light.png) | ![Kenzo App in dark mode](docs/screenshots/unreleased-priority-dark.png) |

![Theme and reminder settings](docs/screenshots/unreleased-settings.png)

## Features

- Locally saved username, without an account or server.
- Add, edit, complete, restore and delete tasks; deletion requires confirmation.
- Three persisted priorities; higher priorities appear first, then time within each priority. Untimed tasks follow timed tasks at the same priority.
- Previous days grouped in history; copy an earlier task into today without changing the original.
- Persistent **System / Light / Dark** appearance settings.
- Local reminders five minutes before a task, plus optional repeats at +10 and +30 minutes while incomplete. Persisted delivery records prevent replay after restart; expired slots are skipped.
- Notifications open the corresponding task; a private **Bajarildi** action completes it and cancels pending reminders. Opening alone does not complete it.
- Today's completion count and progress; subtle 200 ms motion, optional completion haptics, and stored animation/vibration settings that respect system controls.
- Official K/checkmark adaptive and legacy icons; standard Android splash with light/dark backgrounds and no artificial delay.
- Permission status and recovery actions in Settings.
- Data-preserving APK updates using the same package ID and signing key.

The design adapts the supplied reference's warm background, bold typography, yellow accents, rounded outlined cards and offset shadows into a softer task-focused interface.

## Stack

Kotlin · Jetpack Compose · Material 3 · ViewModel/StateFlow · coroutines · SQLite · SharedPreferences · AlarmManager

```text
app/src/main/java/com/example/kunlikvazifalar/
  data/          SQLite, task repository, user/theme preferences
  notification/  Permission-aware scheduling, delivery and restoration
  theme/         Light/dark palettes
  ui/            Screens, dialogs and state management
  util/          Date/time formatting and reminder calculations
```

Minimum Android: **7.0 / API 24**. Target and compile SDK: **36**.
The app has no Internet permission or backend. Android backup settings are separate from offline storage; this is not an encrypted vault.

## Run and build

On the configured Windows workstation:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run-android.ps1
```

This reuses `Kenzo_API_36`, builds debug, updates with `adb install -r` and opens the app. Add `-EmulatorOnly` to open only the emulator. See [Android development](ANDROID-DEVELOPMENT.md) for setup.

Elsewhere, install Android SDK platform 36 and build tools, configure `sdk.dir` in untracked `local.properties`, and use a compatible JDK for Gradle/AGP. The project requests a Java 17 toolchain.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

Previously prepared signed APK: **[kenzo-app-1.1.0.apk](kenzo-app-1.1.0.apk)**. It predates the Unreleased changes. `versionName=1.1.0`, `versionCode=2`, package `com.example.kunlikvazifalar`. No new release was generated for the current work.
Release signing uses the existing private keystore configured by untracked `keystore.properties`. Never commit keys/passwords. Debug and release certificates differ: keep the same signing track for in-place updates.

## Quality and limits

The [verification report](docs/VERIFICATION.md) distinguishes performed checks from remaining limitations. One API 36 emulator does not establish physical-device, older-Android or OEM battery-management coverage. Reminder delivery also depends on Android permissions and power management.

Version policy: patch for fixes, minor for compatible features, major for breaking changes; increment `versionCode` for every published APK. Theme selection advances the initial **1.0.0 verification build** to **1.1.0**.
