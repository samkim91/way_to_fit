# WayToFit (Spring Boot 3 + Kotlin)

REST API 서버 프로젝트로, 헥사고날 아키텍처(Hexagonal Architecture)를 기반으로 도메인 중심 설계를 따릅니다.

## 🚀 프로젝트 개요
- **목적**: 크로스핏 체육관 서비스를 위한 백엔드 API 서버
- **주요 기능**: OAuth2 (Google) 로그인, JWT 인증, 회원 관리, 운동 관리(예정)
- **핵심 아키텍처**: MSA-like Hexagonal (Domain-first, Ports and Adapters)
- **프로필**: `local`, `prod` (Swagger는 `local`에서만 활성화)

## 🛠 기술 스택
- **언어 및 런타임**: Kotlin (JVM 21), Spring Boot 3.4.3
- **데이터베이스**: PostgreSQL 16 (JPA / Hibernate)
- **보안**: Spring Security, OAuth2 Client, JWT (io.jsonwebtoken)
- **API 문서**: SpringDoc (Swagger UI)
- **빌드 도구**: Gradle (Kotlin DSL), Makefile, Docker Compose

## 📁 주요 디렉토리 구조 및 패턴
- `com.waytofit.{domain}/`: 도메인별 Bounded Context 분리
    - `domain/`: 도메인 모델, 인바운드/아웃바운드 포트(인터페이스), 비즈니스 로직(서비스)
    - `adapter/in/web/`: REST 컨트롤러 및 DTO
    - `adapter/out/persistence/`: JPA 엔티티, 리포지토리, 아웃바운드 포트 구현체
- `com.waytofit.global/`: 전역 공통 관심사
    - `common/response/`: 공통 응답 규격 (`ApiResponse`, `ResponseCode`)
    - `config/`: Spring 설정 (Security, JPA, Swagger 등)
    - `error/`: 전역 예외 처리 (`BusinessException`, `GlobalExceptionHandler`)
    - `security/`: 인증 및 인가 로직 (JWT, OAuth2)
    - `persistence/`: 공통 영속성 설정 (`BaseEntity`, `AuditorAware`)

## 📏 개발 컨벤션 및 규칙
- **도메인 모델 분리**: JPA 엔티티와 도메인 모델(data class)을 엄격히 분리합니다.
    - JPA 엔티티는 `UserEntity`와 같이 이름을 지정하고 `toDomain()`, `fromDomain()` 메서드를 통해 변환합니다.
- **날짜 타입**: 모든 날짜와 시간 필드는 `java.time.Instant` 타입을 사용합니다.
- **감사(Auditing)**: 모든 엔티티는 `BaseEntity`를 상속받아 생성/수정 정보를 기록합니다.
- **Soft Delete**: `BaseEntity`의 `deletedAt` 필드를 사용하여 논리 삭제를 수행합니다.
- **API 응답**: 모든 API는 `ApiResponse<T>` 형태로 응답하며, `ResponseCode` enum을 사용하여 일관된 상태 코드를 전달합니다.
- **의존성 방향**: `{domain}/adapter` → `{domain}/domain` ← `global` 방향을 유지합니다. 도메인 레이어는 외부 프레임워크에 의존하지 않아야 합니다.

## 💻 실행 및 관리 명령어
### 로컬 실행 환경
1. **환경변수 설정**: `.env.example`을 복사하여 `.env` 생성 후 `JWT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` 입력
2. **PostgreSQL 실행**: `docker-compose up -d`
3. **애플리케이션 기동**: `make dev` 또는 `./gradlew bootRun`

### 주요 Gradle 명령어
- **빌드**: `./gradlew build`
- **테스트**: `./gradlew test`
- **클린**: `./gradlew clean`

## 🔗 주요 엔드포인트
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (Local 환경 전용)
- **OAuth2 로그인**: `GET /oauth2/authorization/google`
- **토큰 갱신**: `POST /api/auth/refresh`
