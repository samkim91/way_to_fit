import 'package:json_annotation/json_annotation.dart';

part 'score_dto.g.dart';

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
