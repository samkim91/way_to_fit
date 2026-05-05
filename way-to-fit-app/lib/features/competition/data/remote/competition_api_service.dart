import 'package:dio/dio.dart';
import 'package:retrofit/retrofit.dart';

import '../../../../core/api/dto/api_response_dto.dart';
import '../../../../core/api/dto/page_response_dto.dart';
import '../dto/athlete_profile_dto.dart';
import '../dto/competition_dto.dart';
import '../dto/leaderboard_dto.dart';
import '../dto/registration_dto.dart';
import '../dto/score_dto.dart';

part 'competition_api_service.g.dart';

@RestApi()
abstract class CompetitionApiService {
  factory CompetitionApiService(Dio dio, {String baseUrl}) =
      _CompetitionApiService;

  @GET('/api/competitions')
  Future<ApiResponseDto<PageResponseDto<CompetitionResponseDto>>>
  getCompetitions();

  @GET('/api/competitions/{competitionId}')
  Future<ApiResponseDto<CompetitionResponseDto>> getCompetition(
    @Path('competitionId') String competitionId,
  );

  @GET('/api/competitions/{competitionId}/stages')
  Future<ApiResponseDto<List<CompetitionStageResponseDto>>> getStages(
    @Path('competitionId') String competitionId,
  );

  @GET('/api/competitions/{competitionId}/stages/{stageId}/events')
  Future<ApiResponseDto<List<CompetitionEventResponseDto>>> getEvents(
    @Path('competitionId') String competitionId,
    @Path('stageId') String stageId,
  );

  @GET('/api/competitions/{competitionId}/registrations/me')
  Future<ApiResponseDto<List<RegistrationResponseDto>>> getMyRegistration(
    @Path('competitionId') String competitionId,
  );

  @POST('/api/competitions/{competitionId}/registrations')
  Future<ApiResponseDto<RegistrationResponseDto>> registerIndividual(
    @Path('competitionId') String competitionId,
    @Body() RegisterIndividualRequestDto body,
  );

  @POST('/api/competitions/{competitionId}/events/{eventId}/scores')
  Future<HttpResponse<dynamic>> submitScore(
    @Path('competitionId') String competitionId,
    @Path('eventId') String eventId,
    @Body() SubmitScoreRequestDto body,
  );

  @GET('/api/competitions/{competitionId}/events/{eventId}/leaderboard')
  Future<ApiResponseDto<EventLeaderboardResponseDto>> getEventLeaderboard(
    @Path('competitionId') String competitionId,
    @Path('eventId') String eventId,
    @Query('gender') String? gender,
    @Query('scaleCategory') String? scaleCategory,
  );

  @GET('/api/competitions/{competitionId}/stages/{stageId}/leaderboard')
  Future<ApiResponseDto<OverallLeaderboardResponseDto>> getOverallLeaderboard(
    @Path('competitionId') String competitionId,
    @Path('stageId') String stageId,
    @Query('registrationType') String? registrationType,
    @Query('gender') String? gender,
    @Query('scaleCategory') String? scaleCategory,
  );

  @GET('/api/competitions/{competitionId}/registrations/search-athletes')
  Future<ApiResponseDto<List<AthleteSearchResponseDto>>> searchAthletes(
    @Path('competitionId') String competitionId,
    @Query('name') String name,
  );

  @POST('/api/competitions/{competitionId}/registrations/team')
  Future<ApiResponseDto<RegistrationResponseDto>> registerTeam(
    @Path('competitionId') String competitionId,
    @Body() RegisterTeamRequestDto body,
  );

  @GET('/api/athletes/{userId}')
  Future<ApiResponseDto<AthleteProfileResponseDto>> getAthleteProfile(
    @Path('userId') String userId,
  );

  @GET('/api/athletes/{userId}/competitions')
  Future<ApiResponseDto<AthleteCompetitionHistoryResponseDto>>
  getAthleteHistory(@Path('userId') String userId);
}
