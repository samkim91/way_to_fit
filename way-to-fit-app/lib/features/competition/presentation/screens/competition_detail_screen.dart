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

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detailValue = ref.watch(competitionDetailProvider(competitionId));
    final authState = ref.watch(authControllerProvider).valueOrNull;

    return Scaffold(
      appBar: AppBar(title: const Text('대회 상세')),
      body: SafeArea(
        child: AsyncValueView(
          value: detailValue,
          onRetry: () =>
              ref.invalidate(competitionDetailProvider(competitionId)),
          builder: (bundle) {
            return ListView(
              padding: const EdgeInsets.fromLTRB(20, 8, 20, 28),
              children: [
                Text(
                  bundle.competition.name,
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.w900,
                  ),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    CompetitionStatusBadge(status: bundle.competition.status),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        '${formatDate(bundle.competition.startAt)} - ${formatDate(bundle.competition.endAt)}',
                        style: Theme.of(
                          context,
                        ).textTheme.bodyMedium?.copyWith(color: Colors.white70),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 20),
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
                      ),
                      Text('예금주: ${bundle.competition.accountHolder}'),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                _InfoCard(
                  title: '안내',
                  child: Text(
                    bundle.competition.description.isEmpty
                        ? '대회 설명이 아직 등록되지 않았습니다.'
                        : bundle.competition.description,
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
                if (!bundle.myRegistrations.any((r) => r.registrationType == RegistrationType.individual))
                  _ActionRow(
                    label: authState?.isAuthenticated == true
                        ? '개인 신청하기'
                        : '로그인 후 신청하기',
                    description: '개인전으로 참가 신청합니다.',
                    onPressed: () => authState?.isAuthenticated == true
                        ? context.push(
                            '/competitions/$competitionId/register/individual',
                          )
                        : context.push('/login'),
                  ),
                const SizedBox(height: 16),
                if (authState?.isAuthenticated == true &&
                    !bundle.myRegistrations.any((r) => r.registrationType == RegistrationType.team))
                  _ActionRow(
                    label: '팀 신청하기',
                    description: '팀원을 검색하여 팀으로 참가 신청합니다.',
                    onPressed: () =>
                        context.push('/competitions/$competitionId/register/team'),
                  ),
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
                if (bundle.stages.isEmpty)
                  const Padding(
                    padding: EdgeInsets.symmetric(vertical: 24),
                    child: Text('공개된 스테이지와 이벤트가 아직 없습니다.'),
                  ),
              ],
            );
          },
        ),
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
                color: Colors.white54,
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
                color: Colors.white54,
              ),
            ),
          ),
          Expanded(child: Text(value, style: const TextStyle(fontWeight: FontWeight.w600))),
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
                    ).textTheme.bodySmall?.copyWith(color: Colors.white70),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '${event.wodType} · 마감 ${formatDateTime(event.submissionDeadline)}',
                  ),
                ],
              ),
            ),
            if (score != null)
              _ScoreChip(score: score)
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
  const _ScoreChip({required this.score});

  final MyEventScore score;

  static const _statusColors = {
    ScoreStatus.submitted:   (bg: Color(0xFFF1F5F9), fg: Color(0xFF64748B)),
    ScoreStatus.underReview: (bg: Color(0xFFDBEAFE), fg: Color(0xFF1E40AF)),
    ScoreStatus.approved:    (bg: Color(0xFFDCFCE7), fg: Color(0xFF166534)),
    ScoreStatus.adjusted:    (bg: Color(0xFFFEF3C7), fg: Color(0xFF92400E)),
    ScoreStatus.rejected:    (bg: Color(0xFFFEE2E2), fg: Color(0xFF991B1B)),
  };

  String get _scoreLabel {
    if (score.isDnf) return 'DNF';
    if (score.resultCustom != null) return score.resultCustom!;
    return formatTimeSeconds(score.resultTimeSeconds);
  }

  @override
  Widget build(BuildContext context) {
    final colors = _statusColors[score.status]!;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.end,
      children: [
        Text(
          _scoreLabel,
          style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w800),
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
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w600,
              color: colors.fg,
            ),
          ),
        ),
      ],
    );
  }
}
