---
name: feature
description: Interactive feature workflow — gather requirements interactively (description, API list, branch name, context), then run design → implement → PR in sequence. Use when the user wants to build a new feature end-to-end.
allowed-tools: AskUserQuestion, Read, Write, Bash
---

You are running an interactive feature workflow. Follow each stage in order.

## Stage 1: Feature basics

Use AskUserQuestion with these two questions together:

**Q1 — Feature description:**
- header: "기능 설명"
- question: "어떤 기능을 만들까요?"
- options:
  - API 엔드포인트 추가 (새 REST API 구현)
  - UI 컴포넌트 추가 (새 화면 또는 컴포넌트)
  - 배치/스케줄러 추가 (백그라운드 작업)
  - 기타 (직접 입력)

**Q2 — API 개수:**
- header: "API 수"
- question: "구현할 API가 몇 개인가요? (없으면 0)"
- options:
  - 0개 (API 없음)
  - 1–2개
  - 3–4개
  - 5개 이상

## Stage 2: API 목록 수집

Based on the API count from Stage 1:

- If 0개: skip this stage.
- If 1–2개: Use AskUserQuestion with 2 questions:
  - Q1: header "API 1", question "첫 번째 API를 입력하세요", options: ["GET /api/... - 조회", "POST /api/... - 생성", "PUT /api/... - 수정", "DELETE /api/... - 삭제"]
  - Q2: header "API 2", question "두 번째 API를 입력하세요 (없으면 없음 선택)", options: ["없음", "GET /api/...", "POST /api/...", "PUT /api/..."]
- If 3–4개: Use AskUserQuestion with 4 questions (API 1–4), same pattern.
- If 5개 이상: Use AskUserQuestion to ask: header "API 목록", question "모든 API를 입력해주세요 (예: GET /api/x, POST /api/y)", options: ["직접 입력"]

For each question, the user will likely select "Other" to type the actual endpoint. Collect all non-"없음" answers.

## Stage 3: 추가 맥락 및 브랜치

Generate a suggested branch name from the feature description: `feat/<kebab-case>`.

Use AskUserQuestion with two questions:

**Q1 — 추가 맥락:**
- header: "추가 맥락"
- question: "DB 요구사항, 특수 로직 등 추가 맥락이 있나요?"
- options:
  - 없음
  - DB 스키마 변경 필요
  - 외부 API 연동 필요
  - 기타 (직접 입력)

**Q2 — 브랜치명:**
- header: "브랜치명"
- question: "브랜치명을 확인하세요. (변경하려면 기타 선택)"
- options:
  - `<suggested branch name>` (자동 생성)
  - 직접 입력

## Stage 4: 최종 확인

Print a summary:
```
────────────────────────────────────
기능:   <description>
브랜치: <branch>
APIs:
  • <api1>
  • <api2>
맥락:   <context or 없음>
────────────────────────────────────
```

Use AskUserQuestion:
- header: "확인"
- question: "이대로 시작할까요?"
- options:
  - 네, 시작 (설계 → 구현 → PR 진행)
  - 아니오, 취소

If 취소: stop here.

## Stage 5: 실행

1. Write spec to `/tmp/spec.md`:
   ```markdown
   # <description>

   ## API 목록
   <bullet list of collected APIs, or "없음">

   ## 구현 요구사항
   - 적절한 HTTP 메서드 사용
   - 요청/응답 DTO 작성
   - 입력 검증 및 에러 처리
   - 기존 시스템과의 통합

   <## 추가 맥락\n<context> — only if context was provided>

   ## 체크리스트
   - [ ] 모든 API 구현
   - [ ] 입력 검증
   - [ ] 에러 처리
   - [ ] 테스트 작성
   ```

2. Invoke `/design` skill with the feature description as the argument.

3. After design is approved, invoke `/implement` skill.

4. After implementation, run:
   ```bash
   git checkout -b <branch>
   git add -A
   git commit -m "feat: <description>"
   ```

5. Use AskUserQuestion to ask:
   - header: "PR 생성"
   - question: "PR을 바로 생성할까요?"
   - options:
     - 네, 지금 생성
     - 아니오, 나중에

   If yes, run `gh pr create` with appropriate title and body.
