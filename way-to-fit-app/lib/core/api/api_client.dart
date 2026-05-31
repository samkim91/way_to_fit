import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';
import 'package:dio_cookie_manager/dio_cookie_manager.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../auth/auth_session.dart';
import 'api_exception.dart';
import 'error_message_resolver.dart';

final apiBaseUrlProvider = Provider<String>((ref) {
  return const String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://localhost:8080',
  );
});

final cookieJarProvider = Provider<CookieJar>((ref) => CookieJar());

final dioProvider = Provider<Dio>((ref) {
  final cookieJar = ref.watch(cookieJarProvider);

  final dio = Dio(
    BaseOptions(
      baseUrl: ref.watch(apiBaseUrlProvider),
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 15),
      headers: const {'Accept': 'application/json'},
    ),
  );

  dio.interceptors.add(CookieManager(cookieJar));
  dio.interceptors.add(
    InterceptorsWrapper(
      onRequest: (options, handler) async {
        final storage = ref.read(flutterSecureStorageProvider);
        final token = await storage.read(key: 'access_token');
        if (token != null && token.isNotEmpty) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        handler.next(options);
      },
      onError: (error, handler) async {
        final message =
            extractResponseMessage(error.response?.data) ??
            error.message ??
            defaultErrorMessage;

        if (error.response?.statusCode == 401) {
          final storage = ref.read(flutterSecureStorageProvider);
          final refreshDio = Dio(
            BaseOptions(
              baseUrl: ref.read(apiBaseUrlProvider),
              connectTimeout: const Duration(seconds: 10),
              receiveTimeout: const Duration(seconds: 15),
            ),
          );
          // 동일한 cookieJar 공유: RT 쿠키를 refreshDio도 전송할 수 있게 함
          refreshDio.interceptors.add(CookieManager(cookieJar));
          try {
            final response = await refreshDio.post('/api/auth/reissue');
            final newToken =
                (response.data as Map<String, dynamic>)['data']['accessToken']
                    as String?;
            if (newToken == null || newToken.isEmpty) {
              throw const ApiException('토큰 재발급 응답이 올바르지 않습니다.');
            }

            await storage.write(key: 'access_token', value: newToken);
            error.requestOptions.headers['Authorization'] = 'Bearer $newToken';
            final retryResponse = await dio.fetch(error.requestOptions);
            return handler.resolve(retryResponse);
          } catch (_) {
            await storage.delete(key: 'access_token');
            await ref.read(authControllerProvider.notifier).clear();
          }
        }

        handler.next(
          DioException(
            requestOptions: error.requestOptions,
            response: error.response,
            type: error.type,
            error: ApiException(
              message,
              statusCode: error.response?.statusCode,
            ),
            message: message,
          ),
        );
      },
    ),
  );

  return dio;
});
