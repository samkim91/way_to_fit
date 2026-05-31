// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'score_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ScoreResponseDto _$ScoreResponseDtoFromJson(Map<String, dynamic> json) =>
    ScoreResponseDto(
      id: json['id'] as String,
      eventId: json['eventId'] as String,
      registrationId: json['registrationId'] as String,
      videoUrl: json['videoUrl'] as String,
      status: json['status'] as String,
      resultStatus: json['resultStatus'] as String,
      reviewerNote: json['reviewerNote'] as String?,
      resultTimeSeconds: (json['resultTimeSeconds'] as num?)?.toInt(),
      resultRounds: (json['resultRounds'] as num?)?.toInt(),
      resultReps: (json['resultReps'] as num?)?.toInt(),
      resultWeight: json['resultWeight'] as num?,
      resultCustom: json['resultCustom'] as String?,
    );

Map<String, dynamic> _$ScoreResponseDtoToJson(ScoreResponseDto instance) =>
    <String, dynamic>{
      'id': instance.id,
      'eventId': instance.eventId,
      'registrationId': instance.registrationId,
      'videoUrl': instance.videoUrl,
      'status': instance.status,
      'resultStatus': instance.resultStatus,
      'reviewerNote': instance.reviewerNote,
      'resultTimeSeconds': instance.resultTimeSeconds,
      'resultRounds': instance.resultRounds,
      'resultReps': instance.resultReps,
      'resultWeight': instance.resultWeight,
      'resultCustom': instance.resultCustom,
    };

SubmitScoreRequestDto _$SubmitScoreRequestDtoFromJson(
  Map<String, dynamic> json,
) => SubmitScoreRequestDto(
  registrationId: json['registrationId'] as String,
  videoUrl: json['videoUrl'] as String,
  resultTimeSeconds: (json['resultTimeSeconds'] as num?)?.toInt(),
  resultRounds: (json['resultRounds'] as num?)?.toInt(),
  resultReps: (json['resultReps'] as num?)?.toInt(),
  resultWeight: json['resultWeight'] as num?,
  resultCustom: json['resultCustom'] as String?,
  resultStatus: json['resultStatus'] as String,
);

Map<String, dynamic> _$SubmitScoreRequestDtoToJson(
  SubmitScoreRequestDto instance,
) => <String, dynamic>{
  'registrationId': instance.registrationId,
  'videoUrl': instance.videoUrl,
  'resultTimeSeconds': instance.resultTimeSeconds,
  'resultRounds': instance.resultRounds,
  'resultReps': instance.resultReps,
  'resultWeight': instance.resultWeight,
  'resultCustom': instance.resultCustom,
  'resultStatus': instance.resultStatus,
};
