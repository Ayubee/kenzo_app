# Android development workflow

- Use one existing compatible Android emulator for this project. Prefer the configured
  `Kenzo_API_36`; do not create additional AVDs for ordinary development.
- After material app changes, use `powershell -NoProfile -ExecutionPolicy Bypass -File
  .\run-android.ps1` to rebuild, update the same emulator, and open the app.
- Preserve username and tasks. Update with `adb install -r`; never uninstall,
  clear app data, wipe emulator storage, replace the debug signing key, or recreate
  the AVD as a routine update or install-error workaround.
- Preserve data through database migrations when changing storage schemas.
- Report emulator checks only if actually performed. If Windows restart or another
  prerequisite blocks the emulator, state the blocker and the checks still pending.
- Prepare the final release APK only after design and core functionality have been
  checked on the emulator AND the user explicitly says to prepare the APK.
  Until then, keep the distributed version unchanged and record changes under
  Unreleased. The daily launch script builds debug only.
- See `ANDROID-DEVELOPMENT.md` for setup and operation.
