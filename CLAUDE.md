# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Single-module Android app "Golden Nile" (Gradle project name), a Jetpack Compose slot/levels game with an Egyptian theme. Root package is `com.com2usholdings.starsailors.android.google.global.nor` — this differs from the project display name and is also the `applicationId`; it's intentional (re-skin build), so do not "correct" it.

- Kotlin 2.2.10, Compose BOM 2026.02.01, AGP 9.1.1
- `compileSdk` 36.1, `minSdk` 24, `targetSdk` 36, JVM 11
- Version catalog: `gradle/libs.versions.toml`
- Firebase (BoM, Analytics, Crashlytics, Messaging), Google Mobile Ads, Core SplashScreen, Navigation Compose

## Build / run commands

```
./gradlew assembleDebug            # build debug APK
./gradlew installDebug             # install on connected device/emulator
./gradlew bundleRelease            # build release .aab (see proguard note below)
./gradlew test                     # JVM unit tests (app/src/test)
./gradlew connectedAndroidTest     # instrumented tests (needs device/emulator)
./gradlew :app:testDebugUnitTest --tests "com.com2usholdings...ExampleUnitTest.addition_isCorrect"
./gradlew lint
./gradlew clean
```

Test sources are scaffolding only (`ExampleUnitTest`, `ExampleInstrumentedTest`); there is no real test suite yet.

### Release bundling quirk

`app/build.gradle.kts` registers a `removeProguardMap` task wired as `finalizedBy` on `bundleRelease`. It rewrites `app/release/app-release.aab` to strip `BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map` and writes it out as `app/release/proguard.map`. Additionally, `uploadCrashlyticsMappingFileRelease` is disabled. If you change release packaging, keep these in mind — expect the finished `.aab` to be post-processed in place.

Both `debug` and `release` buildTypes have `isMinifyEnabled = true` (unusual — debug is also minified). R8 rules live in `app/proguard-rules.pro`.

## Architecture

### Two-activity launch flow

The launcher activity is **`LoadingActivity`** (not `MainActivity`). It installs the splash screen and hosts `LoadingGraph` (`navigation/LoadingNav.kt`), a tiny nav graph with two destinations:

- `CONNECT` — shown when `Context.isEgyptConnected()` (any active network transport) returns false; offers retry via `ConnectScreen`.
- `LOADING` — shown once online; waits 2s then `startActivity(MainActivity)` + `finish()`.

`MainActivity` then hosts `AppNavHost` (`navigation/AppNavHost.kt`) for the real app. Two separate activities mean any state that must survive the loading→main handoff has to live in `PreferencesManager` or be re-derived — don't try to pass it through in-memory.

### Nav graph (MainActivity)

Routes defined in `navigation/NavRoutes.kt`. `AppNavHost` wires: `MENU` → `LEVELS` → `GAME` (with int `level` arg via `NavRoutes.game(n)`) plus `SETTINGS`, `HOW_TO_PLAY`, `PRIVACY`, `LEADERBOARD`. Transitions are fade in/out. The `onExitApp` callback from `MainActivity` is `finish()` and is passed into `MenuScreen`.

Note: `ui/screens/GameScreen.kt` is currently an empty composable stub — the game logic is not implemented yet. Adding gameplay goes there.

### Audio

`audio/AudioManager.kt` defines:
- `GameAudioController` interface with no-op defaults,
- `NoopAudioController` for previews/tests,
- `GameAudioManager` — real impl owned by `MainActivity`; `MediaPlayer` for looped `game_music.mp3` and a `SoundPool` for short sfx (`slot_rounded`, `level_win`, `level_lose`).

It's exposed to the Compose tree via `staticCompositionLocalOf { NoopAudioController }` (`LocalAudioManager`) and bound to the current lifecycle by `BindAudioToLifecycle` (started/stopped on `ON_START`/`ON_STOP`). To use from a screen: `val audio = LocalAudioManager.current; audio.playWin()`. The manager reads `PreferencesManager.musicEnabled`/`soundEnabled` itself — callers just call `setMusicEnabled/setSoundEnabled` on the controller and don't touch prefs directly.

