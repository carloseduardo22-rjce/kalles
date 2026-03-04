"use client";

import { AlertCircle } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { ApiError } from "@/shared/services/api";
import { cn } from "@/lib/utils";

interface ErrorAlertProps {
  error: unknown;
  title?: string;
  className?: string;
}

function getErrorMessage(error: unknown): string {
  if (error instanceof ApiError) return error.message;
  if (error instanceof Error) return error.message;
  if (typeof error === "string") return error;
  return "Ocorreu um erro inesperado. Tente novamente.";
}

export function ErrorAlert({
  error,
  title = "Erro",
  className,
}: ErrorAlertProps) {
  return (
    <Alert variant="destructive" className={cn(className)}>
      <AlertCircle className="h-4 w-4" />
      <AlertTitle>{title}</AlertTitle>
      <AlertDescription>{getErrorMessage(error)}</AlertDescription>
    </Alert>
  );
}
