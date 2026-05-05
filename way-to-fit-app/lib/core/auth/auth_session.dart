import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

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

  Future<void> clear() async {
    final storage = ref.read(flutterSecureStorageProvider);
    await storage.delete(key: _accessTokenKey);
    state = const AsyncData(AuthState());
  }
}

final authControllerProvider = AsyncNotifierProvider<AuthController, AuthState>(
  AuthController.new,
);
