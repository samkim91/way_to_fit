import { useEffect, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
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
import { eventApi } from '@/features/competition/api';
import {
  eventTypeLabels,
  genderCategoryLabels,
  wodTypeLabels,
} from '@/features/competition/labels';
import type {
  CompetitionEvent,
  CreateEventRequest,
  EventType,
  GenderCategory,
  WeightUnit,
  WodType,
} from '@/features/competition/types';

interface EventFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  competitionId: string;
  stageId: string;
  event: CompetitionEvent | null;
}

interface FormState {
  name: string;
  description: string;
  eventType: EventType;
  wodType: WodType;
  order: string;
  gender: GenderCategory;
  scaleCategories: string;
  submissionDeadline: string;
  releaseAt: string;
  timeCapMinutes: string;
  amrapMinutes: string;
  emomMinutes: string;
  weightUnit: WeightUnit | '';
}

const defaultForm: FormState = {
  name: '',
  description: '',
  eventType: 'INDIVIDUAL',
  wodType: 'FOR_TIME',
  order: '1',
  gender: 'MIXED',
  scaleCategories: '',
  submissionDeadline: '',
  releaseAt: '',
  timeCapMinutes: '',
  amrapMinutes: '',
  emomMinutes: '',
  weightUnit: '',
};

function toMinutes(seconds: number | null | undefined): string {
  if (!seconds) return '';
  return String(Math.round(seconds / 60));
}

function toSeconds(minutes: string): number | null {
  const n = Number(minutes);
  return minutes.trim() && !isNaN(n) && n > 0 ? n * 60 : null;
}

function parseScaleCategories(raw: string): string[] {
  return raw.split(',').map((s) => s.trim()).filter(Boolean);
}

