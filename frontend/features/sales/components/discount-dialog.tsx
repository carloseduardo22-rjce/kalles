"use client";

import { useState } from "react";
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
import { formatCurrency } from "@/shared/utils/formatters";
import type { SaleItemResponse } from "../types";

interface DiscountDialogProps {
  open: boolean;
  item: SaleItemResponse | null;
  isLoading: boolean;
  error: string | null;
  onClose: () => void;
  onConfirm: (itemId: string, discountAmount: number) => Promise<void>;
}

export function DiscountDialog({
  open,
  item,
  isLoading,
  error,
  onClose,
  onConfirm,
}: DiscountDialogProps) {
  const [discountStr, setDiscountStr] = useState("");

  if (!item) return null;

  const maxDiscount = item.unitPrice * item.quantity;

  async function handleConfirm() {
    const discount = parseFloat(discountStr.replace(",", "."));
    if (isNaN(discount) || discount < 0) return;
    await onConfirm(item!.id, discount);
    setDiscountStr("");
  }

  function handleClose() {
    setDiscountStr("");
    onClose();
  }

  return (
    <Dialog open={open} onOpenChange={() => handleClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Aplicar desconto</DialogTitle>
          <DialogDescription>
            Produto: <strong>{item.productName}</strong>
            <br />
            Valor total do item: {formatCurrency(maxDiscount)}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-2">
          <Label htmlFor="discount-amount" className="text-sm">
            Valor do desconto (R$)
          </Label>
          <Input
            data-testid="discount-amount-input"
            id="discount-amount"
            value={discountStr}
            onChange={(e) => setDiscountStr(e.target.value)}
            placeholder="0,00"
            inputMode="decimal"
          />
          <p className="text-xs text-muted-foreground">
            Desconto máximo permitido: {formatCurrency(maxDiscount)}
          </p>
        </div>

        {error && <ErrorAlert error={error} title="Erro ao aplicar desconto" />}

        <DialogFooter>
          <Button variant="outline" onClick={handleClose} disabled={isLoading}>
            Cancelar
          </Button>
          <Button
            data-testid="discount-confirm"
            onClick={handleConfirm}
            disabled={isLoading || !discountStr.trim()}
          >
            {isLoading ? <LoadingSpinner size="sm" label="" /> : "Aplicar"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
