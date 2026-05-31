import 'package:dio/dio.dart';

import 'api_exception.dart';

const defaultErrorMessage = '요청 중 오류가 발생했습니다.';

String resolveErrorMessage(
  Object error, {
  String fallbackMessage = defaultErrorMessage,
}) {
  if (error is ApiException) {
    return _sanitizeMessage(error.message) ?? fallbackMessage;
  }

  if (error is DioException) {
    final nestedError = error.error;
    if (nestedError is ApiException) {
      return _sanitizeMessage(nestedError.message) ?? fallbackMessage;
    }

    final responseMessage = extractResponseMessage(error.response?.data);
    if (responseMessage != null) {
      return responseMessage;
    }

    return _sanitizeMessage(error.message) ?? fallbackMessage;
  }

  return _sanitizeMessage(error.toString()) ?? fallbackMessage;
}

String? extractResponseMessage(Object? data) {
  return switch (data) {
    {'message': final String message} => _sanitizeMessage(message),
    _ => null,
  };
}

String? _sanitizeMessage(String? message) {
  if (message == null) return null;
  final trimmed = message.trim();
  if (trimmed.isEmpty || trimmed == 'null') {
    return null;
  }
  return trimmed;
}
