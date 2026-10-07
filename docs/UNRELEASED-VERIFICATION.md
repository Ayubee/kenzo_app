# Kenzo App — current development verification

Date: **2026-10-07**, user timezone **Asia/Tashkent**. Tests use the existing **Kenzo_API_36**, Android 16/API 36, x86_64, serial `emulator-5554`. One AVD only, 4 CPU / 4 GB RAM, WHPX acceleration. The emulator uses UTC; reminder deadlines were calculated in its timezone.

## Current source and artifacts

On resuming work, disk already contained version **1.2.0 / code 3**, a warm minimal palette, a bottom-sheet time picker and `kenzo-app-1.2.0.apk`. Those changes were retained. This continuation made **debug-only Unreleased fixes**; it did not bump the version, change package/signing keys, or build a release. The existing APK predates the current keyboard/theme fixes and is not the tested final debug artifact.

Package remains `com.example.kunlikvazifalar`. Daily update command:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run-android.ps1
```

It builds debug, updates with `adb install -r`, and opens the same emulator. To open the emulator alone, append `-EmulatorOnly`. No uninstall, data clearing, AVD recreation or wipe was used.

## Implemented and checked before the resume

These checks were actually performed in the earlier part of this task. Storage/reminder/ViewModel code remains unchanged on resume; subsequent visual fixes have additional checks below.

| Area | Evidence and result |
| --- | --- |
| Existing data / migration | Compared the two original SQLite rows before/after v1 → v2 migration: IDs, text, date, time, completion and creation timestamps preserved. Both received priority NORMAL. Username `Kenzo` preserved. Database name and preference keys retained. |
| Priority | Actual Add/Edit UI saved HIGH/NORMAL/LOW, changed LOW → HIGH → LOW; SQLite persisted values. Higher priority appeared first. SQL tests cover priority, time order and untimed placement. Completed cards use reduced accents and text labels accompany colors. |
| Completion / progress | UI completion and restoration updated daily totals. MainViewModel stress tests issued 100 rapid completion calls and 100 rapid restoration calls: each batch produced one consistent stored transition. A stale editor could not undo a completion stored by the notification action. |
| Settings | Theme, repeat-reminder, animation and haptic choices persisted through process restart. Saving/completing works with app animation disabled and with Android animation scales set to zero. Scales restored afterward. |
| Actual reminders | Android AlarmManager delivered the -5, +10 and +30 slots. Device time was moved to 12 seconds before each deadline, then restored; this was not a continuous 30-minute wall-clock wait. Each delivery was recorded once in SQLite. No fourth slot remained. |
| Open/action | Tapping an actual notification opened its specific task and left it incomplete. Tapping actual Android **BAJARILDI** completed another test row and canceled both remaining repeats. |
| Repeat off / reboot | Disabling repeats removed the +10/+30 pending identities and retained the -5 slot. Re-enabling restored future slots. Rebooted the same AVD: only future unsent repeats were restored; the sent -5 notification was not replayed. |
| Permission denial | Instrumentation actually revoked notification permission and denied exact-alarm access separately. Scheduling returned truthful blocked/approximate states without crashing; enabled access exercised the exact path. Completed, untimed and past tasks did not schedule. These are runtime checks, not inference from manifest declarations. |

Fifteen Android instrumentation tests passed with no skips: **6 database**, **4 permission/policy**, **3 actual notification/delivery**, **2 ViewModel stress**. Isolated tests use UUID databases or reserved task IDs; stress fixtures in the app database were removed without clearing user data. Raw results are retained in ignored `.local/android/unreleased`.

## Latest debug checks

- Rebuilt, installed with `install -r` and opened after the material UI changes.
- **18 unit tests passed**, zero failures/errors: 9 DateUtils, 6 production reminder-policy, 3 sorting/logic. Includes cumulative minute addition and midnight wrapping.
- Android lint: **0 errors, 37 warnings**. Debug and instrumentation APK assembly succeeded. No release task was run in this continuation.
- Found a real keyboard defect: dialog actions were hidden behind LatinIME. Added IME-aware dialog bounds. Actual software keyboard was then shown with a long draft; Add, Edit and Settings actions stayed above it, and task content remained scrollable. No draft or username change was saved for those checks.
- Filled Material 3 surface/container roles with the current warm palette, replacing unintended default purple dialog surfaces. Time picker content now scrolls and buttons can grow with text size.
- Restored per-card priority emphasis in the resumed design: NORMAL gets a softer red border, HIGH a stronger border and subtle red surface, completed rows return to neutral surfaces/badges. Kept the compact progress line for small lists as well as dots, defined the zero-task count, and retained quiet encouragement text. These changes preserve the existing warm palette and green completion control.

- Actual history UI: copied HIGH with its default priority intact, deleted that test copy, then copied it as LOW. The original remained HIGH. On the temporarily advanced empty day, the empty state displayed without invalid percentages; the two copied fixtures were removed and the device clock restored.
- Actual time editing: changed a test task to two different future times. Inspected Android AlarmManager: exactly the new -5/+10/+30 deadlines remained; all previous deadlines disappeared. Deleting that test task removed every remaining reminder identity.
- Current visual checks: Light, Dark and System-following screens captured; 130% font size inspected on main/settings/time-picker screens. The bottom sheet's custom time, cumulative midnight wrap (23:50 → 00:05 → 00:35 → 01:35), cancel and untimed paths passed. The native picker uses uppercase **TANLASH** in this Android UI. Captured and visually inspected actual light/dark cold-launch logo frames without delaying the splash. Opened the real launcher app drawer and verified the K/checkmark icon labeled **Kenzo App**; an earlier incorrect Home capture was replaced.
- Latest UI after priority/progress changes: completed and restored HIGH with Android animation scales at zero; actual counts moved 2/6 → 3/6 → 2/6. Visually inspected neutral completed cards. On an empty day, the new 0/0 count and empty state appeared without invalid values. Device clock, automatic time, font size and animation defaults restored afterward.
- Final cleanup used the UI to delete only this task's known verification fixtures. Re-read SQLite: **exactly the two original rows**, all six original fields unchanged, priority NORMAL for both; username **Kenzo** unchanged. No app reset. System theme and all three preference toggles restored. Existing 1.1.0/1.2.0 release hashes unchanged. App left open on the same emulator. Android's crash log buffer was empty at this final check.

## Actual screenshots

Screenshots are captured directly from the running emulator, not generated mockups. Verification fixture names are intentionally visible.

- [Light tasks](screenshots/unreleased-priority-light.png), [dark tasks](screenshots/unreleased-priority-dark.png), [settings](screenshots/unreleased-settings.png).
- [Real keyboard / long draft](screenshots/unreleased-keyboard.png), [Edit keyboard](screenshots/unreleased-edit-keyboard.png), [Settings keyboard](screenshots/unreleased-settings-keyboard.png).
- [History copy](screenshots/unreleased-history-copy.png), [empty day](screenshots/unreleased-zero-progress.png).
- [Time picker light](screenshots/unreleased-time-picker-light.png), [dark](screenshots/unreleased-time-picker-dark.png), [130% text](screenshots/unreleased-time-picker-large-text.png).
- [Launcher](screenshots/unreleased-launcher.png), [app info](screenshots/unreleased-app-info.png), [light splash](screenshots/unreleased-splash-light.png), [dark splash](screenshots/unreleased-splash-dark.png).
- [Completed neutral cards](screenshots/unreleased-completed-neutral.png), [final preserved original data](screenshots/unreleased-final-preserved-data.png).
- [Actual -5 notification](screenshots/unreleased-notification-before.png), [repeat notification](screenshots/unreleased-notification-repeat.png), [specific task opened](screenshots/unreleased-notification-task.png), [completion action](screenshots/unreleased-notification-action.png).

Earlier reminder screenshots show the preceding palette; their purpose is evidence of actual delivery/action, not a claim to show the resumed visual design. Failed automation attempts and transition frames are not counted as successful visual checks.

## Security and remaining limits

[Security audit](SECURITY-AUDIT.md): no matching OSV advisory among 125 resolved third-party runtime Maven coordinates. No APK or source uploaded. Existing 1.2.0 APK signature/package/permissions independently checked; release certificate matches earlier releases. No Internet permission or app network/dynamic-loader code identified in the scoped checks.

**The installation warning remains unclassified:** only the logo image was supplied; its exact warning text/device details are still missing. No Play Protect bypass or safety guarantee is offered. APK signatures/checksums establish identity/integrity, not absence of malware.

Not physically verified: haptic sensation, OEM battery/background restrictions, real-device Play Protect, multiple launcher masks or legacy Android devices. Adaptive safe-zone geometry and legacy resources were source-checked; API 36 is the runtime device. Loading/error/retry UI exists, but no database failure was deliberately injected and onboarding was not reset merely to retest it. Cold-launch screenshots only establish the sampled frames, not every frame on every device. Historical [three-agent review](REVIEW.md) covers 1.1.0; no new three-agent review is claimed for the resumed changes.

A new release requires the owner's explicit APK request and final checks of the then-current code. Existing release files were retained.
