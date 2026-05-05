// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'leaderboard_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventLeaderboardResponseDto _$EventLeaderboardResponseDtoFromJson(
  Map<String, dynamic> json,
) => EventLeaderboardResponseDto(
  eventId: json['eventId'] as String,
  entries: (json['entries'] as List<dynamic>)
      .map(
        (e) => LeaderboardEntryResponseDto.fromJson(e as Map<String, dynamic>),
      )
      .toList(),
);

Map<String, dynamic> _$EventLeaderboardResponseDtoToJson(
  EventLeaderboardResponseDto instance,
) => <String, dynamic>{
  'eventId': instance.eventId,
  'entries': instance.entries,
};

OverallLeaderboardResponseDto _$OverallLeaderboardResponseDtoFromJson(
  Map<String, dynamic> json,
) => OverallLeaderboardResponseDto(
  stageId: json['stageId'] as String,
  entries: (json['entries'] as List<dynamic>)
      .map(
        (e) => OverallLeaderboardEntryResponseDto.fromJson(
          e as Map<String, dynamic>,
        ),
      )
      .toList(),
);

Map<String, dynamic> _$OverallLeaderboardResponseDtoToJson(
  OverallLeaderboardResponseDto instance,
) => <String, dynamic>{
  'stageId': instance.stageId,
  'entries': instance.entries,
};

LeaderboardEntryResponseDto _$LeaderboardEntryResponseDtoFromJson(
  Map<String, dynamic> json,
) => LeaderboardEntryResponseDto(
  rank: (json['rank'] as num).toInt(),
  registrationId: json['registrationId'] as String,
  participantName: json['participantName'] as String,
  scaleCategory: json['scaleCategory'] as String,
  resultTimeSeconds: (json['resultTimeSeconds'] as num?)?.toInt(),
  resultRounds: (json['resultRounds'] as num?)?.toInt(),
  resultReps: (json['resultReps'] as num?)?.toInt(),
  resultWeight: json['resultWeight'] as num?,
  resultCustom: json['resultCustom'] as String?,
  resultStatus: json['resultStatus'] as String?,
  videoUrl: json['videoUrl'] as String,
  memberIds: (json['memberIds'] as List<dynamic>)
      .map((e) => e as String)
      .toList(),
);

Map<String, dynamic> _$LeaderboardEntryResponseDtoToJson(
  LeaderboardEntryResponseDto instance,
) => <String, dynamic>{
  'rank': instance.rank,
  'registrationId': instance.registrationId,
  'participantName': instance.participantName,
  'scaleCategory': instance.scaleCategory,
  'resultTimeSeconds': instance.resultTimeSeconds,
  'resultRounds': instance.resultRounds,
  'resultReps': instance.resultReps,
  'resultWeight': instance.resultWeight,
  'resultCustom': instance.resultCustom,
  'resultStatus': instance.resultStatus,
  'videoUrl': instance.videoUrl,
  'memberIds': instance.memberIds,
};

OverallLeaderboardEntryResponseDto _$OverallLeaderboardEntryResponseDtoFromJson(
  Map<String, dynamic> json,
) => OverallLeaderboardEntryResponseDto(
  rank: (json['rank'] as num).toInt(),
  registrationId: json['registrationId'] as String,
  participantName: json['participantName'] as String,
  scaleCategory: json['scaleCategory'] as String,
  totalPoints: (json['totalPoints'] as num).toInt(),
  eventRanks: Map<String, int>.from(json['eventRanks'] as Map),
  manualRank: (json['manualRank'] as num?)?.toInt(),
  memberIds: (json['memberIds'] as List<dynamic>)
      .map((e) => e as String)
      .toList(),
);

Map<String, dynamic> _$OverallLeaderboardEntryResponseDtoToJson(
  OverallLeaderboardEntryResponseDto instance,
) => <String, dynamic>{
  'rank': instance.rank,
  'registrationId': instance.registrationId,
  'participantName': instance.participantName,
  'scaleCategory': instance.scaleCategory,
  'totalPoints': instance.totalPoints,
  'eventRanks': instance.eventRanks,
  'manualRank': instance.manualRank,
  'memberIds': instance.memberIds,
};
