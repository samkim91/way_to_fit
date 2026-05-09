package com.waytofit.global.common.response

import org.springframework.http.HttpStatus

enum class ResponseCode(
    val httpStatus: HttpStatus,
    val code: String,
    val message: String
) {
    // Common (0000 ~ 0999)
    SUCCESS(HttpStatus.OK, "0000", "성공"),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "0001", "잘못된 요청 본문입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "0013", "잘못된 요청 파라미터입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "0002", "존재하지 않는 리소스입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "0007", "인증되지 않은 사용자입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "0010", "권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "9000", "알 수 없는 오류가 발생했습니다. 관리자에게 문의하세요."),

    // User (1000 ~ 1999)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "1000", "존재하지 않는 사용자입니다."),
    USER_DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "1001", "사용자 데이터 무결성 오류가 발생했습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "1002", "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "1003", "존재하지 않거나 만료된 리프레시 토큰입니다."),
    OAUTH_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "1004", "지원하지 않는 OAuth 제공자입니다."),
    USER_ID_NOT_FOUND_AFTER_LOGIN(HttpStatus.INTERNAL_SERVER_ERROR, "1005", "로그인 성공 후 사용자 ID를 찾을 수 없습니다."),
    REFRESH_TOKEN_IS_EMPTY(HttpStatus.UNAUTHORIZED, "1006", "리프레시 토큰이 비어 있습니다."),
    AUTH_CODE_NOT_FOUND(HttpStatus.UNAUTHORIZED, "1007", "존재하지 않거나 이미 사용된 인증 코드입니다."),
    AUTH_CODE_EXPIRED(HttpStatus.UNAUTHORIZED, "1008", "만료된 인증 코드입니다."),
    USER_ID_MUST_NOT_BE_NULL(HttpStatus.BAD_REQUEST, "1009", "사용자 ID가 null일 수 없습니다."),

    // Box & Membership (3000 ~ 3999)
    BOX_NOT_FOUND(HttpStatus.NOT_FOUND, "3000", "존재하지 않는 박스입니다."),
    BOX_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "3001", "박스에 가입된 회원이 아닙니다."),
    ALREADY_JOINED_BOX(HttpStatus.BAD_REQUEST, "3002", "이미 이 박스에 가입 신청을 했거나 활동 중입니다."),
    BOX_ACCESS_DENIED(HttpStatus.FORBIDDEN, "3003", "해당 박스에 대한 접근 권한이 없습니다."),
    CANNOT_MODIFY_SELF(HttpStatus.BAD_REQUEST, "3004", "자기 자신의 권한이나 상태를 변경할 수 없습니다."),
    HOLD_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "3005", "존재하지 않는 홀딩 요청입니다."),
    BOX_MEMBER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "3006", "박스 회원 조회 권한이 없습니다."),
    LOCKER_NUMBER_DUPLICATED(HttpStatus.CONFLICT, "3007", "이미 사용 중인 락커룸 번호입니다."),
    INVALID_MEMBERSHIP_PERIOD(HttpStatus.BAD_REQUEST, "3008", "회원권 종료일은 시작일 이후여야 합니다."),

    // Schedule (4000 ~ 4999)
    SCHEDULE_EFFECTIVE_DATE_PAST(HttpStatus.BAD_REQUEST, "4000", "적용 시작일은 오늘 이후여야 합니다."),
    SCHEDULE_NOT_MODIFIABLE(HttpStatus.BAD_REQUEST, "4002", "DRAFT 상태인 시간표만 수정/삭제/배포할 수 있습니다."),
    INSTANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "4003", "존재하지 않는 수업입니다."),
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "4004", "존재하지 않는 시간표입니다."),
    INVALID_ROUTINE_TIME(HttpStatus.BAD_REQUEST, "4005", "수업 시간은 06:00에서 24:00 사이여야 합니다."),
    INVALID_ROUTINE_DURATION(HttpStatus.BAD_REQUEST, "4006", "수업 종료 시간은 시작 시간 이후여야 합니다."),

    // WOD (5000 ~ 5999)
    WOD_NOT_FOUND(HttpStatus.NOT_FOUND, "5000", "존재하지 않는 WOD입니다."),
    WOD_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "5001", "존재하지 않는 WOD 템플릿입니다."),
    WOD_TYPE_CHANGE_BLOCKED(HttpStatus.CONFLICT, "5002", "기록이 존재하는 WOD의 타입은 변경할 수 없습니다."),
    WOD_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "5003", "존재하지 않는 기록입니다."),
    WOD_TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "5004", "존재하지 않는 팀입니다."),
    WOD_TEAM_DUPLICATE_MEMBER(HttpStatus.CONFLICT, "5005", "이미 다른 팀에 소속된 회원입니다."),
    WOD_TEAM_INVALID_LEADER(HttpStatus.BAD_REQUEST, "5006", "리더는 정확히 1명이어야 합니다."),
    WOD_NOT_TEAM_MODE(HttpStatus.BAD_REQUEST, "5007", "팀전 모드가 아닌 WOD입니다."),
    WOD_SCALE_GROUP_CHANGE_BLOCKED(HttpStatus.CONFLICT, "5008", "기록이 있는 스케일 그룹의 ID는 변경할 수 없습니다."),
    WOD_NOT_PUBLISHED(HttpStatus.BAD_REQUEST, "5009", "아직 발행되지 않은 WOD입니다."),

    // Competition (6000 ~ 6999)
    COMPETITION_REGISTRATION_DATE_INVALID(HttpStatus.BAD_REQUEST, "6000", "신청 시작일은 신청 마감일보다 빨라야 합니다."),
    COMPETITION_PERIOD_INVALID(HttpStatus.BAD_REQUEST, "6001", "대회 시작일은 대회 종료일 이전이거나 같아야 합니다."),
    COMPETITION_REGISTRATION_END_INVALID(HttpStatus.BAD_REQUEST, "6002", "신청 마감일은 대회 종료일 이전이거나 같아야 합니다."),
    ;
}
