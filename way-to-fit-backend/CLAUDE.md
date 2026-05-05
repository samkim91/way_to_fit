# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Bside Crossfit 백엔드 REST API 서버. Spring Boot 3.4.3 + Kotlin 2.3.10 + Hexagonal Architecture.

- **인증**: OAuth2 로그인(Google) → 자체 JWT (Access Token + Refresh Token) 발급
- **RT Rotation**: Refresh Token 재발급 시 기존 토큰 무효화 (RTR 패턴 구현됨)
- **Swagger**: `local` 프로파일에서만 활성화 (`application-local.yml`)
- **소프트 삭제**: `@SQLRestriction("deleted_at IS NULL")` 적용

## Build & Run

```bash
# PostgreSQL 실행
docker-compose up -d

# 빌드
./gradlew build

# 실행 (local 프로파일 기본 적용)
./gradlew bootRun

# 테스트 전체
./gradlew test

# 단일 테스트
./gradlew test --tests "com.waytofit.SomeTest.methodName"

# 클린 빌드
./gradlew clean build
```

## Architecture: MSA-like Hexagonal (Domain-first)

최상위는 **도메인(bounded context)** 기준으로 분리. 각 도메인 안에서 헥사고날 레이어를 적용.
새 도메인 추가 시 (`workout/`, `attendance/` 등) 동일한 패턴을 반복.

```
com.waytofit/
├── user/                              # User bounded context
│   ├── domain/                        # 도메인 레이어 (순수 비즈니스 모델)
│   │   ├── User.kt                    # 도메인 객체 (JPA Entity 아님)
│   │   ├── RefreshToken.kt
│   │   └── enums/                     # UserRole, OAuthProvider
│   ├── application/                   # 애플리케이션 레이어
│   │   ├── service/                   # UseCase 구현체 (UserService.kt)
│   │   └── port/
│   │       ├── in/                    # UseCase 인터페이스 (input ports)
│   │       └── out/                   # 저장소 인터페이스 (output ports)
│   └── adapter/                       # 어댑터 레이어
│       ├── in/web/                    # REST Controller, Request/Response DTO
│       └── out/persistence/           # JPA Entity, JpaRepository, PersistenceAdapter
├── global/                            # 전역 공통 (cross-cutting concerns)
│   ├── config/                        # SecurityConfig, SwaggerConfig, JpaConfig
│   ├── common/response/               # ApiResponse, ApiPageResponse, ResponseCode
│   ├── error/                         # BusinessException, GlobalExceptionHandler
│   ├── persistence/                   # BaseEntity (Auditing), AuditorAwareImpl
│   ├── security/
│   │   ├── jwt/                       # JwtTokenProvider, JwtAuthenticationFilter
│   │   └── oauth2/                    # CustomOAuth2UserService, OAuth2LoginSuccessHandler
│   └── util/logging/                  # MdcFilter (Trace ID), RequestLoggingFilter
└── WayToFitApplication.kt
```

**의존성 방향**: `{domain}/adapter` → `{domain}/application` → `{domain}/domain` ← `global`
도메인 레이어는 Spring, JPA 등 외부 프레임워크에 의존하지 않아야 함.

## Key Configuration

| 항목 | 위치 |
|------|------|
| DB / JWT / OAuth2 설정 | `application.yml` |
| Swagger 활성화 | `application-local.yml` |
| 환경변수 예시 | `.env.example` |

환경변수(`JWT_SECRET`, `GOOGLE_CLIENT_ID` 등)는 `.env.example` 참고.

## Auth Flow

1. `GET /oauth2/authorization/google` → Google OAuth2 인가
2. Google 콜백 → `CustomOAuth2UserService.loadUser()` → DB upsert
3. `OAuth2LoginSuccessHandler` → AT + RT 발급, RT는 HttpOnly 쿠키에 저장, AT는 리다이렉트 URL 파라미터로 전달
4. 이후 API 요청: `Authorization: Bearer {accessToken}`
5. RT 갱신: `POST /api/auth/reissue` (Cookie: refresh_token) → RT Rotation 적용됨
6. 로그아웃: `POST /api/auth/logout` (Cookie: refresh_token) → RT 삭제

## API Endpoints

| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| POST | `/api/auth/reissue` | 토큰 재발급 (RTR) | Cookie(refresh_token) |
| POST | `/api/auth/logout` | 로그아웃 | Cookie(refresh_token) |

## Common Response Format

```json
{
  "code": "0000",
  "message": "성공",
  "data": { }
}
```

**ResponseCode 범위**
- `0000~1999`: 공통 (SUCCESS, NOT_FOUND, UNAUTHORIZED 등)
- `2000~2999`: User 도메인 (REFRESH_TOKEN_INVALID, REFRESH_TOKEN_NOT_FOUND 등)
- `9000~9999`: 시스템 에러

## Global Utilities

- **Trace ID**: 모든 요청에 12자 UUID 부여 → MDC 기록 + `X-Trace-Id` 응답 헤더
- **Request Logging**: 요청 시작/종료 및 응답 시간(ms) 로깅 (Swagger/favicon 제외)
- **Soft Delete**: `BaseEntity`에 `deletedAt`, `deletedBy` 포함. `@SQLRestriction`으로 자동 필터링
- **JPA Auditing**: `createdAt`, `createdBy`, `lastModifiedAt`, `lastModifiedBy` 자동 기록
