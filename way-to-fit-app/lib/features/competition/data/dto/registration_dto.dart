import 'package:json_annotation/json_annotation.dart';

import '../../domain/models.dart';

part 'registration_dto.g.dart';

@JsonSerializable()
class RegisterIndividualRequestDto {
  const RegisterIndividualRequestDto({
    required this.scaleCategory,
    required this.gender,
    this.paymentNote,
  });

  final String scaleCategory;
  final String gender;
  final String? paymentNote;

  factory RegisterIndividualRequestDto.fromJson(Map<String, dynamic> json) =>
      _$RegisterIndividualRequestDtoFromJson(json);

  Map<String, dynamic> toJson() => _$RegisterIndividualRequestDtoToJson(this);
}

@JsonSerializable()
class TeamMemberResponseDto {
  const TeamMemberResponseDto({required this.userId, required this.gender});

  final String userId;
  final String gender;

  factory TeamMemberResponseDto.fromJson(Map<String, dynamic> json) =>
      _$TeamMemberResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$TeamMemberResponseDtoToJson(this);

  TeamMember toDomain() => TeamMember(userId: userId, gender: gender);
}

@JsonSerializable()
class TeamMemberInputDto {
  const TeamMemberInputDto({required this.userId, required this.gender});

  final String userId;
  final String gender;

  factory TeamMemberInputDto.fromJson(Map<String, dynamic> json) =>
      _$TeamMemberInputDtoFromJson(json);

  Map<String, dynamic> toJson() => _$TeamMemberInputDtoToJson(this);
}

@JsonSerializable()
class RegisterTeamRequestDto {
  const RegisterTeamRequestDto({
    required this.teamName,
    required this.scaleCategory,
    required this.members,
    this.paymentNote,
  });

  final String teamName;
  final String scaleCategory;
  final List<TeamMemberInputDto> members;
  final String? paymentNote;

  factory RegisterTeamRequestDto.fromJson(Map<String, dynamic> json) =>
      _$RegisterTeamRequestDtoFromJson(json);

  Map<String, dynamic> toJson() => _$RegisterTeamRequestDtoToJson(this);
}

@JsonSerializable()
class SetLineupRequestDto {
  const SetLineupRequestDto({required this.participatingMemberIds});

  final List<String> participatingMemberIds;

  factory SetLineupRequestDto.fromJson(Map<String, dynamic> json) =>
      _$SetLineupRequestDtoFromJson(json);

  Map<String, dynamic> toJson() => _$SetLineupRequestDtoToJson(this);
}

@JsonSerializable()
class EventLineupResponseDto {
  const EventLineupResponseDto({
    required this.id,
    required this.eventId,
    required this.registrationId,
    required this.participatingMemberIds,
  });

  final String id;
  final String eventId;
  final String registrationId;
  final List<String> participatingMemberIds;

  factory EventLineupResponseDto.fromJson(Map<String, dynamic> json) =>
      _$EventLineupResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$EventLineupResponseDtoToJson(this);

  EventLineup toDomain() => EventLineup(
    id: id,
    eventId: eventId,
    registrationId: registrationId,
    participatingMemberIds: participatingMemberIds,
  );
}

@JsonSerializable()
class RegistrationResponseDto {
  const RegistrationResponseDto({
    required this.id,
    required this.competitionId,
    required this.userId,
    required this.registrationType,
    required this.teamName,
    required this.scaleCategory,
    required this.paymentStatus,
    required this.gender,
    required this.paymentNote,
    required this.members,
    required this.createdAt,
  });

  final String id;
  final String competitionId;
  final String userId;
  final String registrationType;
  final String? teamName;
  final String scaleCategory;
  final String paymentStatus;
  final String gender;
  final String? paymentNote;
  final List<TeamMemberResponseDto>? members;
  final DateTime? createdAt;

  factory RegistrationResponseDto.fromJson(Map<String, dynamic> json) =>
      _$RegistrationResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$RegistrationResponseDtoToJson(this);

  Registration toDomain() => Registration(
    id: id,
    registrationType: RegistrationType.fromJson(registrationType),
    teamName: teamName,
    scaleCategory: scaleCategory,
    paymentStatus: PaymentStatus.fromJson(paymentStatus),
    gender: gender,
    paymentNote: paymentNote,
    members: (members ?? const []).map((member) => member.toDomain()).toList(),
  );
}
