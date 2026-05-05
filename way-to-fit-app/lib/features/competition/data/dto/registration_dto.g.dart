// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'registration_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegisterIndividualRequestDto _$RegisterIndividualRequestDtoFromJson(
  Map<String, dynamic> json,
) => RegisterIndividualRequestDto(
  scaleCategory: json['scaleCategory'] as String,
  gender: json['gender'] as String,
  paymentNote: json['paymentNote'] as String?,
);

Map<String, dynamic> _$RegisterIndividualRequestDtoToJson(
  RegisterIndividualRequestDto instance,
) => <String, dynamic>{
  'scaleCategory': instance.scaleCategory,
  'gender': instance.gender,
  'paymentNote': instance.paymentNote,
};

TeamMemberResponseDto _$TeamMemberResponseDtoFromJson(
  Map<String, dynamic> json,
) => TeamMemberResponseDto(
  userId: json['userId'] as String,
  gender: json['gender'] as String,
);

Map<String, dynamic> _$TeamMemberResponseDtoToJson(
  TeamMemberResponseDto instance,
) => <String, dynamic>{'userId': instance.userId, 'gender': instance.gender};

TeamMemberInputDto _$TeamMemberInputDtoFromJson(Map<String, dynamic> json) =>
    TeamMemberInputDto(
      userId: json['userId'] as String,
      gender: json['gender'] as String,
    );

Map<String, dynamic> _$TeamMemberInputDtoToJson(TeamMemberInputDto instance) =>
    <String, dynamic>{'userId': instance.userId, 'gender': instance.gender};

RegisterTeamRequestDto _$RegisterTeamRequestDtoFromJson(
  Map<String, dynamic> json,
) => RegisterTeamRequestDto(
  teamName: json['teamName'] as String,
  scaleCategory: json['scaleCategory'] as String,
  members: (json['members'] as List<dynamic>)
      .map((e) => TeamMemberInputDto.fromJson(e as Map<String, dynamic>))
      .toList(),
  paymentNote: json['paymentNote'] as String?,
);

Map<String, dynamic> _$RegisterTeamRequestDtoToJson(
  RegisterTeamRequestDto instance,
) => <String, dynamic>{
  'teamName': instance.teamName,
  'scaleCategory': instance.scaleCategory,
  'members': instance.members,
  'paymentNote': instance.paymentNote,
};

RegistrationResponseDto _$RegistrationResponseDtoFromJson(
  Map<String, dynamic> json,
) => RegistrationResponseDto(
  id: json['id'] as String,
  competitionId: json['competitionId'] as String,
  userId: json['userId'] as String,
  registrationType: json['registrationType'] as String,
  teamName: json['teamName'] as String?,
  scaleCategory: json['scaleCategory'] as String,
  paymentStatus: json['paymentStatus'] as String,
  gender: json['gender'] as String,
  paymentNote: json['paymentNote'] as String?,
  members: (json['members'] as List<dynamic>?)
      ?.map((e) => TeamMemberResponseDto.fromJson(e as Map<String, dynamic>))
      .toList(),
  createdAt: json['createdAt'] == null
      ? null
      : DateTime.parse(json['createdAt'] as String),
);

Map<String, dynamic> _$RegistrationResponseDtoToJson(
  RegistrationResponseDto instance,
) => <String, dynamic>{
  'id': instance.id,
  'competitionId': instance.competitionId,
  'userId': instance.userId,
  'registrationType': instance.registrationType,
  'teamName': instance.teamName,
  'scaleCategory': instance.scaleCategory,
  'paymentStatus': instance.paymentStatus,
  'gender': instance.gender,
  'paymentNote': instance.paymentNote,
  'members': instance.members,
  'createdAt': instance.createdAt?.toIso8601String(),
};
