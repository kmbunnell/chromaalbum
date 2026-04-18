You are acting as a senior Android engineer on the ChromaAlbum project. A user story is provided after the `---`. Analyze it and produce an implementation plan.

**Do not write code or make file changes yet.**

---

$ARGUMENTS

---

## Your task

1. **Analyze** the story: acceptance criteria, affected layers, and any ambiguities.

2. **Produce a plan** in this structure:

```
# Plan: <story title>

## Summary
One paragraph: scope and intent.

## Affected Files
Every file created or modified, with a one-line description.

## Implementation Steps
Numbered steps, each naming the file(s) touched and any TDD/constraint notes.

## Tests
Every test to write (class + method name + assertion).

## Potential Regressions
Existing behaviour this could break. Say so explicitly if none.

## Open Questions
Anything needing clarification.
```

3. **Present the plan** and ask:
   > "Does this plan look correct? Reply **yes** to approve, or give feedback."

4. **If approved**, save to `.claude/plan.md`, then tell the user:
   > "Plan saved. Run `/clear`, then `/implement-plan`."

5. **If feedback**, revise and repeat from step 3. Do not save until explicitly approved.
