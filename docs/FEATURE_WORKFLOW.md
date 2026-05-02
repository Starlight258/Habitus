# Feature Workflow Guide

이 가이드는 `design` → `implement` → `PR 생성`까지 자동으로 진행하는 워크플로우를 설명합니다.

## 개요

일반적으로 새로운 기능을 추가할 때:

1. `/design` 스킬로 설계 명세 작성
2. `/implement` 스킬로 명세를 기반으로 구현
3. Git commit 생성 및 PR 오픈

이 세 단계를 자동화하는 워크플로우입니다.

## 사용 방법

### Option 1: Bash 스크립트 (추천)

```bash
./scripts/feature-workflow.sh "Feature Description" "branch-name"
```

**예시:**

```bash
./scripts/feature-workflow.sh \
  "Add activity template management API" \
  "feat/add-activity-template-management-api"
```

### Option 2: Node.js 스크립트

```bash
node scripts/feature-workflow.js "Feature Description" "branch-name"
```

### Option 3: 별칭 설정 (선택)

`.zshrc` 또는 `.bashrc`에 다음을 추가하면 더 쉽게 사용할 수 있습니다:

```bash
# Habitus feature workflow
alias feature-workflow='bash ~/path/to/Habitus/scripts/feature-workflow.sh'
```

그 후:

```bash
feature-workflow "Feature Description" "branch-name"
```

## 워크플로우 단계

### 1. 브랜치 생성
- 지정한 브랜치명으로 새 브랜치 생성 및 체크아웃
- 이미 존재하면 기존 브랜치로 전환

### 2. 설계 단계 (`/design` 실행)
- `/tmp/spec.md` 생성
- 명세 문서 작성

### 3. 구현 단계 (`/implement` 실행)
- 명세에 따라 코드 생성
- `/tmp/reply_*.txt`에 구현 요약 저장

### 4. Commit 생성
```
feat: Feature Description

- Implementation details
- Key changes

Co-Authored-By: Claude Haiku 4.5 <noreply@anthropic.com>
```

### 5. PR 생성
- GitHub의 main으로 PR 오픈
- PR 제목: `feat: Feature Description`
- PR 본문에 자동으로 체크리스트 포함

## 필수 사항

- Claude Code CLI (`claude` 명령)
- Git
- (선택) GitHub CLI (`gh` 명령) - PR 자동 생성용

### gh CLI 설치

```bash
# macOS
brew install gh

# 또는 다른 OS
https://github.com/cli/cli#installation
```

## 환경 변수

스크립트는 다음 환경 변수를 사용할 수 있습니다:

- `REPO_ROOT`: 저장소 루트 경로 (자동 감지됨)

## 주의 사항

1. **브랜치명**: 컨벤션에 맞는 이름 사용 (예: `feat/`, `fix/`, `docs/`)
2. **설명문**: 명확하고 간결한 설명 (50자 이내 추천)
3. **실행 환경**: 저장소 루트에서 실행하거나 상대경로 사용
4. **PR 생성**: `gh` CLI가 없으면 수동으로 PR 생성 필요

## 예시

### 예시 1: Activity Management API 추가

```bash
./scripts/feature-workflow.sh \
  "Add activity template management API" \
  "feat/add-activity-template-management-api"
```

결과:
- `/tmp/spec.md` 생성 (설계 명세)
- 코드 구현됨
- `feat/add-activity-template-management-api` 브랜치 생성 및 push
- GitHub에 PR 자동 생성

### 예시 2: 버그 수정

```bash
./scripts/feature-workflow.sh \
  "Fix recommendation algorithm edge case" \
  "fix/recommendation-algorithm-edge-case"
```

## 문제 해결

### `/tmp/spec.md` 생성 실패

- Claude Code CLI가 설치되어 있는지 확인
- `/design` 스킬이 올바르게 작동하는지 확인 (수동으로 실행해보기)

### 구현이 안됨

- 설계 명세 (`/tmp/spec.md`)가 제대로 생성되었는지 확인
- Claude가 충분한 권한을 가지고 있는지 확인
- 코드 생성 후 `/tmp/reply_*.txt` 확인

### PR 생성 실패

- `gh` CLI 설치 여부 확인
- GitHub 인증 확인: `gh auth status`
- 수동으로 PR 생성: GitHub 웹사이트에서 브랜치를 main으로 PR 생성

## 커스터마이징

### 커밋 메시지 변경

`feature-workflow.sh` 또는 `.js` 파일에서 `COMMIT_MSG` 또는 `commitMsg` 부분을 수정합니다.

### PR 템플릿 변경

스크립트에서 `--body` 부분을 수정하여 PR 본문 커스터마이징 가능합니다.

### 자동 머지 추가

PR 생성 후 자동 머지를 원하면:

```bash
gh pr merge <PR_URL> --auto
```

스크립트에 추가 가능합니다.

## 참고

- 워크플로우 실행 중 에러 발생 시 해당 단계에서 중단됩니다
- 각 단계의 출력은 터미널에 표시됩니다
- 모든 git 커맨드는 원본 저장소에 영향을 미칩니다 (커밋, 푸시 등)
