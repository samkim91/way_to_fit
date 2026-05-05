import { Badge } from '@/components/ui/badge';
import { paymentStatusLabels } from '../labels';
import type { PaymentStatus } from '../types';

const STATUS_CONFIG: Record<PaymentStatus, { label: string; className: string }> = {
  PENDING: {
    label: paymentStatusLabels.PENDING,
    className: 'bg-yellow-100 text-yellow-800 border-yellow-200',
  },
  CONFIRMED: {
    label: paymentStatusLabels.CONFIRMED,
    className: 'bg-green-100 text-green-700 border-green-200',
  },
  REJECTED: {
    label: paymentStatusLabels.REJECTED,
    className: 'bg-red-100 text-red-700 border-red-200',
  },
};

interface Props {
  status: PaymentStatus;
  className?: string;
}

export function RegistrationStatusBadge({ status, className }: Props) {
  const cfg = STATUS_CONFIG[status] ?? STATUS_CONFIG.PENDING;
  return (
    <Badge
      variant="outline"
      className={`text-[10px] font-semibold tracking-wide ${cfg.className} ${className ?? ''}`}
    >
      {cfg.label}
    </Badge>
  );
}
