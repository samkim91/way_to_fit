# Flutter 앱 개발자 가이드 — Competition Platform

> 대상: Flutter 앱 개발자  
> 작성일: 2026-04-30  
> 구현 범위: **대회(competition) 기능만** — CompetitionListScreen, CompetitionDetailScreen,
> RegistrationScreen, ScoreSubmitScreen, LeaderboardScreen, AthleteProfileScreen  
> 참고 문서: [UI 명세](../competition-platform-design-spec.md) · [API 명세](../specs/competition-platform-spec.md) · [태스크 목록](../plans/competition-platform-plan.md)

---

## 목차

1. [앱 범위 및 전제 조건](#1-앱-범위-및-전제-조건)
2. [프로젝트 초기 설정](#2-프로젝트-초기-설정)
3. [폴더 구조](#3-폴더-구조)
4. [아키텍처 결정](#4-아키텍처-결정)
5. [Auth 흐름 (Flutter OAuth + JWT)](#5-auth-흐름)
6. [API 레이어 패턴](#6-api-레이어-패턴)
7. [WebSocket(STOMP) 클라이언트](#7-websocket-클라이언트)
8. [화면 구현 순서](#8-화면-구현-순서)
9. [Mock API 전략 (백엔드 미완성 시)](#9-mock-api-전략)
10. [테마 & 컬러 시스템](#10-테마--컬러-시스템)

---

## 1. 앱 범위 및 전제 조건

### 구현 범위

이 가이드는 **대회 기능만** 다룬다. WOD·시간표·기록 등 기존 회원 앱 탭은 별도 가이드로 분리된다.

구현할 화면 (UI 상세 명세 → `docs/competition-platform-design-spec.md` 섹션 4):

| 화면 | 인증 필요 | 설명 |
|---|---|---|
| CompetitionListScreen | ✗ | 공개 대회 목록 |
| CompetitionDetailScreen | ✗ | 대회 상세, Stage/Event 목록 |
| IndividualRegScreen | ✓ | 개인 참가 신청 |
| TeamRegScreen | ✓ | 팀 참가 신청 |
| EventLineupScreen | ✓ | 팀 이벤트별 출전 멤버 설정 |
| ScoreSubmitScreen | ✓ (CONFIRMED) | 이벤트별 기록 제출 |
| LeaderboardScreen | ✗ | 이벤트별/종합 순위, 30초 폴링 |
| AthleteProfileScreen | ✗ | 선수 프로필 공개 조회 |

### 전제 조건

- 백엔드 Phase 0~5 API가 완성되어 있어야 한다.
- 백엔드가 미완성인 경우 → [섹션 9 Mock API 전략](#9-mock-api-전략) 참고
- 백엔드 베이스 URL: `http://localhost:8080` (개발), 환경변수로 관리

---

## 2. 프로젝트 초기 설정

### 프로젝트 생성

```bash
flutter create way_to_fit_app --org com.waytofit --platforms ios,android
cd way_to_fit_app
```

### pubspec.yaml 의존성

```yaml
dependencies:
  flutter:
    sdk: flutter

  # 네트워크
  dio: ^5.8.0
  dio_cookie_manager: ^3.1.1
  cookie_jar: ^4.0.8

  # 라우팅
  go_router: ^15.0.0

  # 상태관리
  flutter_riverpod: ^2.6.1
  riverpod_annotation: ^2.6.1

  # 인증
  flutter_web_auth_2: ^4.0.0   # OAuth 브라우저 흐름
  flutter_secure_storage: ^9.2.4  # JWT 저장

  # WebSocket
  stomp_dart_client: ^1.2.0

  # UI
  cached_network_image: ^3.4.1
  intl: ^0.20.2

dev_dependencies:
  flutter_test:
    sdk: flutter
  build_runner: ^2.4.14
  riverpod_generator: ^2.6.3
  json_serializable: ^6.9.5
```

### 코드 생성 명령

```bash
# Riverpod + JSON 직렬화 코드 자동 생성
flutter pub run build_runner build --delete-conflicting-outputs

# 개발 중 watch 모드
flutter pub run build_runner watch --delete-conflicting-outputs
```

---

## 3. 폴더 구조

```
lib/
├── core/
│   ├── api/
│   │   ├── api_client.dart          # Dio 싱글톤, interceptor
│   │   └── api_exception.dart       # 에러 타입 정의
│   ├── auth/
│   │   ├── auth_service.dart        # OAuth 로그인 흐름
│   │   └── token_storage.dart       # SecureStorage 래퍼
│   └── router/
│       └── app_router.dart          # go_router 설정
│
└── features/
    └── competition/
        ├── data/
        │   ├── competition_api.dart          # Dio API 호출
        │   ├── competition_repository_impl.dart
        │   └── models/                       # JSON 직렬화 DTO
        │       ├── competition_dto.dart
        │       ├── registration_dto.dart
        │       ├── score_dto.dart
        │       └── leaderboard_dto.dart
        ├── domain/
        │   ├── competition_repository.dart   # 추상 인터페이스
        │   └── models/                       # 도메인 모델 (순수 Dart)
        │       ├── competition.dart
        │       ├── competition_event.dart
        │       ├── registration.dart
        │       └── leaderboard_entry.dart
        └── presentation/
            ├── screens/
            │   ├── competition_list_screen.dart
            │   ├── competition_detail_screen.dart
            │   ├── individual_reg_screen.dart
            │   ├── team_reg_screen.dart
            │   ├── event_lineup_screen.dart
            │   ├── score_submit_screen.dart
            │   ├── leaderboard_screen.dart
            │   └── athlete_profile_screen.dart
            ├── widgets/
            │   ├── competition_card.dart
            │   ├── competition_status_badge.dart
            │   ├── score_status_chip.dart
            │   ├── leaderboard_row.dart
            │   └── wod_type_input.dart        # WodType별 기록 입력 위젯
            └── providers/
                ├── competition_providers.dart
                ├── registration_providers.dart
                ├── score_providers.dart
                └── leaderboard_providers.dart
```

---

## 4. 아키텍처 결정

### 레이어 의존 방향

```
presentation → domain ← data
```

- `presentation`: 화면 + 위젯 + Riverpod providers (UI 로직)
- `domain`: Repository 인터페이스 + 도메인 모델 (순수 Dart, 프레임워크 의존 없음)
- `data`: Repository 구현체 + Dio API 호출 + JSON 직렬화 DTO

### Riverpod 패턴

서버 데이터는 `AsyncNotifier`, UI 상태는 `Notifier` 사용:

```dart
// 대회 목록 (서버 데이터)
@riverpod
class CompetitionList extends _$CompetitionList {
  @override
  Future<List<Competition>> build() async {
    final repo = ref.watch(competitionRepositoryProvider);
    return repo.getCompetitions();
  }

  Future<void> refresh() => ref.refresh(competitionListProvider.future);
}

// 리더보드 필터 (UI 상태)
@riverpod
class LeaderboardFilter extends _$LeaderboardFilter {
  @override
  LeaderboardFilterState build() => const LeaderboardFilterState(
    gender: null,
    scaleCategory: null,
    eventType: EventType.individual,
  );

  void setGender(String? gender) =>
      state = state.copyWith(gender: gender);
}
```

### 에러 처리

Riverpod의 `AsyncValue`를 활용하여 로딩/에러/성공 상태를 UI에서 일관되게 처리:

```dart
// 화면에서 사용
ref.watch(competitionListProvider).when(
  loading: () => const CircularProgressIndicator(),
  error: (err, _) => ErrorView(message: err.toString()),
  data: (competitions) => CompetitionListView(competitions),
);
```

---

## 5. Auth 흐름

### 개요

백엔드는 서버사이드 OAuth2 흐름을 사용한다:

```
Flutter → 시스템 브라우저 열기
  → https://api/oauth2/authorization/google
  → Google 인증 페이지
  → 백엔드 콜백 처리 (AT 발급)
  → 딥링크 리다이렉트 → waytofit://auth/callback?token=AT
Flutter ← 딥링크 수신 → AT 저장
```

### 백엔드 설정 협의 필요

백엔드의 `OAUTH2_REDIRECT_URI` 환경변수에 모바일용 딥링크 URI를 추가해야 한다:

```
# 개발 환경 (현재)
OAUTH2_REDIRECT_URI=http://localhost:5173/auth/callback

# 모바일 지원 시 변경 필요
OAUTH2_REDIRECT_URI=waytofit://auth/callback
```

### Android 설정 (`android/app/src/main/AndroidManifest.xml`)

```xml
<activity android:name=".MainActivity" ...>
  <!-- 기존 intent-filter ... -->

  <!-- OAuth2 딥링크 수신 -->
  <intent-filter android:label="flutter_web_auth_2">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="waytofit" android:host="auth" />
  </intent-filter>
</activity>
```

### iOS 설정 (`ios/Runner/Info.plist`)

```xml
<key>CFBundleURLTypes</key>
<array>
  <dict>
    <key>CFBundleTypeRole</key>
    <string>Editor</string>
    <key>CFBundleURLSchemes</key>
    <array>
      <string>waytofit</string>
    </array>
  </dict>
</array>
```

### auth_service.dart

```dart
class AuthService {
  static const _callbackUrlScheme = 'waytofit';
  static const _callbackUrl = 'waytofit://auth/callback';
  static const _authUrl = 'https://api.waytofit.com/oauth2/authorization/google';

  final TokenStorage _tokenStorage;

  Future<bool> loginWithGoogle() async {
    try {
      final result = await FlutterWebAuth2.authenticate(
        url: _authUrl,
        callbackUrlScheme: _callbackUrlScheme,
      );

      final uri = Uri.parse(result);
      final accessToken = uri.queryParameters['token'];

      if (accessToken == null) return false;

      await _tokenStorage.saveAccessToken(accessToken);
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<void> logout() async {
    final rt = await _tokenStorage.getRefreshToken();
    // POST /api/auth/logout (cookie)
    await _tokenStorage.clear();
  }
}
```

### token_storage.dart

```dart
class TokenStorage {
  static const _atKey = 'access_token';
  final _storage = const FlutterSecureStorage();

  Future<void> saveAccessToken(String token) =>
      _storage.write(key: _atKey, value: token);

  Future<String?> getAccessToken() =>
      _storage.read(key: _atKey);

  Future<void> clear() => _storage.deleteAll();
}
```

### AT 자동 갱신 (Dio Interceptor)

백엔드 RT는 HttpOnly 쿠키로 저장되므로 `dio_cookie_manager`로 자동 전송된다:

```dart
class AuthInterceptor extends Interceptor {
  final Dio _dio;
  final TokenStorage _tokenStorage;

  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) async {
    final token = await _tokenStorage.getAccessToken();
    if (token != null) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    if (err.response?.statusCode == 401) {
      // RT 쿠키를 이용해 AT 재발급
      try {
        final response = await _dio.post('/api/auth/reissue');
        final newAt = response.data['data']['accessToken'];
        await _tokenStorage.saveAccessToken(newAt);

        // 원래 요청 재시도
        final opts = err.requestOptions;
        opts.headers['Authorization'] = 'Bearer $newAt';
        final cloned = await _dio.fetch(opts);
        return handler.resolve(cloned);
      } catch (_) {
        await _tokenStorage.clear();
        // 로그인 화면으로 이동 (router 사용)
      }
    }
    handler.next(err);
  }
}
```

---

## 6. API 레이어 패턴

### api_client.dart

```dart
final dioProvider = Provider<Dio>((ref) {
  final tokenStorage = ref.watch(tokenStorageProvider);
  final dio = Dio(BaseOptions(
    baseUrl: const String.fromEnvironment(
      'API_BASE_URL',
      defaultValue: 'http://localhost:8080',
    ),
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
  ));

  dio.interceptors.add(CookieManager(CookieJar()));
  dio.interceptors.add(AuthInterceptor(dio, tokenStorage));

  return dio;
});
```

### Repository 인터페이스 (domain)

```dart
abstract class CompetitionRepository {
  Future<List<Competition>> getCompetitions({CompetitionLifecycle? lifecycle});
  Future<Competition> getCompetition(String id);
  Future<Registration> registerIndividual(String competitionId, IndividualRegRequest req);
  Future<Registration> registerTeam(String competitionId, TeamRegRequest req);
  Future<Registration> getMyRegistration(String competitionId);
  Future<void> submitScore(String competitionId, String eventId, SubmitScoreRequest req);
  Future<EventLeaderboard> getEventLeaderboard(String competitionId, String eventId, {String? gender, String? scaleCategory});
  Future<OverallLeaderboard> getOverallLeaderboard(String competitionId, String stageId, {String? eventType});
  Future<AthleteProfile> getAthleteProfile(String userId);
  Future<List<AthleteCompetitionHistory>> getAthleteHistory(String userId);
}
```

`Competition` 모델은 신청 화면과 리더보드 필터가 참조할 `scaleCategories` 공용 목록을 포함해야 한다. 기록 제출은 `registration.scaleCategory` 를 표시만 하고, `SubmitScoreRequest` 에 별도 스케일 필드를 두지 않는다.

### Repository 구현 (data)

```dart
class CompetitionRepositoryImpl implements CompetitionRepository {
  final Dio _dio;

  @override
  Future<List<Competition>> getCompetitions({CompetitionLifecycle? lifecycle}) async {
    final params = <String, dynamic>{};
    if (lifecycle != null) params['lifecycle'] = lifecycle.name;

    final res = await _dio.get('/api/competitions', queryParameters: params);
    return (res.data['data']['content'] as List)
        .map((e) => CompetitionDto.fromJson(e).toDomain())
        .toList();
  }

  @override
  Future<void> submitScore(String competitionId, String eventId, SubmitScoreRequest req) async {
    await _dio.post(
      '/api/competitions/$competitionId/events/$eventId/scores',
      data: req.toJson(),
    );
  }
  // ...
}
```

### ApiResponse 포맷

백엔드 공통 응답 구조:

```json
{
  "code": "0000",
  "message": "성공",
  "data": { }
}
```

페이지 응답:
```json
{
  "code": "0000",
  "message": "성공",
  "data": {
    "content": [...],
    "pageInfo": { "pageNumber": 1, "pageSize": 20, "totalElements": 47, ... }
  }
}
```

---

## 7. WebSocket 클라이언트

LeaderboardScreen에서 관중(Spectator)용 리더보드는 **REST API 30초 폴링**을 사용한다.
WebSocket 프로젝션은 웹 어드민 전용이므로 Flutter 앱에서는 구현하지 않는다.

### 30초 폴링 구현 (leaderboard_providers.dart)

```dart
@riverpod
class OverallLeaderboard extends _$OverallLeaderboard {
  Timer? _timer;

  @override
  Future<OverallLeaderboardData> build(
    String competitionId,
    String stageId,
    String eventType,
  ) async {
    // 자동 갱신 타이머 설정
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 30), (_) {
      ref.invalidateSelf();
    });

    ref.onDispose(() => _timer?.cancel());

    final repo = ref.watch(competitionRepositoryProvider);
    return repo.getOverallLeaderboard(competitionId, stageId, eventType: eventType);
  }
}
```

### (선택) WebSocket 구독 예시

추후 실시간 기능이 필요할 경우를 위한 참고 코드:

```dart
class LeaderboardWebSocketService {
  StompClient? _client;

  void connect(String competitionId, String token, Function(dynamic) onData) {
    _client = StompClient(
      config: StompConfig(
        url: 'ws://localhost:8080/ws',
        onConnect: (frame) {
          _client!.subscribe(
            destination: '/topic/competitions/$competitionId/leaderboard',
            callback: (frame) => onData(jsonDecode(frame.body!)),
          );
        },
        webSocketConnectHeaders: {'Authorization': 'Bearer $token'},
        onDisconnect: (_) => Future.delayed(const Duration(seconds: 3), () => connect(competitionId, token, onData)),
      ),
    );
    _client!.activate();
  }

  void disconnect() => _client?.deactivate();
}
```

---

## 8. 화면 구현 순서

plan의 Task와 매핑:

| Task | 화면 | 의존 |
|---|---|---|
| F-7-1 | 프로젝트 설정 (이 가이드 섹션 2~6) | 없음 |
| F-7-2 | OAuth 로그인 + JWT 관리 (이 가이드 섹션 5) | F-7-1 |
| F-7-3 | CompetitionListScreen + CompetitionDetailScreen | F-7-2 |
| F-7-4 | IndividualRegScreen, TeamRegScreen, EventLineupScreen | F-7-3 |
| F-7-5 | ScoreSubmitScreen | F-7-4 |
| F-7-6 | LeaderboardScreen (30초 폴링) | F-7-5 |
| F-7-7 | AthleteProfileScreen | F-7-6 |

### go_router 설정 (app_router.dart)

```dart
final routerProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/competitions',
    routes: [
      GoRoute(path: '/login', builder: (_, __) => const LoginScreen()),
      GoRoute(path: '/auth/callback', builder: (_, state) {
        final token = state.uri.queryParameters['token'];
        return AuthCallbackScreen(token: token);
      }),
      ShellRoute(
        builder: (_, __, child) => MainScaffold(child: child),
        routes: [
          GoRoute(
            path: '/competitions',
            builder: (_, __) => const CompetitionListScreen(),
            routes: [
              GoRoute(
                path: ':id',
                builder: (_, state) => CompetitionDetailScreen(id: state.pathParameters['id']!),
                routes: [
                  GoRoute(path: 'register/individual', builder: (_, state) => IndividualRegScreen(competitionId: state.pathParameters['id']!)),
                  GoRoute(path: 'register/team', builder: (_, state) => TeamRegScreen(competitionId: state.pathParameters['id']!)),
                  GoRoute(path: 'leaderboard', builder: (_, state) => LeaderboardScreen(competitionId: state.pathParameters['id']!)),
                ],
              ),
            ],
          ),
          GoRoute(
            path: '/athletes/:userId',
            builder: (_, state) => AthleteProfileScreen(userId: state.pathParameters['userId']!),
          ),
        ],
      ),
    ],
  );
});
```

### WodType별 기록 입력 위젯 분기

`ScoreSubmitScreen`의 핵심 로직 (design-spec 4.8 참고):

```dart
Widget _buildScoreInput(WodType wodType) {
  return switch (wodType) {
    WodType.forTime => ForTimeInput(onChanged: ...),
    WodType.amrap   => AmrapInput(onChanged: ...),
    WodType.emom    => EmomInput(onChanged: ...),
    WodType.maxWeight => MaxWeightInput(onChanged: ...),
    WodType.custom  => CustomTextInput(onChanged: ...),
  };
}
```

---

## 9. Mock API 전략

백엔드 API가 완성되기 전에 화면 개발을 시작하려면 MockRepository를 주입한다.

### MockRepository 패턴

```dart
// mock/mock_competition_repository.dart
class MockCompetitionRepository implements CompetitionRepository {
  @override
  Future<List<Competition>> getCompetitions({CompetitionLifecycle? lifecycle}) async {
    await Future.delayed(const Duration(milliseconds: 500)); // 네트워크 지연 시뮬레이션
    return [
      Competition(
        id: '1',
        title: '2026 Seoul CrossFit Open',
        lifecycle: CompetitionLifecycle.registrationOpen,
        startDate: DateTime(2026, 5, 1),
        endDate: DateTime(2026, 5, 10),
        registrationEndAt: DateTime(2026, 4, 30),
        participantCount: 47,
      ),
    ];
  }
  // ...
}
```

### 환경변수로 주입 전환

```dart
final competitionRepositoryProvider = Provider<CompetitionRepository>((ref) {
  const useMock = bool.fromEnvironment('USE_MOCK', defaultValue: false);
  if (useMock) return MockCompetitionRepository();
  return CompetitionRepositoryImpl(ref.watch(dioProvider));
});
```

실행 시:
```bash
flutter run --dart-define USE_MOCK=true
```

---

## 10. 테마 & 컬러 시스템

`design-spec.md` 섹션 2.2 기준. 다크 모드 우선:

```dart
class AppColors {
  // 기본
  static const primary = Color(0xFFFF6B2B);      // 에너지 오렌지
  static const background = Color(0xFF1A202C);
  static const surface = Color(0xFF2D3748);

  // 대회 상태 배지
  static const lifecycleOpen = Color(0xFF9CA3AF);
  static const lifecycleRegistrationOpen = Color(0xFF2563EB);
  static const lifecycleInProgress = Color(0xFFF97316);
  static const lifecycleCompleted = Color(0xFF22C55E);
  static const lifecycleRegistrationClosed = Color(0xFFE53E3E);
  static const statusPending = Color(0xFFFFC107);
  static const statusConfirmed = Color(0xFF4CAF50);

  // 순위 메달
  static const gold   = Color(0xFFFFD700);
  static const silver = Color(0xFFC0C0C0);
  static const bronze = Color(0xFFCD7F32);

  // 이벤트 타입
  static const individual = Color(0xFFFF6B2B);   // Primary 오렌지
  static const team       = Color(0xFF3B82F6);   // 블루
}
```

---

## 부록: 주요 API 엔드포인트 빠른 참조

전체 명세 → `docs/specs/competition-platform-spec.md`

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/api/competitions` | 대회 목록 | 없음 |
| GET | `/api/competitions/{id}` | 대회 상세 | 없음 |
| POST | `/api/competitions/{id}/registrations` | 참가 신청 | Bearer |
| GET | `/api/competitions/{id}/registrations/me` | 내 신청 조회 | Bearer |
| PUT | `/api/competitions/{id}/events/{eventId}/lineups/{regId}` | 출전 멤버 설정 | Bearer |
| POST | `/api/competitions/{id}/events/{eventId}/scores` | 기록 제출 | Bearer |
| GET | `/api/competitions/{id}/events/{eventId}/leaderboard` | 이벤트 리더보드 | 없음 |
| GET | `/api/competitions/{id}/stages/{stageId}/leaderboard` | 종합 리더보드 | 없음 |
| GET | `/api/athletes/{userId}` | 선수 프로필 | 없음 |
| GET | `/api/athletes/{userId}/competitions` | 선수 대회 이력 | 없음 |
| POST | `/api/auth/reissue` | AT 재발급 | Cookie(refresh_token) |
| POST | `/api/auth/logout` | 로그아웃 | Cookie(refresh_token) |
