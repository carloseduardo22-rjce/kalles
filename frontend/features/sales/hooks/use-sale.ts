"use client";

import { useCallback, useState } from "react";
import type { PaymentMethod, ProductCodeType, SaleResponse } from "../types";
import { saleService } from "../services/sale.service";
import { ApiError } from "@/shared/services/api";

interface UseSaleReturn {
  sale: SaleResponse | null;
  isLoading: boolean;
  error: string | null;
  createSale: () => Promise<void>;
  addItem: (
    type: ProductCodeType,
    code: string,
    quantity?: number,
  ) => Promise<void>;
  decrementItem: (internalCode: string) => Promise<void>;
  removeItem: (
    productCode: string,
    type: ProductCodeType,
    operatorId: string,
    authorizerId?: string,
  ) => Promise<void>;
  cancelSale: (operatorId: string, authorizerId?: string) => Promise<void>;
  addPayment: (method: PaymentMethod, amount: number) => Promise<void>;
  completeSale: () => Promise<void>;
  applyDiscount: (itemId: string, discountAmount: number) => Promise<void>;
  associateClient: (clientId: string) => Promise<void>;
  applyFidelityDiscount: () => Promise<void>;
  refreshSale: () => Promise<void>;
  clearError: () => void;
  resetSale: () => void;
}

export function useSale(sessionToken: string): UseSaleReturn {
  const [sale, setSale] = useState<SaleResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function handleError(err: unknown, fallback: string): never {
    const message = err instanceof ApiError ? err.message : fallback;
    setError(message);
    throw err;
  }

  async function withLoading<T>(fn: () => Promise<T>): Promise<T> {
    setIsLoading(true);
    setError(null);
    try {
      return await fn();
    } finally {
      setIsLoading(false);
    }
  }

  const createSale = useCallback(async () => {
    await withLoading(async () => {
      try {
        const created = await saleService.createSale(sessionToken);
        setSale(created);
      } catch (err) {
        handleError(err, "Falha ao criar venda.");
      }
    });
  }, [sessionToken]);

  const refreshSale = useCallback(async () => {
    try {
      const currentSale = await saleService.getSale(sessionToken);
      setSale(currentSale);
    } catch (err) {
      // Don't show critical errors to user if just polling fails
      console.error("Falha ao atualizar venda:", err);
    }
  }, [sessionToken]);

  const addItem = useCallback(
    async (type: ProductCodeType, code: string, quantity = 1) => {
      await withLoading(async () => {
        try {
          const updated = await saleService.addItem(
            sessionToken,
            type,
            code,
            quantity,
          );
          setSale(updated);
        } catch (err) {
          handleError(err, "Falha ao adicionar produto.");
        }
      });
    },
    [sessionToken],
  );

  const removeItem = useCallback(
    async (
      productCode: string,
      type: ProductCodeType,
      operatorId: string,
      authorizerId?: string,
    ) => {
      await withLoading(async () => {
        try {
          await saleService.removeItem(
            sessionToken,
            productCode,
            type,
            operatorId,
            authorizerId,
          );
          setSale((prev) => {
            if (!prev) return prev;
            return {
              ...prev,
              items: prev.items.filter(
                (i) => i.productInternalCode !== productCode,
              ),
            };
          });
        } catch (err) {
          handleError(err, "Falha ao remover item.");
        }
      });
    },
    [sessionToken],
  );

  const cancelSale = useCallback(
    async (operatorId: string, authorizerId?: string) => {
      await withLoading(async () => {
        try {
          await saleService.cancelSale(sessionToken, operatorId, authorizerId);
          setSale((prev) => (prev ? { ...prev, state: "CANCELED" } : prev));
        } catch (err) {
          handleError(err, "Falha ao cancelar venda.");
        }
      });
    },
    [sessionToken],
  );

  const addPayment = useCallback(
    async (method: PaymentMethod, amount: number) => {
      await withLoading(async () => {
        try {
          const updated = await saleService.addPayment(
            sessionToken,
            method,
            amount,
          );
          setSale(updated);
        } catch (err) {
          handleError(err, "Falha ao processar pagamento.");
        }
      });
    },
    [sessionToken],
  );

  const completeSale = useCallback(async () => {
    await withLoading(async () => {
      try {
        await saleService.completeSale(sessionToken);
        setSale((prev) => (prev ? { ...prev, state: "COMPLETED" } : prev));
      } catch (err) {
        handleError(err, "Falha ao concluir venda.");
      }
    });
  }, [sessionToken]);

  const applyDiscount = useCallback(
    async (itemId: string, discountAmount: number) => {
      await withLoading(async () => {
        try {
          await saleService.applyDiscount(sessionToken, itemId, discountAmount);
          setSale((prev) => {
            if (!prev) return prev;
            const items = prev.items.map((item) => {
              if (item.id !== itemId) return item;
              const subtotal = item.unitPrice * item.quantity - discountAmount;
              return { ...item, discount: discountAmount, subtotal };
            });
            const subtotal = items.reduce((sum, i) => sum + i.subtotal, 0);
            return { ...prev, items, subtotal, total: subtotal };
          });
        } catch (err) {
          handleError(err, "Falha ao aplicar desconto.");
        }
      });
    },
    [sessionToken],
  );

  const decrementItem = useCallback(
    async (internalCode: string) => {
      await withLoading(async () => {
        try {
          const updated = await saleService.decrementItem(
            sessionToken,
            internalCode,
          );
          setSale(updated);
        } catch (err) {
          handleError(err, "Falha ao decrementar quantidade.");
        }
      });
    },
    [sessionToken],
  );

  const associateClient = useCallback(
    async (clientId: string) => {
      await withLoading(async () => {
        try {
          const updated = await saleService.associateClient(
            sessionToken,
            clientId,
          );
          setSale(updated);
        } catch (err) {
          handleError(err, "Falha ao associar cliente.");
        }
      });
    },
    [sessionToken],
  );

  const applyFidelityDiscount = useCallback(async () => {
    await withLoading(async () => {
      try {
        const updated = await saleService.applyFidelityDiscount(sessionToken);
        setSale(updated);
      } catch (err) {
        handleError(err, "Falha ao aplicar desconto de fidelidade.");
      }
    });
  }, [sessionToken]);

  const clearError = useCallback(() => setError(null), []);

  const resetSale = useCallback(() => {
    setSale(null);
    setError(null);
  }, []);

  return {
    sale,
    isLoading,
    error,
    createSale,
    addItem,
    decrementItem,
    removeItem,
    cancelSale,
    addPayment,
    completeSale,
    applyDiscount,
    associateClient,
    applyFidelityDiscount,
    refreshSale,
    clearError,
    resetSale,
  };
}
