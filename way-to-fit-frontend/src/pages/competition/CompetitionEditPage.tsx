import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQueryClient, useQuery } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DateTimeRangePicker } from '@/components/ui/date-time-range-picker';
import { DateRangePicker } from '@/components/ui/date-range-picker';
import { competitionApi } from '@/features/competition/api';
import { dayjs } from '@/utils';
import {
  competitionVisibilityOptions,
  normalizeCompetitionVisibility,
} from '@/features/competition/labels';
import type { CompetitionVisibility } from '@/features/competition/types';

export function CompetitionEditPage() {
  const { competitionId } = useParams<{ competitionId: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: competition, isLoading } = useQuery({
    queryKey: ['competition', competitionId],
    queryFn: () => competitionApi.getCompetition(competitionId!),
    enabled: !!competitionId,
  });

  const [formData, setFormData] = useState({
    name: '',
    description: '',
    startAt: '',
    endAt: '',
    registrationStartAt: '',
    registrationEndAt: '',
    bankName: '',
    accountNumber: '',
    accountHolder: '',
    entryFee: 0,
    bannerImageUrl: '',
    visibility: 'PRIVATE' as CompetitionVisibility,
  });

  useEffect(() => {
    if (competition) {
      setFormData({
        name: competition.name,
        description: competition.description || '',
        startAt: dayjs(competition.startAt).format('YYYY-MM-DD'),
        endAt: dayjs(competition.endAt).format('YYYY-MM-DD'),
        registrationStartAt: dayjs(competition.registrationStartAt).format('YYYY-MM-DDTHH:mm'),
        registrationEndAt: dayjs(competition.registrationEndAt).format('YYYY-MM-DDTHH:mm'),
        bankName: competition.bankName || '',
        accountNumber: competition.accountNumber || '',
        accountHolder: competition.accountHolder || '',
        entryFee: competition.entryFee || 0,
        bannerImageUrl: competition.bannerImageUrl || '',
        visibility: normalizeCompetitionVisibility(competition.visibility),
      });
    }
  }, [competition]);

  const mutation = useMutation({
    mutationFn: (data: typeof formData) => {
      if (!competitionId) throw new Error('No competition ID');
      return competitionApi.updateCompetition(competitionId, {
        ...data,
        startAt: new Date(data.startAt).toISOString(),
        endAt: new Date(data.endAt).toISOString(),
        registrationStartAt: new Date(data.registrationStartAt).toISOString(),
        registrationEndAt: new Date(data.registrationEndAt).toISOString(),
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-competitions'] });
      queryClient.invalidateQueries({ queryKey: ['competition', competitionId] });
      navigate(`/competitions/${competitionId}/config`);
    },
    onError: (error) => {
      console.error(error);
      alert('대회 수정에 실패했습니다.');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    mutation.mutate(formData);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  if (isLoading) return <div>로딩 중...</div>;

  return (
    <div className="mx-auto max-w-3xl space-y-6 pb-12">
      <div className="flex items-center space-x-4">
        <Button variant="ghost" onClick={() => navigate(`/competitions/${competitionId}/config`)}>
          ← 대회 상세
        </Button>
        <h1 className="text-2xl font-bold">대회 편집</h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>기본 정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">대회명 *</Label>
              <Input id="name" name="name" value={formData.name} onChange={handleChange} required />
            </div>
            <div className="space-y-2">
              <Label htmlFor="description">설명</Label>
              <Textarea id="description" name="description" value={formData.description} onChange={handleChange} />
            </div>
            <div className="space-y-2">
              <Label htmlFor="startAt">대회 기간 *</Label>
              <DateRangePicker
                value={{
                  from: formData.startAt ? new Date(formData.startAt) : undefined,
                  to: formData.endAt ? new Date(formData.endAt) : undefined,
                }}
                onChange={(range) =>
                  setFormData((prev) => ({
                    ...prev,
                    startAt: range?.from ? range.from.toISOString() : '',
                    endAt: range?.to ? range.to.toISOString() : '',
                  }))
                }
                placeholder="시작일과 종료일 선택"
              />
            </div>
            <div className="space-y-2">
              <Label>공개 상태</Label>
              <Select
                value={formData.visibility}
                onValueChange={(val) => setFormData((p) => ({ ...p, visibility: val as CompetitionVisibility }))}
              >
                <SelectTrigger>
                  <SelectValue placeholder="공개 상태 선택" />
                </SelectTrigger>
                <SelectContent>
                  {competitionVisibilityOptions.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>참가 신청 기간</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            <Label htmlFor="registrationStartAt">신청 기간 *</Label>
            <DateTimeRangePicker
              value={{
                from: formData.registrationStartAt ? new Date(formData.registrationStartAt) : undefined,
                to: formData.registrationEndAt ? new Date(formData.registrationEndAt) : undefined,
              }}
              onChange={(range) =>
                setFormData((prev) => ({
                  ...prev,
                  registrationStartAt: range?.from ? range.from.toISOString() : '',
                  registrationEndAt: range?.to ? range.to.toISOString() : '',
                }))
              }
              placeholder="신청 시작일시와 마감일시 선택"
            />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>참가비 & 계좌 정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="entryFee">참가비 (원)</Label>
              <Input type="number" id="entryFee" name="entryFee" value={formData.entryFee} onChange={handleChange} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="bankName">은행명</Label>
                <Input id="bankName" name="bankName" value={formData.bankName} onChange={handleChange} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="accountNumber">계좌번호</Label>
                <Input id="accountNumber" name="accountNumber" value={formData.accountNumber} onChange={handleChange} />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="accountHolder">예금주</Label>
              <Input id="accountHolder" name="accountHolder" value={formData.accountHolder} onChange={handleChange} />
            </div>
          </CardContent>
        </Card>

        <div className="flex justify-end space-x-4">
          <Button type="button" variant="outline" onClick={() => navigate(-1)}>취소</Button>
          <Button type="submit" disabled={mutation.isPending}>
            {mutation.isPending ? '저장 중...' : '저장하기'}
          </Button>
        </div>
      </form>
    </div>
  );
}
