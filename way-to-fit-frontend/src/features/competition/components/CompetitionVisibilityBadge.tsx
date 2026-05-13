import { Badge } from '@/components/ui/badge';
import { competitionVisibilityLabels } from '../labels';
import type { CompetitionVisibility } from '../types';

const VISIBILITY_CONFIG: Record<
  CompetitionVisibility,
  { label: string; className: string }
> = {
  PUBLIC: {
    label: competitionVisibilityLabels.PUBLIC,
    className: 'bg-emerald-100 text-emerald-700 border-emerald-200',
  },
  PRIVATE: {
    label: competitionVisibilityLabels.PRIVATE,
    className: 'bg-zinc-100 text-zinc-700 border-zinc-200',
  },
};

interface Props {
  visibility: CompetitionVisibility;
  className?: string;
}

export function CompetitionVisibilityBadge({ visibility, className }: Props) {
  const cfg = VISIBILITY_CONFIG[visibility];
  return (
    <Badge
      variant="outline"
      className={`text-[10px] font-semibold tracking-wide ${cfg.className} ${className ?? ''}`}
    >
      {cfg.label}
    </Badge>
  );
}
