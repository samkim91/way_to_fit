import 'package:json_annotation/json_annotation.dart';

import '../../domain/models.dart';

part 'competition_dto.g.dart';

@JsonSerializable()
class CompetitionResponseDto {
  const CompetitionResponseDto({
    required this.id,
    required this.name,
    required this.description,
    required this.startAt,
    required this.endAt,
    required this.registrationStartAt,
    required this.registrationEndAt,
    required this.status,
    required this.bankName,
    required this.accountNumber,
    required this.accountHolder,
    required this.entryFee,
    required this.bannerImageUrl,
    required this.createdAt,
  });

  final String id;
  final String name;
  final String description;
  final DateTime startAt;
  final DateTime endAt;
  final DateTime registrationStartAt;
  final DateTime registrationEndAt;
  final String status;
  final String bankName;
  final String accountNumber;
  final String accountHolder;
  final int entryFee;
  final String? bannerImageUrl;
  final DateTime? createdAt;

  factory CompetitionResponseDto.fromJson(Map<String, dynamic> json) =>
      _$CompetitionResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$CompetitionResponseDtoToJson(this);

  Competition toDomain() => Competition(
    id: id,
    name: name,
    description: description,
    startAt: startAt.toLocal(),
    endAt: endAt.toLocal(),
    registrationStartAt: registrationStartAt.toLocal(),
    registrationEndAt: registrationEndAt.toLocal(),
    status: CompetitionStatus.fromJson(status),
    bankName: bankName,
    accountNumber: accountNumber,
    accountHolder: accountHolder,
    entryFee: entryFee,
    bannerImageUrl: bannerImageUrl,
  );
}

@JsonSerializable()
class CompetitionStageResponseDto {
  const CompetitionStageResponseDto({
    required this.id,
    required this.competitionId,
    required this.name,
    required this.stageType,
    required this.stageFormat,
    required this.startAt,
    required this.endAt,
  });

  final String id;
  final String competitionId;
  final String name;
  final String stageType;
  final String stageFormat;
  final DateTime startAt;
  final DateTime endAt;

  factory CompetitionStageResponseDto.fromJson(Map<String, dynamic> json) =>
      _$CompetitionStageResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$CompetitionStageResponseDtoToJson(this);

  CompetitionStage toDomain() => CompetitionStage(
    id: id,
    name: name,
    stageType: stageType,
    stageFormat: stageFormat,
    startAt: startAt.toLocal(),
    endAt: endAt.toLocal(),
  );
}

@JsonSerializable()
class CompetitionEventResponseDto {
  const CompetitionEventResponseDto({
    required this.id,
    required this.stageId,
    required this.competitionId,
    required this.name,
    required this.description,
    required this.eventType,
    required this.wodType,
    required this.order,
    required this.scaleCategories,
    required this.submissionDeadline,
    required this.gender,
    required this.releaseAt,
    required this.timeCap,
    required this.amrapDuration,
    required this.emomDuration,
    required this.weightUnit,
  });

  final String id;
  final String stageId;
  final String competitionId;
  final String name;
  final String description;
  final String eventType;
  final String wodType;
  final int order;
  final List<String> scaleCategories;
  final DateTime submissionDeadline;
  final String gender;
  final DateTime? releaseAt;
  final int? timeCap;
  final int? amrapDuration;
  final int? emomDuration;
  final String? weightUnit;

  factory CompetitionEventResponseDto.fromJson(Map<String, dynamic> json) =>
      _$CompetitionEventResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$CompetitionEventResponseDtoToJson(this);

  CompetitionEvent toDomain() => CompetitionEvent(
    id: id,
    name: name,
    description: description,
    eventType: eventType,
    wodType: wodType,
    order: order,
    scaleCategories: scaleCategories,
    submissionDeadline: submissionDeadline.toLocal(),
    gender: gender,
    releaseAt: releaseAt?.toLocal(),
    timeCap: timeCap,
    amrapDuration: amrapDuration,
    emomDuration: emomDuration,
    weightUnit: weightUnit,
  );
}
