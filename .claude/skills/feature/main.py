#!/usr/bin/env python3

"""
Interactive Feature Workflow
입력받은 기능 설명과 맥락으로 설계 → 구현 → PR을 자동화합니다.
"""

import os
import sys
import subprocess
from pathlib import Path
from typing import List, Dict

# ANSI colors
class Colors:
    BLUE = '\033[34m'
    GREEN = '\033[32m'
    YELLOW = '\033[33m'
    RED = '\033[31m'
    RESET = '\033[0m'

def print_header():
    print(f"""
{Colors.BLUE}╔═══════════════════════════════════════╗{Colors.RESET}
{Colors.BLUE}║   Feature Workflow (Interactive)      ║{Colors.RESET}
{Colors.BLUE}║   설계 → 구현 → PR 자동화             ║{Colors.RESET}
{Colors.BLUE}╚═══════════════════════════════════════╝{Colors.RESET}
""")

def get_feature_description() -> str:
    """기능 설명 입력받기"""
    print(f"{Colors.BLUE}[1/5] 기능 설명을 입력하세요{Colors.RESET}")
    print("예: Add activity template management API\n")

    description = input(f"{Colors.GREEN}➤ {Colors.RESET}").strip()

    if not description:
        print(f"{Colors.RED}✗ 기능 설명은 필수입니다.{Colors.RESET}")
        sys.exit(1)

    return description

def get_api_list() -> List[str]:
    """API 목록 입력받기"""
    print(f"\n{Colors.BLUE}[2/5] 구현할 API들을 입력하세요{Colors.RESET}")
    print("(각 줄에 하나씩, 빈 줄로 종료)\n")
    print("예:")
    print("  GET /api/activities - List all activities")
    print("  POST /api/activities - Create new activity")
    print("  GET /api/activities/{id} - Get single activity\n")

    apis = []
    line_num = 1

    while True:
        api = input(f"{Colors.GREEN}➤ {Colors.RESET}").strip()

        if not api:
            if not apis:
                print(f"{Colors.RED}✗ 최소 1개의 API는 입력해야 합니다.{Colors.RESET}")
                continue
            break

        apis.append(api)
        line_num += 1

    return apis

def get_additional_context() -> str:
    """추가 맥락 입력받기"""
    print(f"\n{Colors.BLUE}[3/5] 추가 맥락이 있으신가요? (선택사항){Colors.RESET}")
    print("데이터베이스 요구사항, 특수 로직 등을 입력하세요.")
    print("(빈 줄로 종료)\n")

    lines = []
    while True:
        line = input(f"{Colors.GREEN}➤ {Colors.RESET}")
        if not line.strip():
            if lines and lines[-1] != "":
                break
            if not lines:
                break
        lines.append(line)

    return "\n".join(lines).strip()

def get_branch_name(description: str) -> str:
    """브랜치명 생성 및 확인"""
    # 자동 생성: feat/add-activity-template-management-api 같은 형식
    branch_base = description.lower().replace(" ", "-")
    # 특수문자 제거
    branch_base = "".join(c if c.isalnum() or c == "-" else "" for c in branch_base)
    branch_name = f"feat/{branch_base}"

    print(f"\n{Colors.BLUE}[4/5] 브랜치명{Colors.RESET}")
    print(f"자동 생성: {Colors.YELLOW}{branch_name}{Colors.RESET}")
    print("변경하려면 새 값을 입력하세요. (엔터로 승인)\n")

    custom = input(f"{Colors.GREEN}➤ {Colors.RESET}").strip()

    if custom:
        return custom
    return branch_name

def display_summary(description: str, apis: List[str], context: str, branch: str) -> bool:
    """요약 표시 및 승인받기"""
    print(f"\n{Colors.BLUE}[5/5] 요약 및 확인{Colors.RESET}\n")

    print(f"{Colors.BLUE}══════════════════════════════════════{Colors.RESET}")
    print(f"기능: {Colors.GREEN}{description}{Colors.RESET}")
    print(f"브랜치: {Colors.GREEN}{branch}{Colors.RESET}")
    print(f"\nAPI 목록:")
    for api in apis:
        print(f"  {Colors.GREEN}✓{Colors.RESET} {api}")

    if context:
        print(f"\n추가 맥락:")
        for line in context.split('\n'):
            print(f"  {line}")

    print(f"{Colors.BLUE}══════════════════════════════════════{Colors.RESET}\n")

    print(f"이런 기능을 원하시나요?")
    while True:
        response = input(f"{Colors.GREEN}(yes/no) ➤ {Colors.RESET}").strip().lower()
        if response in ['yes', 'y']:
            return True
        elif response in ['no', 'n']:
            return False
        else:
            print(f"{Colors.YELLOW}yes 또는 no를 입력하세요.{Colors.RESET}")