`MainActivity.onDestroy` calls `audioManager.release()`. Previews get `NoopAudioController` for free.

### Persistence

`data/PreferencesManager.kt` — thread-safe lazy singleton over SharedPreferences (`pyramid_riches_prefs`). Holds: music/sound toggles, unlocked level (1..`TOTAL_LEVELS` = 33), per-level best score, global best score, total wins/losses. `registerWin(level, score)` is the canonical progression hook — it increments wins, updates best, stores per-level best, and unlocks `level + 1` (but only when `level == unlockedLevel`, so completing an earlier level does not re-advance progression). `registerLoss` updates stats/per-level best without unlocking. `totalPlayerScore()` sums per-level bests for the leaderboard. Prefer this API over reading/writing keys directly.

### UI conventions

- `ui/theme/Theme.kt` — `GoldenNileTheme`. `MainActivity` always calls it with `dynamicColor = false, darkTheme = true`. Dynamic Material You is wired but intentionally disabled. Default color scheme is the Compose template purple; the actual palette lives in ad-hoc `EgyptGold` / `EgyptShadow` / etc. constants in `ui/theme/Color.kt`, not in `MaterialTheme.colorScheme`.
- `ui/theme/Type.kt` — all text uses `EgyptFontFamily` loaded from `res/font/font.ttf`. Call sites typically bypass `MaterialTheme.typography` and hand-roll `TextStyle(fontFamily = EgyptFontFamily, …)` — match that style rather than introducing theme typography unless the change is broad.
- `ui/components/` — reusable building blocks:
  - `EgyptBackground { … }` — full-screen `bg.png` + animated radial gold glow + optional dim scrim. Most screens wrap their content in this.
  - `MainButton` / `IconImageButton` — tappable image-backed buttons. Both use the `pressableWithCooldown` modifier.
  - `ScreenTitle` / `EgyptBodyText` — styled text.
  - `EgyptPopup`, `PausePopup`, `ResultPopup` — modal overlay + `popup_1.png` card.
  - `GameHud`, `ScoreChip` — top-of-screen level/score row.
- `ui/modiefier/Pressable.kt` (the directory name is misspelled — keep it as-is to avoid a mass rename). `Modifier.pressableWithCooldown(cooldownMillis = 1000L, …)` scales the element on press and debounces clicks. Use this instead of raw `clickable` for tappable UI — it's the project's standard press feel and gates double-taps.

### Input quirk: multi-touch is blocked globally

`MainActivity.dispatchTouchEvent` cancels and swallows any gesture where `pointerCount > 1`. All input is effectively single-touch. If you add gestures that need two fingers (pinch, two-finger swipe), you'll need to revisit this.

### Window / system bars

Both activities hide system bars via `WindowInsetsControllerCompat`, re-hiding them on `onWindowFocusChanged`. Both are locked to portrait (`screenOrientation="portrait"` in the manifest).

### AdMob / Firebase

`AndroidManifest.xml` uses the **Google test AdMob App ID** `ca-app-pub-3940256099942544~3347511713`. Swap for a real ID before shipping. `google-services.json` is committed at `app/google-services.json`. `usesCleartextTraffic="true"` is set.

## Things to know before editing

- There is no version control in this working copy (not a git repo). Standard git workflows (branches, commits) won't work here unless you `git init` first.
- `GameScreen` is empty. If a task mentions "the game," assume you're building it from scratch, not modifying existing logic.
- Don't add `clickable`/`Button` with custom ripple — use `pressableWithCooldown` + the image-button components so press feel stays consistent.
- Don't read/write raw SharedPreferences keys — go through `PreferencesManager`.
- Audio-touching code must work under `NoopAudioController` (previews use it) — don't downcast `LocalAudioManager.current` to `GameAudioManager`.
