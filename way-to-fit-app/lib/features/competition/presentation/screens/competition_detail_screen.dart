import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/auth/auth_session.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/async_value_view.dart';
import '../../domain/models.dart';
import '../providers/competition_providers.dart';
import '../widgets/competition_status_badge.dart';

CompetitionEvent? _firstEventForReg(
  List<CompetitionStageBundle> stages,
  Registration reg,
) {
  for (final stageBundle in stages) {
    for (final event in stageBundle.events) {
      final type = event.eventType == 'TEAM'
          ? RegistrationType.team
          : RegistrationType.individual;
      if (type == reg.registrationType) return event;
    }
  }
  return null;
}

class CompetitionDetailScreen extends ConsumerWidget {
  const CompetitionDetailScreen({super.key, required this.competitionId});

  final String competitionId;

  Registration? _findRegistration(List<Registration> registrations, String eventType) {
    final type = eventType == 'TEAM' ? RegistrationType.team : RegistrationType.individual;
    for (final r in registrations) {
      if (r.registrationType == type) return r;
    }
    return null;
  }

  void _showRegistrationTypeSelector(BuildContext context, Competition competition) {
    showModalBottomSheet<void>(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) => Padding(
        padding: const EdgeInsets.fromLTRB(24, 24, 24, 40),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '참가 방식 선택',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.w900,
              ),
            ),
            const SizedBox(height: 20),
            ListTile(
              leading: const Icon(Icons.person, color: Colors.blue),
              title: const Text('개인전 신청하기', style: TextStyle(fontWeight: FontWeight.bold)),
              subtitle: const Text('혼자 대회에 참여합니다.'),
              onTap: () {
                Navigator.of(context).pop();
                context.push(
                  '/competitions/${competition.id}/register/individual',
                  extra: competition,
                );
              },
            ),
            const Divider(),
            ListTile(
              leading: const Icon(Icons.people, color: Colors.green),
              title: const Text('팀전 신청하기', style: TextStyle(fontWeight: FontWeight.bold)),
              subtitle: const Text('팀원들과 함께 대회에 참여합니다.'),
              onTap: () {
                Navigator.of(context).pop();
                context.push(
                  '/competitions/${competition.id}/register/team',
                  extra: competition,
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detailValue = ref.watch(competitionDetailProvider(competitionId));
    final authState = ref.watch(authControllerProvider).valueOrNull;

    return Scaffold(
      appBar: AppBar(
        title: const Text('대회 상세'),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      extendBodyBehindAppBar: true,
      body: AsyncValueView(
        value: detailValue,
        onRetry: () =>
            ref.invalidate(competitionDetailProvider(competitionId)),
        builder: (bundle) {
          final theme = Theme.of(context);
          final mutedText = theme.colorScheme.onSurface.withValues(alpha: 0.78);
          final showIndividualReg = !bundle.myRegistrations.any((r) => r.registrationType == RegistrationType.individual);
          final showTeamReg = !bundle.myRegistrations.any((r) => r.registrationType == RegistrationType.team);
          final canRegister = showIndividualReg || showTeamReg;
          
          String buttonText = '';
          bool isButtonEnabled = false;
          final now = DateTime.now();

          switch (bundle.competition.status) {
            case CompetitionStatus.open:
              buttonText = '${formatDate(bundle.competition.registrationStartAt)}부터 신청 가능';
              isButtonEnabled = false;
              break;
            case CompetitionStatus.registrationOpen:
              if (now.isBefore(bundle.competition.registrationStartAt)) {
                buttonText = '${formatDate(bundle.competition.registrationStartAt)}부터 신청 가능';
                isButtonEnabled = false;
              } else if (now.isAfter(bundle.competition.registrationEndAt)) {
                buttonText = '신청 마감';
                isButtonEnabled = false;
              } else {
                buttonText = canRegister ? '대회 신청하기' : '신청 완료';
                isButtonEnabled = canRegister;
              }
              break;
            case CompetitionStatus.inProgress:
              if (now.isBefore(bundle.competition.registrationEndAt)) {
                buttonText = canRegister ? '대회 신청하기' : '신청 완료';
                isButtonEnabled = canRegister;
              } else {
                buttonText = '진행 중인 대회 (신청 불가)';
                isButtonEnabled = false;
              }
              break;
            case CompetitionStatus.completed:
              buttonText = '종료된 대회';
              isButtonEnabled = false;
              break;
            default:
              buttonText = '신청 불가';
              isButtonEnabled = false;
          }

          return Scaffold(
            bottomNavigationBar: SafeArea(
                    child: Padding(
                      padding: const EdgeInsets.fromLTRB(20, 0, 20, 16),
                      child: FilledButton(
                        style: FilledButton.styleFrom(
                          minimumSize: const Size.fromHeight(56),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(18),
                          ),
                        ),
                        onPressed: isButtonEnabled ? () {
                          if (authState?.isAuthenticated != true) {
                            context.push('/login');
                            return;
                          }
                          if (showIndividualReg && showTeamReg) {
                            _showRegistrationTypeSelector(context, bundle.competition);
                          } else if (showIndividualReg) {
                            context.push(
                              '/competitions/$competitionId/register/individual',
                              extra: bundle.competition,
                            );
                          } else {
                            context.push(
                              '/competitions/$competitionId/register/team',
                              extra: bundle.competition,
                            );
                          }
                        } : null,
                        child: Text(
                          (authState?.isAuthenticated == true || !isButtonEnabled) ? buttonText : '로그인 후 신청하기',
                          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w800),
                        ),
                      ),
                    ),
                  ),
            body: ListView(
              padding: EdgeInsets.zero,
              children: [
                Container(
                  height: 240,
                  width: double.infinity,
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                      colors: [
                        Theme.of(context).colorScheme.primary,
                        Theme.of(context).colorScheme.secondary,
                      ],
                    ),
                    image: bundle.competition.bannerImageUrl != null
                        ? DecorationImage(
                            image: NetworkImage(bundle.competition.bannerImageUrl!),
                            fit: BoxFit.cover,
                          )
                        : null,
                  ),
                  child: Stack(
                    children: [
                      Container(
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            begin: Alignment.topCenter,
                            end: Alignment.bottomCenter,
                            colors: [
                              Colors.black.withValues(alpha: 0.6),
                              Colors.transparent,
                              Colors.black.withValues(alpha: 0.8),
                            ],
                          ),
                        ),
                      ),
                      Positioned(
                        bottom: 20,
                        left: 20,
                        right: 20,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                CompetitionStatusBadge(status: bundle.competition.status),
                                const SizedBox(width: 8),
                                Text(
                                  '${formatDate(bundle.competition.startAt)} - ${formatDate(bundle.competition.endAt)}',
                                  style: theme.textTheme.labelMedium?.copyWith(
                                    color: Colors.white.withValues(alpha: 0.82),
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 8),
                            Text(
                              bundle.competition.name,
                              style: theme.textTheme.headlineSmall?.copyWith(
                                color: Colors.white,
                                fontWeight: FontWeight.w900,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 20, 20, 28),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _InfoCard(
                        title: '안내',
                        child: Text(
                          bundle.competition.description.isEmpty
                              ? '대회 설명이 아직 등록되지 않았습니다.'
                              : bundle.competition.description,
                        ),
                      ),
                      const SizedBox(height: 16),
                      _InfoCard(
                        title: '참가비 및 계좌',
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              formatCurrency(bundle.competition.entryFee),
                              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                                fontWeight: FontWeight.w800,
                              ),
                            ),
                            const SizedBox(height: 8),
                            Text(
                              '${bundle.competition.bankName} ${bundle.competition.accountNumber}',
                              style: theme.textTheme.bodyMedium?.copyWith(
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            Text(
                              '예금주: ${bundle.competition.accountHolder}',
                              style: theme.textTheme.bodyMedium?.copyWith(
                                color: mutedText,
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 16),
                      _ActionRow(
                        label: '리더보드',
                        description: '이벤트별 / 종합 순위를 확인합니다.',
                        onPressed: () =>
                            context.push('/competitions/$competitionId/leaderboard'),
                      ),
                      const SizedBox(height: 16),
                      for (final reg in bundle.myRegistrations)
                        Padding(
                          padding: const EdgeInsets.only(bottom: 16),
                          child: _InfoCard(
                            title: '나의 참가 상태 · ${reg.registrationType.label}',
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Text('${reg.scaleCategory}${reg.teamName != null ? ' · ${reg.teamName}' : ''}'),
                                    Text(
                                      reg.paymentStatus.label,
                                      style: TextStyle(
                                        color: reg.paymentStatus == PaymentStatus.confirmed
                                            ? const Color(0xFF4CAF50)
                                            : const Color(0xFFFFC107),
                                        fontWeight: FontWeight.w700,
                                      ),
                                    ),
                                  ],
                                ),
                                if (reg.paymentStatus == PaymentStatus.confirmed) ...[
                                  const SizedBox(height: 12),
                                  Row(
                                    children: [
                                      Expanded(
                                        child: FilledButton(
                                          onPressed: () {
                                            final event = _firstEventForReg(bundle.stages, reg);
                                            if (event != null) {
                                              context.push(
                                                '/competitions/$competitionId/submit-score?eventId=${event.id}&registrationId=${reg.id}',
                                              );
                                            }
                                          },
                                          child: const Text('기록 제출하기'),
                                        ),
                                      ),
                                      const SizedBox(width: 8),
                                      Expanded(
                                        child: OutlinedButton(
                                          onPressed: () => _showRegistrationDetail(context, reg),
                                          child: const Text('신청 내역 보기'),
                                        ),
                                      ),
                                    ],
                                  ),
                                ],
                              ],
                            ),
                          ),
                        ),
                      if (bundle.stages.isNotEmpty) ...[
                        const SizedBox(height: 24),
                        Text(
                          'Stage & Event',
                          style: Theme.of(
                            context,
                          ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900),
                        ),
                        const SizedBox(height: 12),
                        for (final stageBundle in bundle.stages) ...[
                          _InfoCard(
                            title:
                                '${stageBundle.stage.name} · ${stageBundle.stage.stageFormat}',
                            child: Column(
                              children: [
                                for (final event in stageBundle.events) ...[
                                  _EventTile(
                                    competitionId: competitionId,
                                    event: event,
                                    registration: _findRegistration(bundle.myRegistrations, event.eventType),
                                    myScore: bundle.myScores[event.id],
                                  ),
                                  if (event != stageBundle.events.last)
                                    const Divider(height: 28),
                                ],
                              ],
                            ),
                          ),
                          const SizedBox(height: 14),
                        ],
                      ] else ...[
                        const SizedBox(height: 24),
                        Center(
                          child: Padding(
                            padding: const EdgeInsets.symmetric(vertical: 24),
                            child: Text(
                              '공개된 스테이지와 이벤트가 아직 없습니다.',
                              style: TextStyle(
                                color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.6),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _InfoCard extends StatelessWidget {
  const _InfoCard({required this.title, required this.child});

  final String title;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: SizedBox(
        width: double.infinity,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: Theme.of(
                  context,
                ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w800),
              ),
              const SizedBox(height: 12),
              child,
            ],
          ),
        ),
      ),
    );
  }
}

class _ActionRow extends StatelessWidget {
  const _ActionRow({
    required this.label,
    required this.description,
    required this.onPressed,
  });

  final String label;
  final String description;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
        title: Text(
          label,
          style: Theme.of(
            context,
          ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w800),
        ),
        subtitle: Padding(
          padding: const EdgeInsets.only(top: 6),
          child: Text(description),
        ),
        trailing: const Icon(Icons.chevron_right),
        onTap: onPressed,
      ),
    );
  }
}

void _showRegistrationDetail(BuildContext context, Registration reg) {
  showModalBottomSheet<void>(
    context: context,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (context) => Padding(
      padding: const EdgeInsets.fromLTRB(24, 20, 24, 32),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '신청 내역',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 16),
          _DetailRow(label: '참가 유형', value: reg.registrationType.label),
          _DetailRow(label: '스케일', value: reg.scaleCategory),
          _DetailRow(label: '결제 상태', value: reg.paymentStatus.label),
          if (reg.paymentNote != null && reg.paymentNote!.isNotEmpty)
            _DetailRow(label: '입금자명', value: reg.paymentNote!),
          if (reg.teamName != null)
            _DetailRow(label: '팀명', value: reg.teamName!),
          if (reg.members.isNotEmpty) ...[
            const SizedBox(height: 8),
            Text(
              '팀원',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: Theme.of(
                  context,
                ).colorScheme.onSurface.withValues(alpha: 0.64),
              ),
            ),
            const SizedBox(height: 4),
            for (final m in reg.members)
              Padding(
                padding: const EdgeInsets.only(bottom: 4),
                child: Text(m.name ?? m.userId),
              ),
          ],
        ],
      ),
    ),
  );
}

