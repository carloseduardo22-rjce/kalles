"use client";

import { useState } from "react";
import { ShieldAlert, XCircle } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ErrorAlert } from "@/shared/components/error-alert";

interface CancellationDialogProps {
  open: boolean;
  operatorId: string;
  isLoading: boolean;
  error: string | null;
  onClose: () => void;
  onConfirm: (operatorId: string, authorizerId?: string) => Promise<void>;
}

export function CancellationDialog({
  open,
  operatorId,
  isLoading,
  error,
  onClose,
  onConfirm,
}: CancellationDialogProps) {
  const [authorizerId, setAuthorizerId] = useState("");
  const [needsAuth, setNeedsAuth] = useState(false);

  async function handleConfirm() {
    try {
      await onConfirm(operatorId, authorizerId.trim() || undefined);
    } catch (err: unknown) {
      if (
        typeof err === "object" &&
        err !== null &&
        "status" in err &&
        (err as { status: number }).status === 403
      ) {
        setNeedsAuth(true);
      }
    }
  }

  function handleClose() {
    setAuthorizerId("");
    setNeedsAuth(false);
    onClose();
  }

  return (
    <Dialog open={open} onOpenChange={() => handleClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <XCircle className="h-5 w-5 text-destructive" />
            Cancelar venda
          </DialogTitle>
          <DialogDescription>
            Esta ação cancelará todos os itens da venda atual e restaurará o
            estoque. O cancelamento ficará registrado na auditoria.
          </DialogDescription>
        </DialogHeader>

        {needsAuth && (
          <div className="space-y-3 rounded-lg border border-amber-300 bg-amber-50 p-3 dark:border-amber-700 dark:bg-amber-950">
            <div className="flex items-center gap-2 text-amber-700 dark:text-amber-400">
              <ShieldAlert className="h-4 w-4" />
              <p className="text-sm font-medium">
                Autorização de supervisor necessária
              </p>
            </div>
            <p className="text-xs text-amber-600 dark:text-amber-500">
              Seu nível de permissão não permite cancelar vendas. Solicite o ID
              de um supervisor ou gerente.
            </p>
            <div className="space-y-1">
              <Label htmlFor="cancel-auth-id" className="text-xs">
                ID do autorizador (UUID)
              </Label>
              <Input
                data-testid="cancel-auth-id-input"
                id="cancel-auth-id"
                value={authorizerId}
                onChange={(e) => setAuthorizerId(e.target.value)}
                placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                className="font-mono text-xs"
              />
            </div>
          </div>
        )}

        {error && !needsAuth && (
          <ErrorAlert error={error} title="Erro ao cancelar venda" />
        )}

        <DialogFooter>
          <Button variant="outline" onClick={handleClose} disabled={isLoading}>
            Voltar
          </Button>
          <Button
            data-testid="cancel-sale-confirm"
            variant="destructive"
            onClick={handleConfirm}
            disabled={isLoading || (needsAuth && !authorizerId.trim())}
          >
            {isLoading ? (
              <LoadingSpinner size="sm" label="" />
            ) : (
              "Cancelar Venda"
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
