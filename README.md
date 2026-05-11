# ChromaAlbum

An Android photo album app that auto-generates a Material 3 theme from the colors of the photos inside each album.

## Tech stack

Kotlin · Jetpack Compose · Material 3 · Hilt · Room · Navigation Compose · Coil 3 · AndroidX Palette · kotlinx.serialization · Coroutines/Flow.

## Build & run

Requires JDK 11 and the Android SDK (`compileSdk 36`, `minSdk 26`).

```bash
./gradlew assembleDebug
./gradlew test
```

## Contributing

See [`CLAUDE.md`](CLAUDE.md) for architecture, engineering standards, and the TDD policy.
