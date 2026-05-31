import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/widgets/async_value_view.dart';
import '../../domain/models.dart';
import '../providers/competition_providers.dart';
import '../widgets/competition_card.dart';

class CompetitionListScreen extends ConsumerStatefulWidget {
  const CompetitionListScreen({super.key});

  @override
  ConsumerState<CompetitionListScreen> createState() =>
      _CompetitionListScreenState();
}

class _CompetitionListScreenState extends ConsumerState<CompetitionListScreen> {
  CompetitionStatus? selectedStatus = CompetitionStatus.registrationOpen;

  @override
  Widget build(BuildContext context) {
    final value = ref.watch(competitionListProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('대회'),
      ),
      body: SafeArea(
        child: AsyncValueView(
          value: value,
          onRetry: () => ref.invalidate(competitionListProvider),
          builder: (competitions) {
            final filtered = selectedStatus == null
                ? competitions
                : competitions
                      .where((item) => item.status == selectedStatus)
                      .toList();

            return RefreshIndicator(
              onRefresh: () async =>
                  ref.refresh(competitionListProvider.future),
              child: ListView(
                padding: const EdgeInsets.fromLTRB(20, 8, 20, 28),
                children: [
                  Text(
                    '참가자와 관중을 위한 공개 대회 목록',
                    style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                      color: Theme.of(
                        context,
                      ).colorScheme.onSurface.withValues(alpha: 0.78),
                    ),
                  ),
                  const SizedBox(height: 18),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: [
                      FilterChip(
                        label: const Text('전체'),
                        selected: selectedStatus == null,
                        onSelected: (_) =>
                            setState(() => selectedStatus = null),
                      ),
                      for (final status in [
                        CompetitionStatus.registrationOpen,
                        CompetitionStatus.inProgress,
                        CompetitionStatus.open,
                        CompetitionStatus.completed,
                      ])
                        FilterChip(
                          label: Text(status.label),
                          selected: selectedStatus == status,
                          onSelected: (_) =>
                              setState(() => selectedStatus = status),
                        ),
                    ],
                  ),
                  const SizedBox(height: 20),
                  if (filtered.isEmpty)
                    const Padding(
                      padding: EdgeInsets.symmetric(vertical: 48),
                      child: Center(child: Text('조건에 맞는 공개 대회가 없습니다.')),
                    ),
                  for (final competition in filtered) ...[
                    CompetitionCard(
                      competition: competition,
                      onTap: () =>
                          context.push('/competitions/${competition.id}'),
                    ),
                    const SizedBox(height: 16),
                  ],
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}
