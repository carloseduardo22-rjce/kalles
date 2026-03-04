"use client";

import { Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";

interface LoadingSpinnerProps {
  className?: string;
  size?: "sm" | "md" | "lg";
  label?: string;
}

const sizeMap = { sm: "w-4 h-4", md: "w-6 h-6", lg: "w-10 h-10" };

export function LoadingSpinner({
  className,
  size = "md",
  label = "Carregando…",
}: LoadingSpinnerProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center gap-2 p-6",
        className,
      )}
      role="status"
      aria-label={label}
    >
      <Loader2 className={cn("animate-spin text-primary", sizeMap[size])} />
      {label && <p className="text-sm text-muted-foreground">{label}</p>}
    </div>
  );
}
