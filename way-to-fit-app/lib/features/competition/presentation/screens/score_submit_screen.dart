import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/api/error_message_resolver.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/async_value_view.dart';
import '../../data/competition_repository.dart';
import '../../domain/models.dart';
import '../providers/competition_providers.dart';

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

  // WOD 타입별 입력 컨트롤러
  final _minutesController = TextEditingController();
  final _secondsController = TextEditingController();
  final _roundsController = TextEditingController();
  final _repsController = TextEditingController();
  final _weightController = TextEditingController();
  final _customController = TextEditingController();

  bool _dnf = false;
  bool _submitting = false;
  bool _isInitialized = false;

  bool _isValidVideoUrl(String value) {
    final uri = Uri.tryParse(value.trim());
    if (uri == null || !uri.hasScheme || uri.host.isEmpty) {
      return false;
    }
    return uri.scheme == 'http' || uri.scheme == 'https';
  }

  @override
  void dispose() {
    _videoUrlController.dispose();
    _minutesController.dispose();
    _secondsController.dispose();
    _roundsController.dispose();
    _repsController.dispose();
    _weightController.dispose();
    _customController.dispose();
    super.dispose();
  }

  Widget _buildScoreInput(String wodType, String? weightUnit) {
    if (_dnf) {
      return const SizedBox.shrink();
    }

    switch (wodType) {
      case 'FOR_TIME':
        return Row(
          children: [
            Expanded(
              child: TextField(
                controller: _minutesController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: '분',
                  hintText: '0',
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: TextField(
                controller: _secondsController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: '초',
                  hintText: '0',
                ),
              ),
            ),
          ],
        );
      case 'AMRAP':
        return Row(
          children: [
            Expanded(
              child: TextField(
                controller: _roundsController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: '라운드 (Rounds)',
                  hintText: '0',
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: TextField(
                controller: _repsController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: '추가 횟수 (Reps)',
                  hintText: '0',
                ),
              ),
            ),
          ],
        );
      case 'EMOM':
        return TextField(
          controller: _repsController,
          keyboardType: TextInputType.number,
          decoration: const InputDecoration(
            labelText: '총 횟수 (Reps)',
            hintText: '0',
          ),
        );
      case 'MAX_WEIGHT':
        return TextField(
          controller: _weightController,
          keyboardType: const TextInputType.numberWithOptions(decimal: true),
          decoration: InputDecoration(
            labelText: '무게 (${weightUnit ?? 'kg'})',
            hintText: '0.0',
            suffixText: weightUnit ?? 'kg',
          ),
        );
      case 'CUSTOM':
      default:
        return TextField(
          controller: _customController,
          decoration: const InputDecoration(
            labelText: '기록 입력',
            hintText: '기록을 자유롭게 입력해 주세요 (예: 150 reps)',
          ),
        );
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final detailValue = ref.watch(
      competitionDetailProvider(widget.competitionId),
    );
    
    if (!_isInitialized && detailValue.hasValue) {
      _isInitialized = true;
      final bundle = detailValue.value!;
      final myScore = bundle.myScores[widget.eventId];
      if (myScore != null) {
        CompetitionEvent? event;
        for (final stageBundle in bundle.stages) {
          for (final ev in stageBundle.events) {
            if (ev.id == widget.eventId) {
              event = ev;
              break;
            }
          }
          if (event != null) break;
        }
        if (event != null) {
          WidgetsBinding.instance.addPostFrameCallback((_) {
            if (!mounted) return;
            setState(() {
              _dnf = myScore.isDnf;
              _videoUrlController.text = myScore.videoUrl ?? '';
              if (!myScore.isDnf) {
                switch (event!.wodType) {
                  case 'FOR_TIME':
                    if (myScore.resultTimeSeconds != null) {
                      _minutesController.text = (myScore.resultTimeSeconds! ~/ 60).toString();
                      _secondsController.text = (myScore.resultTimeSeconds! % 60).toString();
                    }
                    break;
                  case 'AMRAP':
                    if (myScore.resultRounds != null) _roundsController.text = myScore.resultRounds.toString();
                    if (myScore.resultReps != null) _repsController.text = myScore.resultReps.toString();
                    break;
                  case 'EMOM':
                    if (myScore.resultReps != null) _repsController.text = myScore.resultReps.toString();
                    break;
                  case 'MAX_WEIGHT':
                    if (myScore.resultWeight != null) _weightController.text = myScore.resultWeight.toString();
                    break;
                  case 'CUSTOM':
                  default:
                    _customController.text = myScore.resultCustom ?? '';
                    break;
                }
              }
            });
          });
        }
      }
    }

    final canSubmit =
        widget.eventId.isNotEmpty && widget.registrationId.isNotEmpty;

    return Scaffold(
      appBar: AppBar(title: const Text('기록 제출')),
      body: SafeArea(
        child: !canSubmit
            ? const Padding(
                padding: EdgeInsets.all(20),
                child: Card(
                  child: Padding(
                    padding: EdgeInsets.all(20),
                    child: Text('이 화면은 eventId, registrationId가 함께 필요합니다.'),
                  ),
                ),
              )
            : AsyncValueView(
                value: detailValue,
                onRetry: () => ref.invalidate(
                  competitionDetailProvider(widget.competitionId),
                ),
                builder: (bundle) {
                  CompetitionEvent? event;
                  for (final stageBundle in bundle.stages) {
                    for (final ev in stageBundle.events) {
                      if (ev.id == widget.eventId) {
                        event = ev;
                        break;
                      }
                    }
                    if (event != null) break;
                  }

                  Registration? registration;
                  for (final reg in bundle.myRegistrations) {
                    if (reg.id == widget.registrationId) {
                      registration = reg;
                      break;
                    }
                  }

                  if (event == null || registration == null) {
                    return const Padding(
                      padding: EdgeInsets.all(20),
                      child: Card(
                        child: Padding(
                          padding: EdgeInsets.all(20),
                          child: Text('해당 이벤트 또는 참가 신청 정보를 찾을 수 없습니다.'),
                        ),
                      ),
                    );
                  }

                  return Column(
                    children: [
                      Expanded(
                        child: ListView(
                          padding: const EdgeInsets.all(20),
                          children: [
                      Container(
                        width: double.infinity,
                        padding: const EdgeInsets.all(20),
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            colors: [
                              theme.colorScheme.primary,
                              theme.colorScheme.secondary,
                            ],
                          ),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Container(
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 8,
                                    vertical: 4,
                                  ),
                                  decoration: BoxDecoration(
                                    color: Colors.black.withValues(alpha: 0.3),
                                    borderRadius: BorderRadius.circular(6),
                                  ),
                                  child: Text(
                                    event.wodType,
                                    style: theme.textTheme.labelMedium
                                        ?.copyWith(
                                          color: Colors.white,
                                          fontWeight: FontWeight.w700,
                                        ),
                                  ),
                                ),
                                const SizedBox(width: 8),
                                Text(
                                  '마감: ${formatDateTime(event.submissionDeadline)}',
                                  style: theme.textTheme.bodySmall?.copyWith(
                                    color: Colors.white.withValues(alpha: 0.82),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),
                            Text(
                              event.name,
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 20,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 16),
                      if (event.description.isNotEmpty) ...[
                        Card(
                          color: Colors.white.withValues(alpha: 0.03),
                          child: Padding(
                            padding: const EdgeInsets.all(16),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const Text(
                                  'WOD 설명',
                                  style: TextStyle(fontWeight: FontWeight.bold),
                                ),
                                const SizedBox(height: 8),
                                Text(
                                  event.description,
                                  style: const TextStyle(
                                    fontFamily: 'Courier',
                                    fontSize: 14,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                        const SizedBox(height: 16),
                      ],
                      if (event.rulebook.isNotEmpty) ...[
                        Card(
                          color: Colors.white.withValues(alpha: 0.03),
                          child: Padding(
                            padding: const EdgeInsets.all(16),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const Text(
                                  '룰북',
                                  style: TextStyle(fontWeight: FontWeight.bold),
                                ),
                                const SizedBox(height: 8),
                                Text(
                                  event.rulebook,
                                  style: const TextStyle(fontSize: 14),
                                ),
                              ],
                            ),
                          ),
                        ),
                        const SizedBox(height: 16),
                      ],
                      Card(
                        child: Padding(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 20,
                            vertical: 14,
                          ),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              const Text(
                                '참가 스케일',
                                style: TextStyle(fontWeight: FontWeight.w600),
                              ),
                              Container(
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 12,
                                  vertical: 6,
                                ),
                                decoration: BoxDecoration(
                                  color: theme.colorScheme.primary.withValues(
                                    alpha: 0.16,
                                  ),
                                  borderRadius: BorderRadius.circular(8),
                                  border: Border.all(
                                    color: theme.colorScheme.primary,
                                  ),
                                ),
                                child: Text(
                                  registration.scaleCategory,
                                  style: TextStyle(
                                    color: theme.colorScheme.primary,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      const SizedBox(height: 24),
                      TextField(
                        controller: _videoUrlController,
                        decoration: const InputDecoration(
                          labelText: '영상 URL',
                          hintText: 'https://example.com/video',
                        ),
                      ),
                      const SizedBox(height: 16),
                      _buildScoreInput(event.wodType, event.weightUnit),
                      const SizedBox(height: 12),
                      CheckboxListTile(
                        value: _dnf,
                        onChanged: (value) =>
                            setState(() => _dnf = value ?? false),
                        contentPadding: EdgeInsets.zero,
                        title: const Text('DNF (Did Not Finish)'),
                      ),
                    ],
                  ),
                ),
                SafeArea(
                  child: Padding(
                    padding: const EdgeInsets.fromLTRB(20, 16, 20, 16),
                    child: FilledButton(
                      style: FilledButton.styleFrom(
                        minimumSize: const Size.fromHeight(56),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(18),
                        ),
                      ),
                        onPressed: _submitting
                            ? null
                            : () async {
                                final videoUrl = _videoUrlController.text
                                    .trim();
                                if (videoUrl.isEmpty) {
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    const SnackBar(
                                      content: Text('영상 URL을 입력해주세요.'),
                                    ),
                                  );
                                  return;
                                }

                                if (!_isValidVideoUrl(videoUrl)) {
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    const SnackBar(
                                      content: Text(
                                        'http 또는 https 형식의 영상 URL을 입력해주세요.',
                                      ),
                                    ),
                                  );
                                  return;
                                }

                                setState(() => _submitting = true);
                                try {
                                  int? resultTimeSeconds;
                                  int? resultRounds;
                                  int? resultReps;
                                  num? resultWeight;
                                  String? resultCustom;

                                  if (!_dnf && event != null) {
                                    switch (event.wodType) {
                                      case 'FOR_TIME':
                                        final m =
                                            int.tryParse(
                                              _minutesController.text.trim(),
                                            ) ??
                                            0;
                                        final s =
                                            int.tryParse(
                                              _secondsController.text.trim(),
                                            ) ??
                                            0;
                                        resultTimeSeconds = m * 60 + s;
                                        break;
                                      case 'AMRAP':
                                        resultRounds = int.tryParse(
                                          _roundsController.text.trim(),
                                        );
                                        resultReps = int.tryParse(
                                          _repsController.text.trim(),
                                        );
                                        break;
                                      case 'EMOM':
                                        resultReps = int.tryParse(
                                          _repsController.text.trim(),
                                        );
                                        break;
                                      case 'MAX_WEIGHT':
                                        resultWeight = num.tryParse(
                                          _weightController.text.trim(),
                                        );
                                        break;
                                      case 'CUSTOM':
                                      default:
                                        resultCustom = _customController.text
                                            .trim();
                                        break;
                                    }
                                  }

                                  await ref
                                      .read(competitionRepositoryProvider)
                                      .submitScore(
                                        widget.competitionId,
                                        eventId: widget.eventId,
                                        registrationId: widget.registrationId,
                                        videoUrl: videoUrl,
                                        resultStatus: _dnf
                                            ? 'DNF'
                                            : 'COMPLETED',
                                        resultTimeSeconds: resultTimeSeconds,
                                        resultRounds: resultRounds,
                                        resultReps: resultReps,
                                        resultWeight: resultWeight,
                                        resultCustom: resultCustom,
                                      );

                                  // invalidate provider to reload competition detail data
                                  ref.invalidate(
                                    competitionDetailProvider(
                                      widget.competitionId,
                                    ),
                                  );

                                  if (!context.mounted) return;
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    const SnackBar(
                                      content: Text('기록을 제출했습니다.'),
                                    ),
                                  );
                                  Navigator.of(context).pop();
                                } catch (error) {
                                  if (!context.mounted) return;
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    SnackBar(
                                      content: Text(resolveErrorMessage(error)),
                                    ),
                                  );
                                } finally {
                                  if (mounted) {
                                    setState(() => _submitting = false);
                                  }
                                }
                              },
                        child: Text(
                          _submitting ? '제출 중...' : '제출하기',
                          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w800),
                        ),
                      ),
                  ),
                ),
              ],
            );
          },
        ),
      ),
    );
  }
}
