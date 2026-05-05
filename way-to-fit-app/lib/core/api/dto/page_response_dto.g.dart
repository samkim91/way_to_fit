// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'page_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PageResponseDto<T> _$PageResponseDtoFromJson<T>(
  Map<String, dynamic> json,
  T Function(Object? json) fromJsonT,
) => PageResponseDto<T>(
  content: (json['content'] as List<dynamic>).map(fromJsonT).toList(),
  pageInfo: json['pageInfo'] == null
      ? null
      : PageInfoDto.fromJson(json['pageInfo'] as Map<String, dynamic>),
);

Map<String, dynamic> _$PageResponseDtoToJson<T>(
  PageResponseDto<T> instance,
  Object? Function(T value) toJsonT,
) => <String, dynamic>{
  'content': instance.content.map(toJsonT).toList(),
  'pageInfo': instance.pageInfo,
};

PageInfoDto _$PageInfoDtoFromJson(Map<String, dynamic> json) => PageInfoDto(
  pageNumber: (json['pageNumber'] as num?)?.toInt(),
  pageSize: (json['pageSize'] as num?)?.toInt(),
  totalElements: (json['totalElements'] as num?)?.toInt(),
  totalPages: (json['totalPages'] as num?)?.toInt(),
);

Map<String, dynamic> _$PageInfoDtoToJson(PageInfoDto instance) =>
    <String, dynamic>{
      'pageNumber': instance.pageNumber,
      'pageSize': instance.pageSize,
      'totalElements': instance.totalElements,
      'totalPages': instance.totalPages,
    };
