#!/bin/bash
set -e

PR=${1:?Usage: ./scripts/review-pr.sh <PR_NUMBER>}
REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)

echo "==> PR #$PR 리뷰 시작 ($REPO)"

# Setup venv
if [ ! -f .claude/venv/bin/python3 ]; then
  echo "==> Python 가상환경 설치 중..."
  python3 -m venv .claude/venv
  .claude/venv/bin/pip install PyJWT cryptography requests -q
fi

# PEM 키 경로 설정 (환경변수 우선, 기본값은 ~/.claude/pem/)
LINUS_PEM=${LINUS_PEM_PATH:-$HOME/.claude/pem/linus.pem}
FOWLER_PEM=${FOWLER_PEM_PATH:-$HOME/.claude/pem/fowler.pem}
VOGELS_PEM=${VOGELS_PEM_PATH:-$HOME/.claude/pem/vogels.pem}

for key_file in "$LINUS_PEM" "$FOWLER_PEM" "$VOGELS_PEM"; do
  if [ ! -f "$key_file" ]; then
    echo "ERROR: PEM 파일을 찾을 수 없습니다: $key_file"
    echo "환경변수로 지정하거나 ~/.claude/pem/ 에 저장하세요:"
    echo "  LINUS_PEM_PATH=<path> FOWLER_PEM_PATH=<path> VOGELS_PEM_PATH=<path> $0 $PR"
    exit 1
  fi
done

# personas.json 생성
cat > .claude/personas.json << EOF
{
  "linus":  { "app_id": "3576024", "installation_id": "128879572", "pem_path": "$LINUS_PEM" },
  "fowler": { "app_id": "3576053", "installation_id": "128880139", "pem_path": "$FOWLER_PEM" },
  "vogels": { "app_id": "3576069", "installation_id": "128880656", "pem_path": "$VOGELS_PEM" }
}
EOF

# PR diff 가져오기
echo "==> PR diff 가져오는 중..."
gh pr diff $PR > /tmp/diff.txt

DIFF=$(cat /tmp/diff.txt)

# ── Round 1: 초기 리뷰 생성 ──────────────────────────────────────────────────
echo "==> [1/6] Linus 리뷰 생성 중..."
claude -p "You are Linus Torvalds. Blunt. Focus: bad taste, unnecessary complexity, wrong abstractions.
Review this diff and respond in EXACTLY this format (under 120 words total, nothing else):

## 🐧 Linus Torvalds
**Verdict:** LGTM | Nitpick | Request Changes
- \`file:line\` — [issue] — [why bad taste]
(max 3 bullets; if nothing notable write \"No bad taste detected.\")

Diff to review:
$DIFF" > /tmp/review_linus.md

echo "==> [2/6] Fowler 리뷰 생성 중..."
claude -p "You are Martin Fowler. Calm. Focus: code smells, testability, maintainability.
Review this diff and respond in EXACTLY this format (under 120 words total, nothing else):

## 📚 Martin Fowler
**Verdict:** LGTM | Nitpick | Request Changes
- \`file:line\` — [smell name] — [refactoring to apply]
(max 3 bullets; if nothing notable write \"No smells detected.\")

Diff to review:
$DIFF" > /tmp/review_fowler.md

echo "==> [3/6] Vogels 리뷰 생성 중..."
claude -p "You are Werner Vogels. Strategic. Focus: failure modes, missing timeouts/retries, observability.
Review this diff and respond in EXACTLY this format (under 120 words total, nothing else):

## ☁️ Werner Vogels
**Verdict:** LGTM | Nitpick | Request Changes
- [failure scenario] — [impact] — [fix]
(max 3 bullets; if nothing notable write \"No resilience gaps detected.\")

Diff to review:
$DIFF" > /tmp/review_vogels.md

# 초기 리뷰 포스팅
echo "==> 초기 리뷰 포스팅 중..."
PY=.claude/venv/bin/python3
SC=.claude/post_as_persona.py

for persona in linus fowler vogels; do
  FILE="/tmp/review_${persona}.md"
  if [ -f "$FILE" ]; then
    $PY $SC $persona $REPO $PR $FILE
    echo "  ✓ $persona 리뷰 포스팅 완료"
  fi
done

# ── Round 2: 토론 답변 생성 ────────────────────────────────────────────────────
echo "==> [4/6] Linus 답변 생성 중..."
LINUS_REVIEW=$(cat /tmp/review_linus.md)
FOWLER_REVIEW=$(cat /tmp/review_fowler.md)
VOGELS_REVIEW=$(cat /tmp/review_vogels.md)

claude -p "You are Linus Torvalds. You've read Martin Fowler's review below.
Respond in 1-2 blunt sentences. Stay in character.
Format (nothing else):
🐧 **Linus** (responding to Fowler):
[1-2 sentences]

Fowler's review:
$FOWLER_REVIEW" > /tmp/discuss_linus.md

echo "==> [5/6] Fowler 답변 생성 중..."
claude -p "You are Martin Fowler. You've read both Linus's and Werner's reviews below.
Respond in 1-2 diplomatic sentences. Stay in character.
Format (nothing else):
📚 **Martin** (in reply):
[1-2 sentences]

Linus's review:
$LINUS_REVIEW

Werner's review:
$VOGELS_REVIEW" > /tmp/discuss_fowler.md

echo "==> [6/6] Vogels 답변 생성 중..."
claude -p "You are Werner Vogels. You've read both reviews below.
Add one operational angle they missed, in 1-2 sentences.
Format (nothing else):
☁️ **Werner** (on the operational side):
[1-2 sentences]

Linus's review:
$LINUS_REVIEW

Fowler's review:
$FOWLER_REVIEW" > /tmp/discuss_vogels.md

# 토론 답변 포스팅
echo "==> 토론 답변 포스팅 중..."
for persona in linus fowler vogels; do
  FILE="/tmp/discuss_${persona}.md"
  if [ -f "$FILE" ]; then
    $PY $SC $persona $REPO $PR $FILE
    echo "  ✓ $persona 답변 포스팅 완료"
  fi
done

echo ""
echo "✅ 리뷰 완료! PR #$PR에서 확인하세요."
