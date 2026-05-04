# Habitus

> **삶의 도약을 위한 디딤돌** — 7가지 자본을 균형 있게 성장시키는 습관 관리 서비스

[![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Build](https://img.shields.io/badge/build-Gradle-blue?logo=gradle)](https://gradle.org/)

[Lovable 프로토타입 보기 →](https://habitus-demo.lovable.app)

---

## 개요

Habitus는 일상의 활동을 통해 7가지 자본을 축적하고, 제한된 시간 안에서 최적의 활동 조합을 추천해주는 개인 성장 플랫폼입니다.

### 7가지 자본

| 자본 | 설명 |
|------|------|
| 신체자본 (Physical) | 체력, 건강, 신체 능력 |
| 심리자본 (Mental) | 회복탄력성, 자기효능감, 정서 안정 |
| 지식자본 (Knowledge) | 학습, 전문성, 사고력 |
| 문화자본 (Cultural) | 예술, 취향, 교양 |
| 언어자본 (Linguistic) | 언어 능력, 표현력, 소통 |
| 사회자본 (Social) | 네트워크, 관계, 협력 |
| 경제자본 (Economic) | 재무 능력, 자산, 경제적 판단력 |

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.9 |
| Persistence | Spring Data JPA, H2 (dev) / MySQL (prod) |
| Build | Gradle |
| Utilities | Lombok |
| Test | JUnit 5, MockMvc |

---

## 아키텍처

Clean Architecture 기반의 4계층 구조를 따릅니다.

```
Presentation  ──▶  Application  ──▶  Domain  ◀──  Infrastructure
  (Controller)       (Service)      (Aggregate)     (JPA Entity)
                      (DTO)        (Repository      (Repository
                   (Exception)      Interface)        Impl)
```

```
src/main/java/com/mint/habitus/
├── presentation/
│   ├── activity/              # 활동 템플릿 API
│   └── activityhistory/       # 활동 이력 API
├── application/
│   ├── activity/              # 활동 서비스 + DTO
│   ├── activityhistory/       # 이력 서비스 + DTO
│   └── recommendation/        # 추천 서비스 + DTO
├── domain/
│   ├── activity/domain/       # Activity 애그리게이트
│   ├── activityhistory/domain/# ActivityHistory 애그리게이트
│   ├── capital/domain/        # CapitalType 열거형
│   ├── priority/domain/       # Priority 도메인
│   └── recommendation/domain/ # 추천 알고리즘 도메인
└── infrastructure/
    ├── activity/              # JPA 엔티티, 매퍼, 레포지토리 구현체
    └── activityhistory/       # JPA 엔티티, 매퍼, 레포지토리 구현체
```

---

## API 명세

### 활동 템플릿 `/api/activities`

| Method | Endpoint | 설명 | 응답 |
|--------|----------|------|------|
| `GET` | `/api/activities` | 전체 활동 목록 조회 | `200` |
| `GET` | `/api/activities/{id}` | 단건 조회 | `200` / `404` |
| `POST` | `/api/activities` | 활동 생성 | `201` + Location |
| `PUT` | `/api/activities/{id}` | 활동 수정 | `200` / `404` |
| `DELETE` | `/api/activities/{id}` | 활동 삭제 | `204` / `404` |

<details>
<summary>요청/응답 예시</summary>

**POST /api/activities**
```json
{
  "name": "아침 달리기",
  "description": "30분 조깅",
  "durationMinutes": 30,
  "cost": 0,
  "effects": {
    "PHYSICAL": 10,
    "MENTAL": 5
  }
}
```

**응답**
```json
{
  "id": 1,
  "name": "아침 달리기",
  "description": "30분 조깅",
  "durationMinutes": 30,
  "cost": 0,
  "effects": {
    "PHYSICAL": 10,
    "MENTAL": 5
  }
}
```
</details>

---

### 활동 이력 `/api/activity-histories`

| Method | Endpoint | 설명 | 응답 |
|--------|----------|------|------|
| `GET` | `/api/activity-histories?page=0&size=20` | 이력 목록 조회 (페이지네이션, 최대 100) | `200` |
| `GET` | `/api/activity-histories/{id}` | 단건 조회 | `200` / `404` |
| `POST` | `/api/activity-histories` | 이력 기록 | `201` + Location |
| `DELETE` | `/api/activity-histories/{id}` | 이력 삭제 | `204` / `404` |

<details>
<summary>요청/응답 예시</summary>

**POST /api/activity-histories**
```json
{
  "activityId": 1,
  "performedAt": "2024-01-15T10:30:00",
  "durationMinutes": 30,
  "notes": "오늘 컨디션 좋았음"
}
```
> `performedAt` 생략 시 현재 시각으로 자동 설정됩니다.

**응답**
```json
{
  "id": 1,
  "activityId": 1,
  "performedAt": "2024-01-15T10:30:00",
  "durationMinutes": 30,
  "notes": "오늘 컨디션 좋았음"
}
```
</details>

---

### 활동 추천 `/api/activities/recommendation`

제한된 시간 내에서 자본 우선순위에 따라 최적 활동 조합을 추천합니다.

| Method | Endpoint | 설명 | 응답 |
|--------|----------|------|------|
| `POST` | `/api/activities/recommendation` | 주간 활동 추천 | `200` |

<details>
<summary>요청/응답 예시</summary>

**POST /api/activities/recommendation**
```json
{
  "userId": 1,
  "availableMinutes": 300,
  "priorities": {
    "PHYSICAL": 5,
    "MENTAL": 3,
    "KNOWLEDGE": 4
  }
}
```
</details>

---

### 에러 응답 형식

```json
{
  "code": "ACTIVITY_HISTORY_NOT_FOUND",
  "message": "Activity history not found: id=42",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

| 코드 | HTTP | 상황 |
|------|------|------|
| `VALIDATION_FAILED` | 400 | 입력값 검증 실패 |
| `INVALID_REQUEST` | 400 | JSON 파싱 오류 |
| `ACTIVITY_NOT_FOUND` | 404 | 활동 없음 |
| `ACTIVITY_HISTORY_NOT_FOUND` | 404 | 이력 없음 |
| `ACTIVITY_NAME_ALREADY_EXISTS` | 409 | 이름 중복 |
| `INTERNAL_SERVER_ERROR` | 500 | 서버 오류 |

---

## 실행 방법

```bash
# 빌드
./gradlew build

# 실행 (H2 인메모리 DB, 기본 포트 8080)
./gradlew bootRun

# 테스트
./gradlew test
```

---

## 기술적 결정 기록

- [활동 추천 알고리즘 선택 과정 — Greedy → DP](docs/choosing-the-right-algorithm-for-activity-recommendations.md)
