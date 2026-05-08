import { useEffect, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { DateTimePicker } from '@/components/ui/date-time-picker';
import { stageApi } from '@/features/competition/api';
import { stageFormatLabels, stageTypeLabels } from '@/features/competition/labels';
import type { CompetitionStage, StageFormat, StageType } from '@/features/competition/types';

interface StageFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  competitionId: string;
  stage: CompetitionStage | null;
}

interface FormState {
  name: string;
  stageType: StageType;
  stageFormat: StageFormat;
  startAt: string;
  endAt: string;
}

const defaultForm: FormState = {
  name: '',
  stageType: 'QUALIFIER',
  stageFormat: 'ONLINE',
  startAt: '',
  endAt: '',
};

export function StageFormDialog({ open, onOpenChange, competitionId, stage }: StageFormDialogProps) {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState<FormState>(defaultForm);
  const [errors, setErrors] = useState<Partial<Record<keyof FormState, string>>>({});

  useEffect(() => {
    if (open) {
      setErrors({});
      setFormData(
        stage
          ? { name: stage.name, stageType: stage.stageType, stageFormat: stage.stageFormat, startAt: stage.startAt, endAt: stage.endAt }
          : defaultForm,
      );
    }
  }, [open, stage]);

  const mutation = useMutation({
    mutationFn: (data: FormState) =>
      stage
        ? stageApi.updateStage(competitionId, stage.id, data)
        : stageApi.createStage(competitionId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['stages', competitionId] });
      onOpenChange(false);
    },
    onError: () => {
      alert('저장에 실패했습니다.');
    },
  });

  const handleSubmit = () => {
    const newErrors: typeof errors = {};
    if (!formData.name.trim()) newErrors.name = '이름을 입력해주세요.';
    if (!formData.startAt) newErrors.startAt = '시작일시를 선택해주세요.';
    if (!formData.endAt) newErrors.endAt = '종료일시를 선택해주세요.';
    if (formData.startAt && formData.endAt && formData.startAt >= formData.endAt)
      newErrors.endAt = '종료일시는 시작일시보다 늦어야 합니다.';
    if (Object.keys(newErrors).length > 0) { setErrors(newErrors); return; }
    mutation.mutate(formData);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{stage ? 'Stage 편집' : 'Stage 추가'}</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <div className="space-y-1">
            <Label>이름 *</Label>
            <Input
              value={formData.name}
              onChange={(e) => setFormData((p) => ({ ...p, name: e.target.value }))}
              placeholder="예: 예선"
            />
            {errors.name && <p className="text-xs text-destructive">{errors.name}</p>}
          </div>

          <div className="space-y-1">
            <Label>Stage 타입 *</Label>
            <Select
              value={formData.stageType}
              onValueChange={(v) => setFormData((p) => ({ ...p, stageType: v as StageType }))}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {(Object.keys(stageTypeLabels) as StageType[]).map((k) => (
                  <SelectItem key={k} value={k}>{stageTypeLabels[k]}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-1">
            <Label>진행 방식 *</Label>
            <Select
              value={formData.stageFormat}
              onValueChange={(v) => setFormData((p) => ({ ...p, stageFormat: v as StageFormat }))}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {(Object.keys(stageFormatLabels) as StageFormat[]).map((k) => (
                  <SelectItem key={k} value={k}>{stageFormatLabels[k]}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label>시작일시 *</Label>
              <DateTimePicker
                value={formData.startAt ? new Date(formData.startAt) : undefined}
                onChange={(date) => setFormData((p) => ({ ...p, startAt: date ? date.toISOString() : '' }))}
              />
              {errors.startAt && <p className="text-xs text-destructive">{errors.startAt}</p>}
            </div>
            <div className="space-y-1">
              <Label>종료일시 *</Label>
              <DateTimePicker
                value={formData.endAt ? new Date(formData.endAt) : undefined}
                onChange={(date) => setFormData((p) => ({ ...p, endAt: date ? date.toISOString() : '' }))}
              />
              {errors.endAt && <p className="text-xs text-destructive">{errors.endAt}</p>}
            </div>
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <Button variant="outline" onClick={() => onOpenChange(false)}>취소</Button>
            <Button onClick={handleSubmit} disabled={mutation.isPending}>
              {mutation.isPending ? '저장 중...' : '저장'}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
