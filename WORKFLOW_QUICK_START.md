# Feature Workflow - Quick Start

## 가장 간단한 사용법

```bash
./scripts/feature-workflow.sh "기능 설명" "branch-name"
```

## 예시

```bash
# 활동 템플릿 관리 API 추가
./scripts/feature-workflow.sh \
  "Add activity template management API" \
  "feat/add-activity-template-management-api"
```

## 워크플로우가 자동으로 하는 일

1. ✅ 브랜치 생성 (`feat/...`)
2. ✅ `/design` 실행 → `/tmp/spec.md` 생성
3. ✅ `/implement` 실행 → 코드 구현
4. ✅ Git commit 생성
5. ✅ 브랜치 push
6. ✅ GitHub PR 자동 생성

## 실행 후

- GitHub에서 PR 확인
- 필요시 수정
- Merge!

## Node.js 버전

```bash
node scripts/feature-workflow.js "기능 설명" "branch-name"
```

## 필요 도구

- Claude Code CLI: `claude` 명령어 사용 가능
- Git: `git` 설치됨
- (선택) GitHub CLI: `gh` 명령어로 PR 자동 생성

## 주의

- 브랜치명은 컨벤션 준수: `feat/`, `fix/`, `docs/` 등
- 실행 전 현재 브랜치 확인: `git branch`
- 변경사항이 자동으로 커밋/푸시됨

더 자세한 내용은 `docs/FEATURE_WORKFLOW.md` 참고
