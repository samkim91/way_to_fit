// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'athlete_profile_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AthleteSearchResponseDto _$AthleteSearchResponseDtoFromJson(
  Map<String, dynamic> json,
) => AthleteSearchResponseDto(
  userId: json['userId'] as String,
  name: json['name'] as String,
  gender: json['gender'] as String?,
  profileImageUrl: json['profileImageUrl'] as String?,
);

Map<String, dynamic> _$AthleteSearchResponseDtoToJson(
  AthleteSearchResponseDto instance,
) => <String, dynamic>{
  'userId': instance.userId,
  'name': instance.name,
  'gender': instance.gender,
  'profileImageUrl': instance.profileImageUrl,
};

AthleteProfileResponseDto _$AthleteProfileResponseDtoFromJson(
  Map<String, dynamic> json,
) => AthleteProfileResponseDto(
  id: json['id'] as String?,
  userId: json['userId'] as String,
  biography: json['biography'] as String?,
  profileImageUrl: json['profileImageUrl'] as String?,
);

Map<String, dynamic> _$AthleteProfileResponseDtoToJson(
  AthleteProfileResponseDto instance,
) => <String, dynamic>{
  'id': instance.id,
  'userId': instance.userId,
  'biography': instance.biography,
  'profileImageUrl': instance.profileImageUrl,
};

UpdateAthleteProfileRequestDto _$UpdateAthleteProfileRequestDtoFromJson(
  Map<String, dynamic> json,
) => UpdateAthleteProfileRequestDto(
  biography: json['biography'] as String?,
  profileImageUrl: json['profileImageUrl'] as String?,
);

Map<String, dynamic> _$UpdateAthleteProfileRequestDtoToJson(
  UpdateAthleteProfileRequestDto instance,
) => <String, dynamic>{
  'biography': instance.biography,
  'profileImageUrl': instance.profileImageUrl,
};

AthleteCompetitionHistoryResponseDto
_$AthleteCompetitionHistoryResponseDtoFromJson(Map<String, dynamic> json) =>
    AthleteCompetitionHistoryResponseDto(
      userId: json['userId'] as String,
      items: (json['items'] as List<dynamic>)
          .map(
            (e) =>
                CompetitionHistoryItemDto.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    );

Map<String, dynamic> _$AthleteCompetitionHistoryResponseDtoToJson(
  AthleteCompetitionHistoryResponseDto instance,
) => <String, dynamic>{'userId': instance.userId, 'items': instance.items};

CompetitionHistoryItemDto _$CompetitionHistoryItemDtoFromJson(
  Map<String, dynamic> json,
) => CompetitionHistoryItemDto(
  competitionId: json['competitionId'] as String,
  name: json['name'] as String,
  bannerImageUrl: json['bannerImageUrl'] as String?,
  endAt: DateTime.parse(json['endAt'] as String),
  registrationId: json['registrationId'] as String,
  registrationType: json['registrationType'] as String,
  scaleCategory: json['scaleCategory'] as String,
  overallRank: (json['overallRank'] as num?)?.toInt(),
  totalPoints: (json['totalPoints'] as num?)?.toInt(),
  eventScores: (json['eventScores'] as List<dynamic>)
      .map((e) => EventScoreItemDto.fromJson(e as Map<String, dynamic>))
      .toList(),
);

Map<String, dynamic> _$CompetitionHistoryItemDtoToJson(
  CompetitionHistoryItemDto instance,
) => <String, dynamic>{
  'competitionId': instance.competitionId,
  'name': instance.name,
  'bannerImageUrl': instance.bannerImageUrl,
  'endAt': instance.endAt.toIso8601String(),
  'registrationId': instance.registrationId,
  'registrationType': instance.registrationType,
  'scaleCategory': instance.scaleCategory,
  'overallRank': instance.overallRank,
  'totalPoints': instance.totalPoints,
  'eventScores': instance.eventScores,
};

EventScoreItemDto _$EventScoreItemDtoFromJson(Map<String, dynamic> json) =>
    EventScoreItemDto(
      eventId: json['eventId'] as String,
      eventName: json['eventName'] as String,
      rank: (json['rank'] as num?)?.toInt(),
      resultStatus: json['resultStatus'] as String?,
      resultTimeSeconds: (json['resultTimeSeconds'] as num?)?.toInt(),
      resultRounds: (json['resultRounds'] as num?)?.toInt(),
      resultReps: (json['resultReps'] as num?)?.toInt(),
      resultWeight: json['resultWeight'] as num?,
      resultCustom: json['resultCustom'] as String?,
    );

Map<String, dynamic> _$EventScoreItemDtoToJson(EventScoreItemDto instance) =>
    <String, dynamic>{
      'eventId': instance.eventId,
      'eventName': instance.eventName,
      'rank': instance.rank,
      'resultStatus': instance.resultStatus,
      'resultTimeSeconds': instance.resultTimeSeconds,
      'resultRounds': instance.resultRounds,
      'resultReps': instance.resultReps,
      'resultWeight': instance.resultWeight,
      'resultCustom': instance.resultCustom,
    };
