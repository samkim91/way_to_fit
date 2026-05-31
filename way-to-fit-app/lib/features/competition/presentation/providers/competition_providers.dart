import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dio/dio.dart';

import '../../../../core/auth/auth_session.dart';
import '../../../../core/api/api_exception.dart';
import '../../data/competition_repository.dart';
import '../../domain/models.dart';

final competitionListProvider = FutureProvider.autoDispose<List<Competition>>((
  ref,
) async {
  final repository = ref.watch(competitionRepositoryProvider);
  return repository.getCompetitions();
});

Registration? _matchRegistration(
  List<Registration> registrations,
  String eventType,
) {
  final type = eventType == 'TEAM'
      ? RegistrationType.team
      : RegistrationType.individual;
  for (final r in registrations) {
    if (r.registrationType == type) return r;
  }
  return null;
}

bool _isAuthFailure(Object error) {
  if (error is ApiException) {
    return error.statusCode == 401 || error.statusCode == 403;
  }
  if (error is DioException) {
    final statusCode = error.response?.statusCode;
    return statusCode == 401 || statusCode == 403;
  }
  return false;
}

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
        try {
          myRegistrations = await repository.getMyRegistrations(competitionId);
        } catch (error) {
          if (!_isAuthFailure(error)) {
            rethrow;
          }
        }
      }

      final myScores = <String, MyEventScore>{};
      if (myRegistrations.isNotEmpty) {
        final futures = <MapEntry<String, Future<MyEventScore?>>>[];
        for (final bundle in stageBundles) {
          for (final event in bundle.events) {
            final reg = _matchRegistration(myRegistrations, event.eventType);
            if (reg != null && reg.paymentStatus == PaymentStatus.confirmed) {
              futures.add(
                MapEntry(
                  event.id,
                  repository.getScore(competitionId, event.id, reg.id),
                ),
              );
            }
          }
        }
        List<MyEventScore?> results;
        try {
          results = await Future.wait(futures.map((e) => e.value));
        } catch (error) {
          if (!_isAuthFailure(error)) {
            rethrow;
          }
          results = const [];
        }
        for (var i = 0; i < futures.length; i++) {
          if (i >= results.length) break;
          final score = results[i];
          if (score != null) myScores[futures[i].key] = score;
        }
      }

      return CompetitionDetailBundle(
        competition: competition,
        stages: stageBundles,
        myRegistrations: myRegistrations,
        myScores: myScores,
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

final myProfileProvider = FutureProvider.autoDispose<AthleteProfileBundle>((
  ref,
) async {
  final authState = ref.watch(authControllerProvider).valueOrNull;
  final userId = authState?.userId;
  if (userId == null || userId.isEmpty) {
    throw StateError('로그인이 필요합니다.');
  }

  final repository = ref.watch(competitionRepositoryProvider);
  final profile = await repository.getAthleteProfile(userId);
  final history = await repository.getAthleteHistory(userId);
  return AthleteProfileBundle(profile: profile, history: history);
});
