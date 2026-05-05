# Diff Review Brief

Explain the implemented code diff and prioritize what reviewers should inspect.

## Input

`$ARGUMENTS` is optional.

- Empty: review the current working-tree diff against `HEAD`.
- Git ref or branch: review `git diff <ref>...HEAD`.
- PR number: if it is only digits and `gh` is available, review `gh pr diff <number>`.

## Intent

This command is for local review and reviewer handoff. Do not post comments to GitHub. If the user wants GitHub PR comments, use the existing `/review` flow instead.

## Steps

1. Inspect repository state:

   ```bash
   git status --short
   ```

2. Collect the diff:

   - For empty arguments:

     ```bash
     git diff --stat HEAD
     git diff --find-renames HEAD
     git ls-files --others --exclude-standard
     ```

   - For a git ref:

     ```bash
     git diff --stat "$ARGUMENTS"...HEAD
     git diff --find-renames "$ARGUMENTS"...HEAD
     ```

   - For a PR number:

     ```bash
     gh pr diff "$(echo "$ARGUMENTS" | tr -cd '0-9')"
     ```

3. If the diff is large, first summarize by file and changed line count, then inspect only the meaningful hunks needed to identify behavior and risk.

4. Produce the response in this exact structure:

   ```markdown
   ## Diff Summary
   - [What changed, grouped by behavior or layer]

   ## Review Priorities
   1. **P0/P1/P2/P3 - `file:line` - [short title]**
      Impact: [why this matters]
      Check/Fix: [specific thing to verify or change]

   ## Reviewer Checklist
   1. [Highest-value thing to inspect first]
   2. [Next most important check]
   3. [Next check]

   ## Verification
   - Ran: [commands run, or "Not run"]
   - Still needed: [commands or manual checks]
   ```

## Priority Rules

Sort by impact, not by file order.

- `P0 Blocker`: build failure, data loss, security issue, broken core flow.
- `P1 High`: likely production bug, API contract break, persistence/migration risk, missing critical validation.
- `P2 Medium`: maintainability issue, incomplete tests for changed behavior, unclear architecture boundary.
- `P3 Low`: naming, formatting, local cleanup, non-blocking polish.

If there are no issues, say that clearly and still provide the highest-value checklist and remaining verification.
