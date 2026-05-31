import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:way_to_fit_app/core/api/api_exception.dart';
import 'package:way_to_fit_app/core/auth/auth_session.dart';
import 'package:way_to_fit_app/features/competition/data/competition_repository.dart';
import 'package:way_to_fit_app/features/competition/domain/models.dart';
import 'package:way_to_fit_app/features/competition/presentation/providers/competition_providers.dart';

void main() {
  group('competitionDetailProvider', () {
    test(
      'keeps public detail when my registrations request is unauthorized',
      () async {
        final repository = _FakeCompetitionRepository(
          getMyRegistrationsError: const ApiException(
            '로그인이 필요합니다.',
            statusCode: 401,
          ),
        );
        final container = ProviderContainer(
          overrides: [
            competitionRepositoryProvider.overrideWithValue(repository),
            authControllerProvider.overrideWith(
              () => _FakeAuthController.authenticated(),
            ),
          ],
        );
        addTearDown(container.dispose);
        final subscription = container.listen(
          competitionDetailProvider('competition-1'),
          (previous, next) {},
        );
        addTearDown(subscription.close);

        final bundle = await container.read(
          competitionDetailProvider('competition-1').future,
        );

        expect(bundle.competition.id, 'competition-1');
        expect(bundle.stages, hasLength(1));
        expect(bundle.myRegistrations, isEmpty);
        expect(bundle.myScores, isEmpty);
      },
    );

    test('keeps public detail when my score request is unauthorized', () async {
      final repository = _FakeCompetitionRepository(
        myRegistrations: [
          Registration(
            id: 'registration-1',
            registrationType: RegistrationType.individual,
            teamName: null,
            scaleCategory: 'RXD',
            paymentStatus: PaymentStatus.confirmed,
            gender: 'MALE',
            paymentNote: null,
            members: const [],
          ),
        ],
        getScoreError: const ApiException('로그인이 필요합니다.', statusCode: 401),
      );
      final container = ProviderContainer(
        overrides: [
          competitionRepositoryProvider.overrideWithValue(repository),
          authControllerProvider.overrideWith(
            () => _FakeAuthController.authenticated(),
          ),
        ],
      );
      addTearDown(container.dispose);
      final subscription = container.listen(
        competitionDetailProvider('competition-1'),
        (previous, next) {},
      );
      addTearDown(subscription.close);

      final bundle = await container.read(
        competitionDetailProvider('competition-1').future,
      );

      expect(bundle.competition.id, 'competition-1');
      expect(bundle.myRegistrations, hasLength(1));
      expect(bundle.myScores, isEmpty);
    });
  });
}

class _FakeAuthController extends AuthController {
  _FakeAuthController(this._state);

  final AuthState _state;

  factory _FakeAuthController.authenticated() {
    return _FakeAuthController(
      const AuthState(accessToken: 'token', userId: 'user-1'),
    );
  }

  @override
  Future<AuthState> build() async => _state;
}

class _FakeCompetitionRepository extends CompetitionRepository {
  _FakeCompetitionRepository({
    this.myRegistrations = const [],
    this.getMyRegistrationsError,
    this.getScoreError,
  });

  final List<Registration> myRegistrations;
  final Object? getMyRegistrationsError;
  final Object? getScoreError;

  @override
  Future<Competition> getCompetition(String competitionId) async {
    return Competition(
      id: competitionId,
      name: '2026 Seoul CrossFit Open',
      description: '테스트용 공개 대회',
      startAt: DateTime(2026, 5, 1),
      endAt: DateTime(2026, 5, 10),
      registrationStartAt: DateTime(2026, 4, 1),
      registrationEndAt: DateTime(2026, 4, 20),
      status: CompetitionStatus.registrationOpen,
      bankName: '국민은행',
      accountNumber: '123-456',
      accountHolder: '홍주최',
      entryFee: 50000,
      scaleCategories: const ['RXD'],
      bannerImageUrl: null,
    );
  }

  @override
  Future<List<CompetitionStage>> getStages(String competitionId) async {
    return [
      CompetitionStage(
        id: 'stage-1',
        name: 'Qualifier',
        stageType: 'ONLINE',
        stageFormat: 'ONLINE',
        startAt: DateTime(2026, 5, 1),
        endAt: DateTime(2026, 5, 3),
      ),
    ];
  }

  @override
  Future<List<CompetitionEvent>> getEvents(
    String competitionId,
    String stageId,
  ) async {
    return [
      CompetitionEvent(
        id: 'event-1',
        name: 'Event 1',
        description: 'Test event',
        rulebook: '',
        eventType: 'INDIVIDUAL',
        wodType: 'FOR_TIME',
        order: 1,
        scaleCategories: const ['RXD'],
        submissionDeadline: DateTime(2026, 5, 2),
        gender: 'MALE',
        releaseAt: DateTime(2026, 5, 1),
        timeCap: 600,
        amrapDuration: null,
        emomDuration: null,
        weightUnit: 'KG',
      ),
    ];
  }

  @override
  Future<List<Registration>> getMyRegistrations(String competitionId) async {
    if (getMyRegistrationsError != null) {
      throw getMyRegistrationsError!;
    }
    return myRegistrations;
  }

  @override
  Future<MyEventScore?> getScore(
    String competitionId,
    String eventId,
    String registrationId,
  ) async {
    if (getScoreError != null) {
      throw getScoreError!;
    }
    return null;
  }

  @override
  Future<List<Competition>> getCompetitions() {
    throw UnimplementedError();
  }

  @override
  Future<List<AthleteSearchResult>> searchAthletes(
    String competitionId,
    String name,
  ) {
    throw UnimplementedError();
  }

  @override
  Future<Registration> registerTeam(
    String competitionId, {
    required String teamName,
    required String scaleCategory,
    required List<({String userId, String gender})> members,
    String? paymentNote,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<Registration> registerIndividual(
    String competitionId, {
    required String gender,
    required String scaleCategory,
    String? paymentNote,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<void> submitScore(
    String competitionId, {
    required String eventId,
    required String registrationId,
    required String videoUrl,
    required String resultStatus,
    int? resultTimeSeconds,
    int? resultRounds,
    int? resultReps,
    num? resultWeight,
    String? resultCustom,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<List<LeaderboardEntry>> getEventLeaderboard(
    String competitionId, {
    required String eventId,
    String? gender,
    String? scaleCategory,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<List<OverallLeaderboardEntry>> getOverallLeaderboard(
    String competitionId, {
    required String stageId,
    String? registrationType,
    String? gender,
    String? scaleCategory,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<AthleteProfile> getAthleteProfile(String userId) {
    throw UnimplementedError();
  }

  @override
  Future<AthleteProfile> updateAthleteProfile({
    String? biography,
    String? profileImageUrl,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<List<CompetitionHistoryItem>> getAthleteHistory(String userId) {
    throw UnimplementedError();
  }

  @override
  Future<EventLineup> setEventLineup(
    String competitionId,
    String eventId,
    String registrationId,
    List<String> memberIds,
  ) {
    throw UnimplementedError();
  }

  @override
  Future<EventLineup?> getEventLineup(
    String competitionId,
    String eventId,
    String registrationId,
  ) {
    throw UnimplementedError();
  }
}
