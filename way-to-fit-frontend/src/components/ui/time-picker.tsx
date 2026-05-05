import * as React from "react"
import { Check, Clock3, ChevronDown } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { ScrollArea } from "@/components/ui/scroll-area"
import { cn } from "@/lib/utils"

interface TimePickerProps {
  value?: string | null
  onChange?: (value: string) => void
  placeholder?: string
  stepMinutes?: number
}

function pad(value: number) {
  return String(value).padStart(2, "0")
}

function buildTimeOptions(stepMinutes: number) {
  const options: string[] = []

  for (let hour = 0; hour < 24; hour += 1) {
    for (let minute = 0; minute < 60; minute += stepMinutes) {
      options.push(`${pad(hour)}:${pad(minute)}`)
    }
  }

  return options
}

function TimePicker({
  value,
  onChange,
  placeholder = "시간 선택",
  stepMinutes = 30,
}: TimePickerProps) {
  const options = React.useMemo(() => buildTimeOptions(stepMinutes), [stepMinutes])
  const displayValue = value || ""

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          type="button"
          variant="outline"
          className={cn(
            "w-full justify-between font-normal",
            !displayValue && "text-muted-foreground"
          )}
        >
          <span className="flex items-center gap-2">
            <Clock3 className="h-4 w-4 opacity-70" />
            {displayValue || placeholder}
          </span>
          <ChevronDown className="h-4 w-4 opacity-60" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[320px] p-0" align="start">
        <div className="border-b px-4 py-3">
          <div className="text-sm font-medium">고정 공개 시간</div>
          <p className="mt-1 text-xs text-muted-foreground">빠른 선택 또는 직접 입력으로 시간을 지정합니다.</p>
        </div>

        <div className="border-b px-4 py-3">
          <Input
            type="time"
            step={300}
            value={displayValue}
            onChange={(event) => onChange?.(event.target.value)}
          />
        </div>

        <ScrollArea className="h-64">
          <div className="grid grid-cols-3 gap-2 p-3">
            {options.map((option) => {
              const isSelected = option === displayValue

              return (
                <Button
                  key={option}
                  type="button"
                  variant={isSelected ? "default" : "outline"}
                  size="sm"
                  className="justify-between"
                  onClick={() => onChange?.(option)}
                >
                  {option}
                  {isSelected ? <Check className="h-3.5 w-3.5" /> : null}
                </Button>
              )
            })}
          </div>
        </ScrollArea>
      </PopoverContent>
    </Popover>
  )
}

export { TimePicker }
