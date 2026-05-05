import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/auth/auth_session.dart';
import '../../data/competition_repository.dart';
import '../../domain/models.dart';

final competitionListProvider = FutureProvider.autoDispose<List<Competition>>((
  ref,
) async {
  final repository = ref.watch(competitionRepositoryProvider);
  return repository.getCompetitions();
});

final competitionDetailProvider = FutureProvider.autoDispose
    .family<CompetitionDetailBundle, String>((ref, competitionId) async {
      final repository = ref.watch(competitionRepositoryProvider);
      final authState = ref.watch(authControllerProvider).valueOrNull;

      final competition = await repository.getCompetition(competitionId);
      final stages = await repository.getStages(competitionId);
      final stageBundles = <CompetitionStageBundle>[];
      for (final stage in stages) {
        final events = await repository.getEvents(competitionId, stage.id);
        stageBundles.add(CompetitionStageBundle(stage: stage, events: events));
      }

      List<Registration> myRegistrations = [];
      if (authState?.isAuthenticated == true) {
        myRegistrations = await repository.getMyRegistrations(competitionId);
      }

      return CompetitionDetailBundle(
        competition: competition,
        stages: stageBundles,
        myRegistrations: myRegistrations,
      );
    });

class LeaderboardQuery {
  const LeaderboardQuery({
    required this.competitionId,
    required this.stageId,
    required this.eventId,
    required this.registrationType,
    this.gender,
    this.scaleCategory,
  });

  final String competitionId;
  final String stageId;
  final String? eventId;
  final RegistrationType registrationType;
  final String? gender;
  final String? scaleCategory;
}

final eventLeaderboardProvider = FutureProvider.autoDispose
    .family<List<LeaderboardEntry>, LeaderboardQuery>((ref, query) async {
      final repository = ref.watch(competitionRepositoryProvider);
      return repository.getEventLeaderboard(
        query.competitionId,
        eventId: query.eventId!,
        gender: query.gender,
        scaleCategory: query.scaleCategory,
      );
    });

final overallLeaderboardProvider = FutureProvider.autoDispose
    .family<List<OverallLeaderboardEntry>, LeaderboardQuery>((
      ref,
      query,
    ) async {
      final repository = ref.watch(competitionRepositoryProvider);
      return repository.getOverallLeaderboard(
        query.competitionId,
        stageId: query.stageId,
        registrationType: query.registrationType == RegistrationType.team
            ? 'TEAM'
            : 'INDIVIDUAL',
        gender: query.gender,
        scaleCategory: query.scaleCategory,
      );
    });

final athleteProfileProvider = FutureProvider.autoDispose
    .family<AthleteProfileBundle, String>((ref, userId) async {
      final repository = ref.watch(competitionRepositoryProvider);
      final profile = await repository.getAthleteProfile(userId);
      final history = await repository.getAthleteHistory(userId);
      return AthleteProfileBundle(profile: profile, history: history);
    });
