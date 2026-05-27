import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:dio/dio.dart';

import '../../../core/api/api_client.dart';
import '../../../core/api/api_exception.dart';
import '../../../core/api/dto/api_response_dto.dart';
import '../../../core/api/dto/page_response_dto.dart';
import '../domain/models.dart';
import 'dto/athlete_profile_dto.dart';
import 'dto/competition_dto.dart';
import 'dto/leaderboard_dto.dart';
import 'dto/registration_dto.dart';
import 'dto/score_dto.dart';
import 'remote/competition_api_service.dart';

final competitionApiServiceProvider = Provider<CompetitionApiService>((ref) {
  final dio = ref.watch(dioProvider);
  return CompetitionApiService(dio);
});

final competitionRepositoryProvider = Provider<CompetitionRepository>((ref) {
  return CompetitionRepositoryImpl(ref.watch(competitionApiServiceProvider));
});

abstract class CompetitionRepository {
  Future<List<Competition>> getCompetitions();
  Future<Competition> getCompetition(String competitionId);
  Future<List<CompetitionStage>> getStages(String competitionId);
  Future<List<CompetitionEvent>> getEvents(
    String competitionId,
    String stageId,
  );
  Future<List<Registration>> getMyRegistrations(String competitionId);
  Future<List<AthleteSearchResult>> searchAthletes(
    String competitionId,
    String name,
  );
  Future<Registration> registerTeam(
    String competitionId, {
    required String teamName,
    required String scaleCategory,
    required List<({String userId, String gender})> members,
    String? paymentNote,
  });
  Future<Registration> registerIndividual(
    String competitionId, {
    required String gender,
    required String scaleCategory,
    String? paymentNote,
  });
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
  });
  Future<List<LeaderboardEntry>> getEventLeaderboard(
    String competitionId, {
    required String eventId,
    String? gender,
    String? scaleCategory,
  });
  Future<List<OverallLeaderboardEntry>> getOverallLeaderboard(
    String competitionId, {
    required String stageId,
    String? registrationType,
    String? gender,
    String? scaleCategory,
  });
  Future<AthleteProfile> getAthleteProfile(String userId);
  Future<AthleteProfile> updateAthleteProfile({
    String? biography,
    String? profileImageUrl,
  });
  Future<List<CompetitionHistoryItem>> getAthleteHistory(String userId);
  Future<EventLineup> setEventLineup(
    String competitionId,
    String eventId,
    String registrationId,
    List<String> memberIds,
  );
  Future<EventLineup?> getEventLineup(
    String competitionId,
    String eventId,
    String registrationId,
  );
}

class CompetitionRepositoryImpl implements CompetitionRepository {
  CompetitionRepositoryImpl(this._service);

  final CompetitionApiService _service;

  @override
  Future<List<Competition>> getCompetitions() async {
    final response = await _service.getCompetitions();
    final page = _requireData<PageResponseDto<CompetitionResponseDto>>(
      response,
    );
    return page.content.map((item) => item.toDomain()).toList();
  }

  @override
  Future<Competition> getCompetition(String competitionId) async {
    final response = await _service.getCompetition(competitionId);
    return _requireData<CompetitionResponseDto>(response).toDomain();
  }

  @override
  Future<List<CompetitionStage>> getStages(String competitionId) async {
    final response = await _service.getStages(competitionId);
    return _requireData<List<CompetitionStageResponseDto>>(
      response,
    ).map((item) => item.toDomain()).toList();
  }

  @override
  Future<List<CompetitionEvent>> getEvents(
    String competitionId,
    String stageId,
  ) async {
    final response = await _service.getEvents(competitionId, stageId);
    return _requireData<List<CompetitionEventResponseDto>>(
      response,
    ).map((item) => item.toDomain()).toList();
  }

  @override
  Future<List<Registration>> getMyRegistrations(String competitionId) async {
    final response = await _service.getMyRegistration(competitionId);
    return (response.data ?? []).map((dto) => dto.toDomain()).toList();
  }

  @override
  Future<List<AthleteSearchResult>> searchAthletes(
    String competitionId,
    String name,
  ) async {
    final response = await _service.searchAthletes(competitionId, name);
    return _requireData<List<AthleteSearchResponseDto>>(
      response,
    ).map((dto) => dto.toDomain()).toList();
  }

