# Kenzo App — security audit, 2026-10-07

## Installation warning

**Unclassified: exact warning text is missing.** The supplied image is the official K/checkmark logo, not an installation warning. The owner was asked for the exact text. There is no evidence yet to distinguish unknown-source permission, a Play Protect scanning recommendation, a harmful-app block, an OEM warning or another installation error.

No advice to disable Play Protect, hide detections or change the package ID was given. No appeal was submitted. If a harmful-app verdict is supplied, first record its text/device/Android version and check the existing APK's SHA-256 report before considering an upload. APK/source uploads and submission of an appeal require separate owner approval.

## Scope and findings

Reviewed the current source, resolved debug runtime dependencies, merged debug manifest, original `KunlikVazifalar-1.0.0.apk`, existing `kenzo-app-1.1.0.apk` and the new local debug APK. **No new release APK was built.**

| Area | Actual result |
| --- | --- |
| Permissions | Current source and 1.1.0 release use `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `SCHEDULE_EXACT_ALARM`, `VIBRATE`; AndroidX adds a signature-level private receiver permission. No Internet, storage, location, contacts, microphone, camera or package-install permission found. Original 1.0.0 also had `USE_EXACT_ALARM`; it was already removed in 1.1.0. |
| Exported components | MainActivity remains exported for launcher entry. Reminder, completion-action and boot receivers are private. AndroidX Startup provider is private. AndroidX ProfileInstallReceiver is exported with privileged `android.permission.DUMP`, rather than open to ordinary applications. |
| Debug entry points | Audit found exported preview/test host activities supplied by debug libraries. Added a debug-only manifest override to make both private; verified in merged manifest and rebuilt debug APK. These hosts are absent from the existing release. |
| Intents | Explicit immutable PendingIntents; per-task/per-slot URI identities prevent collisions. External MainActivity extras only select a task for display; opening never completes a task. Completion is a private receiver action and reads/writes SQLite before canceling reminders. |
| Dependencies / SDKs | Resolved runtime groups belong to AndroidX, Kotlin/JetBrains, Guava and standard annotation libraries. No advertising/analytics SDK identified. Removed unused Navigation 3 runtime dependencies and an unused serialization plugin from the app module. Added only AndroidX's standard splash compatibility library. |
| Network / dynamic loading | No app network client, tracking endpoint or dynamic-loader code found in main source. APK manifests have no Internet permission. DEX string inspection found no `DexClassLoader` or `InMemoryDexClassLoader` markers; native library entries are AndroidX graphics-path binaries for four ABIs. This is a scoped static check, not exhaustive reverse engineering or a behavioral malware scan. |
| Secrets | No credential/private-key patterns found in main Kotlin/XML source or the version catalog. Release properties and keystore extensions are Git-ignored and not tracked; no key or password was copied to documentation/logs. Existing signing tracks retained. |
| Data | Username/settings in private SharedPreferences; tasks/delivery ledger in private SQLite. Existing backup permission remains enabled; Android may back up/transfer app data. Storage is not an encrypted vault. |
| Build tracks | Debug is debuggable and intended for local testing. Existing signed 1.1.0 release is not debuggable. Release signing configuration unchanged; missing local credentials must be supplied before a future signed release. |

### Known dependency advisories

On 2026-10-07, queried **125 resolved third-party Maven coordinates** from `debugRuntimeClasspath` against the [OSV batch API](https://google.github.io/osv.dev/post-v1-querybatch/). **No matching advisory was returned.** Only public package names/versions were sent; APK and source were not uploaded. The project component was excluded. This covers resolved runtime coordinates, not every build-tool plugin or every vulnerability database; absent advisories are not proof of safety. Raw resolved coordinates and response remain locally in ignored `.local/android/unreleased`.

The splash implementation follows the [Android standard SplashScreen migration](https://developer.android.com/develop/ui/views/launch/splash-screen/migrate) using [AndroidX core-splashscreen](https://developer.android.com/jetpack/androidx/releases/core). It adds no separate splash Activity, fixed delay or animation framework.

### Existing APK identities

Both existing releases passed local `apksigner verify`; their release certificates match. Hash/signature correctness establishes identity/integrity, **not a security guarantee**.

| Existing file | SHA-256 |
| --- | --- |
| `KunlikVazifalar-1.0.0.apk` | `33329a2fc98f5f1626615bf5f41820961e617692174060a012f34e3933af19b6` |
| `kenzo-app-1.1.0.apk` | `213cf7671081f30474a5911696e7e5a7b0b8a6c46f3a583b1d9e051f04e0e5ae` |

No external malware verdict is claimed. A physical device's Play Protect or manufacturer-specific behavior was not reproduced by the emulator. See [current development verification](UNRELEASED-VERIFICATION.md) for actual permission and reminder checks.
