enum CompetitionStatus {
  open,
  registrationOpen,
  registrationClosed,
  inProgress,
  completed;

  factory CompetitionStatus.fromJson(String value) {
    return switch (value) {
      'OPEN' => open,
      'REGISTRATION_OPEN' => registrationOpen,
      'REGISTRATION_CLOSED' => registrationClosed,
      'IN_PROGRESS' => inProgress,
      'COMPLETED' => completed,
      _ => open,
    };
  }

  String get label => switch (this) {
    open => '오픈',
    registrationOpen => '신청중',
    registrationClosed => '마감',
    inProgress => '진행중',
    completed => '종료',
  };
}

enum RegistrationType {
  individual,
  team;

  factory RegistrationType.fromJson(String value) {
    return value == 'TEAM' ? team : individual;
  }

  String get label => this == team ? '팀전' : '개인전';
}

enum PaymentStatus {
  pending,
  confirmed,
  rejected;

  factory PaymentStatus.fromJson(String value) {
    return switch (value) {
      'CONFIRMED' => confirmed,
      'REJECTED' => rejected,
      _ => pending,
    };
  }

  String get label => switch (this) {
    pending => '입금 대기',
    confirmed => '승인 완료',
    rejected => '거절됨',
  };
}

enum ScoreStatus {
  submitted,
  underReview,
  approved,
  adjusted,
  rejected;

  factory ScoreStatus.fromJson(String value) {
    return switch (value) {
      'UNDER_REVIEW' => underReview,
      'APPROVED' => approved,
      'ADJUSTED' => adjusted,
      'REJECTED' => rejected,
      _ => submitted,
    };
  }
}

class Competition {
  Competition({
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
  });

  final String id;
  final String name;
  final String description;
  final DateTime startAt;
  final DateTime endAt;
  final DateTime registrationStartAt;
  final DateTime registrationEndAt;
  final CompetitionStatus status;
  final String bankName;
  final String accountNumber;
  final String accountHolder;
  final int entryFee;
  final String? bannerImageUrl;
}

class CompetitionStage {
  CompetitionStage({
    required this.id,
    required this.name,
    required this.stageType,
    required this.stageFormat,
    required this.startAt,
    required this.endAt,
  });

  final String id;
  final String name;
  final String stageType;
  final String stageFormat;
  final DateTime startAt;
  final DateTime endAt;
}

class CompetitionEvent {
  CompetitionEvent({
    required this.id,
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
}

class CompetitionStageBundle {
  CompetitionStageBundle({required this.stage, required this.events});

  final CompetitionStage stage;
  final List<CompetitionEvent> events;
}

class Registration {
  Registration({
    required this.id,
    required this.registrationType,
    required this.teamName,
    required this.scaleCategory,
    required this.paymentStatus,
    required this.gender,
    required this.paymentNote,
    required this.members,
  });

  final String id;
  final RegistrationType registrationType;
  final String? teamName;
  final String scaleCategory;
  final PaymentStatus paymentStatus;
  final String gender;
  final String? paymentNote;
  final List<TeamMember> members;
}

class TeamMember {
  TeamMember({required this.userId, required this.gender, this.name});

  final String userId;
  final String gender;
  final String? name;
}

class EventLineup {
  EventLineup({
    required this.id,
    required this.eventId,
    required this.registrationId,
    required this.participatingMemberIds,
  });

  final String id;
  final String eventId;
  final String registrationId;
  final List<String> participatingMemberIds;
}

class LeaderboardEntry {
  LeaderboardEntry({
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
}

class OverallLeaderboardEntry {
  OverallLeaderboardEntry({
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
}

class AthleteProfile {
  AthleteProfile({
    required this.userId,
    required this.biography,
    required this.profileImageUrl,
  });

  final String userId;
  final String? biography;
  final String? profileImageUrl;
}

class CompetitionHistoryItem {
  CompetitionHistoryItem({
    required this.competitionId,
    required this.name,
    required this.bannerImageUrl,
    required this.endAt,
    required this.registrationType,
    required this.scaleCategory,
    required this.overallRank,
    required this.totalPoints,
    required this.eventScores,
  });

  final String competitionId;
  final String name;
  final String? bannerImageUrl;
  final DateTime endAt;
  final RegistrationType registrationType;
  final String scaleCategory;
  final int? overallRank;
  final int? totalPoints;
  final List<EventScoreItem> eventScores;
}

class EventScoreItem {
  EventScoreItem({
    required this.eventId,
    required this.eventName,
    required this.rank,
    required this.resultStatus,
    required this.resultTimeSeconds,
    required this.resultRounds,
    required this.resultReps,
    required this.resultWeight,
    required this.resultCustom,
  });

  final String eventId;
  final String eventName;
  final int? rank;
  final String? resultStatus;
  final int? resultTimeSeconds;
  final int? resultRounds;
  final int? resultReps;
  final num? resultWeight;
  final String? resultCustom;
}

class CompetitionDetailBundle {
  CompetitionDetailBundle({
    required this.competition,
    required this.stages,
    this.myRegistrations = const [],
  });

  final Competition competition;
  final List<CompetitionStageBundle> stages;
  final List<Registration> myRegistrations;
}

class AthleteSearchResult {
  AthleteSearchResult({
    required this.userId,
    required this.name,
    required this.gender,
    required this.profileImageUrl,
  });

  final String userId;
  final String name;
  final String? gender;
  final String? profileImageUrl;
}

class AthleteProfileBundle {
  AthleteProfileBundle({required this.profile, required this.history});

  final AthleteProfile profile;
  final List<CompetitionHistoryItem> history;
}
