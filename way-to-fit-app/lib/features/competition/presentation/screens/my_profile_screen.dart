import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/api/api_client.dart';
import '../../../../core/api/error_message_resolver.dart';
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
        messenger.showSnackBar(SnackBar(content: Text(resolveErrorMessage(e))));
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
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            resolveErrorMessage(e, fallbackMessage: '프로필 저장에 실패했습니다.'),
          ),
        ),
      );
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

            final history = bundle.history;
            final totalComps = history.length;

            int totalScores = 0;
            int? bestRank;
            for (final h in history) {
              totalScores += h.eventScores.length;
              if (h.overallRank != null) {
                if (bestRank == null || h.overallRank! < bestRank) {
                  bestRank = h.overallRank;
                }
              }
            }

            return RefreshIndicator(
              onRefresh: () async => ref.refresh(myProfileProvider.future),
              child: ListView(
                padding: const EdgeInsets.fromLTRB(20, 8, 20, 28),
                children: [
                  _ProfileHeader(profile: profile, userId: userId),
                  const SizedBox(height: 20),
                  // 통계 카드 추가
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(vertical: 18),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          _StatItem(label: '대회 참가', value: '$totalComps회'),
                          Container(width: 1, height: 24, color: Colors.white.withValues(alpha: 0.15)),
                          _StatItem(label: '기록 제출', value: '$totalScores회'),
                          Container(width: 1, height: 24, color: Colors.white.withValues(alpha: 0.15)),
                          _StatItem(
                            label: '최고 순위',
                            value: bestRank != null ? '$bestRank위' : '-',
                          ),
                        ],
                      ),
                    ),
                  ),
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
                  _HistorySection(history: history),
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
    final theme = Theme.of(context);

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
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                '프로필을 확인하려면 로그인하세요.',
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: theme.colorScheme.onSurface.withValues(alpha: 0.78),
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
    final theme = Theme.of(context);

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
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.w900,
                ),
              ),
              if (userId == null) ...[
                const SizedBox(height: 4),
                Text(
                  '토큰에서 사용자 ID를 확인하지 못했습니다.',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: Colors.orangeAccent,
                  ),
                ),
              ],
            ],
          ),
        ),
      ],
    );
  }
}

class _StatItem extends StatelessWidget {
  const _StatItem({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Column(
      children: [
        Text(
          label,
          style: theme.textTheme.labelMedium?.copyWith(
            color: theme.colorScheme.onSurface.withValues(alpha: 0.64),
            fontWeight: FontWeight.w500,
          ),
        ),
        const SizedBox(height: 6),
        Text(
          value,
          style: theme.textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.w900,
          ),
        ),
      ],
    );
  }
}

class _StatusBadge extends StatelessWidget {
  const _StatusBadge({required this.status});
  final String? status;

  @override
  Widget build(BuildContext context) {
    if (status == null) return const SizedBox.shrink();
    final theme = Theme.of(context);

    final isApproved = status == 'APPROVED';
    final isRejected = status == 'REJECTED';

    final bg = isApproved
        ? const Color(0xFFDCFCE7)
        : (isRejected ? const Color(0xFFFEE2E2) : const Color(0xFFF1F5F9));
    final fg = isApproved
        ? const Color(0xFF166534)
        : (isRejected ? const Color(0xFF991B1B) : const Color(0xFF64748B));
    final label = isApproved ? '승인' : (isRejected ? '거절' : '검토중');

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        label,
        style: theme.textTheme.labelSmall?.copyWith(
          color: fg,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class _RankBadge extends StatelessWidget {
  const _RankBadge({required this.rank});
  final int? rank;

  @override
  Widget build(BuildContext context) {
    if (rank == null) return const SizedBox.shrink();
    final theme = Theme.of(context);

    final label = switch (rank) {
      1 => '🥇',
      2 => '🥈',
      3 => '🥉',
      _ => '$rank위',
    };

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: const BoxDecoration(
        color: Colors.black26,
        borderRadius: BorderRadius.all(Radius.circular(6)),
      ),
      child: Text(
        label,
        style: theme.textTheme.labelSmall?.copyWith(
          color: Colors.white,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class _HistorySection extends StatelessWidget {
  const _HistorySection({required this.history});

  final List<CompetitionHistoryItem> history;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '대회 이력',
          style: theme.textTheme.titleLarge?.copyWith(
            fontWeight: FontWeight.w900,
          ),
        ),
        const SizedBox(height: 12),
        if (history.isEmpty)
          const Padding(
            padding: EdgeInsets.symmetric(vertical: 20),
            child: Center(child: Text('공개된 대회 이력이 없습니다.')),
          )
        else
          for (final item in history) ...[
            Card(
              clipBehavior: Clip.antiAlias,
              child: ExpansionTile(
                title: Text(
                  item.name,
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w800,
                  ),
                ),
                subtitle: Text(
                  '${formatDate(item.endAt)} · ${item.registrationType.label} · ${item.scaleCategory} · 최종 ${item.overallRank ?? '-'}위',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurface.withValues(alpha: 0.64),
                  ),
                ),
                children: [
                  if (item.eventScores.isNotEmpty)
                    Padding(
                      padding: const EdgeInsets.fromLTRB(18, 0, 18, 18),
                      child: Column(
                        children: [
                          const Divider(),
                          const SizedBox(height: 8),
                          for (final score in item.eventScores) ...[
                            Padding(
                              padding: const EdgeInsets.symmetric(vertical: 8),
                              child: Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Expanded(
                                    child: Text(
                                      score.eventName,
                                      style: const TextStyle(fontWeight: FontWeight.w600),
                                    ),
                                  ),
                                  Row(
                                    children: [
                                      Text(
                                        score.resultCustom ?? formatTimeSeconds(score.resultTimeSeconds),
                                        style: theme.textTheme.bodyMedium?.copyWith(
                                          fontWeight: FontWeight.w700,
                                        ),
                                      ),
                                      const SizedBox(width: 8),
                                      _StatusBadge(status: score.resultStatus),
                                      const SizedBox(width: 8),
                                      _RankBadge(rank: score.rank),
                                    ],
                                  ),
                                ],
                              ),
                            ),
                            if (score != item.eventScores.last)
                              Divider(height: 8, color: Colors.white.withValues(alpha: 0.05)),
                          ],
                        ],
                      ),
                    )
                  else
                    const Padding(
                      padding: EdgeInsets.all(18),
                      child: Text('제출된 기록이 없습니다.'),
                    ),
                ],
              ),
            ),
            const SizedBox(height: 12),
          ],
      ],
    );
  }
}
