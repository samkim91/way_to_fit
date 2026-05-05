import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/async_value_view.dart';
import '../providers/competition_providers.dart';

class AthleteProfileScreen extends ConsumerWidget {
  const AthleteProfileScreen({super.key, required this.userId});

  final String userId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(athleteProfileProvider(userId));

    return Scaffold(
      appBar: AppBar(title: const Text('선수 프로필')),
      body: SafeArea(
        child: AsyncValueView(
          value: value,
          onRetry: () => ref.invalidate(athleteProfileProvider(userId)),
          builder: (bundle) {
            return ListView(
              padding: const EdgeInsets.fromLTRB(20, 8, 20, 28),
              children: [
                CircleAvatar(
                  radius: 40,
                  backgroundColor: Colors.white.withValues(alpha: 0.08),
                  backgroundImage: bundle.profile.profileImageUrl != null
                      ? NetworkImage(bundle.profile.profileImageUrl!)
                      : null,
                  child: bundle.profile.profileImageUrl == null
                      ? const Icon(Icons.person, size: 42)
                      : null,
                ),
                const SizedBox(height: 16),
                Text(
                  '선수 ${bundle.profile.userId.substring(0, 8)}',
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.w900,
                  ),
                ),
                const SizedBox(height: 10),
                Text(
                  bundle.profile.biography?.isNotEmpty == true
                      ? bundle.profile.biography!
                      : '등록된 선수 소개가 없습니다.',
                  style: Theme.of(
                    context,
                  ).textTheme.bodyLarge?.copyWith(color: Colors.white70),
                ),
                const SizedBox(height: 24),
                Text(
                  '대회 이력',
                  style: Theme.of(
                    context,
                  ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900),
                ),
                const SizedBox(height: 12),
                if (bundle.history.isEmpty)
                  const Text('공개된 대회 이력이 없습니다.')
                else
                  for (final item in bundle.history) ...[
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
                            if (item.eventScores.isNotEmpty) ...[
                              const SizedBox(height: 14),
                              for (final score in item.eventScores)
                                Padding(
                                  padding: const EdgeInsets.only(bottom: 8),
                                  child: Text(
                                    '${score.eventName} · ${score.rank ?? '-'}위 · ${score.resultCustom ?? formatTimeSeconds(score.resultTimeSeconds)}',
                                  ),
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
          },
        ),
      ),
    );
  }
}
