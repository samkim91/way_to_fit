// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:way_to_fit_app/features/competition/data/competition_repository.dart';
import 'package:way_to_fit_app/features/competition/domain/models.dart';
import 'package:way_to_fit_app/main.dart';

void main() {
  testWidgets('competition list screen renders', (WidgetTester tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          competitionRepositoryProvider.overrideWithValue(
            _FakeCompetitionRepository(),
          ),
        ],
        child: const WayToFitApp(),
      ),
    );
    await tester.pump();

    expect(find.text('대회'), findsOneWidget);
  });
}

class _FakeCompetitionRepository extends CompetitionRepository {
  @override
  Future<List<Competition>> getCompetitions() async {
    return [
      Competition(
        id: 'competition-1',
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
        bannerImageUrl: null,
      ),
    ];
  }

  @override
  Future<AthleteProfile> getAthleteProfile(String userId) {
    throw UnimplementedError();
  }

  @override
  Future<List<CompetitionHistoryItem>> getAthleteHistory(String userId) {
    throw UnimplementedError();
  }

  @override
  Future<Competition> getCompetition(String competitionId) {
    throw UnimplementedError();
  }

  @override
  Future<List<CompetitionEvent>> getEvents(
    String competitionId,
    String stageId,
  ) {
    throw UnimplementedError();
  }

  @override
  Future<List<CompetitionStage>> getStages(String competitionId) {
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
  Future<List<Registration>> getMyRegistrations(String competitionId) async {
    return [];
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
}
