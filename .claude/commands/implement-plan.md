You are acting as a senior Android engineer on the ChromaAlbum project. Implement the plan saved in `.claude/plan.md`.

**Engineering standards from `CLAUDE.md` apply in full.**

---

## Your task

1. **Read `.claude/plan.md`**. If missing, stop:
   > "No plan found. Run `/plan-story` first."

2. **Step 0 — Spotless baseline**: Run `./gradlew :app:spotlessCheck`. Record pre-existing violations — do not fix them now.

3. **Work through Implementation Steps in order.** For each step:
   a. State step (e.g. "Step 2 of 7 — …").
   b. Write the failing unit test first (per CLAUDE.md testing table), show it, then implement.
   c. Apply changes via Edit/Write.
   d. **Micro-checklist** before moving on:
      - Coroutines? → CE guard present (`catch (e: CancellationException) { throw e }` before any swallowing `catch (e: Exception)`)?
      - New class? → Verified no existing class/component covers this?
      - ViewModel touched? → Zero business logic (delegates to UseCase only)?
   e. Run `./gradlew :app:testDebugUnitTest --tests "*.ClassName"` (or `:app:assembleDebug` if no targeted test).

4. **After all steps:**
   a. `./gradlew :app:spotlessApply` then `./gradlew :app:spotlessCheck` — fix only violations introduced by this branch.
   b. `./gradlew :app:lint` — fix new warnings.
   c. `./gradlew :app:testDebugUnitTest` (full suite). If a previously-passing test fails, list it as a **regression**, stop, and ask how to proceed.
   d. Tell the user:
      > "Implementation complete. Review the diff and commit when ready."

## What NOT to do
- Do not batch all changes — apply and verify step by step.
- Do not proceed if the current step's tests or build is red.
- After 3 failed fix attempts on the same test, stop and report the full error. Do not attempt a 4th fix.
