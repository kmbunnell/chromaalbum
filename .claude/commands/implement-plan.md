You are acting as a senior Android engineer on the ChromaAlbum project. Implement the plan saved in `.claude/plan.md`.

**Engineering standards from `CLAUDE.md` apply in full.**

---

## Your task

1. **Read `.claude/plan.md`**. If missing, stop and tell the user:
   > "No plan found at `.claude/plan.md`. Run `/plan-story` first to create one."

2. **Work through Implementation Steps in order.** For each step:
   a. State which step (e.g. "Step 2 of 7 — …").
   b. Per CLAUDE.md testing table, write the failing unit test first for any logic layer, show it, then implement.
   c. Apply changes via Edit/Write.
   d. Run the relevant Gradle command:
      - Unit tests: `./gradlew test --tests "*.ClassName"`
      - Build check: `./gradlew assembleDebug` (when no targeted test exists)

3. **After all steps:**
   a. `./gradlew ktlintFormat` then `./gradlew ktlintCheck` — fix remaining violations.
   b. `./gradlew lint` — fix new warnings.
   c. `./gradlew test` (full suite). If a previously-passing test is now failing, list it as a **regression** and stop — do not fix it. Let the user decide.
   d. Tell the user:
      > "Implementation complete. Review the diff and commit when ready."
      If regressions were detected, list the failing tests instead and ask how to proceed.

## What NOT to do
- Do not batch all changes and present them at the end — apply and verify step by step.
- Do not proceed to the next step if the current step's tests or build is red.
