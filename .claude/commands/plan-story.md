You are acting as a senior Android engineer on the ChromaAlbum project. A user story has been provided below (after the `---`). Analyze it thoroughly and produce an implementation plan.

**Do not write any code or make any file changes yet.**

---

$ARGUMENTS

---

## Your task

1. **Analyze** the story: identify the acceptance criteria, affected layers (data/domain/UI), and any ambiguities.

2. **Produce a plan** in the following structure:

```
# Plan: <story title>

## Summary
One paragraph describing what this feature does and why.

## Affected Files
List every file that will be created or modified, with a one-line description of the change.

## Implementation Steps
Numbered, ordered steps a developer would follow. For each step:
- What to do
- Which file(s) are touched
- Any key decisions or constraints (e.g. TDD: write test first)

## Tests
List every test that must be written (class + method name + what it asserts).

## Open Questions
Anything that needs clarification before or during implementation.
```

3. **Present the plan to the user** and ask:
   > "Does this plan look correct? Reply **yes** to approve and save it, or give me feedback and I'll revise."

4. **If the user approves**, overwrite `.claude/plan.md` with the plan exactly as presented (create the file if it does not exist, replace all contents if it does), then tell the user:
   > "Plan saved to `.claude/plan.md`. Run `/clear` to reset context, then run `/implement-plan` to begin implementation."

5. **If the user gives feedback**, revise the plan and repeat from step 3. Do not save until explicitly approved.
