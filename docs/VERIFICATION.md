# Kenzo App 1.1.0 — verification / tekshiruv

Historical report for the existing 1.1.0 APK. Subsequent code is **Unreleased**; see [current development checks](UNRELEASED-VERIFICATION.md). The APK described here has not been replaced.

Date: **2026-10-07**. The existing project was updated, not recreated. The original 1.0.0 APK remains an initial verification build.

## Requirements / talablar holati

| Requirement | Status | Evidence |
| --- | --- | --- |
| Kenzo App name and Android label | Bajarilgan / done | UI screenshots and final APK manifest inspected with `aapt`. |
| Softer design based on supplied reference | Bajarilgan / done | Warm background, yellow/lavender/lime accents, rounded outlined cards and subtle offset shadows; actual light/dark screens reviewed. |
| System / Light / Dark | Bajarilgan / done | All three selected through UI; preference survived app restart; manual overrides and system changes checked. |
| Three-agent bug, design and UX review | Bajarilgan / done | [Separate review findings and fixes](REVIEW.md). |
| Open and test in emulator | Bajarilgan / done | One `Kenzo_API_36`, Android 16 / API 36 AOSP x86_64, `emulator-5554`. |
| English and Uzbek portfolio README, real screenshots | Bajarilgan / done | [English](../README.md), [Uzbek](../README.uz.md), screenshots below. |
| Preserve username/tasks on update | Bajarilgan / done for debug upgrade | Actual 1.0.0 → 1.1.0 `adb install -r`; preference XML and database hashes unchanged before further test interactions. |
| Final versioned, signed APK | Bajarilgan / done | 1.1.0, code 2; final artifact/signature details below. |
| Physical devices, older Android, OEM background behavior | Tekshirilmagan / not tested | Outside this single-emulator coverage. |

No requested implementation remains pending. The untested coverage below is not represented as a pass.

## Performed checks / amalda bajarilgan

Runtime checks used **debug builds of the final source** on the same emulator, updating with `run-android.ps1`. The release artifact was assembled after design/core checks, then inspected separately. No app uninstall, data clear, AVD wipe or replacement signing key was used.

| Check | Result |
| --- | --- |
| Unit tests | **11 passed**: strict date/time handling, reminder calculations, task sorting and logic. |
| Instrumentation | **7 passed, no skips**: 3 isolated SQLite regression tests and 4 permission/scheduling tests under controlled actual Android permission states. |
| Build and lint | Debug, Android test APK and signed release built successfully. Debug lint: **0 errors, 28 warnings**; release vital lint passed. Warnings remain, chiefly dependency suggestions/legacy resources. |
| Task UI | Blank title rejected; add, edit, complete, restore, delete cancellation and confirmed deletion checked. Only disposable test rows were deleted. |
| History UI | Temporarily advanced emulator clock one day; date grouping, copy into today and deleting that copy checked. Original historical row remained. Clock and automatic time restored afterward. |
| Theme UI | Light/dark visual checks, System following Android, explicit override of opposite system mode, restart persistence, system-bar contrast. |
| Layout | Add dialog with keyboard, history actions, settings and 130% font size inspected. Fixed wrapping of theme chips and rechecked. Font restored to 100%. |
| Existing data | Username `Kenzo` and original `Emulator sinovi` survived repeated updates. Initial 1.0.0 → 1.1.0 database and preference SHA-256 pairs matched. Subsequent deliberate theme/task test changes naturally changed storage. |
| Stability | Android crash buffer was empty at the end of the checked UI flows. This is not a long-duration stability claim. |

Instrumentation uses separate UUID-named test databases and a reserved reminder ID. It does not clear or replace user storage.

### Notification and exact-alarm permissions

These results come from UI interaction, instrumentation and Android service state, not from manifest declarations:

