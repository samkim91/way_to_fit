import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/async_value_view.dart';
import '../../domain/models.dart';
import '../providers/competition_providers.dart';

class EventDetailScreen extends ConsumerWidget {
  const EventDetailScreen({
    super.key,
    required this.competitionId,
    required this.eventId,
  });

  final String competitionId;
  final String eventId;

  Registration? _findRegistration(List<Registration> registrations, String eventType) {
    final type = eventType == 'TEAM' ? RegistrationType.team : RegistrationType.individual;
    for (final r in registrations) {
      if (r.registrationType == type) return r;
    }
    return null;
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detailValue = ref.watch(competitionDetailProvider(competitionId));

    return Scaffold(
      appBar: AppBar(
        title: const Text('이벤트 상세'),
      ),
      body: AsyncValueView(
        value: detailValue,
        onRetry: () => ref.invalidate(competitionDetailProvider(competitionId)),
        builder: (bundle) {
          CompetitionEvent? event;
          for (final stageBundle in bundle.stages) {
            for (final e in stageBundle.events) {
              if (e.id == eventId) {
                event = e;
                break;
              }
            }
            if (event != null) break;
          }

          if (event == null) {
            return const Center(child: Text('이벤트를 찾을 수 없습니다.'));
          }

          final registration = _findRegistration(bundle.myRegistrations, event.eventType);
          final myScore = bundle.myScores[event.id];

          return ListView(
            padding: const EdgeInsets.all(20),
            children: [
              Text(
                event.name,
                style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                      fontWeight: FontWeight.w900,
                    ),
              ),
              const SizedBox(height: 12),
              Text(
                '${event.eventType} · ${event.gender} · ${event.scaleCategories.join('/')}',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      color: Theme.of(context).colorScheme.primary,
                      fontWeight: FontWeight.w700,
                    ),
              ),
              const SizedBox(height: 12),
              _DetailRow(
                label: '측정 방식',
                value: event.wodType,
              ),
              if (event.weightUnit != null)
                _DetailRow(
                  label: '무게 단위',
                  value: event.weightUnit!,
                ),
              _DetailRow(
                label: '제출 마감',
                value: formatDateTime(event.submissionDeadline),
              ),
              if (event.description.isNotEmpty) ...[
                const SizedBox(height: 24),
                Text(
                  '상세 내용',
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.w800,
                      ),
                ),
                const SizedBox(height: 12),
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: Theme.of(context).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    event.description,
                    style: Theme.of(context).textTheme.bodyLarge,
                  ),
                ),
              ],
              const SizedBox(height: 32),
              if (myScore != null) ...[
                Text(
                  '내 기록',
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.w800,
                      ),
                ),
                const SizedBox(height: 12),
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              '현재 상태',
                              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                    color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.6),
                                  ),
                            ),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                              decoration: BoxDecoration(
                                color: Theme.of(context).colorScheme.primaryContainer,
                                borderRadius: BorderRadius.circular(16),
                              ),
                              child: Text(
                                myScore.status.label,
                                style: Theme.of(context).textTheme.labelMedium?.copyWith(
                                      color: Theme.of(context).colorScheme.onPrimaryContainer,
                                      fontWeight: FontWeight.bold,
                                    ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 16),
                        const Divider(),
                        const SizedBox(height: 16),
                        _DetailRow(
                          label: '기록',
                          value: _getScoreLabel(myScore, event),
                        ),

                      ],
                    ),
                  ),
                ),
              ],
              const SizedBox(height: 24),
              if (registration != null && registration.paymentStatus == PaymentStatus.confirmed)
                FilledButton(
                  style: FilledButton.styleFrom(
                    minimumSize: const Size.fromHeight(56),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(18),
                    ),
                  ),
                  onPressed: () {
                    context.push(
                      '/competitions/$competitionId/submit-score?eventId=${event!.id}&registrationId=${registration.id}',
                    );
                  },
                  child: Text(
                    myScore == null ? '기록 제출하기' : '기록 수정하기',
                    style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w800),
                  ),
                ),
            ],
          );
        },
      ),
    );
  }

  String _getScoreLabel(MyEventScore score, CompetitionEvent event) {
    if (score.isDnf) return 'DNF';
    if (score.resultCustom != null && score.resultCustom!.isNotEmpty) {
      return score.resultCustom!;
    }

    switch (event.wodType) {
      case 'FOR_TIME':
        return formatTimeSeconds(score.resultTimeSeconds);
      case 'AMRAP':
        final rounds = score.resultRounds ?? 0;
        final reps = score.resultReps ?? 0;
        return '$rounds Rds + $reps Reps';
      case 'EMOM':
        return '${score.resultReps ?? 0} Reps';
      case 'MAX_WEIGHT':
        final weight = score.resultWeight ?? 0;
        final unit = event.weightUnit ?? 'kg';
        return '$weight $unit';
      case 'CUSTOM':
      default:
        if (score.resultTimeSeconds != null) {
          return formatTimeSeconds(score.resultTimeSeconds!);
        }
        if (score.resultReps != null) {
          return '${score.resultReps} Reps';
        }
        return '-';
    }
  }
}

class _DetailRow extends StatelessWidget {
  const _DetailRow({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 80,
            child: Text(
              label,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.6),
                  ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
            ),
          ),
        ],
      ),
    );
  }
}
