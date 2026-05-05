import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/async_value_view.dart';
import '../../domain/models.dart';
import '../providers/competition_providers.dart';

class LeaderboardScreen extends ConsumerStatefulWidget {
  const LeaderboardScreen({super.key, required this.competitionId});

  final String competitionId;

  @override
  ConsumerState<LeaderboardScreen> createState() => _LeaderboardScreenState();
}

class _LeaderboardScreenState extends ConsumerState<LeaderboardScreen> {
  RegistrationType registrationType = RegistrationType.individual;
  int selectedTab = 0;

  @override
  Widget build(BuildContext context) {
    final detailValue = ref.watch(
      competitionDetailProvider(widget.competitionId),
    );

    return Scaffold(
      appBar: AppBar(title: const Text('리더보드')),
      body: SafeArea(
        child: AsyncValueView(
          value: detailValue,
          builder: (bundle) {
            if (bundle.stages.isEmpty) {
              return const Center(child: Text('리더보드 대상 스테이지가 없습니다.'));
            }

            final stage = bundle.stages.first;
            final eventTabs = [
              null,
              ...stage.events.where(
                (event) => registrationType == RegistrationType.individual
                    ? event.eventType == 'INDIVIDUAL'
                    : event.eventType == 'TEAM',
              ),
            ];
            final normalizedIndex = selectedTab >= eventTabs.length
                ? 0
                : selectedTab;
            final selectedEvent = eventTabs[normalizedIndex];
            final query = LeaderboardQuery(
              competitionId: widget.competitionId,
              stageId: stage.stage.id,
              eventId: selectedEvent?.id,
              registrationType: registrationType,
            );

            return Column(
              children: [
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 8, 20, 0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      SegmentedButton<RegistrationType>(
                        segments: const [
                          ButtonSegment(
                            value: RegistrationType.individual,
                            label: Text('개인전'),
                          ),
                          ButtonSegment(
                            value: RegistrationType.team,
                            label: Text('팀전'),
                          ),
                        ],
                        selected: {registrationType},
                        onSelectionChanged: (value) {
                          setState(() {
                            registrationType = value.first;
                            selectedTab = 0;
                          });
                        },
                      ),
                      const SizedBox(height: 14),
                      SingleChildScrollView(
                        scrollDirection: Axis.horizontal,
                        child: Row(
                          children: [
                            for (
                              var index = 0;
                              index < eventTabs.length;
                              index++
                            )
                              Padding(
                                padding: const EdgeInsets.only(right: 8),
                                child: ChoiceChip(
                                  label: Text(eventTabs[index]?.name ?? '종합'),
                                  selected: normalizedIndex == index,
                                  onSelected: (_) =>
                                      setState(() => selectedTab = index),
                                ),
                              ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                Expanded(
                  child: selectedEvent == null
                      ? _OverallLeaderboardList(query: query)
                      : _EventLeaderboardList(query: query),
                ),
              ],
            );
          },
          onRetry: () =>
              ref.invalidate(competitionDetailProvider(widget.competitionId)),
        ),
      ),
    );
  }
}

class _OverallLeaderboardList extends ConsumerWidget {
  const _OverallLeaderboardList({required this.query});

  final LeaderboardQuery query;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(overallLeaderboardProvider(query));

    return AsyncValueView(
      value: value,
      onRetry: () => ref.invalidate(overallLeaderboardProvider(query)),
      builder: (entries) {
        if (entries.isEmpty) {
          return const Center(child: Text('집계된 종합 순위가 없습니다.'));
        }

        return RefreshIndicator(
          onRefresh: () async =>
              ref.refresh(overallLeaderboardProvider(query).future),
          child: ListView.separated(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 28),
            itemCount: entries.length,
            separatorBuilder: (context, index) => const SizedBox(height: 12),
            itemBuilder: (_, index) {
              final entry = entries[index];
              return Card(
                child: ListTile(
                  contentPadding: const EdgeInsets.all(18),
                  title: Text(
                    '${_rankLabel(entry.rank)} ${entry.participantName}',
                    style: const TextStyle(fontWeight: FontWeight.w800),
                  ),
                  subtitle: Padding(
                    padding: const EdgeInsets.only(top: 8),
                    child: Text(
                      '총 ${entry.totalPoints}pt · ${entry.scaleCategory}\n이벤트 순위 ${entry.eventRanks.values.join(' / ')}',
                    ),
                  ),
                  onTap: entry.memberIds.isNotEmpty
                      ? () => context.push('/athletes/${entry.memberIds.first}')
                      : null,
                ),
              );
            },
          ),
        );
      },
    );
  }
}

class _EventLeaderboardList extends ConsumerWidget {
  const _EventLeaderboardList({required this.query});

  final LeaderboardQuery query;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(eventLeaderboardProvider(query));

    return AsyncValueView(
      value: value,
      onRetry: () => ref.invalidate(eventLeaderboardProvider(query)),
      builder: (entries) {
        if (entries.isEmpty) {
          return const Center(child: Text('집계된 이벤트 순위가 없습니다.'));
        }

        return RefreshIndicator(
          onRefresh: () async =>
              ref.refresh(eventLeaderboardProvider(query).future),
          child: ListView.separated(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 28),
            itemCount: entries.length,
            separatorBuilder: (context, index) => const SizedBox(height: 12),
            itemBuilder: (_, index) {
              final entry = entries[index];
              final score =
                  entry.resultCustom ??
                  formatTimeSeconds(entry.resultTimeSeconds);
              return Card(
                child: ListTile(
                  contentPadding: const EdgeInsets.all(18),
                  title: Text(
                    '${_rankLabel(entry.rank)} ${entry.participantName}',
                    style: const TextStyle(fontWeight: FontWeight.w800),
                  ),
                  subtitle: Padding(
                    padding: const EdgeInsets.only(top: 8),
                    child: Text(
                      '$score · ${entry.scaleCategory}'
                      '${entry.resultStatus != null ? ' · ${entry.resultStatus}' : ''}',
                    ),
                  ),
                  onTap: entry.memberIds.isNotEmpty
                      ? () => context.push('/athletes/${entry.memberIds.first}')
                      : null,
                ),
              );
            },
          ),
        );
      },
    );
  }
}

String _rankLabel(int rank) {
  return switch (rank) {
    1 => '🥇',
    2 => '🥈',
    3 => '🥉',
    _ => '$rank위',
  };
}
