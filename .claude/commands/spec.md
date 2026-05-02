# Design Spec

Analyze the requirements and create a design spec for Codex to implement.

## Input

`$ARGUMENTS` — what to build (feature, fix, refactor, etc.)

## What makes a good spec

The implementer must be able to build it from this document alone, without asking questions.

A good spec satisfies all of the following:
- Clear on what is being built and what is explicitly out of scope
- Key design decisions with rationale (alternatives considered)
- Concrete file structure, interfaces, data models, and APIs
- Implementation order and dependencies
- Edge cases and known constraints

## Success criteria

When Codex implements from this spec, the user says "this is exactly what I wanted."

## spec.md format rules

spec.md is read by Codex (GPT-based). Write it in Codex-optimized format.

- **Language: English** — Korean = 2.59x more tokens for Claude; Codex is GPT-based (English-native)
- **Style: outcome-first** — define what good looks like + constraints. No step-by-step instructions.
- Specific enough that Codex can implement without follow-up questions

```
Goal: [what we're building and why]
Success looks like: [concrete outcome Codex can verify]
Constraints: [what NOT to do, hard boundaries]
Interfaces: [data models, APIs, file structure]
Edge cases: [known failure modes to handle]
```

## Instructions

1. Read `$ARGUMENTS` to understand the request
2. Explore relevant parts of the codebase to understand the current state
3. Write the spec to `/tmp/spec.md` (in English, Codex-optimized format)
4. Show the full spec to the user and stop

## Constraints

- Do NOT write implementation code
- Save the completed spec to `/tmp/spec.md` (in English)
- Show the spec to the user and **stop until they explicitly approve**
- If changes are requested, update `/tmp/spec.md` and show again

## Approval prompt

After showing the spec, say exactly:

> Please review the design spec above.
> Let me know if you'd like any changes.
> Once confirmed, run `/implement` to start Codex implementation.
