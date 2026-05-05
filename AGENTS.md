# Agent Guide

This repository uses this file as the model-neutral agent guide. Claude-specific slash commands live under `.claude/`, while Codex and other coding agents should use this document for shared project rules.

## Project

Habitus is a Spring Boot 3.5.9 / Java 21 backend for tracking activities, activity histories, and recommendations across seven capital types. The code follows Clean Architecture:

```text
Presentation -> Application -> Domain <- Infrastructure
```

Keep domain logic framework-free. Spring, JPA, HTTP DTOs, and persistence mapping belong outside the domain layer.

## Working Rules

- Prefer existing package boundaries, naming, DTO style, exception handling, and tests.
- Do not rewrite unrelated code or revert user changes.
- Keep changes scoped to the requested feature, fix, or review.
- Add or update tests when behavior changes.
- For API changes, verify request/response shape, status codes, validation errors, and persistence behavior.
- For recommendation logic, check algorithm correctness, edge cases, and performance before style issues.

## Common Commands

```bash
./gradlew test
./gradlew bootRun
```

Use `./gradlew test` as the default verification command after code changes. If the change is narrow and tests are expensive or unavailable, state exactly what was and was not run.

## Existing Claude Workflows

- `/spec`: create `/tmp/spec.md` from a request and stop for approval.
- `/implement`: run Codex against `/tmp/spec.md`.
- `/feature`: interactive design -> implementation -> PR flow.
- `/review`: existing PR persona review flow that posts comments to GitHub.
- `/diff-review`: local code review flow for explaining an implementation diff and sorting review focus by importance.

Use `/diff-review` when the goal is to understand or review the current diff without posting anything externally.

## Code Review Protocol

When asked to review implemented code or explain a diff:

1. Gather context with `git status --short`, `git diff --stat`, and the relevant full diff.
2. Explain what changed in plain language before listing issues.
3. Sort findings by impact, not file order.
4. Use this priority scale:
   - `P0 Blocker`: build failure, data loss, security issue, broken core flow.
   - `P1 High`: likely production bug, API contract break, migration/persistence risk, missing critical validation.
   - `P2 Medium`: maintainability, incomplete tests for changed behavior, unclear boundaries.
   - `P3 Low`: naming, formatting, local cleanup, non-blocking polish.
5. For each finding, include `file:line`, the risk, why it matters, and the smallest useful fix or check.
6. End with the most important verification steps in priority order.

Do not post review comments to GitHub unless the user explicitly asks for PR posting.

## Output Style

Be concise and concrete. Lead with findings for reviews. For implementation summaries, state changed files, behavior changes, and verification. Avoid generic praise.
