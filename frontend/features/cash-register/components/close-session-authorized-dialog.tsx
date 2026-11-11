"use client";

import { useState } from "react";
import { Lock, ShieldAlert } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ErrorAlert } from "@/shared/components/error-alert";
import { isClosingAuthorizationRequired } from "../hooks/use-session";

interface CloseSessionAuthorizedDialogProps {
  isLoading: boolean;
  error: string | null;
  onConfirm: (payload: {
    authorizedOperatorCode?: string;
    countedCashAmount: number;
  }) => Promise<void>;
}

export function CloseSessionAuthorizedDialog({
  isLoading,
  error,
  onConfirm,
}: CloseSessionAuthorizedDialogProps) {
  const [open, setOpen] = useState(false);
  const [countedCashAmount, setCountedCashAmount] = useState("");
  const [authorizerCode, setAuthorizerCode] = useState("");
  const [needsAuthorization, setNeedsAuthorization] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function handleOpenChange(value: boolean) {
    setOpen(value);
    if (!value) {
      setCountedCashAmount("");
      setAuthorizerCode("");
      setNeedsAuthorization(false);
      setFormError(null);
    }
  }

  async function handleConfirm() {
    const countedValue = Number(countedCashAmount.replace(",", "."));
    if (
      !countedCashAmount.trim() ||
      Number.isNaN(countedValue) ||
      countedValue < 0
    ) {
      setFormError("Informe o valor contado em dinheiro.");
      return;
    }
    if (needsAuthorization && !authorizerCode.trim()) {
      setFormError("Informe a matrícula de quem libera o fechamento.");
      return;
    }
    setSubmitting(true);
    setFormError(null);
    try {
      await onConfirm({
        countedCashAmount: countedValue,
        authorizedOperatorCode: needsAuthorization
          ? authorizerCode.trim()
          : undefined,
      });
      setOpen(false);
    } catch (err) {
      if (isClosingAuthorizationRequired(err)) {
        setNeedsAuthorization(true);
      } else {
        setFormError(
          err instanceof Error
            ? err.message
            : "Não foi possível fechar o caixa. Tente novamente.",
        );
      }
    } finally {
      setSubmitting(false);
    }
  }

  const busy = isLoading || submitting;
  const canSubmit =
    !!countedCashAmount.trim() &&
    (!needsAuthorization || !!authorizerCode.trim());

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm">
          <Lock className="mr-2 h-4 w-4" />
          Fechar Caixa
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-sm">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            {needsAuthorization ? (
              <ShieldAlert className="h-5 w-5 text-amber-500" />
            ) : (
              <Lock className="h-5 w-5" />
            )}
            {needsAuthorization ? "Liberação necessária" : "Fechar caixa"}
          </DialogTitle>
          <DialogDescription>
            {needsAuthorization
              ? "Esta contagem precisa da liberação de um supervisor. Confira o dinheiro com ele e peça que informe a matrícula."
              : "Conte o dinheiro da gaveta e informe o total."}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-3 py-2">
          <div className="space-y-1.5">
            <Label htmlFor="counted-cash">Valor contado em dinheiro</Label>
            <Input
              id="counted-cash"
              type="number"
              min="0"
              step="0.01"
              value={countedCashAmount}
              onChange={(e) => {
                setCountedCashAmount(e.target.value);
                setFormError(null);
              }}
              onKeyDown={(e) => e.key === "Enter" && !busy && handleConfirm()}
              autoFocus
              autoComplete="off"
            />
          </div>
          {needsAuthorization && (
            <div className="space-y-1.5">
              <Label htmlFor="authorizer-code">Matrícula do supervisor</Label>
              <Input
                id="authorizer-code"
                value={authorizerCode}
                onChange={(e) => {
                  setAuthorizerCode(e.target.value);
                  setFormError(null);
                }}
                onKeyDown={(e) => e.key === "Enter" && !busy && handleConfirm()}
                autoFocus
                autoComplete="off"
              />
            </div>
          )}
          {(formError || error) && (
            <ErrorAlert error={formError ?? error} className="text-sm" />
          )}
        </div>

        <DialogFooter>
          <Button
            variant="ghost"
            onClick={() => handleOpenChange(false)}
            disabled={busy}
          >
            Cancelar
          </Button>
          <Button
            variant="destructive"
            onClick={handleConfirm}
            disabled={busy || !canSubmit}
          >
            {busy ? (
              <LoadingSpinner size="sm" className="mr-2" />
            ) : (
              <Lock className="mr-2 h-4 w-4" />
            )}
            {needsAuthorization ? "Liberar e fechar" : "Fechar Caixa"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