- Revoked notification permission, opened the actual Android request dialog and chose **Don't allow**. App remained usable and Settings reported disabled notifications. Denied-state instrumentation confirmed no reminder scheduling/posting.
- Checked the repeated-denial path when Android stopped presenting the request dialog. Fixed stale request-button state; the recovery action opened Android notification settings. Enabled notifications there and verified the app refreshed its status on return.
- With exact-alarm access denied, instrumentation confirmed the approximate scheduling result. With access granted, it confirmed the exact result. The UI explains the possibility of delay without access.
- Opened Android **Alarms & reminders** from app Settings, changed access through the system switch and verified the app's status refreshed.
- Created `Eslatma sinovi` at **07:02**, with an exact reminder due at **06:57** (Asia/Tashkent). Android's alarm dump showed an exact alarm; at **06:57:08**, notification service state showed the posted notification with the correct title/time. The notification drawer was captured. Completing the task removed the posted notification.
- Instrumentation checked that completed, untimed and past tasks do not schedule reminders.

The emulator is left with System theme, Android light mode, normal font size, correct clock, and notification/exact-alarm access enabled. Original username/task remain; the completed reminder test task remains as visible test data.

## Actual screenshots / haqiqiy skrinshotlar

All images are unmodified Android `screencap` captures from the running app, not mockups.

| Screen / holat | Screenshot |
| --- | --- |
| Today, light / bugun, yorug' | [today-light.png](screenshots/today-light.png) |
| Today, dark / bugun, qorong'i | [today-dark.png](screenshots/today-dark.png) |
| Settings / sozlamalar | [settings.png](screenshots/settings.png) |
| History / tarix | [history.png](screenshots/history.png) — captured while the emulator date was temporarily advanced for the history test. |
| Completed task / bajarilgan vazifa | [completed.png](screenshots/completed.png) |
| Add with keyboard / klaviatura bilan qo'shish | [add-task-keyboard.png](screenshots/add-task-keyboard.png) |
| Android notification request | [notification-prompt.png](screenshots/notification-prompt.png) |
| Permissions denied / ruxsatlar berilmagan | [permissions-denied.png](screenshots/permissions-denied.png) |
| Delivered notification / kelgan bildirishnoma | [notification-delivered.png](screenshots/notification-delivered.png) |
| Settings, 130% text | [settings-large-text.png](screenshots/settings-large-text.png) |
| Explicit theme overrides | [Dark over light system](screenshots/dark-override.png), [Light over dark system](screenshots/light-override.png) |
| System theme changes | [System dark](screenshots/system-dark.png), [System light](screenshots/system-light.png) |

## Release artifact / yakuniy APK

- File: **[kenzo-app-1.1.0.apk](../kenzo-app-1.1.0.apk)**, 12,362,291 bytes.
- `versionName=1.1.0`, `versionCode=2`, label **Kenzo App**.
- Package unchanged: `com.example.kunlikvazifalar`; release is not debuggable.
- `apksigner verify` passed. Release certificate SHA-256 matches the original `KunlikVazifalar-1.0.0.apk`: `5c94c0f4be44cef19750885333c81791dead3f23681e46740d91455e33d16a0c`.
- APK SHA-256: `213cf7671081f30474a5911696e7e5a7b0b8a6c46f3a583b1d9e051f04e0e5ae`.
- [Checksum file](../kenzo-app-1.1.0.apk.sha256), [machine-readable metadata](release.json).

The existing release keystore was reused. Debug and release use different existing certificates. To preserve the emulator's debug data, the release was **not** installed over debug. An actual release-to-release upgrade was not run; package/version/signature compatibility was checked statically. Runtime screenshots and tests therefore demonstrate the debug build of the same source, not an installed final release APK.

## Remaining coverage limits / tekshirilmagan holatlar

- Physical phones, API 24–35, Google Play images and manufacturer-specific power management.
- Actual reboot/boot-restoration delivery, prolonged background/Doze behavior and approximate-alarm delivery timing. Restoration paths were source-reviewed.
- Injected storage/service failures, exhaustive accessibility/device-size testing and long-duration use.
- Fresh-profile onboarding in 1.1.0: source-reviewed; earlier 1.0.0 onboarding was run. Existing profile was preserved rather than cleared for this check.
- Final release installation and runtime/release-to-release upgrade, as explained above.

Local raw logs, UI dumps and helper scripts remain under ignored `.local/android/verification` and `.local/android`; the public screenshots and this report summarize the evidence. For daily development, use the [single-command instructions](../ANDROID-DEVELOPMENT.md).