export function EventFormDialog({ open, onOpenChange, competitionId, stageId, event }: EventFormDialogProps) {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState<FormState>(defaultForm);
  const [errors, setErrors] = useState<Partial<Record<string, string>>>({});

  useEffect(() => {
    if (open) {
      setErrors({});
      if (event) {
        setFormData({
          name: event.name,
          description: event.description,
          eventType: event.eventType,
          wodType: event.wodType,
          order: String(event.order),
          gender: event.gender,
          scaleCategories: event.scaleCategories.join(', '),
          submissionDeadline: event.submissionDeadline,
          releaseAt: event.releaseAt ?? '',
          timeCapMinutes: toMinutes(event.timeCap),
          amrapMinutes: toMinutes(event.amrapDuration),
          emomMinutes: toMinutes(event.emomDuration),
          weightUnit: event.weightUnit ?? '',
        });
      } else {
        setFormData(defaultForm);
      }
    }
  }, [open, event]);

  const saveMutation = useMutation({
    mutationFn: (data: FormState) => {
      const payload: CreateEventRequest = {
        name: data.name.trim(),
        description: data.description.trim(),
        eventType: data.eventType,
        wodType: data.wodType,
        order: Number(data.order),
        gender: data.gender,
        scaleCategories: parseScaleCategories(data.scaleCategories),
        submissionDeadline: data.submissionDeadline,
        releaseAt: data.releaseAt || null,
        timeCap: data.wodType === 'FOR_TIME' ? toSeconds(data.timeCapMinutes) : null,
        amrapDuration: data.wodType === 'AMRAP' ? toSeconds(data.amrapMinutes) : null,
        emomDuration: data.wodType === 'EMOM' ? toSeconds(data.emomMinutes) : null,
        weightUnit: data.wodType === 'MAX_WEIGHT' && data.weightUnit ? (data.weightUnit as WeightUnit) : null,
      };
      return event
        ? eventApi.updateEvent(competitionId, stageId, event.id, payload)
        : eventApi.createEvent(competitionId, stageId, payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events', competitionId, stageId] });
      onOpenChange(false);
    },
    onError: () => {
      alert('저장에 실패했습니다.');
    },
  });

  const handleSubmit = () => {
    const newErrors: typeof errors = {};
    if (!formData.name.trim()) newErrors.name = '이름을 입력해주세요.';
    if (!formData.order || Number(formData.order) < 1) newErrors.order = '순서는 1 이상이어야 합니다.';
    if (!formData.submissionDeadline) newErrors.submissionDeadline = '제출 마감일시를 선택해주세요.';
    if (!parseScaleCategories(formData.scaleCategories).length)
      newErrors.scaleCategories = '스케일 카테고리를 하나 이상 입력해주세요.';
    if (Object.keys(newErrors).length > 0) { setErrors(newErrors); return; }
    saveMutation.mutate(formData);
  };

  const set = (field: keyof FormState) => (value: string) =>
    setFormData((p) => ({ ...p, [field]: value }));

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{event ? 'Event 편집' : 'Event 추가'}</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <div className="space-y-1">
            <Label>이름 *</Label>
            <Input
              value={formData.name}
              onChange={(e) => set('name')(e.target.value)}
              placeholder="예: Event 1 — 21.1"
            />
            {errors.name && <p className="text-xs text-destructive">{errors.name}</p>}
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label>Event 타입 *</Label>
              <Select value={formData.eventType} onValueChange={set('eventType')}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {(Object.keys(eventTypeLabels) as EventType[]).map((k) => (
                    <SelectItem key={k} value={k}>{eventTypeLabels[k]}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-1">
              <Label>성별 *</Label>
              <Select value={formData.gender} onValueChange={set('gender')}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {(Object.keys(genderCategoryLabels) as GenderCategory[]).map((k) => (
                    <SelectItem key={k} value={k}>{genderCategoryLabels[k]}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="space-y-1">
            <Label>스케일 카테고리 * (쉼표로 구분)</Label>
            <Input
              value={formData.scaleCategories}
              onChange={(e) => set('scaleCategories')(e.target.value)}
              placeholder="예: RXD, SCALED"
            />
            {errors.scaleCategories && <p className="text-xs text-destructive">{errors.scaleCategories}</p>}
          </div>

          <div className="space-y-1">
            <Label>WOD 타입 *</Label>
            <Select value={formData.wodType} onValueChange={set('wodType')}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                {(Object.keys(wodTypeLabels) as WodType[]).map((k) => (
                  <SelectItem key={k} value={k}>{wodTypeLabels[k]}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {formData.wodType === 'FOR_TIME' && (
            <div className="space-y-1">
              <Label>타임캡 (분, 없으면 비워두기)</Label>
              <Input
                type="number"
                min="1"
                value={formData.timeCapMinutes}
                onChange={(e) => set('timeCapMinutes')(e.target.value)}
                placeholder="예: 20"
              />
            </div>
          )}

          {formData.wodType === 'AMRAP' && (
            <div className="space-y-1">
              <Label>AMRAP 시간 (분) *</Label>
              <Input
                type="number"
                min="1"
                value={formData.amrapMinutes}
                onChange={(e) => set('amrapMinutes')(e.target.value)}
                placeholder="예: 20"
              />
            </div>
          )}

          {formData.wodType === 'EMOM' && (
            <div className="space-y-1">
              <Label>EMOM 시간 (분) *</Label>
              <Input
                type="number"
                min="1"
                value={formData.emomMinutes}
                onChange={(e) => set('emomMinutes')(e.target.value)}
                placeholder="예: 12"
              />
            </div>
          )}

          {formData.wodType === 'MAX_WEIGHT' && (
            <div className="space-y-1">
              <Label>중량 단위 *</Label>
              <Select value={formData.weightUnit} onValueChange={set('weightUnit')}>
                <SelectTrigger><SelectValue placeholder="선택" /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="KG">KG</SelectItem>
                  <SelectItem value="LB">LB</SelectItem>
                </SelectContent>
              </Select>
            </div>
          )}

          <div className="space-y-1">
            <Label>WOD 설명 (선택)</Label>
            <Textarea
              value={formData.description}
              onChange={(e) => set('description')(e.target.value)}
              rows={3}
              placeholder="예: 21-15-9&#10;Thrusters&#10;Pull-ups"
            />
          </div>

          <div className="space-y-1">
            <Label>순서 *</Label>
            <Input
              type="number"
              min="1"
              value={formData.order}
              onChange={(e) => set('order')(e.target.value)}
              disabled={!!event}
            />
            {event && <p className="text-xs text-muted-foreground">기록이 있는 이벤트의 순서는 변경할 수 없습니다.</p>}
            {errors.order && <p className="text-xs text-destructive">{errors.order}</p>}
          </div>

          <div className="space-y-1">
            <Label>제출 마감일시 *</Label>
            <DateTimePicker
              value={formData.submissionDeadline ? new Date(formData.submissionDeadline) : undefined}
              onChange={(date) => set('submissionDeadline')(date ? date.toISOString() : '')}
            />
            {errors.submissionDeadline && <p className="text-xs text-destructive">{errors.submissionDeadline}</p>}
          </div>

          <div className="space-y-1">
            <Label>공개 시점 (선택, 비워두면 대회 OPEN 시 일괄 공개)</Label>
            <DateTimePicker
              value={formData.releaseAt ? new Date(formData.releaseAt) : undefined}
              onChange={(date) => set('releaseAt')(date ? date.toISOString() : '')}
            />
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <Button variant="outline" onClick={() => onOpenChange(false)}>취소</Button>
            <Button onClick={handleSubmit} disabled={saveMutation.isPending}>
              {saveMutation.isPending ? '저장 중...' : '저장'}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
