# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

ChromaAlbum — Android photo album app that auto-generates dynamic color themes from album photos using the Palette API. Target: API 26+, single-module.

## Guiding Principle

Always present the best architectural solution, not merely one that satisfies acceptance criteria. Evaluate trade-offs before proposing a plan — if a cleaner design exists, recommend it even if a simpler path would compile and pass tests.

## Engineering Standards

All code must be written to senior Android engineer quality:
- Follow Google's [Android Architecture Guidelines](https://developer.android.com/topic/architecture)
- Use Test-Driven Development: write the failing test first, then implement
- Unidirectional data flow; stateless composables observe ViewModel `StateFlow`
- No third-party paid dependencies — first-party Jetpack/Google only
- DRY: reuse before creating. Check `ui/components/`, mappers, and extensions before adding similar code.
- SOLID: single responsibility per class; depend on interfaces, not concretions (Hilt wires them).
- ViewModels expose state and forward intents only. **All business logic lives in use cases** under `domain/usecase/`.
- Never swallow `CancellationException` in coroutines. When a `catch (e: Exception)` block handles an error without rethrowing, precede it with `catch (e: CancellationException) { throw e }`. If the catch block unconditionally rethrows (e.g. cleanup then `throw e`), the separate CE guard is redundant — a single `catch (e: Exception)` suffices since CE is rethrown with everything else. Avoid `runCatching { }.getOrNull()` for the same reason.
- Do not create new packages or directories without asking first.
- Do not manually construct class instances in Compose screens — always inject
- Comments: do not restate what the code already says. Only comment when the WHY or algorithm is non-obvious. Public API functions with non-trivial behavior may use multi-line KDoc; inline comments must be one line.

## Commands

```bash
./gradlew test                          # Unit tests
./gradlew test --tests "*.ClassName"    # Single test class
./gradlew test --tests "*.Class.method" # Single test method
./gradlew assembleDebug                 # Build debug APK
./gradlew lint                          # Lint checks
./gradlew clean build                   # Clean rebuild
```

## Architecture

**Pattern:** Single-Activity, MVVM + UseCase + Repository, unidirectional data flow.

**Domain layer:** Domain services (e.g. `PaletteCompleter`) encapsulate pure domain logic shared across use cases and live alongside the interfaces they support (e.g. `domain/palette/`).

**Key data flow:** `Room Flow<Album>` → `ViewModel` parses `paletteJson` → `BlendedPalette` → `ColorSchemeMapper` → scoped `MaterialTheme(colorScheme)` wrapping album screens.

## Dynamic Theming

- `paletteJson` on the `Album` entity stores 6 swatches (vibrant, darkVibrant, lightVibrant, muted, darkMuted, lightMuted) as JSON
- `ColorSchemeMapper` produces both light and dark `ColorScheme` from palette swatches (see TDD §5.3 for role mapping table)
- Contrast enforcement: WCAG AA (4.5:1) minimum — correct failing pairs by lightening/darkening text color at runtime
- Theme transitions: `animateColorAsState` with 400ms tween on palette regeneration
- Dynamic theming is **not** Material You — derived entirely from photo palette

## Palette Engine

- Blends palettes across **all** photos (not just cover): downsample each to 100×100, extract per-photo Palette, weighted-average by pixel population per swatch category
- Photo additions debounced 2 seconds before triggering regeneration
- Albums >50 photos: sample 50 evenly-spaced
3- Raw extraction leaves nulls in `BlendedPalette`. `PaletteCompleter` fills structural gaps using HSL derivation from available swatches; if all swatches are null it passes through unchanged. `ColorSchemeMapper` owns the final hardcoded fallback (`#6750A4`) for the all-null case.
- All processing on `Dispatchers.Default`; cancels with `viewModelScope`

## Photo Storage

Store `content://` URIs from Android Photo Picker. Call `takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION)` **before** inserting into Room. If persistence fails, abort the batch (release any already-granted permissions) and surface an error; do not copy bytes. Validate URIs on album open via `ContentResolver.query()`; show broken-image placeholder + Snackbar for invalid URIs.

## Testing Requirements

Unit tests only (`./gradlew test`). No instrumented tests. Pure wiring — annotations, DI graph, manifest — does not require tests. All other features require tests written **before** implementation.

Structure every test **Given–When–Then**: arrange state, invoke the unit under test, assert outcome. One behaviour per test. Name methods `methodUnderTest_givenX_whenY_thenZ`.

Every test must assert behavior **we wrote**, not framework guarantees. Before adding a test, ask: if I deleted our code but left the framework intact, would this test fail? If not, drop it. Specifically:
- Do not test Room's FK CASCADE, empty-query defaults, or constraint enforcement — Room owns those.
- Do not test basic `@Insert`/`@Update`/`@Delete` mechanics on repository pass-throughs — those are annotation-processor guarantees, not our logic.
- Do not write two tests that would pass or fail together — merge them or drop the weaker one.


| Layer | Tool | Focus |
|---|---|---|
| `PaletteEngine` | JUnit4 | Given N bitmaps with known colors → assert weighted-average output |
| `ColorSchemeMapper` | JUnit4 | Given `BlendedPalette` → assert M3 role assignments pass contrast checks |
| `AlbumRepository` | JUnit4 + in-memory Room | Photo add triggers regen; debounce works; DAO correctness covered here, not in standalone DAO tests |
| Use cases | JUnit4 + mock repo | Business rules in isolation (debounce, sampling, fallback chain) |
| ViewModels | JUnit4 + Turbine | `loading → success → error` state transitions; delegates to use cases |

## Navigation

Type-safe Compose Navigation (Kotlin serialization):
- `home` → `HomeScreen`
- `album/{albumId: Long}` → `AlbumDetailScreen`
- `viewer/{albumId: Long}/{startIndex: Int}` → `PhotoViewerScreen`

## Key Dependencies (libs.versions.toml)

Add as needed: `androidx.room`, `androidx.hilt`, `androidx.navigation-compose`, `coil3`, `androidx.palette`, `androidx.window` (WindowSizeClass), `kotlinx.serialization`, `app.cash.turbine`, `robolectric` (JVM unit tests requiring Android Context, e.g. in-memory Room).
