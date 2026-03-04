"use client";

import { useState } from "react";
import { ShieldAlert } from "lucide-react";
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
import type { ProductCodeType, SaleItemResponse } from "../types";

interface ItemRemovalDialogProps {
  open: boolean;
  item: SaleItemResponse | null;
  operatorId: string;
  isLoading: boolean;
  error: string | null;
  onClose: () => void;
  onConfirm: (
    productCode: string,
    type: ProductCodeType,
    operatorId: string,
    authorizerId?: string,
  ) => Promise<void>;
}

export function ItemRemovalDialog({
  open,
  item,
  operatorId,
  isLoading,
  error,
  onClose,
  onConfirm,
}: ItemRemovalDialogProps) {
  const [authorizerId, setAuthorizerId] = useState("");
  const [needsAuth, setNeedsAuth] = useState(false);

  if (!item) return null;

  async function handleConfirm() {
    if (!item) return;
    try {
      await onConfirm(
        item.productInternalCode,
        "INTERNAL_CODE",
        operatorId,
        authorizerId.trim() || undefined,
      );
      // success — parent will handle closure
    } catch (err: unknown) {
      // If 403, ask for authorizer
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
          <DialogTitle>Remover item</DialogTitle>
          <DialogDescription>
            Removendo <strong>{item.productName}</strong> (
            {item.productInternalCode}) da venda.
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
              Seu nível de permissão não permite remover itens. Solicite o ID de
              um supervisor ou gerente.
            </p>
            <div className="space-y-1">
              <Label htmlFor="auth-id" className="text-xs">
                ID do autorizador (UUID)
              </Label>
              <Input
                id="auth-id"
                value={authorizerId}
                onChange={(e) => setAuthorizerId(e.target.value)}
                placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                className="font-mono text-xs"
              />
            </div>
          </div>
        )}

        {error && !needsAuth && (
          <ErrorAlert error={error} title="Erro ao remover item" />
        )}

        <DialogFooter>
          <Button variant="outline" onClick={handleClose} disabled={isLoading}>
            Cancelar
          </Button>
          <Button
            variant="destructive"
            onClick={handleConfirm}
            disabled={isLoading || (needsAuth && !authorizerId.trim())}
          >
            {isLoading ? <LoadingSpinner size="sm" label="" /> : "Remover"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