class _DetailRow extends StatelessWidget {
  const _DetailRow({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Row(
        children: [
          SizedBox(
            width: 80,
            child: Text(
              label,
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: Theme.of(
                  context,
                ).colorScheme.onSurface.withValues(alpha: 0.64),
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: Theme.of(
                context,
              ).textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.w600),
            ),
          ),
        ],
      ),
    );
  }
}

class _EventTile extends StatelessWidget {
  const _EventTile({
    required this.competitionId,
    required this.event,
    required this.registration,
    this.myScore,
  });

  final String competitionId;
  final CompetitionEvent event;
  final Registration? registration;
  final MyEventScore? myScore;

  @override
  Widget build(BuildContext context) {
    final score = myScore;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    event.name,
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    '${event.eventType} · ${event.gender} · ${event.scaleCategories.join('/')}',
                    style: Theme.of(
                      context,
                    ).textTheme.bodySmall?.copyWith(
                      color: Theme.of(
                        context,
                      ).colorScheme.onSurface.withValues(alpha: 0.78),
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '${event.wodType} · 마감 ${formatDateTime(event.submissionDeadline)}',
                  ),
                ],
              ),
            ),
            if (score != null)
              _ScoreChip(score: score, event: event)
            else if (registration != null &&
                registration!.paymentStatus == PaymentStatus.confirmed)
              OutlinedButton(
                onPressed: () => context.push(
                  '/competitions/$competitionId/submit-score?eventId=${event.id}&registrationId=${registration!.id}',
                ),
                child: const Text('기록 제출'),
              ),
          ],
        ),
        if (event.description.isNotEmpty) ...[
          const SizedBox(height: 8),
          Text(event.description),
        ],
      ],
    );
  }
}

