import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useOutletContext } from 'react-router-dom';
import { registrationApi } from '@/features/competition/api';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { RegistrationStatusBadge } from './RegistrationStatusBadge';
import { useState } from 'react';

export function RegistrationsTab() {
  const { competitionId } = useOutletContext<{ competitionId: string }>();
  const queryClient = useQueryClient();
  const [filter, setFilter] = useState<'ALL' | 'PENDING' | 'CONFIRMED' | 'REJECTED'>('ALL');

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

  const registrations = pageData?.content || [];
  const filtered = filter === 'ALL' ? registrations : registrations.filter(r => r.paymentStatus === filter);

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-2">
        <Button variant={filter === 'ALL' ? 'default' : 'outline'} size="sm" onClick={() => setFilter('ALL')}>전체</Button>
        <Button variant={filter === 'PENDING' ? 'default' : 'outline'} size="sm" onClick={() => setFilter('PENDING')}>대기중</Button>
        <Button variant={filter === 'CONFIRMED' ? 'default' : 'outline'} size="sm" onClick={() => setFilter('CONFIRMED')}>승인됨</Button>
        <Button variant={filter === 'REJECTED' ? 'default' : 'outline'} size="sm" onClick={() => setFilter('REJECTED')}>거절됨</Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>이름/팀명</TableHead>
              <TableHead>타입</TableHead>
              <TableHead>성별</TableHead>
              <TableHead>스케일</TableHead>
              <TableHead>상태</TableHead>
              <TableHead className="text-right">액션</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow><TableCell colSpan={6} className="text-center">로딩 중...</TableCell></TableRow>
            ) : filtered.length === 0 ? (
              <TableRow><TableCell colSpan={6} className="text-center">신청 내역이 없습니다.</TableCell></TableRow>
            ) : (
              filtered.map(reg => (
                <TableRow key={reg.id}>
                  <TableCell className="font-medium">
                    {reg.registrationType === 'TEAM' ? reg.teamName : (reg.athleteName ?? reg.userId.substring(0, 8))}
                  </TableCell>
                  <TableCell>{reg.registrationType === 'TEAM' ? '팀전' : '개인전'}</TableCell>
                  <TableCell>{reg.gender === 'MALE' ? '남성' : reg.gender === 'FEMALE' ? '여성' : '-'}</TableCell>
                  <TableCell>{reg.scaleCategory}</TableCell>
                  <TableCell><RegistrationStatusBadge status={reg.paymentStatus} /></TableCell>
                  <TableCell className="text-right">
                    {reg.paymentStatus === 'PENDING' && (
                      <div className="flex justify-end space-x-2">
                        <Button size="sm" onClick={() => mutation.mutate({ id: reg.id, status: 'CONFIRMED' })}>승인</Button>
                        <Button size="sm" variant="destructive" onClick={() => mutation.mutate({ id: reg.id, status: 'REJECTED' })}>거절</Button>
                      </div>
                    )}
                    {reg.paymentStatus === 'CONFIRMED' && (
                      <Button size="sm" variant="outline" onClick={() => mutation.mutate({ id: reg.id, status: 'REJECTED' })}>거절로 변경</Button>
                    )}
                    {reg.paymentStatus === 'REJECTED' && (
                      <Button size="sm" variant="outline" onClick={() => mutation.mutate({ id: reg.id, status: 'CONFIRMED' })}>승인으로 변경</Button>
                    )}
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
