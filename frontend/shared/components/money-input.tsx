"use client";

import * as React from "react";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";

const MAX_DIGITS = 14;

const moneyFormatter = new Intl.NumberFormat("pt-BR", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

function formatMoneyInput(value: number): string {
  return moneyFormatter.format(Math.max(0, value));
}

function centsFromDigits(digits: string): number {
  return digits ? Number(digits) / 100 : 0;
}

function parsePastedMoney(text: string): number | null {
  const cleaned = text.replace(/[^\d.,]/g, "");
  if (!/\d/.test(cleaned)) return null;
  const lastSeparator = Math.max(
    cleaned.lastIndexOf(","),
    cleaned.lastIndexOf("."),
  );
  const hasCents =
    lastSeparator >= 0 && cleaned.length - lastSeparator - 1 <= 2;
  const integerDigits = (
    hasCents ? cleaned.slice(0, lastSeparator) : cleaned
  ).replace(/\D/g, "");
  const centDigits = hasCents
    ? cleaned.slice(lastSeparator + 1).padEnd(2, "0")
    : "00";
  const digits = (integerDigits + centDigits).replace(/^0+(?=\d)/, "");
  if (digits.length > MAX_DIGITS) return null;
  return centsFromDigits(digits);
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
  placeholder,
  onFocus,
  onClick,
  onPaste,
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
        placeholder={placeholder ?? "0,00"}
        value={value == null ? "" : formatMoneyInput(value)}
        onChange={(event) => {
          const digits = event.target.value.replace(/\D/g, "");
          if (!digits && value == null) return;
          onValueChange(centsFromDigits(digits.slice(0, MAX_DIGITS)));
          keepCaretAtEnd(event.target);
        }}
        onPaste={(event) => {
          event.preventDefault();
          const pasted = parsePastedMoney(event.clipboardData.getData("text"));
          if (pasted !== null) onValueChange(pasted);
          keepCaretAtEnd(event.currentTarget);
          onPaste?.(event);
        }}
        onFocus={(event) => {
          keepCaretAtEnd(event.target);
          onFocus?.(event);
        }}
        onClick={(event) => {
          keepCaretAtEnd(event.currentTarget);
          onClick?.(event);
        }}
        className={cn("pl-10 text-right tabular-nums", className)}
      />
    </div>
  );
}