class _ScoreChip extends StatelessWidget {
  const _ScoreChip({required this.score, required this.event});

  final MyEventScore score;
  final CompetitionEvent event;

  static const _statusColors = {
    ScoreStatus.submitted:   (bg: Color(0xFFF1F5F9), fg: Color(0xFF64748B)),
    ScoreStatus.underReview: (bg: Color(0xFFDBEAFE), fg: Color(0xFF1E40AF)),
    ScoreStatus.approved:    (bg: Color(0xFFDCFCE7), fg: Color(0xFF166534)),
    ScoreStatus.adjusted:    (bg: Color(0xFFFEF3C7), fg: Color(0xFF92400E)),
    ScoreStatus.rejected:    (bg: Color(0xFFFEE2E2), fg: Color(0xFF991B1B)),
  };

  String get _scoreLabel {
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
          return formatTimeSeconds(score.resultTimeSeconds);
        }
        if (score.resultReps != null) {
          return '${score.resultReps} Reps';
        }
        return '-';
    }
  }

  @override
  Widget build(BuildContext context) {
    final colors = _statusColors[score.status]!;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.end,
      children: [
        Text(
          _scoreLabel,
          style: Theme.of(
            context,
          ).textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.w800),
        ),
        const SizedBox(height: 4),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
          decoration: BoxDecoration(
            color: colors.bg,
            borderRadius: BorderRadius.circular(20),
          ),
          child: Text(
            score.status.label,
            style: Theme.of(context).textTheme.labelMedium?.copyWith(
              fontWeight: FontWeight.w600,
              color: colors.fg,
            ),
          ),
        ),
      ],
    );
  }
}
