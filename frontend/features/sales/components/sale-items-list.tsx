"use client";

import { Trash2, Tag } from "lucide-react";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/shared/components/empty-state";
import { formatCurrency } from "@/shared/utils/formatters";
import type { SaleItemResponse, ProductCodeType } from "../types";
import { ShoppingCart } from "lucide-react";

interface SaleItemsListProps {
  items: SaleItemResponse[];
  saleState: string;
  onRemoveItem: (productCode: string, type: ProductCodeType) => void;
  onApplyDiscount: (item: SaleItemResponse) => void;
}

export function SaleItemsList({
  items,
  saleState,
  onRemoveItem,
  onApplyDiscount,
}: SaleItemsListProps) {
  const isEditable = saleState === "OPEN";

  if (items.length === 0) {
    return (
      <EmptyState
        title="Nenhum item adicionado"
        description="Escaneie ou digite o código de um produto para começar."
        icon={<ShoppingCart className="h-10 w-10 opacity-30" />}
      />
    );
  }

  return (
    <div className="space-y-1">
      {items.map((item) => (
        <div
          key={item.id}
          className="flex items-center gap-3 rounded-md border bg-card p-2.5"
        >
          {/* Product info */}
          <div className="flex-1 min-w-0">
            <p className="truncate text-sm font-medium">{item.productName}</p>
            <p className="text-xs text-muted-foreground font-mono">
              {item.productInternalCode}
            </p>
          </div>

          {/* Qty + price */}
          <div className="text-right shrink-0">
            <p className="text-xs text-muted-foreground">
              {item.quantity} × {formatCurrency(item.unitPrice)}
            </p>
            {item.discount > 0 && (
              <p className="text-xs text-destructive">
                − {formatCurrency(item.discount)}
              </p>
            )}
            <p className="text-sm font-semibold">
              {formatCurrency(item.subtotal)}
            </p>
          </div>

          {/* Actions */}
          {isEditable && (
            <div className="flex gap-1 shrink-0">
              <Button
                variant="ghost"
                size="icon"
                className="h-7 w-7 text-muted-foreground hover:text-primary"
                title="Aplicar desconto"
                onClick={() => onApplyDiscount(item)}
              >
                <Tag className="h-3.5 w-3.5" />
              </Button>
              <Button
                variant="ghost"
                size="icon"
                className="h-7 w-7 text-muted-foreground hover:text-destructive"
                title="Remover item"
                onClick={() =>
                  onRemoveItem(item.productInternalCode, "INTERNAL_CODE")
                }
              >
                <Trash2 className="h-3.5 w-3.5" />
              </Button>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
