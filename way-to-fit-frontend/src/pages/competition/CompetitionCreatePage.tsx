import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { DateTimeRangePicker } from '@/components/ui/date-time-range-picker';
import { DateRangePicker } from '@/components/ui/date-range-picker';
import { competitionApi } from '@/features/competition/api';

export function CompetitionCreatePage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

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
  });

  const [scaleCategoriesInput, setScaleCategoriesInput] = useState('');

  const mutation = useMutation({
    mutationFn: ({ formData: data, scaleCategories }: { formData: typeof formData; scaleCategories: string[] }) => {
      return competitionApi.createCompetition({
        ...data,
        startAt: new Date(data.startAt).toISOString(),
        endAt: new Date(data.endAt).toISOString(),
        registrationStartAt: new Date(data.registrationStartAt).toISOString(),
        registrationEndAt: new Date(data.registrationEndAt).toISOString(),
        scaleCategories,
      });
    },
    onSuccess: (res) => {
      queryClient.invalidateQueries({ queryKey: ['my-competitions'] });
      navigate(`/competitions/${res.id}/config`);
    },
    onError: (error) => {
      console.error(error);
      alert('대회 생성에 실패했습니다.');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const scaleCategories = scaleCategoriesInput.split(',').map((s) => s.trim()).filter(Boolean);
    if (scaleCategories.length === 0) {
      alert('참가 부문을 하나 이상 입력해주세요.');
      return;
    }
    mutation.mutate({ formData, scaleCategories });
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <div className="mx-auto max-w-3xl space-y-6 pb-12">
      <div className="flex items-center space-x-4">
        <Button variant="ghost" onClick={() => navigate('/competitions')}>
          ← 대회 목록
        </Button>
        <h1 className="text-2xl font-bold">새 대회 만들기</h1>
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
              <Label htmlFor="scaleCategories">참가 부문 (쉼표로 구분) *</Label>
              <Input
                id="scaleCategories"
                value={scaleCategoriesInput}
                onChange={(e) => setScaleCategoriesInput(e.target.value)}
                placeholder="예: RXD, SCALED, MASTERS"
                required
              />
              <p className="text-xs text-muted-foreground">
                등록 신청 및 이벤트에서 선택 가능한 부문 목록입니다.
              </p>
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
          <Button type="button" variant="outline" onClick={() => navigate('/competitions')}>취소</Button>
          <Button type="submit" disabled={mutation.isPending}>
            {mutation.isPending ? '생성 중...' : '저장 후 Stage 구성 →'}
          </Button>
        </div>
      </form>
    </div>
  );
}