  @override
  Future<Registration> registerTeam(
    String competitionId, {
    required String teamName,
    required String scaleCategory,
    required List<({String userId, String gender})> members,
    String? paymentNote,
  }) async {
    final response = await _service.registerTeam(
      competitionId,
      RegisterTeamRequestDto(
        teamName: teamName,
        scaleCategory: scaleCategory,
        members: members
            .map((m) => TeamMemberInputDto(userId: m.userId, gender: m.gender))
            .toList(),
        paymentNote: paymentNote,
      ),
    );
    return _requireData<RegistrationResponseDto>(response).toDomain();
  }

  @override
  Future<Registration> registerIndividual(
    String competitionId, {
    required String gender,
    required String scaleCategory,
    String? paymentNote,
  }) async {
    final response = await _service.registerIndividual(
      competitionId,
      RegisterIndividualRequestDto(
        scaleCategory: scaleCategory,
        gender: gender,
        paymentNote: paymentNote,
      ),
    );
    return _requireData<RegistrationResponseDto>(response).toDomain();
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
  }) async {
    final response = await _service.submitScore(
      competitionId,
      eventId,
      SubmitScoreRequestDto(
        registrationId: registrationId,
        videoUrl: videoUrl,
        resultTimeSeconds: resultTimeSeconds,
        resultRounds: resultRounds,
        resultReps: resultReps,
        resultWeight: resultWeight,
        resultCustom: resultCustom,
        resultStatus: resultStatus,
      ),
    );
    if (response.response.statusCode == null ||
        response.response.statusCode! >= 400) {
      throw const ApiException('기록 제출 응답이 올바르지 않습니다.');
    }
  }

  @override
  Future<List<LeaderboardEntry>> getEventLeaderboard(
    String competitionId, {
    required String eventId,
    String? gender,
    String? scaleCategory,
  }) async {
    final response = await _service.getEventLeaderboard(
      competitionId,
      eventId,
      gender,
      scaleCategory,
    );
    return _requireData<EventLeaderboardResponseDto>(
      response,
    ).entries.map((item) => item.toDomain()).toList();
  }

  @override
  Future<List<OverallLeaderboardEntry>> getOverallLeaderboard(
    String competitionId, {
    required String stageId,
    String? registrationType,
    String? gender,
    String? scaleCategory,
  }) async {
    final response = await _service.getOverallLeaderboard(
      competitionId,
      stageId,
      registrationType,
      gender,
      scaleCategory,
    );
    return _requireData<OverallLeaderboardResponseDto>(
      response,
    ).entries.map((item) => item.toDomain()).toList();
  }

  @override
  Future<AthleteProfile> getAthleteProfile(String userId) async {
    final response = await _service.getAthleteProfile(userId);
    return _requireData<AthleteProfileResponseDto>(response).toDomain();
  }

  @override
  Future<AthleteProfile> updateAthleteProfile({
    String? biography,
    String? profileImageUrl,
  }) async {
    final response = await _service.updateAthleteProfile(
      UpdateAthleteProfileRequestDto(
        biography: biography,
        profileImageUrl: profileImageUrl,
      ),
    );
    return _requireData<AthleteProfileResponseDto>(response).toDomain();
  }

  @override
  Future<List<CompetitionHistoryItem>> getAthleteHistory(String userId) async {
    final response = await _service.getAthleteHistory(userId);
    return _requireData<AthleteCompetitionHistoryResponseDto>(
      response,
    ).items.map((item) => item.toDomain()).toList();
  }

  @override
  Future<EventLineup> setEventLineup(
    String competitionId,
    String eventId,
    String registrationId,
    List<String> memberIds,
  ) async {
    final response = await _service.setEventLineup(
      competitionId,
      eventId,
      registrationId,
      SetLineupRequestDto(participatingMemberIds: memberIds),
    );
    return _requireData<EventLineupResponseDto>(response).toDomain();
  }

  @override
  Future<EventLineup?> getEventLineup(
    String competitionId,
    String eventId,
    String registrationId,
  ) async {
    try {
      final response = await _service.getEventLineup(
        competitionId,
        eventId,
        registrationId,
      );
      return response.data?.toDomain();
    } on DioException catch (e) {
      if (e.response?.statusCode == 404) return null;
      rethrow;
    }
  }

  T _requireData<T>(ApiResponseDto<T> response) {
    final data = response.data;
    if (data == null) {
      throw ApiException(
        response.message.isEmpty ? '응답 데이터가 비어 있습니다.' : response.message,
      );
    }
    return data;
  }
}
