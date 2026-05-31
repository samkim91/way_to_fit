import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/api/api_client.dart';
import '../../../../core/api/error_message_resolver.dart';
import '../../../../core/auth/auth_session.dart';

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key});

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  bool _loading = false;

  Future<void> _loginWithGoogle() async {
    final routerState = GoRouterState.of(context);
    final from = routerState.uri.queryParameters['from'];

    setState(() => _loading = true);
    try {
      final dio = ref.read(dioProvider);
      final baseUrl = ref.read(apiBaseUrlProvider);
      await ref
          .read(authControllerProvider.notifier)
          .loginWithGoogle(dio, baseUrl);
      if (mounted) {
        if (from != null && from.isNotEmpty) {
          context.go(Uri.decodeComponent(from));
        } else {
          context.go('/competitions');
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(resolveErrorMessage(e))));
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          tooltip: '뒤로가기',
          onPressed: () {
            context.go('/competitions');
          },
        ),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      body: SafeArea(
        child: Center(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 32),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  'Way To Fit',
                  style: Theme.of(context).textTheme.headlineLarge?.copyWith(
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  '크로스핏 대회 플랫폼',
                  style: Theme.of(
                    context,
                  ).textTheme.bodyMedium?.copyWith(color: Colors.white54),
                ),
                const SizedBox(height: 64),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton.icon(
                    onPressed: _loading ? null : _loginWithGoogle,
                    icon: _loading
                        ? const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: Colors.white,
                            ),
                          )
                        : const Icon(Icons.login),
                    label: Text(_loading ? '로그인 중...' : 'Google로 계속하기'),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
