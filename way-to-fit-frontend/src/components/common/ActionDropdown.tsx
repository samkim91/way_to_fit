import React, { useState, useEffect, useRef } from 'react';
import { ChevronDown, Check } from 'lucide-react';

export interface ActionDropdownOption {
  value: string;
  label: string;
}

interface ActionDropdownProps {
  label: string;
  value: string;
  options: ActionDropdownOption[];
  onSelect: (val: string) => void;
  disabled?: boolean;
  icon: React.ReactNode;
  className?: string;
}

export function ActionDropdown({
  label,
  value,
  options,
  onSelect,
  disabled,
  icon,
  className = '',
}: ActionDropdownProps) {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const selectedOption = options.find((o) => o.value === value);
  const selectedLabel = selectedOption?.label || value;

  return (
    <div className={`relative ${className}`} ref={containerRef}>
      <label className="absolute -top-2 left-3 px-1 bg-background text-[10px] font-black text-primary z-10 uppercase tracking-widest">
        {label}
      </label>
      <button
        type="button"
        disabled={disabled}
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center justify-between w-48 h-12 px-4 bg-background border-2 border-border/60 rounded-xl text-sm font-bold hover:border-primary/40 hover:bg-secondary/20 transition-all focus:outline-none focus:ring-2 focus:ring-primary/20 disabled:opacity-50"
      >
        <div className="flex items-center gap-2.5 min-w-0">
          <div className="text-primary/70 shrink-0">{icon}</div>
          <span className="truncate">{selectedLabel}</span>
        </div>
        <ChevronDown
          className={`h-4 w-4 shrink-0 text-muted-foreground transition-transform duration-300 ${
            isOpen ? 'rotate-180' : ''
          }`}
        />
      </button>

      {isOpen && (
        <div className="absolute top-[calc(100%+8px)] left-0 w-full bg-card border border-border rounded-xl shadow-xl z-50 py-2 animate-in zoom-in-95 duration-200">
          {options.map((opt) => (
            <button
              key={opt.value}
              type="button"
              onClick={() => {
                onSelect(opt.value);
                setIsOpen(false);
              }}
              className={`flex items-center justify-between w-full px-4 py-2.5 text-sm font-medium transition-colors hover:bg-muted ${
                opt.value === value ? 'text-primary bg-primary/5' : 'text-foreground font-normal'
              }`}
            >
              {opt.label}
              {opt.value === value && <Check className="h-4 w-4" />}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
