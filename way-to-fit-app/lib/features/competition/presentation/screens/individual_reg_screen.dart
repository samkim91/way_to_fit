import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/api/error_message_resolver.dart';
import '../../../../core/utils/formatters.dart';
import '../../data/competition_repository.dart';
import '../../domain/models.dart';

class IndividualRegScreen extends ConsumerStatefulWidget {
  const IndividualRegScreen({
    super.key,
    required this.competitionId,
    required this.scaleCategories,
    this.competition,
  });

  final String competitionId;
  final List<String> scaleCategories;
  final Competition? competition;

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
    final theme = Theme.of(context);
    final mutedText = theme.colorScheme.onSurface.withValues(alpha: 0.78);

    return Scaffold(
      backgroundColor: Colors.transparent,
      appBar: AppBar(title: const Text('개인 신청')),
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
                        SnackBar(content: Text(resolveErrorMessage(error))),
                      );
                    } finally {
                      if (mounted) setState(() => submitting = false);
                    }
                  },
            child: Text(
              submitting ? '신청 중...' : '신청하기',
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
            ] else ...[
              const Text('실제 competition API에 연결된 개인 신청 화면입니다.'),
              const SizedBox(height: 20),
            ],
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
          ],
        ),
      ),
    );
  }
}
