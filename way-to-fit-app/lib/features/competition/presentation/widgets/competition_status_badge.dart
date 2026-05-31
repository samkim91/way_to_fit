import 'package:flutter/material.dart';

import '../../domain/models.dart';

class CompetitionStatusBadge extends StatelessWidget {
  const CompetitionStatusBadge({super.key, required this.status});

  final CompetitionStatus status;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color = switch (status) {
      CompetitionStatus.registrationOpen => const Color(0xFF2563EB),
      CompetitionStatus.inProgress => const Color(0xFFF97316),
      CompetitionStatus.completed => const Color(0xFF22C55E),
      CompetitionStatus.registrationClosed => const Color(0xFFDC2626),
      _ => const Color(0xFF475569),
    };

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.16),
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: color.withValues(alpha: 0.45)),
      ),
      child: Text(
        status.label,
        style: theme.textTheme.labelMedium?.copyWith(
          color: color,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}
