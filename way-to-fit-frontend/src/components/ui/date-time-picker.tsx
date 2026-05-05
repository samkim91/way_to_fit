import * as React from "react"
import dayjs from "dayjs"
import { CalendarIcon, Clock3 } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Calendar } from "@/components/ui/calendar"
import { Input } from "@/components/ui/input"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { cn } from "@/lib/utils"

interface DateTimePickerProps
  extends Omit<React.ComponentProps<typeof Button>, "value" | "onChange"> {
  value?: Date
  onChange?: (value: Date | undefined) => void
  placeholder?: string
  defaultTime?: string
  timeStep?: number
}

function applyTime(date: Date, time: string) {
  const [hours = "0", minutes = "0"] = time.split(":")
  return dayjs(date)
    .hour(Number(hours))
    .minute(Number(minutes))
    .second(0)
    .millisecond(0)
    .toDate()
}

function DateTimePicker({
  value,
  onChange,
  placeholder = "날짜와 시간을 선택하세요",
  defaultTime = "00:00",
  timeStep = 300,
  className,
  variant = "outline",
  ...props
}: DateTimePickerProps) {
  const timeValue = value ? dayjs(value).format("HH:mm") : defaultTime

  const handleDateSelect = (nextDate?: Date) => {
    if (!nextDate) {
      onChange?.(undefined)
      return
    }

    onChange?.(applyTime(nextDate, timeValue))
  }

  const handleTimeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const nextTime = event.target.value
    if (!nextTime) {
      return
    }

    onChange?.(applyTime(value ?? new Date(), nextTime))
  }

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          type="button"
          variant={variant}
          className={cn(
            "w-full justify-between text-left font-normal",
            !value && "text-muted-foreground",
            className
          )}
          {...props}
        >
          <span>{value ? dayjs(value).format("YYYY.MM.DD HH:mm") : placeholder}</span>
          <CalendarIcon className="h-4 w-4 opacity-60" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto overflow-hidden p-0" align="start">
        <div className="border-b p-3">
          <Calendar mode="single" selected={value} onSelect={handleDateSelect} initialFocus />
        </div>
        <div className="grid gap-2 p-3">
          <div className="flex items-center gap-2 text-xs text-muted-foreground">
            <Clock3 className="h-3.5 w-3.5" />
            시간
          </div>
          <Input type="time" step={timeStep} value={timeValue} onChange={handleTimeChange} />
        </div>
      </PopoverContent>
    </Popover>
  )
}

export { DateTimePicker }
