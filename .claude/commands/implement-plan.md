You are acting as a senior Android engineer on the ChromaAlbum project. Your job is to implement the plan saved in `.claude/plan.md`.

**Engineering standards from CLAUDE.md apply in full:**
- TDD: write the failing test first, then the implementation — for every step that touches logic
- Unidirectional data flow; stateless composables observe ViewModel `StateFlow`
- No third-party paid dependencies — first-party Jetpack/Google only
- MVVM + Repository; single source of truth in Room
- All palette/bitmap work on `Dispatchers.Default`, cancelled with `viewModelScope`
- Type-safe Compose Navigation (Kotlin serialization)
- `takePersistableUriPermission` before inserting any `content://` URI into Room

---

## Your task

1. **Read `.claude/plan.md`** to load the approved plan. If the file does not exist, stop and tell the user:
   > "No plan found at `.claude/plan.md`. Run `/plan-story` first to create one."

2. **Work through the Implementation Steps in order.** For each step:
   a. State which step you are on (e.g. "Step 2 of 7 — …").
   b. If the step involves logic (ViewModel, Repository, PaletteEngine, Mapper, DAO), **write the test file first**, show it to the user, then implement.
   c. Apply the changes using Edit/Write tools.
   d. After each step, run the relevant Gradle command and report the result:
      - Unit tests: `./gradlew test --tests "*.ClassName"`
      - Build check: `./gradlew assembleDebug` (use when no targeted test exists)
   e. If a test or build fails, fix it before moving to the next step.

3. **After all steps are complete:**
   a. Run `./gradlew lint` and fix any new warnings introduced by this change.
   b. Run `./gradlew test` (full suite) and confirm it is green.
   c. Tell the user:
      > "Implementation complete. All tests pass. Review the changes above, then commit when ready."

## Layer-specific rules

| Layer | Rule |
|---|---|
| Room Entity/DAO | Write in-memory Room test first (cascade deletes, Flow emissions, JSON round-trip) |
| Repository | Mock DAO with Mockito; assert debounce and regen trigger behaviour |
| PaletteEngine | JUnit4 with known-color bitmaps; assert weighted-average output |
| ColorSchemeMapper | JUnit4; assert M3 role assignments pass WCAG AA (4.5:1) contrast |
| ViewModel | JUnit4 + Turbine; assert `loading → success → error` state transitions |
| Composables | No logic in composables; stateless — no unit tests required unless a composable has branching display logic |

## What NOT to do
- Do not skip writing tests for logic layers.
- Do not batch all changes and present them at the end — apply and verify step by step.
- Do not add comments explaining what code does; only add a comment when the WHY is non-obvious.
- Do not introduce abstractions or refactoring beyond what the plan requires.
- Do not proceed to the next step if the current step's tests or build is red.
