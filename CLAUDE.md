# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

ChromaAlbum — Android photo album app that auto-generates dynamic color themes from album photos using the Palette API. Target: API 26+, single-module.

## Engineering Standards

All code must be written to senior Android engineer quality:
- Follow Google's [Android Architecture Guidelines](https://developer.android.com/topic/architecture)
- Use Test-Driven Development: write the failing test first, then implement
- Unidirectional data flow; stateless composables observe ViewModel `StateFlow`
- No third-party paid dependencies — first-party Jetpack/Google only

## Commands

```bash
./gradlew test                          # Unit tests
./gradlew connectedAndroidTest          # Instrumented tests (device required)
./gradlew test --tests "*.ClassName"    # Single test class
./gradlew test --tests "*.Class.method" # Single test method
./gradlew assembleDebug                 # Build debug APK
./gradlew lint                          # Lint checks
./gradlew clean build                   # Clean rebuild
```

## Architecture

**Pattern:** Single-Activity, MVVM + Repository, unidirectional data flow.

```
com.example.chromaalbum
├── data/
│   ├── local/        # Room DB, DAOs, Entities (Album, Photo)
│   ├── repository/   # AlbumRepository — single source of truth
│   └── model/        # Domain models
├── palette/
│   ├── PaletteEngine.kt    # Blended multi-photo palette algorithm
│   └── BlendedPalette.kt   # Data class for 6-swatch palette
├── ui/
│   ├── theme/        # DynamicTheme, ColorSchemeMapper (palette → M3 roles)
│   ├── home/         # HomeScreen + HomeViewModel
│   ├── album/        # AlbumDetailScreen + AlbumViewModel
│   ├── viewer/       # PhotoViewerScreen + ViewerViewModel
│   └── components/   # Shared composables
├── di/               # Hilt modules
└── ChromaAlbumApp.kt
```

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
- Fallback chain when a swatch is absent: `vibrant → muted → darkMuted → #6750A4`
- All processing on `Dispatchers.Default`; cancels with `viewModelScope`

## Photo Storage

Store `content://` URIs from Android Photo Picker. Call `takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION)` **before** inserting into Room. Fall back to copying bytes to internal storage if persistence is unsupported. Validate URIs on album open via `ContentResolver.query()`; show broken-image placeholder + Snackbar for invalid URIs.

## Testing Requirements

Every feature requires tests written **before** implementation:

| Layer | Tool | Focus |
|---|---|---|
| `PaletteEngine` | JUnit4 | Given N bitmaps with known colors → assert weighted-average output |
| `ColorSchemeMapper` | JUnit4 | Given `BlendedPalette` → assert M3 role assignments pass contrast checks |
| `AlbumRepository` | JUnit4 + mock DAO | Photo add triggers regen; debounce works |
| ViewModels | JUnit4 + Turbine | `loading → success → error` state transitions |
| Room DAOs | In-memory Room | Cascade deletes, Flow emissions, `paletteJson` round-trip |
| Navigation | ComposeTestRule | Route args passed correctly, back nav works |
| Screenshot | Paparazzi/Roborazzi | Light + dark dynamic themes per screen |

## Navigation

Type-safe Compose Navigation (Kotlin serialization):
- `home` → `HomeScreen`
- `album/{albumId: Long}` → `AlbumDetailScreen`
- `viewer/{albumId: Long}/{startIndex: Int}` → `PhotoViewerScreen`

## Key Dependencies (libs.versions.toml)

Add as needed: `androidx.room`, `androidx.hilt`, `androidx.navigation-compose`, `coil3`, `androidx.palette`, `androidx.window` (WindowSizeClass), `kotlinx.serialization`, `app.cash.turbine`, `app.cash.paparazzi`.
