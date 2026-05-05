import 'package:json_annotation/json_annotation.dart';

import '../../domain/models.dart';

part 'leaderboard_dto.g.dart';

@JsonSerializable()
class EventLeaderboardResponseDto {
  const EventLeaderboardResponseDto({
    required this.eventId,
    required this.entries,
  });

  final String eventId;
  final List<LeaderboardEntryResponseDto> entries;

  factory EventLeaderboardResponseDto.fromJson(Map<String, dynamic> json) =>
      _$EventLeaderboardResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$EventLeaderboardResponseDtoToJson(this);
}

@JsonSerializable()
class OverallLeaderboardResponseDto {
  const OverallLeaderboardResponseDto({
    required this.stageId,
    required this.entries,
  });

  final String stageId;
  final List<OverallLeaderboardEntryResponseDto> entries;

  factory OverallLeaderboardResponseDto.fromJson(Map<String, dynamic> json) =>
      _$OverallLeaderboardResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$OverallLeaderboardResponseDtoToJson(this);
}

@JsonSerializable()
class LeaderboardEntryResponseDto {
  const LeaderboardEntryResponseDto({
    required this.rank,
    required this.registrationId,
    required this.participantName,
    required this.scaleCategory,
    required this.resultTimeSeconds,
    required this.resultRounds,
    required this.resultReps,
    required this.resultWeight,
    required this.resultCustom,
    required this.resultStatus,
    required this.videoUrl,
    required this.memberIds,
  });

  final int rank;
  final String registrationId;
  final String participantName;
  final String scaleCategory;
  final int? resultTimeSeconds;
  final int? resultRounds;
  final int? resultReps;
  final num? resultWeight;
  final String? resultCustom;
  final String? resultStatus;
  final String videoUrl;
  final List<String> memberIds;

  factory LeaderboardEntryResponseDto.fromJson(Map<String, dynamic> json) =>
      _$LeaderboardEntryResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$LeaderboardEntryResponseDtoToJson(this);

  LeaderboardEntry toDomain() => LeaderboardEntry(
    rank: rank,
    registrationId: registrationId,
    participantName: participantName,
    scaleCategory: scaleCategory,
    resultTimeSeconds: resultTimeSeconds,
    resultRounds: resultRounds,
    resultReps: resultReps,
    resultWeight: resultWeight,
    resultCustom: resultCustom,
    resultStatus: resultStatus,
    videoUrl: videoUrl,
    memberIds: memberIds,
  );
}

@JsonSerializable()
class OverallLeaderboardEntryResponseDto {
  const OverallLeaderboardEntryResponseDto({
    required this.rank,
    required this.registrationId,
    required this.participantName,
    required this.scaleCategory,
    required this.totalPoints,
    required this.eventRanks,
    required this.manualRank,
    required this.memberIds,
  });

  final int rank;
  final String registrationId;
  final String participantName;
  final String scaleCategory;
  final int totalPoints;
  final Map<String, int> eventRanks;
  final int? manualRank;
  final List<String> memberIds;

  factory OverallLeaderboardEntryResponseDto.fromJson(
    Map<String, dynamic> json,
  ) => _$OverallLeaderboardEntryResponseDtoFromJson(json);

  Map<String, dynamic> toJson() =>
      _$OverallLeaderboardEntryResponseDtoToJson(this);

  OverallLeaderboardEntry toDomain() => OverallLeaderboardEntry(
    rank: rank,
    registrationId: registrationId,
    participantName: participantName,
    scaleCategory: scaleCategory,
    totalPoints: totalPoints,
    eventRanks: eventRanks,
    manualRank: manualRank,
    memberIds: memberIds,
  );
}
