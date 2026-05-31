import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/api/error_message_resolver.dart';
import '../../data/competition_repository.dart';

class ScoreSubmitScreen extends ConsumerStatefulWidget {
  const ScoreSubmitScreen({
    super.key,
    required this.competitionId,
    required this.eventId,
    required this.registrationId,
  });

  final String competitionId;
  final String eventId;
  final String registrationId;

  @override
  ConsumerState<ScoreSubmitScreen> createState() => _ScoreSubmitScreenState();
}

class _ScoreSubmitScreenState extends ConsumerState<ScoreSubmitScreen> {
  final _videoUrlController = TextEditingController();
  final _minutesController = TextEditingController();
  final _secondsController = TextEditingController();
  bool _dnf = false;
  bool _submitting = false;

  @override
  void dispose() {
    _videoUrlController.dispose();
    _minutesController.dispose();
    _secondsController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final canSubmit =
        widget.eventId.isNotEmpty && widget.registrationId.isNotEmpty;

    return Scaffold(
      appBar: AppBar(title: const Text('기록 제출')),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            if (!canSubmit)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(20),
                  child: Text(
                    '이 화면은 `eventId`, `registrationId`가 함께 필요합니다. 현재 배치에서는 대회 상세에서 확정 참가자에게만 진입하도록 연결했습니다.',
                  ),
                ),
              )
            else ...[
              TextField(
                controller: _videoUrlController,
                decoration: const InputDecoration(labelText: 'YouTube URL'),
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _minutesController,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(labelText: '분'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: TextField(
                      controller: _secondsController,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(labelText: '초'),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              CheckboxListTile(
                value: _dnf,
                onChanged: (value) => setState(() => _dnf = value ?? false),
                contentPadding: EdgeInsets.zero,
                title: const Text('DNF'),
              ),
              const SizedBox(height: 12),
              FilledButton(
                onPressed: _submitting
                    ? null
                    : () async {
                        setState(() => _submitting = true);
                        try {
                          final minutes =
                              int.tryParse(_minutesController.text.trim()) ?? 0;
                          final seconds =
                              int.tryParse(_secondsController.text.trim()) ?? 0;
                          await ref
                              .read(competitionRepositoryProvider)
                              .submitScore(
                                widget.competitionId,
                                eventId: widget.eventId,
                                registrationId: widget.registrationId,
                                videoUrl: _videoUrlController.text.trim(),
                                resultStatus: _dnf ? 'DNF' : 'COMPLETED',
                                resultTimeSeconds: _dnf
                                    ? null
                                    : (minutes * 60 + seconds),
                              );
                          if (!context.mounted) return;
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(content: Text('기록을 제출했습니다.')),
                          );
                          Navigator.of(context).pop();
                        } catch (error) {
                          if (!context.mounted) return;
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(content: Text(resolveErrorMessage(error))),
                          );
                        } finally {
                          if (mounted) setState(() => _submitting = false);
                        }
                      },
                child: Text(_submitting ? '제출 중...' : '제출하기'),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
