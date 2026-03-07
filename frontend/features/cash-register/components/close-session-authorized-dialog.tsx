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
import { cashRegisterService } from "../services/cash-register.service";

interface CloseSessionAuthorizedDialogProps {
  isLoading: boolean;
  error: string | null;
  onConfirm: () => Promise<void>;
}

const AUTHORIZED_LEVELS = ["SUPERVISOR", "MANAGER", "ADMIN"];

export function CloseSessionAuthorizedDialog({
  isLoading,
  error,
  onConfirm,
}: CloseSessionAuthorizedDialogProps) {
  const [open, setOpen] = useState(false);
  const [operatorCode, setOperatorCode] = useState("");
  const [authError, setAuthError] = useState<string | null>(null);
  const [checking, setChecking] = useState(false);

  function handleOpenChange(value: boolean) {
    setOpen(value);
    if (!value) {
      setOperatorCode("");
      setAuthError(null);
    }
  }

  async function handleConfirm() {
    if (!operatorCode.trim()) {
      setAuthError("Insira o código do operador autorizado.");
      return;
    }
    setChecking(true);
    setAuthError(null);
    try {
      const operators = await cashRegisterService.listOperators();
      const match = operators.find(
        (op) =>
          op.code.toUpperCase() === operatorCode.trim().toUpperCase() &&
          AUTHORIZED_LEVELS.includes(op.permissionLevel ?? ""),
      );
      if (!match) {
        setAuthError(
          "Código inválido ou operador sem permissão para fechar o caixa.",
        );
        return;
      }
      await onConfirm();
      setOpen(false);
    } catch {
      setAuthError("Erro ao verificar autorização. Tente novamente.");
    } finally {
      setChecking(false);
    }
  }

  const busy = isLoading || checking;

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
            <ShieldAlert className="h-5 w-5 text-amber-500" />
            Autorização necessária
          </DialogTitle>
          <DialogDescription>
            O fechamento de caixa requer o código de um supervisor, gerente ou
            administrador.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-3 py-2">
          <div className="space-y-1.5">
            <Label htmlFor="auth-code">Código do operador autorizado</Label>
            <Input
              id="auth-code"
              placeholder="ex: OP-002"
              value={operatorCode}
              onChange={(e) => {
                setOperatorCode(e.target.value);
                setAuthError(null);
              }}
              onKeyDown={(e) => e.key === "Enter" && !busy && handleConfirm()}
              autoFocus
              autoComplete="off"
            />
          </div>
          {(authError || error) && (
            <ErrorAlert error={authError ?? error} className="text-sm" />
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
            disabled={busy || !operatorCode.trim()}
          >
            {busy ? (
              <LoadingSpinner size="sm" className="mr-2" />
            ) : (
              <Lock className="mr-2 h-4 w-4" />
            )}
            Fechar Caixa
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
