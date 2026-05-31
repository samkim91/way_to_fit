import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/utils/formatters.dart';
import '../../data/competition_repository.dart';
import '../../domain/models.dart';

class TeamRegScreen extends ConsumerStatefulWidget {
  const TeamRegScreen({
    super.key,
    required this.competitionId,
    required this.scaleCategories,
    this.competition,
  });

  final String competitionId;
  final List<String> scaleCategories;
  final Competition? competition;

  @override
  ConsumerState<TeamRegScreen> createState() => _TeamRegScreenState();
}

typedef _Member = ({AthleteSearchResult athlete, String gender});

class _TeamRegScreenState extends ConsumerState<TeamRegScreen> {
  final _teamNameController = TextEditingController();
  final _paymentNoteController = TextEditingController();
  late String _scaleCategory;
  bool _submitting = false;

  final List<_Member> _members = [];

  @override
  void initState() {
    super.initState();
    _scaleCategory = widget.scaleCategories.isNotEmpty
        ? widget.scaleCategories.first
        : '';
  }

  @override
  void dispose() {
    _teamNameController.dispose();
    _paymentNoteController.dispose();
    super.dispose();
  }

  void _addMembers(List<AthleteSearchResult> athletes) {
    setState(() {
      for (final athlete in athletes) {
        if (!_members.any((m) => m.athlete.userId == athlete.userId)) {
          _members.add((athlete: athlete, gender: athlete.gender ?? 'MALE'));
        }
      }
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
    if (_scaleCategory.isEmpty) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('스케일 카테고리를 선택해주세요.')));
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
    final theme = Theme.of(context);
    final mutedText = theme.colorScheme.onSurface.withValues(alpha: 0.78);

    return Scaffold(
      backgroundColor: Colors.transparent,
      appBar: AppBar(title: const Text('팀 신청')),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerFloat,
      floatingActionButton: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20),
        child: FilledButton(
          style: FilledButton.styleFrom(
            minimumSize: const Size.fromHeight(56),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(18),
            ),
          ),
          onPressed: _submitting ? null : _submit,
          child: Text(
            _submitting ? '신청 중...' : '신청하기',
            style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w800),
          ),
        ),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 100),
          children: [
            if (widget.competition != null) ...[
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        '참가비 및 계좌 정보',
                        style: theme.textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                      ),
                      const SizedBox(height: 12),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            '참가비',
                            style: theme.textTheme.bodyMedium?.copyWith(
                              color: mutedText,
                            ),
                          ),
                          Text(
                            formatCurrency(widget.competition!.entryFee),
                            style: theme.textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            '입금 계좌',
                            style: theme.textTheme.bodyMedium?.copyWith(
                              color: mutedText,
                            ),
                          ),
                          Text(
                            '${widget.competition!.bankName} ${widget.competition!.accountNumber}',
                            style: theme.textTheme.bodyMedium?.copyWith(
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            '예금주',
                            style: theme.textTheme.bodyMedium?.copyWith(
                              color: mutedText,
                            ),
                          ),
                          Text(
                            widget.competition!.accountHolder,
                            style: theme.textTheme.bodyMedium?.copyWith(
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          color: Colors.blue.withValues(alpha: 0.1),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Row(
                          children: [
                            const Icon(Icons.info_outline, size: 16, color: Colors.blue),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Text(
                                '입금 완료 후 주최자가 확인하여 승인 처리합니다.',
                                style: theme.textTheme.bodySmall?.copyWith(
                                  color: Colors.blue,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
            ],
            TextField(
              controller: _teamNameController,
              decoration: const InputDecoration(labelText: '팀명'),
            ),
            const SizedBox(height: 16),
            if (widget.scaleCategories.isNotEmpty)
              SegmentedButton<String>(
                segments: widget.scaleCategories
                    .map((cat) => ButtonSegment(value: cat, label: Text(cat)))
                    .toList(),
                selected: {_scaleCategory},
                onSelectionChanged: (value) =>
                    setState(() => _scaleCategory = value.first),
              ),
            const SizedBox(height: 24),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  '팀원 추가',
                  style: Theme.of(
                    context,
                  ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
                ),
                TextButton.icon(
                  onPressed: () async {
                    final selected = await Navigator.of(context).push<List<AthleteSearchResult>>(
                      MaterialPageRoute(
                        fullscreenDialog: true,
                        builder: (context) => _TeamMemberSearchScreen(
                          competitionId: widget.competitionId,
                          alreadyAddedIds: _members.map((m) => m.athlete.userId).toSet(),
                        ),
                      ),
                    );
                    if (selected != null && selected.isNotEmpty) {
                      _addMembers(selected);
                    }
                  },
                  icon: const Icon(Icons.person_add),
                  label: const Text('팀원 검색'),
                ),
              ],
            ),
            if (_members.isNotEmpty) ...[
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
            ] else ...[
              const SizedBox(height: 16),
              Center(
                child: Text(
                  '팀원을 검색하여 추가해주세요.',
                  style: TextStyle(
                    color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.5),
                  ),
                ),
              ),
            ],
            const SizedBox(height: 32),
            TextField(
              controller: _paymentNoteController,
              decoration: const InputDecoration(labelText: '입금자명 / 메모'),
            ),
          ],
        ),
      ),
    );
  }
}

