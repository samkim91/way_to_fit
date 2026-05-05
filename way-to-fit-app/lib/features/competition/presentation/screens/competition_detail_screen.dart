import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/auth/auth_session.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/async_value_view.dart';
import '../../domain/models.dart';
import '../providers/competition_providers.dart';
import '../widgets/competition_status_badge.dart';

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
                      child: Row(
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

class _EventTile extends StatelessWidget {
  const _EventTile({
    required this.competitionId,
    required this.event,
    required this.registration,
  });

  final String competitionId;
  final CompetitionEvent event;
  final Registration? registration;

  @override
  Widget build(BuildContext context) {
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
            if (registration != null &&
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
