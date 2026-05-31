import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:way_to_fit_app/core/api/api_exception.dart';
import 'package:way_to_fit_app/core/api/error_message_resolver.dart';

void main() {
  group('resolveErrorMessage', () {
    test('returns ApiException message', () {
      expect(resolveErrorMessage(const ApiException('서버 에러')), '서버 에러');
    });

    test('returns nested ApiException message from DioException', () {
      final exception = DioException(
        requestOptions: RequestOptions(path: '/api/test'),
        error: const ApiException('중복 신청입니다.'),
      );

      expect(resolveErrorMessage(exception), '중복 신청입니다.');
    });

    test('returns message from response body when present', () {
      final exception = DioException(
        requestOptions: RequestOptions(path: '/api/test'),
        response: Response(
          requestOptions: RequestOptions(path: '/api/test'),
          data: {'message': '이미 제출된 기록입니다.'},
        ),
      );

      expect(resolveErrorMessage(exception), '이미 제출된 기록입니다.');
    });

    test('returns DioException.message when server message is absent', () {
      final exception = DioException(
        requestOptions: RequestOptions(path: '/api/test'),
        message: 'Connection timed out',
      );

      expect(resolveErrorMessage(exception), 'Connection timed out');
    });

    test('returns fallback when no usable message exists', () {
      final exception = DioException(
        requestOptions: RequestOptions(path: '/api/test'),
        response: Response(
          requestOptions: RequestOptions(path: '/api/test'),
          data: {'message': '   '},
        ),
      );

      expect(resolveErrorMessage(exception), defaultErrorMessage);
    });
  });

  group('extractResponseMessage', () {
    test('returns null for unsupported payloads', () {
      expect(extractResponseMessage({'error': 'bad request'}), isNull);
      expect(extractResponseMessage(null), isNull);
    });
  });
}