def generate_spec_content(description: str, apis: List[str], context: str) -> str:
    """설계 명세 내용 생성"""
    spec = f"""# {description}

## API 목록

"""

    for api in apis:
        # "GET /api/activities - List all activities" 형식 파싱
        parts = api.split(" - ")
        if len(parts) == 2:
            endpoint = parts[0].strip()
            description_text = parts[1].strip()
            spec += f"- **{endpoint}** - {description_text}\n"
        else:
            spec += f"- {api}\n"

    spec += f"""

## 구현 요구사항

기능: {description}

### API 명세
위의 API 목록을 기반으로 다음을 구현하세요:
- 적절한 HTTP 메서드 사용
- 요청/응답 DTO 작성
- 입력 검증
- 에러 처리
- 기존 시스템과의 통합

"""

    if context:
        spec += f"""
### 추가 맥락

{context}

"""

    spec += """
## 구현 후 체크리스트

- [ ] 모든 API 엔드포인트 구현 완료
- [ ] 입력 검증 추가
- [ ] 에러 처리 구현
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] 코드 리뷰 준비 완료
"""

    return spec

def run_workflow(description: str, apis: List[str], context: str, branch: str) -> int:
    """워크플로우 실행: design → implement → PR"""
    repo_root = Path(__file__).parent.parent

    # Step 1: Spec 생성
    print(f"\n{Colors.BLUE}[1/3] 설계 명세 생성 중...{Colors.RESET}\n")

    spec_content = generate_spec_content(description, apis, context)
    spec_file = Path("/tmp/spec.md")
    spec_file.write_text(spec_content)

    print(f"{Colors.GREEN}✓ 설계 명세 생성 완료: /tmp/spec.md{Colors.RESET}\n")

    # Step 2: Design 스킬 실행
    print(f"{Colors.BLUE}[2/3] Design 스킬 실행 중...{Colors.RESET}\n")

    try:
        result = subprocess.run(
            ["claude", "/design", description],
            cwd=repo_root,
            capture_output=True,
            text=True,
            timeout=300
        )

        if result.returncode == 0:
            print(f"{Colors.GREEN}✓ Design 완료{Colors.RESET}\n")
        else:
            print(f"{Colors.YELLOW}⚠ Design 실행 완료 (상태 확인 필요){Colors.RESET}\n")

    except Exception as e:
        print(f"{Colors.YELLOW}⚠ Design 스킬 실행 스킵: {e}{Colors.RESET}\n")

    # Step 3: Implement 스킬 실행
    print(f"{Colors.BLUE}[3/3] Implement 스킬 실행 중...{Colors.RESET}\n")

    try:
        with open("/tmp/spec.md", "r") as f:
            spec_content = f.read()

        impl_input = spec_content + "\n\nAfter implementation, write a summary of all changes."

        result = subprocess.run(
            ["claude", "exec", "--full-auto"],
            input=impl_input,
            cwd=repo_root,
            capture_output=True,
            text=True,
            timeout=600
        )

        if result.returncode == 0:
            print(f"{Colors.GREEN}✓ Implementation 완료{Colors.RESET}\n")
        else:
            print(f"{Colors.YELLOW}⚠ Implementation 실행 완료{Colors.RESET}\n")

    except Exception as e:
        print(f"{Colors.YELLOW}⚠ Implementation 스킬 실행 스킵: {e}{Colors.RESET}\n")

    # Step 4: 브랜치 생성 및 commit (bash 스크립트 호출)
    print(f"{Colors.BLUE}[4/4] 브랜치 생성 및 PR 준비 중...{Colors.RESET}\n")

    try:
        script_path = repo_root / "scripts" / "feature-workflow.sh"
        result = subprocess.run(
            [str(script_path), description, branch],
            cwd=repo_root,
            timeout=180
        )

        if result.returncode == 0:
            print(f"\n{Colors.GREEN}✓ 워크플로우 완료!{Colors.RESET}")
        else:
            print(f"\n{Colors.YELLOW}⚠ 워크플로우 부분 완료 (로그 확인){Colors.RESET}")

    except FileNotFoundError:
        print(f"{Colors.YELLOW}⚠ feature-workflow.sh를 찾을 수 없습니다.{Colors.RESET}")
        print(f"  수동으로 실행: ./scripts/feature-workflow.sh \"{description}\" \"{branch}\"")
    except Exception as e:
        print(f"{Colors.YELLOW}⚠ 브랜치 생성 실패: {e}{Colors.RESET}")

    return 0

def main():
    try:
        print_header()

        # Step 1: 기능 설명 입력
        description = get_feature_description()

        # Step 2: API 목록 입력
        apis = get_api_list()

        # Step 3: 추가 맥락 입력
        context = get_additional_context()

        # Step 4: 브랜치명 입력/확인
        branch = get_branch_name(description)

        # Step 5: 요약 표시 및 승인
        if not display_summary(description, apis, context, branch):
            print(f"\n{Colors.YELLOW}워크플로우 취소됨.{Colors.RESET}\n")
            return 1

        # Step 6: 워크플로우 실행
        print(f"\n{Colors.BLUE}워크플로우 시작...{Colors.RESET}\n")
        return run_workflow(description, apis, context, branch)

    except KeyboardInterrupt:
        print(f"\n\n{Colors.YELLOW}사용자가 취소했습니다.{Colors.RESET}\n")
        return 1
    except Exception as e:
        print(f"\n{Colors.RED}오류 발생: {e}{Colors.RESET}\n")
        return 1

if __name__ == "__main__":
    sys.exit(main())
