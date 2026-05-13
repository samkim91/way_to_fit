import * as React from 'react';
import dayjs from 'dayjs';
import { CalendarIcon, Clock3 } from 'lucide-react';
import type { DateRange } from 'react-day-picker';

import { Button } from '@/components/ui/button';
import { Calendar } from '@/components/ui/calendar';
import { Input } from '@/components/ui/input';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { cn } from '@/lib/utils';

type DateTimeRangePickerValue = DateRange;

interface DateTimeRangePickerProps
  extends Omit<React.ComponentProps<typeof Button>, 'value' | 'onChange'> {
  value?: DateTimeRangePickerValue;
  onChange?: (value: DateTimeRangePickerValue | undefined) => void;
  placeholder?: string;
  defaultStartTime?: string;
  defaultEndTime?: string;
  timeStep?: number;
}

function applyTime(date: Date, time: string) {
  const [hours = '0', minutes = '0'] = time.split(':');

  return dayjs(date)
    .hour(Number(hours))
    .minute(Number(minutes))
    .second(0)
    .millisecond(0)
    .toDate();
}

function formatRangeLabel(value?: DateTimeRangePickerValue, placeholder?: string) {
  if (!value?.from) {
    return placeholder;
  }

  const fromLabel = dayjs(value.from).format('YYYY.MM.DD HH:mm');

  if (!value.to) {
    return `${fromLabel} - 종료일시 선택`;
  }

  return `${fromLabel} - ${dayjs(value.to).format('YYYY.MM.DD HH:mm')}`;
}

function DateTimeRangePicker({
  value,
  onChange,
  placeholder = '기간을 선택하세요',
  defaultStartTime = '00:00',
  defaultEndTime = '00:00',
  timeStep = 300,
  className,
  variant = 'outline',
  ...props
}: DateTimeRangePickerProps) {
  const startTimeValue = value?.from ? dayjs(value.from).format('HH:mm') : defaultStartTime;
  const endTimeValue = value?.to ? dayjs(value.to).format('HH:mm') : defaultEndTime;

  const handleRangeSelect = (nextRange?: DateRange) => {
    if (!nextRange?.from && !nextRange?.to) {
      onChange?.(undefined);
      return;
    }

    onChange?.({
      from: nextRange?.from ? applyTime(nextRange.from, startTimeValue) : undefined,
      to: nextRange?.to ? applyTime(nextRange.to, endTimeValue) : undefined,
    });
  };

  const handleStartTimeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const nextTime = event.target.value;
    if (!nextTime) {
      return;
    }

    onChange?.({
      from: value?.from ? applyTime(value.from, nextTime) : undefined,
      to: value?.to,
    });
  };

  const handleEndTimeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const nextTime = event.target.value;
    if (!nextTime) {
      return;
    }

    onChange?.({
      from: value?.from,
      to: value?.to ? applyTime(value.to, nextTime) : undefined,
    });
  };

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          type="button"
          variant={variant}
          className={cn(
            'w-full justify-between text-left font-normal',
            !value?.from && 'text-muted-foreground',
            className,
          )}
          {...props}
        >
          <span>{formatRangeLabel(value, placeholder)}</span>
          <CalendarIcon className="h-4 w-4 opacity-60" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto overflow-hidden p-0" align="start">
        <div className="border-b p-3">
          <Calendar
            mode="range"
            numberOfMonths={2}
            defaultMonth={value?.from}
            selected={value?.from ? { from: value.from, to: value.to } : undefined}
            onSelect={handleRangeSelect}
            initialFocus
          />
        </div>
        <div className="grid gap-3 p-3 md:grid-cols-2">
          <div className="grid gap-2">
            <div className="flex items-center gap-2 text-xs text-muted-foreground">
              <Clock3 className="h-3.5 w-3.5" />
              시작 시간
            </div>
            <Input type="time" step={timeStep} value={startTimeValue} onChange={handleStartTimeChange} />
          </div>
          <div className="grid gap-2">
            <div className="flex items-center gap-2 text-xs text-muted-foreground">
              <Clock3 className="h-3.5 w-3.5" />
              종료 시간
            </div>
            <Input type="time" step={timeStep} value={endTimeValue} onChange={handleEndTimeChange} />
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
}

export { DateTimeRangePicker };
