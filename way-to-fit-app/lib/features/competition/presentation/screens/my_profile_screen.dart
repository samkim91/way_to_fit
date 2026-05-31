import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/api/api_client.dart';
import '../../../../core/api/api_exception.dart';
import '../../../../core/auth/auth_session.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/async_value_view.dart';
import '../../data/competition_repository.dart';
import '../../domain/models.dart';
import '../providers/competition_providers.dart';

class MyProfileScreen extends ConsumerStatefulWidget {
  const MyProfileScreen({super.key});

  @override
  ConsumerState<MyProfileScreen> createState() => _MyProfileScreenState();
}

class _MyProfileScreenState extends ConsumerState<MyProfileScreen> {
  final _biographyController = TextEditingController();
  final _profileImageUrlController = TextEditingController();
  String? _loadedUserId;
  bool _saving = false;
  bool _loggingOut = false;

  @override
  void dispose() {
    _biographyController.dispose();
    _profileImageUrlController.dispose();
    super.dispose();
  }

  Future<void> _showLogoutDialog(BuildContext context) async {
    final messenger = ScaffoldMessenger.of(context);

    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('로그아웃'),
        content: const Text('로그아웃 하시겠습니까?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('취소'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('로그아웃'),
          ),
        ],
      ),
    );

    if (confirm != true) return;

    if (!mounted) return;
    setState(() => _loggingOut = true);

    try {
      final dio = ref.read(dioProvider);
      await ref.read(authControllerProvider.notifier).logout(dio);
    } catch (e) {
      if (mounted) {
        messenger.showSnackBar(
          SnackBar(content: Text('로그아웃 중 오류가 발생했습니다: $e')),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _loggingOut = false);
      }
    }
  }

  Future<void> _save() async {
    setState(() => _saving = true);
    try {
      final biography = _biographyController.text.trim();
      final profileImageUrl = _profileImageUrlController.text.trim();
      final repository = ref.read(competitionRepositoryProvider);
      final updated = await repository.updateAthleteProfile(
        biography: biography.isEmpty ? null : biography,
        profileImageUrl: profileImageUrl.isEmpty ? null : profileImageUrl,
      );

      ref.invalidate(myProfileProvider);
      ref.invalidate(athleteProfileProvider(updated.userId));

      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('프로필을 저장했습니다.')));
    } catch (e) {
      if (!mounted) return;
      final message = e is ApiException ? e.message : '프로필 저장에 실패했습니다.';
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text(message)));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final authValue = ref.watch(authControllerProvider);
    final authState = authValue.valueOrNull;
    final isAuthenticated = authState?.isAuthenticated ?? false;

    if (!authValue.isLoading && !isAuthenticated) {
      return _LoginPromptView();
    }

    final userId = authState?.userId;
    final value = ref.watch(myProfileProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('내 프로필'),
        actions: [
          IconButton(
            icon: _loggingOut
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: Colors.white,
                    ),
                  )
                : const Icon(Icons.logout),
            tooltip: '로그아웃',
            onPressed: _loggingOut || _saving
                ? null
                : () => _showLogoutDialog(context),
          ),
        ],
      ),
      body: SafeArea(
        child: AsyncValueView(
          value: value,
          onRetry: () => ref.invalidate(myProfileProvider),
          builder: (bundle) {
            final profile = bundle.profile;
            if (_loadedUserId != profile.userId) {
              _loadedUserId = profile.userId;
              _biographyController.text = profile.biography ?? '';
              _profileImageUrlController.text = profile.profileImageUrl ?? '';
            }

            return RefreshIndicator(
              onRefresh: () async => ref.refresh(myProfileProvider.future),
              child: ListView(
                padding: const EdgeInsets.fromLTRB(20, 8, 20, 28),
                children: [
                  _ProfileHeader(profile: profile, userId: userId),
                  const SizedBox(height: 24),
                  TextField(
                    controller: _profileImageUrlController,
                    decoration: const InputDecoration(
                      labelText: '프로필 이미지 URL',
                      hintText: 'https://example.com/profile.jpg',
                    ),
                    keyboardType: TextInputType.url,
                  ),
                  const SizedBox(height: 16),
                  TextField(
                    controller: _biographyController,
                    decoration: const InputDecoration(
                      labelText: '한마디',
                      hintText: '대회 프로필에 표시할 소개를 입력하세요.',
                    ),
                    maxLines: 4,
                    maxLength: 1000,
                  ),
                  const SizedBox(height: 10),
                  FilledButton.icon(
                    onPressed: _saving ? null : _save,
                    icon: _saving
                        ? const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.save_outlined),
                    label: Text(_saving ? '저장 중...' : '저장'),
                  ),
                  const SizedBox(height: 28),
                  _HistorySection(history: bundle.history),
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}

class _LoginPromptView extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('내 프로필')),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.person_outline, size: 64, color: Colors.white38),
              const SizedBox(height: 24),
              Text(
                '로그인이 필요합니다',
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                '프로필을 확인하려면 로그인하세요.',
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: Colors.white54,
                ),
              ),
              const SizedBox(height: 32),
              SizedBox(
                width: double.infinity,
                child: FilledButton.icon(
                  onPressed: () => context.push('/login'),
                  icon: const Icon(Icons.login),
                  label: const Text('로그인하기'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ProfileHeader extends StatelessWidget {
  const _ProfileHeader({required this.profile, required this.userId});

  final AthleteProfile profile;
  final String? userId;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        CircleAvatar(
          radius: 36,
          backgroundColor: Colors.white.withValues(alpha: 0.08),
          backgroundImage: profile.profileImageUrl != null
              ? NetworkImage(profile.profileImageUrl!)
              : null,
          child: profile.profileImageUrl == null
              ? const Icon(Icons.person, size: 36)
              : null,
        ),
        const SizedBox(width: 16),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                profile.name,
                style: Theme.of(
                  context,
                ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900),
              ),
              if (userId == null) ...[
                const SizedBox(height: 4),
                Text(
                  '토큰에서 사용자 ID를 확인하지 못했습니다.',
                  style: Theme.of(
                    context,
                  ).textTheme.bodySmall?.copyWith(color: Colors.orangeAccent),
                ),
              ],
            ],
          ),
        ),
      ],
    );
  }
}

class _HistorySection extends StatelessWidget {
  const _HistorySection({required this.history});

  final List<CompetitionHistoryItem> history;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '대회 이력',
          style: Theme.of(
            context,
          ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900),
        ),
        const SizedBox(height: 12),
        if (history.isEmpty)
          const Text('공개된 대회 이력이 없습니다.')
        else
          for (final item in history) ...[
            Card(
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      item.name,
                      style: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      '${formatDate(item.endAt)} · ${item.registrationType.label} · ${item.scaleCategory}',
                    ),
                    if (item.overallRank != null) ...[
                      const SizedBox(height: 10),
                      Text(
                        '최종 ${item.overallRank}위 · ${item.totalPoints ?? '-'}pt',
                      ),
                    ],
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),
          ],
      ],
    );
  }
}
