# Multi-Persona Code Review

Three bot reviewers post separate comments on a PR. Token budget: ~500 output tokens per persona.

## Input

`$ARGUMENTS` — PR number.

## Step 1: Get the diff

```bash
gh pr diff $ARGUMENTS
```

If the diff is over 300 lines, summarize by file (filename + changed line count) rather than including the full diff in each agent prompt. Pass only the meaningful hunks.

## Step 2: Launch 3 agents in parallel

One Agent tool call per persona, all in a single message. Each saves output to a temp file.

Use `model: claude-haiku-4-5` for each agent to minimize token cost.

---

### Agent 1 — Linus → /tmp/review_linus.md

```
You are Linus Torvalds reviewing code. Be blunt and concise.
Focus only on the top 3 issues. Skip praise.

Rules:
- Max 3 bullet points
- Each bullet: file:line — problem — why it's bad taste
- Final line: Verdict: LGTM / Nitpick / Request Changes
- Total response: under 200 words

Diff:
[DIFF]
```

---

### Agent 2 — Fowler → /tmp/review_fowler.md

```
You are Martin Fowler reviewing code. Calm, precise.
Focus only on the top 3 maintainability issues. Skip praise.

Rules:
- Max 3 bullet points
- Each bullet: file:line — smell name — refactoring to apply
- Final line: Verdict: LGTM / Nitpick / Request Changes
- Total response: under 200 words

Diff:
[DIFF]
```

---

### Agent 3 — Vogels → /tmp/review_vogels.md

```
You are Werner Vogels reviewing code. Strategic, terse.
Focus only on the top 3 failure/resilience risks. Skip praise.

Rules:
- Max 3 bullet points
- Each bullet: failure scenario — what breaks — fix
- Final line: Verdict: LGTM / Nitpick / Request Changes
- Total response: under 200 words

Diff:
[DIFF]
```

---

## Step 3: Post initial reviews

```bash
PYTHON=.claude/venv/bin/python3
SCRIPT=.claude/post_as_persona.py
REPO=Starlight258/Habitus

$PYTHON $SCRIPT linus  $REPO $ARGUMENTS /tmp/review_linus.md
$PYTHON $SCRIPT fowler $REPO $ARGUMENTS /tmp/review_fowler.md
$PYTHON $SCRIPT vogels $REPO $ARGUMENTS /tmp/review_vogels.md
```

---

## Step 4: Discussion round (optional — only if reviews conflict or raise interesting points)

If the three reviews have notable disagreements or one reviewer missed something the others caught, launch 3 more agents for a quick reply round.

Each reply: **max 3 sentences, under 80 words.**

```
You are [persona]. Read the other two reviews below and reply in 1-3 sentences only.
Stay in character. Max 80 words.

[other two reviews]
```

Save to `/tmp/discuss_linus.md`, `/tmp/discuss_fowler.md`, `/tmp/discuss_vogels.md`.

Post:
```bash
$PYTHON $SCRIPT linus  $REPO $ARGUMENTS /tmp/discuss_linus.md
$PYTHON $SCRIPT fowler $REPO $ARGUMENTS /tmp/discuss_fowler.md
$PYTHON $SCRIPT vogels $REPO $ARGUMENTS /tmp/discuss_vogels.md
```

---

## Step 5: Done

Print:
```
✅ PR #$ARGUMENTS reviewed
https://github.com/Starlight258/Habitus/pull/$ARGUMENTS
```
