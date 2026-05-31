import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useOutletContext } from 'react-router-dom';
import { registrationApi } from '@/features/competition/api';
import type { Registration, PaymentStatus, RegistrationType } from '@/features/competition/types';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { RegistrationStatusBadge } from './RegistrationStatusBadge';
import { useState } from 'react';

type TypeFilter = 'ALL' | RegistrationType;
type StatusFilter = 'ALL' | PaymentStatus;

const GENDER_LABEL: Record<string, string> = { MALE: '남', FEMALE: '여' };
const TYPE_FILTER_LABELS: { value: TypeFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'INDIVIDUAL', label: '개인전' },
  { value: 'TEAM', label: '팀전' },
];
const STATUS_FILTER_LABELS: { value: StatusFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'PENDING', label: '대기중' },
  { value: 'CONFIRMED', label: '승인됨' },
  { value: 'REJECTED', label: '거절됨' },
];

function MembersCell({ reg }: { reg: Registration }) {
  const members = reg.members;
  if (!members || members.length === 0) return <span className="text-muted-foreground text-sm">-</span>;
  return (
    <div className="space-y-1">
      {members.map((m) => (
        <div key={m.userId} className="flex items-center gap-1.5 text-sm">
          {m.teamRole === 'LEADER' && (
            <Badge variant="default" className="h-4 px-1 text-[10px]">리더</Badge>
          )}
          <span className={m.teamRole === 'LEADER' ? 'font-medium' : ''}>
            {m.memberName ?? m.userId.substring(0, 8)}
          </span>
          <Badge variant="outline" className="h-4 px-1 text-[10px]">
            {GENDER_LABEL[m.gender] ?? m.gender}
          </Badge>
        </div>
      ))}
    </div>
  );
}

function ActionButtons({
  reg,
  onAction,
}: {
  reg: Registration;
  onAction: (id: string, status: 'CONFIRMED' | 'REJECTED') => void;
}) {
  if (reg.paymentStatus === 'PENDING') {
    return (
      <div className="flex justify-end gap-2">
        <Button size="sm" onClick={() => onAction(reg.id, 'CONFIRMED')}>승인</Button>
        <Button size="sm" variant="destructive" onClick={() => onAction(reg.id, 'REJECTED')}>거절</Button>
      </div>
    );
  }
  if (reg.paymentStatus === 'CONFIRMED') {
    return (
      <Button size="sm" variant="outline" onClick={() => onAction(reg.id, 'REJECTED')}>거절로 변경</Button>
    );
  }
  return (
    <Button size="sm" variant="outline" onClick={() => onAction(reg.id, 'CONFIRMED')}>승인으로 변경</Button>
  );
}

export function RegistrationsTab() {
  const { competitionId } = useOutletContext<{ competitionId: string }>();
  const queryClient = useQueryClient();
  const [typeFilter, setTypeFilter] = useState<TypeFilter>('ALL');
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');

  const { data: pageData, isLoading } = useQuery({
    queryKey: ['registrations', competitionId],
    queryFn: () => registrationApi.getRegistrations(competitionId),
  });

  const mutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: 'CONFIRMED' | 'REJECTED' }) =>
      registrationApi.updatePaymentStatus(competitionId, id, { paymentStatus: status }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['registrations', competitionId] });
    },
  });

  const handleAction = (id: string, status: 'CONFIRMED' | 'REJECTED') => {
    mutation.mutate({ id, status });
  };

  const registrations = pageData?.content ?? [];
  const filtered = registrations.filter((r) => {
    if (typeFilter !== 'ALL' && r.registrationType !== typeFilter) return false;
    if (statusFilter !== 'ALL' && r.paymentStatus !== statusFilter) return false;
    return true;
  });

  const showTeamMode = typeFilter === 'TEAM';
  const showIndividualMode = typeFilter === 'INDIVIDUAL';
  const colSpan = showTeamMode || showIndividualMode ? 5 : 6;

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center gap-4">
        <div className="flex items-center gap-1">
          {TYPE_FILTER_LABELS.map(({ value, label }) => (
            <Button
              key={value}
              variant={typeFilter === value ? 'default' : 'outline'}
              size="sm"
              onClick={() => setTypeFilter(value)}
            >
              {label}
            </Button>
          ))}
        </div>
        <div className="h-5 w-px bg-border" />
        <div className="flex items-center gap-1">
          {STATUS_FILTER_LABELS.map(({ value, label }) => (
            <Button
              key={value}
              variant={statusFilter === value ? 'secondary' : 'ghost'}
              size="sm"
              onClick={() => setStatusFilter(value)}
            >
              {label}
            </Button>
          ))}
        </div>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              {showTeamMode ? (
                <>
                  <TableHead>팀명</TableHead>
                  <TableHead>팀원</TableHead>
                </>
              ) : showIndividualMode ? (
                <>
                  <TableHead>선수명</TableHead>
                  <TableHead>성별</TableHead>
                </>
              ) : (
                <>
                  <TableHead>이름/팀명</TableHead>
                  <TableHead>타입</TableHead>
                  <TableHead>성별/팀원</TableHead>
                </>
              )}
              <TableHead>스케일</TableHead>
              <TableHead>상태</TableHead>
              <TableHead className="text-right">액션</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={colSpan} className="text-center">로딩 중...</TableCell>
              </TableRow>
            ) : filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={colSpan} className="text-center text-muted-foreground">
                  신청 내역이 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((reg) => (
                <TableRow key={reg.id}>
                  {showTeamMode ? (
                    <>
                      <TableCell className="font-medium">{reg.teamName ?? '-'}</TableCell>
                      <TableCell><MembersCell reg={reg} /></TableCell>
                    </>
                  ) : showIndividualMode ? (
                    <>
                      <TableCell className="font-medium">
                        {reg.athleteName ?? reg.userId.substring(0, 8)}
                      </TableCell>
                      <TableCell>{GENDER_LABEL[reg.gender] ?? reg.gender}</TableCell>
                    </>
                  ) : (
                    <>
                      <TableCell className="font-medium">
                        {reg.registrationType === 'TEAM'
                          ? reg.teamName
                          : (reg.athleteName ?? reg.userId.substring(0, 8))}
                      </TableCell>
                      <TableCell>
                        <Badge variant={reg.registrationType === 'TEAM' ? 'secondary' : 'outline'}>
                          {reg.registrationType === 'TEAM' ? '팀전' : '개인전'}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        {reg.registrationType === 'TEAM' ? (
                          <MembersCell reg={reg} />
                        ) : (
                          <span className="text-sm">{GENDER_LABEL[reg.gender] ?? reg.gender}</span>
                        )}
                      </TableCell>
                    </>
                  )}
                  <TableCell>{reg.scaleCategory}</TableCell>
                  <TableCell><RegistrationStatusBadge status={reg.paymentStatus} /></TableCell>
                  <TableCell className="text-right">
                    <ActionButtons reg={reg} onAction={handleAction} />
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
