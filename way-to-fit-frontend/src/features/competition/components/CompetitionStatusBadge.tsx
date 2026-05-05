import { Badge } from '@/components/ui/badge';
import { competitionStatusLabels } from '../labels';
import type { CompetitionStatus } from '../types';

const STATUS_CONFIG: Record<
  CompetitionStatus,
  { label: string; className: string }
> = {
  DRAFT: {
    label: competitionStatusLabels.DRAFT,
    className: 'bg-slate-100 text-slate-600 border-slate-200',
  },
  PUBLISHED: {
    label: competitionStatusLabels.PUBLISHED,
    className: 'bg-slate-100 text-slate-600 border-slate-200',
  },
  REGISTRATION_OPEN: {
    label: competitionStatusLabels.REGISTRATION_OPEN,
    className: 'bg-blue-100 text-blue-700 border-blue-200',
  },
  REGISTRATION_CLOSED: {
    label: competitionStatusLabels.REGISTRATION_CLOSED,
    className: 'bg-red-100 text-red-700 border-red-200',
  },
  IN_PROGRESS: {
    label: competitionStatusLabels.IN_PROGRESS,
    className: 'bg-orange-100 text-orange-700 border-orange-200',
  },
  COMPLETED: {
    label: competitionStatusLabels.COMPLETED,
    className: 'bg-green-100 text-green-700 border-green-200',
  },
};

interface Props {
  status: CompetitionStatus;
  className?: string;
}

export function CompetitionStatusBadge({ status, className }: Props) {
  const cfg = STATUS_CONFIG[status] ?? STATUS_CONFIG.DRAFT;
  return (
    <Badge
      variant="outline"
      className={`text-[10px] font-semibold tracking-wide ${cfg.className} ${className ?? ''}`}
    >
      {cfg.label}
    </Badge>
  );
}
