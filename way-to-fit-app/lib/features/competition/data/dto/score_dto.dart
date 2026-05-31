import 'package:json_annotation/json_annotation.dart';

part 'score_dto.g.dart';

@JsonSerializable()
class ScoreResponseDto {
  const ScoreResponseDto({
    required this.id,
    required this.eventId,
    required this.registrationId,
    required this.videoUrl,
    required this.status,
    required this.resultStatus,
    this.reviewerNote,
    this.resultTimeSeconds,
    this.resultRounds,
    this.resultReps,
    this.resultWeight,
    this.resultCustom,
  });

  final String id;
  final String eventId;
  final String registrationId;
  final String videoUrl;
  final String status;
  final String resultStatus;
  final String? reviewerNote;
  final int? resultTimeSeconds;
  final int? resultRounds;
  final int? resultReps;
  final num? resultWeight;
  final String? resultCustom;

  factory ScoreResponseDto.fromJson(Map<String, dynamic> json) =>
      _$ScoreResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$ScoreResponseDtoToJson(this);
}

@JsonSerializable()
class SubmitScoreRequestDto {
  const SubmitScoreRequestDto({
    required this.registrationId,
    required this.videoUrl,
    required this.resultTimeSeconds,
    required this.resultRounds,
    required this.resultReps,
    required this.resultWeight,
    required this.resultCustom,
    required this.resultStatus,
  });

  final String registrationId;
  final String videoUrl;
  final int? resultTimeSeconds;
  final int? resultRounds;
  final int? resultReps;
  final num? resultWeight;
  final String? resultCustom;
  final String resultStatus;

  factory SubmitScoreRequestDto.fromJson(Map<String, dynamic> json) =>
      _$SubmitScoreRequestDtoFromJson(json);

  Map<String, dynamic> toJson() => _$SubmitScoreRequestDtoToJson(this);
}
