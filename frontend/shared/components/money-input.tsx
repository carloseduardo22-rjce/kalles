"use client";

import * as React from "react";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";

const MAX_DIGITS = 15;

const moneyFormatter = new Intl.NumberFormat("pt-BR", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

export function formatMoneyInput(value: number): string {
  return moneyFormatter.format(Math.max(0, value));
}

export function parseMoneyDigits(text: string): number {
  const digits = text.replace(/\D/g, "").slice(0, MAX_DIGITS);
  return digits ? Number(digits) / 100 : 0;
}

function keepCaretAtEnd(input: HTMLInputElement) {
  window.requestAnimationFrame(() => {
    const end = input.value.length;
    input.setSelectionRange(end, end);
  });
}

type MoneyInputProps = Omit<
  React.ComponentProps<"input">,
  "value" | "defaultValue" | "onChange" | "type"
> & {
  value: number | null | undefined;
  onValueChange: (value: number) => void;
};

export function MoneyInput({
  value,
  onValueChange,
  className,
  onFocus,
  ...props
}: MoneyInputProps) {
  return (
    <div className="relative">
      <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-sm text-muted-foreground">
        R$
      </span>
      <Input
        {...props}
        type="text"
        inputMode="numeric"
        autoComplete="off"
        value={formatMoneyInput(value ?? 0)}
        onChange={(event) => {
          onValueChange(parseMoneyDigits(event.target.value));
          keepCaretAtEnd(event.target);
        }}
        onFocus={(event) => {
          keepCaretAtEnd(event.target);
          onFocus?.(event);
        }}
        className={cn("pl-10 text-right tabular-nums", className)}
      />
    </div>
  );
}
