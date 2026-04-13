"use client";

import { useEffect, useMemo, useState } from "react";
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

interface ItemQuantityDialogProps {
  open: boolean;
  title?: string;
  description?: string;
  isLoading?: boolean;
  onClose: () => void;
  onConfirm: (quantity: number) => Promise<void> | void;
}

export function ItemQuantityDialog({
  open,
  title = "Quantidade do item",
  description = "Informe quantas unidades devem entrar nesta venda.",
  isLoading = false,
  onClose,
  onConfirm,
}: ItemQuantityDialogProps) {
  const [quantity, setQuantity] = useState("1");

  useEffect(() => {
    if (open) {
      setQuantity("1");
    }
  }, [open]);

  const parsedQuantity = useMemo(() => {
    const value = Number(quantity);
    return Number.isInteger(value) && value > 0 ? value : null;
  }, [quantity]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!parsedQuantity) return;
    await onConfirm(parsedQuantity);
  }

  return (
    <Dialog open={open} onOpenChange={(nextOpen) => !nextOpen && onClose()}>
      <DialogContent className="sm:max-w-sm">
        <form onSubmit={handleSubmit} className="space-y-4">
          <DialogHeader>
            <DialogTitle>{title}</DialogTitle>
            <DialogDescription>{description}</DialogDescription>
          </DialogHeader>

          <div className="space-y-2">
            <Input
              type="number"
              min={1}
              step={1}
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              autoFocus
            />
            {!parsedQuantity && (
              <p className="text-xs text-destructive">
                Informe uma quantidade inteira maior que zero.
              </p>
            )}
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={isLoading}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={isLoading || !parsedQuantity}>
              Confirmar
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
