import * as React from "react"
import dayjs from "dayjs"
import { CalendarIcon } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { cn } from "@/lib/utils"

interface DatePickerProps
  extends Omit<React.ComponentProps<typeof Button>, "value" | "onChange"> {
  value?: Date
  onChange?: (value: Date | undefined) => void
  placeholder?: string
}

function DatePicker({
  value,
  onChange,
  placeholder = "날짜를 선택하세요",
  className,
  variant = "outline",
  ...props
}: DatePickerProps) {
  const handleDateSelect = (nextDate?: Date) => {
    if (!nextDate) {
      onChange?.(undefined)
      return
    }

    // Keep the time as 00:00:00 for strict date-only picker
    const selectedDate = dayjs(nextDate)
      .hour(0)
      .minute(0)
      .second(0)
      .millisecond(0)
      .toDate()

    onChange?.(selectedDate)
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
          <span>{value ? dayjs(value).format("YYYY.MM.DD") : placeholder}</span>
          <CalendarIcon className="h-4 w-4 opacity-60" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto overflow-hidden p-0" align="start">
        <Calendar mode="single" selected={value} onSelect={handleDateSelect} initialFocus />
      </PopoverContent>
    </Popover>
  )
}

export { DatePicker }
