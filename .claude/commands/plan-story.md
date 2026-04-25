You are acting as a senior Android engineer on the ChromaAlbum project. A user story is provided after the `---`. Analyze it and produce an implementation plan.

**Do not write code or make file changes yet.**

---

$ARGUMENTS

---

## Your task

1. **Analyze** the story: acceptance criteria, affected layers, ambiguities.

2. **CLAUDE.md constraint check** — before drafting, verify:
   - **DRY**: Search `ui/components/`, mappers, and extensions for reusable code. List what exists vs. what must be created.
   - **Layers**: Business logic → UseCase only. ViewModels forward intents only. Repositories are data-only.
   - **Coroutines**: Any async code? Flag CE guard requirement on each affected class.
   - **Tests**: Unit tests only. Pure wiring (DI, annotations, manifest) needs no tests.

3. **Produce a plan:**

```
# Plan: <story title>

## Summary
One paragraph: scope and intent.

## Affected Files
Every file created or modified, one-line description. Note layer.

## Implementation Steps
Numbered steps. Each names file(s) touched, TDD note, and any CE/DRY/layer constraint.

## Tests
Each entry: `ClassName#methodUnderTest_givenX_whenY_thenZ — one-line GWT assertion`

## Potential Regressions
Existing behaviour this could break. Say "none" if clean.

## Open Questions
Anything needing clarification.
```

4. **Present the plan** and ask:
   > "Does this plan look correct? Reply **yes** to approve, or give feedback."

5. **If approved**, save to `.claude/plan.md`, then say:
   > "Plan saved. Run `/clear`, then `/implement-plan`."

6. **If feedback**, revise and repeat from step 4. Do not save until explicitly approved.
