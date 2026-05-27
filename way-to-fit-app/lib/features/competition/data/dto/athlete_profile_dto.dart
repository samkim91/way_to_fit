import 'package:json_annotation/json_annotation.dart';

import '../../domain/models.dart';

part 'athlete_profile_dto.g.dart';

@JsonSerializable()
class AthleteSearchResponseDto {
  const AthleteSearchResponseDto({
    required this.userId,
    required this.name,
    required this.gender,
    required this.profileImageUrl,
  });

  final String userId;
  final String name;
  final String? gender;
  final String? profileImageUrl;

  factory AthleteSearchResponseDto.fromJson(Map<String, dynamic> json) =>
      _$AthleteSearchResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$AthleteSearchResponseDtoToJson(this);

  AthleteSearchResult toDomain() => AthleteSearchResult(
    userId: userId,
    name: name,
    gender: gender,
    profileImageUrl: profileImageUrl,
  );
}

@JsonSerializable()
class AthleteProfileResponseDto {
  const AthleteProfileResponseDto({
    required this.id,
    required this.userId,
    required this.biography,
    required this.profileImageUrl,
  });

  final String? id;
  final String userId;
  final String? biography;
  final String? profileImageUrl;

  factory AthleteProfileResponseDto.fromJson(Map<String, dynamic> json) =>
      _$AthleteProfileResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$AthleteProfileResponseDtoToJson(this);

  AthleteProfile toDomain() => AthleteProfile(
    userId: userId,
    biography: biography,
    profileImageUrl: profileImageUrl,
  );
}

@JsonSerializable()
class UpdateAthleteProfileRequestDto {
  const UpdateAthleteProfileRequestDto({
    required this.biography,
    required this.profileImageUrl,
  });

  final String? biography;
  final String? profileImageUrl;

  factory UpdateAthleteProfileRequestDto.fromJson(Map<String, dynamic> json) =>
      _$UpdateAthleteProfileRequestDtoFromJson(json);

  Map<String, dynamic> toJson() => _$UpdateAthleteProfileRequestDtoToJson(this);
}

@JsonSerializable()
class AthleteCompetitionHistoryResponseDto {
  const AthleteCompetitionHistoryResponseDto({
    required this.userId,
    required this.items,
  });

  final String userId;
  final List<CompetitionHistoryItemDto> items;

  factory AthleteCompetitionHistoryResponseDto.fromJson(
    Map<String, dynamic> json,
  ) => _$AthleteCompetitionHistoryResponseDtoFromJson(json);

  Map<String, dynamic> toJson() =>
      _$AthleteCompetitionHistoryResponseDtoToJson(this);
}

@JsonSerializable()
class CompetitionHistoryItemDto {
  const CompetitionHistoryItemDto({
    required this.competitionId,
    required this.name,
    required this.bannerImageUrl,
    required this.endAt,
    required this.registrationId,
    required this.registrationType,
    required this.scaleCategory,
    required this.overallRank,
    required this.totalPoints,
    required this.eventScores,
  });

  final String competitionId;
  final String name;
  final String? bannerImageUrl;
  final DateTime endAt;
  final String registrationId;
  final String registrationType;
  final String scaleCategory;
  final int? overallRank;
  final int? totalPoints;
  final List<EventScoreItemDto> eventScores;

  factory CompetitionHistoryItemDto.fromJson(Map<String, dynamic> json) =>
      _$CompetitionHistoryItemDtoFromJson(json);

  Map<String, dynamic> toJson() => _$CompetitionHistoryItemDtoToJson(this);

  CompetitionHistoryItem toDomain() => CompetitionHistoryItem(
    competitionId: competitionId,
    name: name,
    bannerImageUrl: bannerImageUrl,
    endAt: endAt.toLocal(),
    registrationType: RegistrationType.fromJson(registrationType),
    scaleCategory: scaleCategory,
    overallRank: overallRank,
    totalPoints: totalPoints,
    eventScores: eventScores.map((score) => score.toDomain()).toList(),
  );
}

@JsonSerializable()
class EventScoreItemDto {
  const EventScoreItemDto({
    required this.eventId,
    required this.eventName,
    required this.rank,
    required this.resultStatus,
    required this.resultTimeSeconds,
    required this.resultRounds,
    required this.resultReps,
    required this.resultWeight,
    required this.resultCustom,
  });

  final String eventId;
  final String eventName;
  final int? rank;
  final String? resultStatus;
  final int? resultTimeSeconds;
  final int? resultRounds;
  final int? resultReps;
  final num? resultWeight;
  final String? resultCustom;

  factory EventScoreItemDto.fromJson(Map<String, dynamic> json) =>
      _$EventScoreItemDtoFromJson(json);

  Map<String, dynamic> toJson() => _$EventScoreItemDtoToJson(this);

  EventScoreItem toDomain() => EventScoreItem(
    eventId: eventId,
    eventName: eventName,
    rank: rank,
    resultStatus: resultStatus,
    resultTimeSeconds: resultTimeSeconds,
    resultRounds: resultRounds,
    resultReps: resultReps,
    resultWeight: resultWeight,
    resultCustom: resultCustom,
  );
}
