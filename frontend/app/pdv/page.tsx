"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Store, Lock, RefreshCw, XCircle, User } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ProductSearch } from "@/features/sales/components/product-search";
import { SaleItemsList } from "@/features/sales/components/sale-items-list";
import { PaymentPanel } from "@/features/sales/components/payment-panel";
import { ItemRemovalDialog } from "@/features/sales/components/item-removal-dialog";
import { CancellationDialog } from "@/features/sales/components/cancellation-dialog";
import { DiscountDialog } from "@/features/sales/components/discount-dialog";
import { SaleStateBadge } from "@/features/sales/components/sale-state-badge";
import { CloseSessionDialog } from "@/features/cash-register/components/close-session-dialog";
import { ErrorAlert } from "@/shared/components/error-alert";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { useSale } from "@/features/sales/hooks/use-sale";
import { useSession } from "@/features/cash-register/hooks/use-session";
import type { ActiveSession } from "@/features/cash-register/types";
import type { ProductCodeType, SaleItemResponse } from "@/features/sales/types";
import { formatCurrency } from "@/shared/utils/formatters";

const STORAGE_KEY = "kalles:active-session";

export default function PdvPage() {
  const router = useRouter();
  const [sessionData, setSessionData] = useState<ActiveSession | null>(null);
  const [hydrated, setHydrated] = useState(false);

  const [removalItem, setRemovalItem] = useState<SaleItemResponse | null>(null);
  const [cancelOpen, setCancelOpen] = useState(false);
  const [discountItem, setDiscountItem] = useState<SaleItemResponse | null>(
    null,
  );

  const {
    closeSession,
    isLoading: sessionLoading,
    error: sessionError,
  } = useSession();

  useEffect(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      router.replace("/open-session");
      return;
    }
    try {
      setSessionData(JSON.parse(raw) as ActiveSession);
    } catch {
      router.replace("/open-session");
    }
    setHydrated(true);
  }, [router]);

  const sessionToken = sessionData?.sessionId ?? "";
  const {
    sale,
    isLoading: saleLoading,
    error: saleError,
    createSale,
    addItem,
    removeItem,
    cancelSale,
    addPayment,
    completeSale,
    applyDiscount,
    clearError: clearSaleError,
    resetSale,
  } = useSale(sessionToken);

  useEffect(() => {
    if (hydrated && sessionToken && sale === null && !saleError) {
      createSale();
    }
  }, [hydrated, sessionToken, sale, saleError, createSale]);

  if (!hydrated || !sessionData) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <LoadingSpinner size="lg" label="Carregando sessão…" />
      </div>
    );
  }

  async function handleCloseSession() {
    const result = await closeSession();
    if (result) {
      sessionStorage.setItem(
        `kalles:report:${result.sessionId}`,
        JSON.stringify(result),
      );
      router.push(`/pdv/report/${result.sessionId}`);
    }
  }

  function handleNewSale() {
    clearSaleError();
    resetSale();
  }

  const isLoading = saleLoading || sessionLoading;
  const isSaleComplete =
    sale?.state === "COMPLETED" || sale?.state === "CANCELED";

  return (
    <div className="flex h-screen flex-col bg-background">
      {/* Header */}
      <header className="flex items-center justify-between border-b bg-card px-4 py-2.5 shadow-sm">
        <div className="flex items-center gap-3">
          <Store className="h-5 w-5 text-primary" />
          <div>
            <p className="text-sm font-semibold leading-none">
              {sessionData.cashRegisterCode}
            </p>
            <p className="text-xs text-muted-foreground flex items-center gap-1 mt-0.5">
              <User className="h-3 w-3" />
              {sessionData.operatorName}
            </p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          {sale && <SaleStateBadge state={sale.state} />}
          <CloseSessionDialog
            isLoading={sessionLoading}
            error={sessionError}
            onConfirm={handleCloseSession}
          />
        </div>
      </header>

      {/* Main grid */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left — cart */}
        <section className="flex w-full flex-col gap-3 overflow-auto p-4 md:w-3/5">
          {/* Add item */}
          {(!sale || sale.state === "OPEN") && (
            <ProductSearch isLoading={isLoading} onAddItem={addItem} />
          )}

          {saleError && (
            <ErrorAlert error={saleError} title="Erro" className="text-sm" />
          )}

          {/* Sale complete banner */}
          {isSaleComplete && (
            <div className="rounded-lg border-2 border-primary/30 bg-primary/5 p-4 text-center">
              <p className="font-semibold">
                {sale!.state === "COMPLETED"
                  ? "✅ Venda concluída com sucesso!"
                  : "❌ Venda cancelada."}
              </p>
              <Button
                variant="outline"
                size="sm"
                className="mt-3"
                onClick={handleNewSale}
              >
                <RefreshCw className="mr-2 h-4 w-4" />
                Nova venda
              </Button>
            </div>
          )}

          {/* Items or empty */}
          <div className="flex-1">
            <SaleItemsList
              items={sale?.items ?? []}
              saleState={sale?.state ?? "OPEN"}
              onRemoveItem={(code, type) => {
                const item = sale?.items.find(
                  (i) => i.productInternalCode === code,
                );
                if (item) setRemovalItem(item);
              }}
              onApplyDiscount={(item) => setDiscountItem(item)}
            />
          </div>

          {/* Totals footer */}
          {sale && !isSaleComplete && (
            <div className="space-y-1 border-t pt-3 text-sm">
              <div className="flex justify-between text-muted-foreground">
                <span>Subtotal</span>
                <span>{formatCurrency(sale.subtotal)}</span>
              </div>
              <div className="flex justify-between font-bold">
                <span>Total</span>
                <span>{formatCurrency(sale.total)}</span>
              </div>
            </div>
          )}

          {/* Cancel sale button */}
          {sale && sale.state === "OPEN" && sale.items.length > 0 && (
            <Button
              variant="ghost"
              size="sm"
              className="w-full text-destructive hover:text-destructive"
              onClick={() => setCancelOpen(true)}
            >
              <XCircle className="mr-2 h-4 w-4" />
              Cancelar venda
            </Button>
          )}
        </section>

        <Separator orientation="vertical" />

        {/* Right — payment */}
        <section className="hidden w-2/5 overflow-auto p-4 md:flex md:flex-col">
          {sale && !isSaleComplete ? (
            sale.state === "OPEN" && sale.total > 0 ? (
              <PaymentPanel
                sale={sale}
                isLoading={isLoading}
                error={saleError}
                onAddPayment={addPayment}
                onCompleteSale={completeSale}
              />
            ) : sale.state === "PAYMENT_IN_PROGRESS" ||
              sale.state === "PAID" ? (
              <PaymentPanel
                sale={sale}
                isLoading={isLoading}
                error={saleError}
                onAddPayment={addPayment}
                onCompleteSale={completeSale}
              />
            ) : null
          ) : (
            <div className="flex h-full items-center justify-center text-muted-foreground text-sm">
              Adicione produtos para habilitar o pagamento.
            </div>
          )}
        </section>
      </div>

      {/* Dialogs */}
      <ItemRemovalDialog
        open={!!removalItem}
        item={removalItem}
        operatorId={sessionData.operatorId}
        isLoading={isLoading}
        error={saleError}
        onClose={() => setRemovalItem(null)}
        onConfirm={async (code, type, opId, authId) => {
          await removeItem(code, type, opId, authId);
          setRemovalItem(null);
        }}
      />

      <CancellationDialog
        open={cancelOpen}
        operatorId={sessionData.operatorId}
        isLoading={isLoading}
        error={saleError}
        onClose={() => setCancelOpen(false)}
        onConfirm={async (opId, authId) => {
          await cancelSale(opId, authId);
          setCancelOpen(false);
        }}
      />

      <DiscountDialog
        open={!!discountItem}
        item={discountItem}
        isLoading={isLoading}
        error={saleError}
        onClose={() => setDiscountItem(null)}
        onConfirm={async (itemId, discount) => {
          await applyDiscount(itemId, discount);
          setDiscountItem(null);
        }}
      />
    </div>
  );
}
