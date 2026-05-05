import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/auth/auth_session.dart';

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key});

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  final _tokenController = TextEditingController();

  @override
  void dispose() {
    _tokenController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final authValue = ref.watch(authControllerProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('세션 관리')),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                '현재 백엔드 모바일 OAuth 리다이렉트가 아직 확정되지 않아, 우선 개발용 액세스 토큰 입력 방식으로 보호 API를 검증합니다.',
                style: Theme.of(
                  context,
                ).textTheme.bodyLarge?.copyWith(color: Colors.white70),
              ),
              const SizedBox(height: 20),
              TextField(
                controller: _tokenController,
                minLines: 3,
                maxLines: 5,
                decoration: const InputDecoration(
                  labelText: 'Bearer Access Token',
                  hintText: '토큰을 붙여 넣으면 secure storage에 저장됩니다.',
                ),
              ),
              const SizedBox(height: 16),
              FilledButton(
                onPressed: () async {
                  await ref
                      .read(authControllerProvider.notifier)
                      .saveAccessToken(_tokenController.text.trim());
                  if (!context.mounted) return;
                  ScaffoldMessenger.of(
                    context,
                  ).showSnackBar(const SnackBar(content: Text('토큰을 저장했습니다.')));
                },
                child: const Text('토큰 저장'),
              ),
              const SizedBox(height: 18),
              authValue.when(
                data: (state) => Text(
                  state.isAuthenticated
                      ? '저장된 토큰 길이: ${state.accessToken!.length}'
                      : '저장된 토큰이 없습니다.',
                ),
                loading: () => const CircularProgressIndicator(),
                error: (error, _) => Text(error.toString()),
              ),
              const SizedBox(height: 12),
              OutlinedButton(
                onPressed: () =>
                    ref.read(authControllerProvider.notifier).clear(),
                child: const Text('토큰 삭제'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
