import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/competition_repository.dart';

class IndividualRegScreen extends ConsumerStatefulWidget {
  const IndividualRegScreen({
    super.key,
    required this.competitionId,
    required this.scaleCategories,
  });

  final String competitionId;
  final List<String> scaleCategories;

  @override
  ConsumerState<IndividualRegScreen> createState() =>
      _IndividualRegScreenState();
}

class _IndividualRegScreenState extends ConsumerState<IndividualRegScreen> {
  String gender = 'MALE';
  late String scaleCategory;
  final paymentNoteController = TextEditingController();
  bool submitting = false;

  @override
  void initState() {
    super.initState();
    scaleCategory = widget.scaleCategories.isNotEmpty
        ? widget.scaleCategories.first
        : '';
  }

  @override
  void dispose() {
    paymentNoteController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final categories = widget.scaleCategories;
    return Scaffold(
      appBar: AppBar(title: const Text('개인 신청')),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            const Text('실제 competition API에 연결된 개인 신청 화면입니다.'),
            const SizedBox(height: 20),
            SegmentedButton<String>(
              segments: const [
                ButtonSegment(value: 'MALE', label: Text('남성')),
                ButtonSegment(value: 'FEMALE', label: Text('여성')),
              ],
              selected: {gender},
              onSelectionChanged: (value) =>
                  setState(() => gender = value.first),
            ),
            const SizedBox(height: 16),
            if (categories.isNotEmpty)
              SegmentedButton<String>(
                segments: categories
                    .map((cat) => ButtonSegment(value: cat, label: Text(cat)))
                    .toList(),
                selected: {scaleCategory},
                onSelectionChanged: (value) =>
                    setState(() => scaleCategory = value.first),
              ),
            const SizedBox(height: 16),
            TextField(
              controller: paymentNoteController,
              decoration: const InputDecoration(labelText: '입금자명 / 메모'),
            ),
            const SizedBox(height: 20),
            FilledButton(
              onPressed: submitting || scaleCategory.isEmpty
                  ? null
                  : () async {
                      setState(() => submitting = true);
                      try {
                        final result = await ref
                            .read(competitionRepositoryProvider)
                            .registerIndividual(
                              widget.competitionId,
                              gender: gender,
                              scaleCategory: scaleCategory,
                              paymentNote:
                                  paymentNoteController.text.trim().isEmpty
                                  ? null
                                  : paymentNoteController.text.trim(),
                            );
                        if (!context.mounted) return;
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text(
                              '${result.registrationType.label} 신청이 완료되었습니다. 현재 상태: ${result.paymentStatus.label}',
                            ),
                          ),
                        );
                        Navigator.of(context).pop();
                      } catch (error) {
                        if (!context.mounted) return;
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text(error.toString())),
                        );
                      } finally {
                        if (mounted) setState(() => submitting = false);
                      }
                    },
              child: Text(submitting ? '신청 중...' : '신청하기'),
            ),
          ],
        ),
      ),
    );
  }
}
