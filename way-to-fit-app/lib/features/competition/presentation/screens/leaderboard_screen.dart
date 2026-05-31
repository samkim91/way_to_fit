import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/async_value_view.dart';
import '../../domain/models.dart';
import '../providers/competition_providers.dart';

enum _LeaderboardRegistrationFilter {
  all('전체'),
  individual('개인전'),
  team('팀전');

  const _LeaderboardRegistrationFilter(this.label);

  final String label;

  RegistrationType? get registrationType => switch (this) {
    _LeaderboardRegistrationFilter.all => null,
    _LeaderboardRegistrationFilter.individual => RegistrationType.individual,
    _LeaderboardRegistrationFilter.team => RegistrationType.team,
  };
}

class LeaderboardScreen extends ConsumerStatefulWidget {
  const LeaderboardScreen({super.key, required this.competitionId});

  final String competitionId;

  @override
  ConsumerState<LeaderboardScreen> createState() => _LeaderboardScreenState();
}

class _ScoreChipForLeaderboard extends StatelessWidget {
  const _ScoreChipForLeaderboard({required this.entry});

  final LeaderboardEntry entry;

  String get _scoreLabel {
    if (entry.resultStatus == 'DNF') return 'DNF';
    if (entry.resultCustom != null && entry.resultCustom!.isNotEmpty) {
      return entry.resultCustom!;
    }
    if (entry.resultTimeSeconds != null) {
      return formatTimeSeconds(entry.resultTimeSeconds);
    }
    if (entry.resultRounds != null || entry.resultReps != null) {
      final rounds = entry.resultRounds ?? 0;
      final reps = entry.resultReps ?? 0;
      return rounds > 0 ? '$rounds Rds + $reps Reps' : '$reps Reps';
    }
    if (entry.resultWeight != null) {
      return '${entry.resultWeight} kg';
    }
    return '-';
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Text(
        _scoreLabel,
        style: theme.textTheme.labelMedium?.copyWith(
          color: Colors.white,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class _LeaderboardScreenState extends ConsumerState<LeaderboardScreen> {
  _LeaderboardRegistrationFilter registrationFilter =
      _LeaderboardRegistrationFilter.all;
  int selectedTab = 0;

  String? genderFilter;
  String? scaleFilter;

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
                (event) =>
                    registrationFilter.registrationType == null ||
                    (registrationFilter.registrationType ==
                            RegistrationType.individual
                        ? event.eventType == 'INDIVIDUAL'
                        : event.eventType == 'TEAM'),
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
              registrationType: selectedEvent != null
                  ? selectedEvent.eventType == 'TEAM'
                        ? RegistrationType.team
                        : RegistrationType.individual
                  : registrationFilter.registrationType,
              gender: genderFilter,
              scaleCategory: scaleFilter,
            );

            final selectedTypeForMe = selectedEvent != null
                ? (selectedEvent.eventType == 'TEAM'
                      ? RegistrationType.team
                      : RegistrationType.individual)
                : registrationFilter.registrationType;

            Registration? myReg;
            if (selectedTypeForMe != null) {
              for (final r in bundle.myRegistrations) {
                if (r.registrationType == selectedTypeForMe) {
                  myReg = r;
                  break;
                }
              }
            }
            final myRegId = myReg?.id;

            return Column(
              children: [
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 8, 20, 0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: [
                          for (final filter
                              in _LeaderboardRegistrationFilter.values)
                            ChoiceChip(
                              label: Text(filter.label),
                              selected: registrationFilter == filter,
                              onSelected: (_) {
                                setState(() {
                                  registrationFilter = filter;
                                  selectedTab = 0;
                                  genderFilter = null;
                                  scaleFilter = null;
                                });
                              },
                            ),
                        ],
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
                      const SizedBox(height: 14),
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: [
                          ChoiceChip(
                            label: const Text('전체 성별'),
                            selected: genderFilter == null,
                            onSelected: (_) =>
                                setState(() => genderFilter = null),
                          ),
                          ChoiceChip(
                            label: const Text('남성'),
                            selected: genderFilter == 'MALE',
                            onSelected: (_) =>
                                setState(() => genderFilter = 'MALE'),
                          ),
                          ChoiceChip(
                            label: const Text('여성'),
                            selected: genderFilter == 'FEMALE',
                            onSelected: (_) =>
                                setState(() => genderFilter = 'FEMALE'),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      SingleChildScrollView(
                        scrollDirection: Axis.horizontal,
                        child: Row(
                          children: [
                            ChoiceChip(
                              label: const Text('전체 스케일'),
                              selected: scaleFilter == null,
                              onSelected: (_) =>
                                  setState(() => scaleFilter = null),
                            ),
                            for (final scale
                                in bundle.competition.scaleCategories) ...[
                              const SizedBox(width: 8),
                              ChoiceChip(
                                label: Text(scale),
                                selected: scaleFilter == scale,
                                onSelected: (selected) => setState(
                                  () => scaleFilter = selected ? scale : null,
                                ),
                              ),
                            ],
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                Expanded(
                  child: selectedEvent == null
                      ? _OverallLeaderboardList(
                          query: query,
                          myRegistrationId: myRegId,
                        )
                      : _EventLeaderboardList(
                          query: query,
                          myRegistrationId: myRegId,
                        ),
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
  const _OverallLeaderboardList({required this.query, this.myRegistrationId});

  final LeaderboardQuery query;
  final String? myRegistrationId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final mutedOnPrimary = Colors.white.withValues(alpha: 0.82);
    final value = ref.watch(overallLeaderboardProvider(query));

    return AsyncValueView(
      value: value,
      onRetry: () => ref.invalidate(overallLeaderboardProvider(query)),
      builder: (entries) {
        if (entries.isEmpty) {
          return const Center(child: Text('집계된 종합 순위가 없습니다.'));
        }

        OverallLeaderboardEntry? myEntry;
        if (myRegistrationId != null) {
          for (final entry in entries) {
            if (entry.registrationId == myRegistrationId) {
              myEntry = entry;
              break;
            }
          }
        }

        return Stack(
          children: [
            RefreshIndicator(
              onRefresh: () async =>
                  ref.refresh(overallLeaderboardProvider(query).future),
              child: ListView.separated(
                padding: EdgeInsets.fromLTRB(
                  20,
                  0,
                  20,
                  myEntry != null ? 100 : 28,
                ),
                itemCount: entries.length,
                separatorBuilder: (context, index) =>
                    const SizedBox(height: 12),
                itemBuilder: (_, index) {
                  final entry = entries[index];
                  final isMe = entry.registrationId == myRegistrationId;

                  return Card(
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(20),
                      side: isMe
                          ? BorderSide(
                              color: theme.colorScheme.primary,
                              width: 2,
                            )
                          : BorderSide.none,
                    ),
                    color: isMe
                        ? theme.colorScheme.primary.withValues(alpha: 0.1)
                        : theme.cardTheme.color,
                    child: ListTile(
                      contentPadding: const EdgeInsets.all(18),
                      title: Text(
                        '${_rankLabel(entry.rank)} ${entry.participantName}',
                        style: TextStyle(
                          fontWeight: FontWeight.w800,
                          color: isMe ? theme.colorScheme.primary : null,
                        ),
                      ),
                      subtitle: Padding(
                        padding: const EdgeInsets.only(top: 8),
                        child: Text(
                          '총 ${entry.totalPoints}pt · ${entry.scaleCategory}\n이벤트 순위 ${entry.eventRanks.values.join(' / ')}',
                        ),
                      ),
                      onTap: entry.memberIds.isNotEmpty
                          ? () => context.push(
                              '/athletes/${entry.memberIds.first}',
                            )
                          : null,
                    ),
                  );
                },
              ),
            ),
            if (myEntry != null)
              Positioned(
                bottom: 16,
                left: 20,
                right: 20,
                child: Card(
                  color: theme.colorScheme.primary,
                  elevation: 8,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(18),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 20,
                      vertical: 14,
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '나의 종합 순위',
                              style: theme.textTheme.labelMedium?.copyWith(
                                color: mutedOnPrimary,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              myEntry.participantName,
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                        Text(
                          '${_rankLabel(myEntry.rank)} (${myEntry.totalPoints}pt)',
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 18,
                            fontWeight: FontWeight.w900,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
          ],
        );
      },
    );
  }
}

class _EventLeaderboardList extends ConsumerWidget {
  const _EventLeaderboardList({required this.query, this.myRegistrationId});

  final LeaderboardQuery query;
  final String? myRegistrationId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final mutedOnPrimary = Colors.white.withValues(alpha: 0.82);
    final value = ref.watch(eventLeaderboardProvider(query));

    return AsyncValueView(
      value: value,
      onRetry: () => ref.invalidate(eventLeaderboardProvider(query)),
      builder: (entries) {
        if (entries.isEmpty) {
          return const Center(child: Text('집계된 이벤트 순위가 없습니다.'));
        }

        LeaderboardEntry? myEntry;
        if (myRegistrationId != null) {
          for (final entry in entries) {
            if (entry.registrationId == myRegistrationId) {
              myEntry = entry;
              break;
            }
          }
        }

        return Stack(
          children: [
            RefreshIndicator(
              onRefresh: () async =>
                  ref.refresh(eventLeaderboardProvider(query).future),
              child: ListView.separated(
                padding: EdgeInsets.fromLTRB(
                  20,
                  0,
                  20,
                  myEntry != null ? 100 : 28,
                ),
                itemCount: entries.length,
                separatorBuilder: (context, index) =>
                    const SizedBox(height: 12),
                itemBuilder: (_, index) {
                  final entry = entries[index];
                  final isMe = entry.registrationId == myRegistrationId;

                  return Card(
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(20),
                      side: isMe
                          ? BorderSide(
                              color: theme.colorScheme.primary,
                              width: 2,
                            )
                          : BorderSide.none,
                    ),
                    color: isMe
                        ? theme.colorScheme.primary.withValues(alpha: 0.1)
                        : theme.cardTheme.color,
                    child: ListTile(
                      contentPadding: const EdgeInsets.all(18),
                      title: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Expanded(
                            child: Text(
                              '${_rankLabel(entry.rank)} ${entry.participantName}',
                              style: TextStyle(
                                fontWeight: FontWeight.w800,
                                color: isMe ? theme.colorScheme.primary : null,
                              ),
                            ),
                          ),
                          _ScoreChipForLeaderboard(entry: entry),
                        ],
                      ),
                      subtitle: Padding(
                        padding: const EdgeInsets.only(top: 8),
                        child: Text(
                          '스케일: ${entry.scaleCategory}'
                          '${entry.resultStatus != null ? ' · Status: ${entry.resultStatus}' : ''}',
                        ),
                      ),
                      onTap: entry.memberIds.isNotEmpty
                          ? () => context.push(
                              '/athletes/${entry.memberIds.first}',
                            )
                          : null,
                    ),
                  );
                },
              ),
            ),
            if (myEntry != null)
              Positioned(
                bottom: 16,
                left: 20,
                right: 20,
                child: Card(
                  color: theme.colorScheme.primary,
                  elevation: 8,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(18),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 20,
                      vertical: 14,
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '나의 이벤트 순위',
                              style: theme.textTheme.labelMedium?.copyWith(
                                color: mutedOnPrimary,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              myEntry.participantName,
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                        Text(
                          _rankLabel(myEntry.rank),
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 18,
                            fontWeight: FontWeight.w900,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
          ],
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
