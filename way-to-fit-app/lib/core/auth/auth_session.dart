import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_web_auth_2/flutter_web_auth_2.dart';

import '../api/api_exception.dart';

final flutterSecureStorageProvider = Provider<FlutterSecureStorage>(
  (ref) => const FlutterSecureStorage(),
);

class AuthState {
  const AuthState({this.accessToken});

  final String? accessToken;

  bool get isAuthenticated => accessToken != null && accessToken!.isNotEmpty;
}

class AuthController extends AsyncNotifier<AuthState> {
  static const _accessTokenKey = 'access_token';

  @override
  Future<AuthState> build() async {
    final storage = ref.read(flutterSecureStorageProvider);
    final token = await storage.read(key: _accessTokenKey);
    return AuthState(accessToken: token);
  }

  Future<void> saveAccessToken(String token) async {
    final storage = ref.read(flutterSecureStorageProvider);
    await storage.write(key: _accessTokenKey, value: token);
    state = AsyncData(AuthState(accessToken: token));
  }

  Future<void> loginWithGoogle(Dio dio, String baseUrl) async {
    final redirectUri = Uri.encodeComponent('waytofit://auth/callback');
    final authUrl = '$baseUrl/oauth2/authorization/google?redirect_uri=$redirectUri';

    final callbackUrl = await FlutterWebAuth2.authenticate(
      url: authUrl,
      callbackUrlScheme: 'waytofit',
    );

    final code = Uri.parse(callbackUrl).queryParameters['code'];
    if (code == null || code.isEmpty) {
      throw const ApiException('OAuth 인가 코드를 받지 못했습니다.');
    }

    final response = await dio.post('/api/auth/token', data: {'code': code});
    final accessToken =
        (response.data as Map<String, dynamic>)['data']['accessToken']
            as String?;
    if (accessToken == null || accessToken.isEmpty) {
      throw const ApiException('토큰 교환에 실패했습니다.');
    }

    await saveAccessToken(accessToken);
  }

  Future<void> logout(Dio dio) async {
    try {
      await dio.post('/api/auth/logout');
    } catch (_) {
      // 서버 로그아웃 실패해도 로컬 토큰은 삭제
    }
    await clear();
  }

  Future<void> clear() async {
    final storage = ref.read(flutterSecureStorageProvider);
    await storage.delete(key: _accessTokenKey);
    state = const AsyncData(AuthState());
  }
}

final authControllerProvider = AsyncNotifierProvider<AuthController, AuthState>(
  AuthController.new,
);