class _TeamMemberSearchScreen extends ConsumerStatefulWidget {
  const _TeamMemberSearchScreen({
    required this.competitionId,
    required this.alreadyAddedIds,
  });

  final String competitionId;
  final Set<String> alreadyAddedIds;

  @override
  ConsumerState<_TeamMemberSearchScreen> createState() => _TeamMemberSearchScreenState();
}

class _TeamMemberSearchScreenState extends ConsumerState<_TeamMemberSearchScreen> {
  final _searchController = TextEditingController();
  List<AthleteSearchResult> _searchResults = [];
  bool _searching = false;
  final Set<AthleteSearchResult> _selectedAthletes = {};
  Timer? _debounce;

  @override
  void dispose() {
    _searchController.dispose();
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
      setState(() {
        _searchResults = results
            .where((r) => !widget.alreadyAddedIds.contains(r.userId))
            .toList();
      });
    } catch (_) {
      if (mounted) setState(() => _searchResults = []);
    } finally {
      if (mounted) setState(() => _searching = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('팀원 검색'),
        actions: [
          if (_selectedAthletes.isNotEmpty)
            TextButton(
              onPressed: () {
                Navigator.of(context).pop(_selectedAthletes.toList());
              },
              child: Text('완료 (${_selectedAthletes.length})'),
            ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: TextField(
              controller: _searchController,
              decoration: InputDecoration(
                labelText: '선수 이름 검색',
                border: const OutlineInputBorder(),
                suffixIcon: _searching
                    ? const Padding(
                        padding: EdgeInsets.all(12),
                        child: SizedBox(
                          width: 16,
                          height: 16,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        ),
                      )
                    : const Icon(Icons.search),
              ),
              onChanged: _onSearchChanged,
            ),
          ),
          if (_searchResults.isEmpty && !_searching && _searchController.text.isNotEmpty)
            const Expanded(
              child: Center(
                child: Text('검색 결과가 없습니다.'),
              ),
            )
          else
            Expanded(
              child: ListView.builder(
                itemCount: _searchResults.length,
                itemBuilder: (context, index) {
                  final athlete = _searchResults[index];
                  final isSelected = _selectedAthletes.contains(athlete);
                  return CheckboxListTile(
                    value: isSelected,
                    onChanged: (checked) {
                      setState(() {
                        if (checked == true) {
                          _selectedAthletes.add(athlete);
                        } else {
                          _selectedAthletes.remove(athlete);
                        }
                      });
                    },
                    title: Text(athlete.name),
                    subtitle: Text(athlete.gender == 'MALE' ? '남성' : '여성'),
                    secondary: CircleAvatar(
                      backgroundImage: athlete.profileImageUrl != null
                          ? NetworkImage(athlete.profileImageUrl!)
                          : null,
                      child: athlete.profileImageUrl == null
                          ? const Icon(Icons.person)
                          : null,
                    ),
                  );
                },
              ),
            ),
        ],
      ),
    );
  }
}
