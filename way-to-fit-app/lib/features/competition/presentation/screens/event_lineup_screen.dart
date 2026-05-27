import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/competition_repository.dart';
import '../../domain/models.dart';
import '../providers/competition_providers.dart';

class EventLineupScreen extends ConsumerStatefulWidget {
  const EventLineupScreen({
    super.key,
    required this.competitionId,
    required this.registrationId,
    required this.members,
  });

  final String competitionId;
  final String registrationId;
  final List<TeamMember> members;

  @override
  ConsumerState<EventLineupScreen> createState() => _EventLineupScreenState();
}

class _EventLineupScreenState extends ConsumerState<EventLineupScreen> {
  bool _loading = true;
  List<CompetitionEvent> _teamEvents = [];
  // eventId → 선택된 memberId 집합
  final Map<String, Set<String>> _selections = {};
  final Set<String> _saving = {};

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    try {
      final bundle = await ref.read(
        competitionDetailProvider(widget.competitionId).future,
      );
      final teamEvents = bundle.stages
          .expand((s) => s.events)
          .where((e) => e.eventType == 'TEAM')
          .toList();

      final repo = ref.read(competitionRepositoryProvider);
      final selections = <String, Set<String>>{};

      for (final event in teamEvents) {
        final existing = await repo.getEventLineup(
          widget.competitionId,
          event.id,
          widget.registrationId,
        );
        selections[event.id] = existing?.participatingMemberIds.toSet() ?? {};
      }

      if (mounted) {
        setState(() {
          _teamEvents = teamEvents;
          _selections.addAll(selections);
          _loading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => _loading = false);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('데이터 로딩 실패: $e')),
        );
      }
    }
  }

  void _toggleMember(String eventId, String userId) {
    setState(() {
      final set = _selections.putIfAbsent(eventId, () => {});
      if (set.contains(userId)) {
        set.remove(userId);
      } else {
        set.add(userId);
      }
    });
  }

  Future<void> _saveLineup(String eventId) async {
    setState(() => _saving.add(eventId));
    try {
      final memberIds = (_selections[eventId] ?? {}).toList();
      await ref.read(competitionRepositoryProvider).setEventLineup(
        widget.competitionId,
        eventId,
        widget.registrationId,
        memberIds,
      );
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('라인업이 저장되었습니다.')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('저장 실패: $e')),
        );
      }
    } finally {
      if (mounted) setState(() => _saving.remove(eventId));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('출전 멤버 설정')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _teamEvents.isEmpty
              ? const Center(child: Text('설정할 팀 이벤트가 없습니다.'))
              : ListView(
                  padding: const EdgeInsets.all(16),
                  children: [
                    for (final event in _teamEvents)
                      _EventLineupCard(
                        event: event,
                        members: widget.members,
                        selectedIds: _selections[event.id] ?? {},
                        saving: _saving.contains(event.id),
                        onToggle: (uid) => _toggleMember(event.id, uid),
                        onSave: () => _saveLineup(event.id),
                      ),
                  ],
                ),
    );
  }
}

class _EventLineupCard extends StatelessWidget {
  const _EventLineupCard({
    required this.event,
    required this.members,
    required this.selectedIds,
    required this.saving,
    required this.onToggle,
    required this.onSave,
  });

  final CompetitionEvent event;
  final List<TeamMember> members;
  final Set<String> selectedIds;
  final bool saving;
  final ValueChanged<String> onToggle;
  final VoidCallback onSave;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              event.name,
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ),
            const Divider(height: 24),
            for (final member in members)
              CheckboxListTile(
                title: Text(
                  member.name != null
                      ? '${member.name} · ${member.gender == 'MALE' ? '남' : '여'}'
                      : '${member.userId.substring(0, 8)}... · ${member.gender == 'MALE' ? '남' : '여'}',
                ),
                value: selectedIds.contains(member.userId),
                onChanged: (_) => onToggle(member.userId),
                contentPadding: EdgeInsets.zero,
                dense: true,
              ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: saving ? null : onSave,
                child: Text(saving ? '저장 중...' : '저장'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
