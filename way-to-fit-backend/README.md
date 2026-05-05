# WayToFit

Spring Boot 3 + Kotlin 기반 REST API 서버

## 기술 스택

- **Language**: Kotlin
- **Framework**: Spring Boot 3.4.3
- **Database**: PostgreSQL 16
- **ORM**: Spring Data JPA
- **인증**: OAuth2 (Google) + JWT (Access Token / Refresh Token)
- **API 문서**: Swagger (local 환경만 활성화)
- **Architecture**: Hexagonal (MSA-like, Domain-first)

---

## 사전 준비

| 항목 | 버전 |
|------|------|
| JDK | 21 |
| Docker Desktop | 최신 |

---

## 로컬 개발 환경 실행

### 1. 환경변수 설정

`.env.example`을 참고해 `.env` 파일을 생성합니다.

```bash
cp .env.example .env
```

`.env` 파일을 열어 값을 채워주세요.

```env
JWT_SECRET=your-secret-key-at-least-32-characters-long
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

> `JWT_SECRET`은 32자 이상의 문자열이어야 합니다.
> Google OAuth2 클라이언트 정보는 [Google Cloud Console](https://console.cloud.google.com)에서 발급받으세요.

### 2. Docker Desktop 실행

Docker Desktop을 실행하고 엔진이 완전히 기동될 때까지 기다립니다.

```bash
# 실행 확인
docker info
```

### 3. PostgreSQL 컨테이너 실행

```bash
docker-compose up -d
```

컨테이너 상태 확인:

```bash
docker ps
# waytofit-postgres 컨테이너가 healthy 상태인지 확인
```

### 4. 애플리케이션 실행

```bash
./gradlew bootRun
```

환경변수를 `.env` 파일로 관리하지 않는 경우 직접 주입:

```bash
JWT_SECRET=your-secret \
GOOGLE_CLIENT_ID=your-client-id \
GOOGLE_CLIENT_SECRET=your-client-secret \
./gradlew bootRun
```

기동 확인:
```
Started WayToFitApplicationKt in X seconds
```

### 5. API 문서 확인 (Swagger)

로컬 환경에서는 Swagger UI가 자동 활성화됩니다.

```
http://localhost:8080/swagger-ui.html
```

---

## 주요 명령어

```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 단일 테스트
./gradlew test --tests "com.waytofit.SomeTest.methodName"

# PostgreSQL 컨테이너 중지
docker-compose down

# PostgreSQL 데이터 포함 완전 삭제
docker-compose down -v
```

---

## 인증 흐름

```
1. GET /oauth2/authorization/google   → Google 로그인 페이지로 이동
2. Google 인증 완료                   → 콜백 처리
3. Access Token + Refresh Token 발급  → 클라이언트에 전달
4. API 요청 시 헤더에 토큰 포함

Authorization: Bearer {accessToken}
```
