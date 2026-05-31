import { Badge } from '@/components/ui/badge';
import { scoreStatusLabels } from '../labels';
import type { ScoreStatus } from '../types';

const STATUS_CONFIG: Record<ScoreStatus, { label: string; className: string }> = {
  SUBMITTED: {
    label: scoreStatusLabels.SUBMITTED,
    className: 'bg-transparent text-slate-500 border-slate-300',
  },
  APPROVED: {
    label: scoreStatusLabels.APPROVED,
    className: 'bg-green-500 text-white border-green-500',
  },
  ADJUSTED: {
    label: scoreStatusLabels.ADJUSTED,
    className: 'bg-orange-500 text-white border-orange-500',
  },
  REJECTED: {
    label: scoreStatusLabels.REJECTED,
    className: 'bg-transparent text-red-600 border-red-300',
  },
};

interface Props {
  status: ScoreStatus;
  className?: string;
}

export function ScoreStatusBadge({ status, className }: Props) {
  const cfg = STATUS_CONFIG[status] ?? STATUS_CONFIG.SUBMITTED;
  return (
    <Badge
      variant="outline"
      className={`text-[10px] font-semibold tracking-wide ${cfg.className} ${className ?? ''}`}
    >
      {cfg.label}
    </Badge>
  );
}
