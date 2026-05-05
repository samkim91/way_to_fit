import 'package:json_annotation/json_annotation.dart';

part 'page_response_dto.g.dart';

@JsonSerializable(genericArgumentFactories: true)
class PageResponseDto<T> {
  const PageResponseDto({required this.content, required this.pageInfo});

  final List<T> content;
  final PageInfoDto? pageInfo;

  factory PageResponseDto.fromJson(
    Map<String, dynamic> json,
    T Function(Object? json) fromJsonT,
  ) => _$PageResponseDtoFromJson(json, fromJsonT);

  Map<String, dynamic> toJson(Object? Function(T value) toJsonT) =>
      _$PageResponseDtoToJson(this, toJsonT);
}

@JsonSerializable()
class PageInfoDto {
  const PageInfoDto({
    required this.pageNumber,
    required this.pageSize,
    required this.totalElements,
    required this.totalPages,
  });

  final int? pageNumber;
  final int? pageSize;
  final int? totalElements;
  final int? totalPages;

  factory PageInfoDto.fromJson(Map<String, dynamic> json) =>
      _$PageInfoDtoFromJson(json);

  Map<String, dynamic> toJson() => _$PageInfoDtoToJson(this);
}
