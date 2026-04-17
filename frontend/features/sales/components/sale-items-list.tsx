"use client";

import { Trash2, Tag, Plus, Minus } from "lucide-react";
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
  onIncrementItem: (internalCode: string) => void;
  onDecrementItem: (internalCode: string) => void;
}

export function SaleItemsList({
  items,
  saleState,
  onRemoveItem,
  onApplyDiscount,
  onIncrementItem,
  onDecrementItem,
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
          data-testid={`sale-item-${item.productInternalCode}`}
          className="rounded-md border bg-card px-3 py-2.5"
        >
          {/* Row 1: name – code – action icons */}
          <div className="flex items-center gap-2">
            <p className="min-w-0 flex-1 truncate text-base font-semibold">
              {item.productName}
            </p>
            <p className="shrink-0 font-mono text-xs text-muted-foreground">
              {item.productInternalCode}
            </p>
            {isEditable && (
              <div className="flex shrink-0 gap-0.5">
                <Button
                  data-testid={`sale-item-discount-${item.productInternalCode}`}
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7 text-muted-foreground hover:text-primary"
                  title="Aplicar desconto"
                  onClick={() => onApplyDiscount(item)}
                >
                  <Tag className="h-3.5 w-3.5" />
                </Button>
                <Button
                  data-testid={`sale-item-remove-${item.productInternalCode}`}
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

          {/* Row 2: price / discount / subtotal */}
          <div className="mt-0.5 flex items-baseline gap-2">
            <p className="text-sm text-muted-foreground">
              {item.quantity > 1
                ? `${item.quantity} × ${formatCurrency(item.unitPrice)}`
                : formatCurrency(item.unitPrice)}
            </p>
            {item.discount > 0 && (
              <p className="text-xs text-destructive">
                − {formatCurrency(item.discount)}
              </p>
            )}
            <p className="ml-auto text-sm font-semibold">
              {formatCurrency(item.subtotal)}
            </p>
          </div>

          {/* Row 3: qty controls */}
          <div className="mt-2 flex items-center justify-end">
            {isEditable ? (
              <div className="flex items-center gap-1">
                <Button
                  data-testid={`sale-item-decrement-${item.productInternalCode}`}
                  variant="outline"
                  size="icon"
                  className="h-6 w-6"
                  title="Diminuir quantidade"
                  disabled={item.quantity <= 1}
                  onClick={() => onDecrementItem(item.productInternalCode)}
                >
                  <Minus className="h-3 w-3" />
                </Button>
                <span className="w-6 text-center text-sm font-semibold tabular-nums">
                  {item.quantity}
                </span>
                <Button
                  data-testid={`sale-item-increment-${item.productInternalCode}`}
                  variant="outline"
                  size="icon"
                  className="h-6 w-6"
                  title="Aumentar quantidade"
                  onClick={() => onIncrementItem(item.productInternalCode)}
                >
                  <Plus className="h-3 w-3" />
                </Button>
              </div>
            ) : (
              <p className="text-xs text-muted-foreground">
                {item.quantity} un.
              </p>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}
