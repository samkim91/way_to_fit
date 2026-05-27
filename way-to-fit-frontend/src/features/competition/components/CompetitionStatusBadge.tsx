import { Badge } from '@/components/ui/badge';
import { competitionLifecycleLabels } from '../labels';
import type { CompetitionLifecycle } from '../types';

const STATUS_CONFIG: Record<
  CompetitionLifecycle,
  { label: string; className: string }
> = {
  OPEN: {
    label: competitionLifecycleLabels.OPEN,
    className: 'bg-slate-100 text-slate-600 border-slate-200',
  },
  REGISTRATION_OPEN: {
    label: competitionLifecycleLabels.REGISTRATION_OPEN,
    className: 'bg-blue-100 text-blue-700 border-blue-200',
  },
  REGISTRATION_CLOSED: {
    label: competitionLifecycleLabels.REGISTRATION_CLOSED,
    className: 'bg-red-100 text-red-700 border-red-200',
  },
  IN_PROGRESS: {
    label: competitionLifecycleLabels.IN_PROGRESS,
    className: 'bg-orange-100 text-orange-700 border-orange-200',
  },
  COMPLETED: {
    label: competitionLifecycleLabels.COMPLETED,
    className: 'bg-green-100 text-green-700 border-green-200',
  },
};

interface Props {
  status: CompetitionLifecycle;
  className?: string;
}

export function CompetitionStatusBadge({ status, className }: Props) {
  const cfg = STATUS_CONFIG[status] ?? STATUS_CONFIG.OPEN;
  return (
    <Badge
      variant="outline"
      className={`text-[10px] font-semibold tracking-wide ${cfg.className} ${className ?? ''}`}
    >
      {cfg.label}
    </Badge>
  );
}
