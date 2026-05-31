// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'competition_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CompetitionResponseDto _$CompetitionResponseDtoFromJson(
  Map<String, dynamic> json,
) => CompetitionResponseDto(
  id: json['id'] as String,
  name: json['name'] as String,
  description: json['description'] as String,
  startAt: DateTime.parse(json['startAt'] as String),
  endAt: DateTime.parse(json['endAt'] as String),
  registrationStartAt: DateTime.parse(json['registrationStartAt'] as String),
  registrationEndAt: DateTime.parse(json['registrationEndAt'] as String),
  lifecycle: json['lifecycle'] as String,
  bankName: json['bankName'] as String,
  accountNumber: json['accountNumber'] as String,
  accountHolder: json['accountHolder'] as String,
  entryFee: (json['entryFee'] as num).toInt(),
  bannerImageUrl: json['bannerImageUrl'] as String?,
  createdAt: json['createdAt'] == null
      ? null
      : DateTime.parse(json['createdAt'] as String),
  scaleCategories: (json['scaleCategories'] as List<dynamic>)
      .map((e) => e as String)
      .toList(),
  participantsCount: (json['participantsCount'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$CompetitionResponseDtoToJson(
  CompetitionResponseDto instance,
) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'description': instance.description,
  'startAt': instance.startAt.toIso8601String(),
  'endAt': instance.endAt.toIso8601String(),
  'registrationStartAt': instance.registrationStartAt.toIso8601String(),
  'registrationEndAt': instance.registrationEndAt.toIso8601String(),
  'lifecycle': instance.lifecycle,
  'bankName': instance.bankName,
  'accountNumber': instance.accountNumber,
  'accountHolder': instance.accountHolder,
  'entryFee': instance.entryFee,
  'bannerImageUrl': instance.bannerImageUrl,
  'createdAt': instance.createdAt?.toIso8601String(),
  'scaleCategories': instance.scaleCategories,
  'participantsCount': instance.participantsCount,
};

CompetitionStageResponseDto _$CompetitionStageResponseDtoFromJson(
  Map<String, dynamic> json,
) => CompetitionStageResponseDto(
  id: json['id'] as String,
  competitionId: json['competitionId'] as String,
  name: json['name'] as String,
  stageType: json['stageType'] as String,
  stageFormat: json['stageFormat'] as String,
  startAt: DateTime.parse(json['startAt'] as String),
  endAt: DateTime.parse(json['endAt'] as String),
);

Map<String, dynamic> _$CompetitionStageResponseDtoToJson(
  CompetitionStageResponseDto instance,
) => <String, dynamic>{
  'id': instance.id,
  'competitionId': instance.competitionId,
  'name': instance.name,
  'stageType': instance.stageType,
  'stageFormat': instance.stageFormat,
  'startAt': instance.startAt.toIso8601String(),
  'endAt': instance.endAt.toIso8601String(),
};

CompetitionEventResponseDto _$CompetitionEventResponseDtoFromJson(
  Map<String, dynamic> json,
) => CompetitionEventResponseDto(
  id: json['id'] as String,
  stageId: json['stageId'] as String,
  competitionId: json['competitionId'] as String,
  name: json['name'] as String,
  description: json['description'] as String,
  rulebook: json['rulebook'] as String,
  eventType: json['eventType'] as String,
  wodType: json['wodType'] as String,
  order: (json['order'] as num).toInt(),
  scaleCategories: (json['scaleCategories'] as List<dynamic>)
      .map((e) => e as String)
      .toList(),
  submissionDeadline: DateTime.parse(json['submissionDeadline'] as String),
  gender: json['gender'] as String,
  releaseAt: json['releaseAt'] == null
      ? null
      : DateTime.parse(json['releaseAt'] as String),
  timeCap: (json['timeCap'] as num?)?.toInt(),
  amrapDuration: (json['amrapDuration'] as num?)?.toInt(),
  emomDuration: (json['emomDuration'] as num?)?.toInt(),
  weightUnit: json['weightUnit'] as String?,
);

Map<String, dynamic> _$CompetitionEventResponseDtoToJson(
  CompetitionEventResponseDto instance,
) => <String, dynamic>{
  'id': instance.id,
  'stageId': instance.stageId,
  'competitionId': instance.competitionId,
  'name': instance.name,
  'description': instance.description,
  'rulebook': instance.rulebook,
  'eventType': instance.eventType,
  'wodType': instance.wodType,
  'order': instance.order,
  'scaleCategories': instance.scaleCategories,
  'submissionDeadline': instance.submissionDeadline.toIso8601String(),
  'gender': instance.gender,
  'releaseAt': instance.releaseAt?.toIso8601String(),
  'timeCap': instance.timeCap,
  'amrapDuration': instance.amrapDuration,
  'emomDuration': instance.emomDuration,
  'weightUnit': instance.weightUnit,
};
