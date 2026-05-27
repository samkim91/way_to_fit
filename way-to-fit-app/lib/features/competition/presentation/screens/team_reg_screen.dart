import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../data/competition_repository.dart';
import '../../domain/models.dart';

class TeamRegScreen extends ConsumerStatefulWidget {
  const TeamRegScreen({super.key, required this.competitionId});

  final String competitionId;

  @override
  ConsumerState<TeamRegScreen> createState() => _TeamRegScreenState();
}

typedef _Member = ({AthleteSearchResult athlete, String gender});

class _TeamRegScreenState extends ConsumerState<TeamRegScreen> {
  final _teamNameController = TextEditingController();
  final _searchController = TextEditingController();
  final _paymentNoteController = TextEditingController();
  String _scaleCategory = 'RXD';
  bool _submitting = false;

  List<AthleteSearchResult> _searchResults = [];
  bool _searching = false;
  final List<_Member> _members = [];

  final Map<String, String> _pendingGender = {};

  Timer? _debounce;

  @override
  void dispose() {
    _teamNameController.dispose();
    _searchController.dispose();
    _paymentNoteController.dispose();
    _debounce?.cancel();
    super.dispose();
  }

  void _onSearchChanged(String value) {
    _debounce?.cancel();
    if (value.trim().isEmpty) {
      setState(() => _searchResults = []);
      return;
    }
    _debounce = Timer(
      const Duration(milliseconds: 300),
      () => _search(value.trim()),
    );
  }

  Future<void> _search(String name) async {
    setState(() => _searching = true);
    try {
      final results = await ref
          .read(competitionRepositoryProvider)
          .searchAthletes(widget.competitionId, name);
      if (!mounted) return;
      final alreadyAdded = _members.map((m) => m.athlete.userId).toSet();
      setState(() {
        _searchResults = results
            .where((r) => !alreadyAdded.contains(r.userId))
            .toList();
      });
    } catch (_) {
      if (mounted) setState(() => _searchResults = []);
    } finally {
      if (mounted) setState(() => _searching = false);
    }
  }

  void _addMember(AthleteSearchResult athlete) {
    final gender = _pendingGender[athlete.userId] ?? athlete.gender ?? 'MALE';
    setState(() {
      _members.add((athlete: athlete, gender: gender));
      _searchResults.removeWhere((r) => r.userId == athlete.userId);
      _pendingGender.remove(athlete.userId);
    });
  }

  void _removeMember(int index) {
    setState(() => _members.removeAt(index));
  }

  Future<void> _submit() async {
    final teamName = _teamNameController.text.trim();
    if (teamName.isEmpty) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('팀명을 입력해주세요.')));
      return;
    }
    if (_members.isEmpty) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('팀원을 한 명 이상 추가해주세요.')));
      return;
    }

    setState(() => _submitting = true);
    Registration? registrationResult;
    Object? submitError;
    try {
      registrationResult = await ref
          .read(competitionRepositoryProvider)
          .registerTeam(
            widget.competitionId,
            teamName: teamName,
            scaleCategory: _scaleCategory,
            members: _members
                .map((m) => (userId: m.athlete.userId, gender: m.gender))
                .toList(),
            paymentNote: _paymentNoteController.text.trim().isEmpty
                ? null
                : _paymentNoteController.text.trim(),
          );
    } catch (error) {
      submitError = error;
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
    if (!mounted) return;
    if (registrationResult != null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            '팀 신청이 완료되었습니다. 현재 상태: ${registrationResult.paymentStatus.label}',
          ),
        ),
      );
      // 팀 신청 완료 후 이벤트 라인업 설정 화면으로 이동
      if (context.mounted) {
        final membersWithNames = _members
            .map(
              (m) => TeamMember(
                userId: m.athlete.userId,
                gender: m.gender,
                name: m.athlete.name,
              ),
            )
            .toList();
        context.push(
          '/competitions/${widget.competitionId}/lineup?registrationId=${registrationResult.id}',
          extra: membersWithNames,
        );
      }
    } else if (submitError != null) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text(submitError.toString())));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('팀 신청')),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            TextField(
              controller: _teamNameController,
              decoration: const InputDecoration(labelText: '팀명'),
            ),
            const SizedBox(height: 16),
            SegmentedButton<String>(
              segments: const [
                ButtonSegment(value: 'RXD', label: Text('RXD')),
                ButtonSegment(value: 'SCALED', label: Text('SCALED')),
              ],
              selected: {_scaleCategory},
              onSelectionChanged: (value) =>
                  setState(() => _scaleCategory = value.first),
            ),
            const SizedBox(height: 24),
            Text(
              '팀원 추가',
              style: Theme.of(
                context,
              ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _searchController,
              decoration: InputDecoration(
                labelText: '선수 이름 검색',
                suffixIcon: _searching
                    ? const Padding(
                        padding: EdgeInsets.all(12),
                        child: SizedBox(
                          width: 16,
                          height: 16,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        ),
                      )
                    : null,
              ),
              onChanged: _onSearchChanged,
            ),
            if (_searchResults.isNotEmpty) ...[
              const SizedBox(height: 8),
              Card(
                margin: EdgeInsets.zero,
                child: Column(
                  children: [
                    for (final athlete in _searchResults)
                      _SearchResultTile(
                        athlete: athlete,
                        selectedGender:
                            _pendingGender[athlete.userId] ??
                            athlete.gender ??
                            'MALE',
                        onGenderChanged: (g) =>
                            setState(() => _pendingGender[athlete.userId] = g),
                        onAdd: () => _addMember(athlete),
                      ),
                  ],
                ),
              ),
            ],
            if (_members.isNotEmpty) ...[
              const SizedBox(height: 16),
              Text(
                '추가된 팀원 (${_members.length}명)',
                style: Theme.of(
                  context,
                ).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700),
              ),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                runSpacing: 4,
                children: [
                  for (int i = 0; i < _members.length; i++)
                    Chip(
                      label: Text(
                        '${_members[i].athlete.name} · ${_members[i].gender == 'MALE' ? '남' : '여'}',
                      ),
                      onDeleted: () => _removeMember(i),
                    ),
                ],
              ),
            ],
            const SizedBox(height: 20),
            TextField(
              controller: _paymentNoteController,
              decoration: const InputDecoration(labelText: '입금자명 / 메모'),
            ),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: _submitting ? null : _submit,
              child: Text(_submitting ? '신청 중...' : '신청하기'),
            ),
          ],
        ),
      ),
    );
  }
}

class _SearchResultTile extends StatelessWidget {
  const _SearchResultTile({
    required this.athlete,
    required this.selectedGender,
    required this.onGenderChanged,
    required this.onAdd,
  });

  final AthleteSearchResult athlete;
  final String selectedGender;
  final ValueChanged<String> onGenderChanged;
  final VoidCallback onAdd;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  athlete.name,
                  style: const TextStyle(fontWeight: FontWeight.w600),
                ),
              ],
            ),
          ),
          SegmentedButton<String>(
            style: ButtonStyle(
              visualDensity: VisualDensity.compact,
              tapTargetSize: MaterialTapTargetSize.shrinkWrap,
            ),
            segments: const [
              ButtonSegment(value: 'MALE', label: Text('남')),
              ButtonSegment(value: 'FEMALE', label: Text('여')),
            ],
            selected: {selectedGender},
            onSelectionChanged: (v) => onGenderChanged(v.first),
          ),
          const SizedBox(width: 8),
          TextButton(onPressed: onAdd, child: const Text('추가')),
        ],
      ),
    );
  }
}
