import * as React from 'react';
import dayjs from 'dayjs';
import { CalendarIcon } from 'lucide-react';
import type { DateRange } from 'react-day-picker';

import { Button } from '@/components/ui/button';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { cn } from '@/lib/utils';

interface DateRangePickerProps
  extends Omit<React.ComponentProps<typeof Button>, 'value' | 'onChange'> {
  value?: DateRange;
  onChange?: (value: DateRange | undefined) => void;
  placeholder?: string;
}

function normalizeDate(value?: Date) {
  if (!value) {
    return undefined;
  }

  return dayjs(value)
    .hour(0)
    .minute(0)
    .second(0)
    .millisecond(0)
    .toDate();
}

function formatRangeLabel(value?: DateRange, placeholder?: string) {
  if (!value?.from) {
    return placeholder;
  }

  if (!value.to) {
    return `${dayjs(value.from).format('YYYY.MM.DD')} - 종료일 선택`;
  }

  return `${dayjs(value.from).format('YYYY.MM.DD')} - ${dayjs(value.to).format('YYYY.MM.DD')}`;
}

function DateRangePicker({
  value,
  onChange,
  placeholder = '기간을 선택하세요',
  className,
  variant = 'outline',
  ...props
}: DateRangePickerProps) {
  const handleRangeSelect = (nextRange?: DateRange) => {
    if (!nextRange?.from && !nextRange?.to) {
      onChange?.(undefined);
      return;
    }

    onChange?.({
      from: normalizeDate(nextRange?.from),
      to: normalizeDate(nextRange?.to),
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
        <Calendar
          mode="range"
          numberOfMonths={2}
          defaultMonth={value?.from}
          selected={value}
          onSelect={handleRangeSelect}
          initialFocus
        />
      </PopoverContent>
    </Popover>
  );
}

export { DateRangePicker };
