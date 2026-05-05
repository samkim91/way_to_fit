// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'score_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

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
