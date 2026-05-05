import 'package:json_annotation/json_annotation.dart';

part 'api_response_dto.g.dart';

@JsonSerializable(genericArgumentFactories: true)
class ApiResponseDto<T> {
  const ApiResponseDto({
    required this.code,
    required this.message,
    required this.data,
  });

  final String code;
  final String message;
  final T? data;

  factory ApiResponseDto.fromJson(
    Map<String, dynamic> json,
    T Function(Object? json) fromJsonT,
  ) => _$ApiResponseDtoFromJson(json, fromJsonT);

  Map<String, dynamic> toJson(Object? Function(T value) toJsonT) =>
      _$ApiResponseDtoToJson(this, toJsonT);
}
