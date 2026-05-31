import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/async_value_view.dart';
import '../../domain/models.dart';
import '../providers/competition_providers.dart';

class AthleteProfileScreen extends ConsumerWidget {
  const AthleteProfileScreen({super.key, required this.userId});

  final String userId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(athleteProfileProvider(userId));
    final theme = Theme.of(context);
    final mutedText = theme.colorScheme.onSurface.withValues(alpha: 0.78);
    final subtleText = theme.colorScheme.onSurface.withValues(alpha: 0.64);

    return Scaffold(
      appBar: AppBar(title: const Text('선수 프로필')),
      body: SafeArea(
        child: AsyncValueView<AthleteProfileBundle>(
          value: value,
          onRetry: () => ref.invalidate(athleteProfileProvider(userId)),
          builder: (bundle) {
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

            return ListView(
              padding: const EdgeInsets.fromLTRB(20, 8, 20, 28),
              children: [
                Center(
                  child: Column(
                    children: [
                      CircleAvatar(
                        radius: 44,
                        backgroundColor: Colors.white.withValues(alpha: 0.08),
                        backgroundImage: bundle.profile.profileImageUrl != null
                            ? NetworkImage(bundle.profile.profileImageUrl!)
                            : null,
                        child: bundle.profile.profileImageUrl == null
                            ? const Icon(Icons.person, size: 44)
                            : null,
                      ),
                      const SizedBox(height: 16),
                      Text(
                        bundle.profile.name,
                        style: theme.textTheme.headlineSmall?.copyWith(
                          fontWeight: FontWeight.w900,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        bundle.profile.biography?.isNotEmpty == true
                            ? bundle.profile.biography!
                            : '등록된 선수 소개가 없습니다.',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: mutedText,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 24),
                // 통계 카드
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
                Text(
                  '대회 이력',
                  style: theme.textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900),
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
                            color: subtleText,
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
          },
        ),
      ),
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
