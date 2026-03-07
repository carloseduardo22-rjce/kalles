"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  Store,
  Lock,
  RefreshCw,
  XCircle,
  User,
  Package,
  Gift,
} from "lucide-react";
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
import { ProductLookupDialog } from "@/features/sales/components/product-lookup-dialog";
import { CloseSessionAuthorizedDialog } from "@/features/cash-register/components/close-session-authorized-dialog";
import { FidelityPdvPanel } from "@/features/sales/components/fidelity-pdv-panel";
import { ErrorAlert } from "@/shared/components/error-alert";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { useSale } from "@/features/sales/hooks/use-sale";
import { useSession } from "@/features/cash-register/hooks/use-session";
import { cashRegisterService } from "@/features/cash-register/services/cash-register.service";
import { companySettingsService } from "@/shared/services/company-settings.service";
import type { ActiveSession } from "@/features/cash-register/types";
import type { ProductCodeType, SaleItemResponse } from "@/features/sales/types";
import { formatCurrency } from "@/shared/utils/formatters";

const STORAGE_KEY = "kalles:active-session";

export default function PdvPage() {
  const router = useRouter();
  const [sessionData, setSessionData] = useState<ActiveSession | null>(null);
  const [hydrated, setHydrated] = useState(false);
  const [logoUrl, setLogoUrl] = useState<string | null>(null);

  const [removalItem, setRemovalItem] = useState<SaleItemResponse | null>(null);
  const [cancelOpen, setCancelOpen] = useState(false);
  const [discountItem, setDiscountItem] = useState<SaleItemResponse | null>(
    null,
  );
  const [lookupOpen, setLookupOpen] = useState(false);

  const {
    closeSession,
    isLoading: sessionLoading,
    error: sessionError,
  } = useSession();

  useEffect(() => {
    async function initSession() {
      let stored: ActiveSession | null = null;
      try {
        const raw = localStorage.getItem(STORAGE_KEY);
        stored = raw ? (JSON.parse(raw) as ActiveSession) : null;
      } catch {
        // ignore parse error
      }

      // Validate against backend: find any open session
      try {
        const [registers, operators] = await Promise.all([
          cashRegisterService.listCashRegisters(),
          cashRegisterService.listOperators(),
        ]);
        const openRegister = registers.find(
          (r) => r.hasActiveSession && r.activeSessionId != null,
        );
        if (openRegister && openRegister.activeSessionId) {
          // Only sync if stored session is missing or stale (different ID)
          if (!stored || stored.sessionId !== openRegister.activeSessionId) {
            const operator = operators.find(
              (o) => o.name === openRegister.activeOperatorName,
            );
            const synced: ActiveSession = {
              sessionId: openRegister.activeSessionId,
              operatorId: operator?.id ?? "",
              cashRegisterCode: openRegister.code,
              operatorName: openRegister.activeOperatorName ?? "",
              initialAmount: openRegister.initialAmount ?? 0,
              openedAt: openRegister.openedAt ?? new Date().toISOString(),
            };
            localStorage.setItem(STORAGE_KEY, JSON.stringify(synced));
            stored = synced;
          }
        }
      } catch {
        // Backend unavailable — fall back to whatever is in localStorage
      }

      if (!stored) {
        router.replace("/open-session");
        return;
      }
      setSessionData(stored);
      setLogoUrl(companySettingsService.getLogo());
      setHydrated(true);
    }

    initSession();
  }, [router]);

  const sessionToken = sessionData?.sessionId ?? "";
  const {
    sale,
    isLoading: saleLoading,
    error: saleError,
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
    clearError: clearSaleError,
    resetSale,
  } = useSale(sessionToken);

  useEffect(() => {
    if (hydrated && sessionToken && sale === null && !saleError) {
      createSale();
    }
  }, [hydrated, sessionToken, sale, saleError, createSale]);

  useEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === "F2") {
        e.preventDefault();
        setLookupOpen(true);
      } else if (e.key === "F4") {
        e.preventDefault();
        router.push("/produtos");
      }
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [router]);

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
          {logoUrl ? (
            <img
              src={logoUrl}
              alt="Logo"
              className="h-8 max-w-16 object-contain"
            />
          ) : (
            <Store className="h-5 w-5 text-primary" />
          )}
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
          {sale && <SaleStateBadge state={sale.state} />}{" "}
          <Button
            variant="ghost"
            size="sm"
            className="gap-1.5 text-xs text-muted-foreground"
            onClick={() => router.push("/produtos")}
            title="Consulta de Produtos (F4)"
          >
            <Package className="h-3.5 w-3.5" />
            <span className="hidden sm:inline">Produtos</span>
            <kbd className="hidden rounded border bg-muted px-1 py-0.5 font-mono text-[10px] sm:inline">
              F4
            </kbd>
          </Button>
          <CloseSessionAuthorizedDialog
            isLoading={sessionLoading}
            error={sessionError}
            onConfirm={handleCloseSession}
          />
        </div>
      </header>

      {/* Main grid */}
      <div className="flex flex-1 overflow-hidden">
        {/* Left — cart */}
        <section className="flex w-full flex-col overflow-hidden md:w-3/5">
          {/* Scrollable top area */}
          <div className="flex flex-1 flex-col gap-3 overflow-y-auto p-4">
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

            {/* Items list */}
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
              onIncrementItem={(code) => addItem("INTERNAL_CODE", code)}
              onDecrementItem={(code) => decrementItem(code)}
            />
          </div>

          {/* Sticky totals footer */}
          {sale && !isSaleComplete && (
            <div className="shrink-0 space-y-2 border-t bg-card px-4 py-3">
              <div className="flex justify-between text-sm text-muted-foreground">
                <span>Subtotal</span>
                <span className="font-medium">
                  {formatCurrency(sale.subtotal)}
                </span>
              </div>
              {sale.fidelityDiscountApplied > 0 && (
                <div className="flex justify-between text-sm text-green-600">
                  <span className="flex items-center gap-1">
                    <Gift className="h-3.5 w-3.5" />
                    Desconto fidelidade
                  </span>
                  <span>- {formatCurrency(sale.fidelityDiscountApplied)}</span>
                </div>
              )}
              <div className="flex justify-between text-base font-bold">
                <span>Total</span>
                <span>{formatCurrency(sale.total)}</span>
              </div>
              {/* Cancel sale button */}
              {sale.state === "OPEN" && sale.items.length > 0 && (
                <Button
                  variant="ghost"
                  size="sm"
                  className="mt-1 w-full text-destructive hover:text-destructive"
                  onClick={() => setCancelOpen(true)}
                >
                  <XCircle className="mr-2 h-4 w-4" />
                  Cancelar venda
                </Button>
              )}
            </div>
          )}
        </section>

        <Separator orientation="vertical" />

        {/* Right — fidelity + payment */}
        <section className="hidden w-2/5 flex-col overflow-hidden md:flex">
          {/* Fidelity panel */}
          {sale && sale.state === "OPEN" && (
            <div className="shrink-0 border-b px-4 pt-4 pb-3">
              <FidelityPdvPanel
                key={sale.id}
                sale={sale}
                onAssociateClient={associateClient}
                onApplyFidelityDiscount={applyFidelityDiscount}
                disabled={isLoading}
              />
            </div>
          )}

          {/* Payment panel */}
          <div className="flex flex-1 flex-col overflow-y-auto p-4">
            {sale &&
            !isSaleComplete &&
            (sale.state === "OPEN" ||
              sale.state === "PAYMENT_IN_PROGRESS" ||
              sale.state === "PAID") ? (
              <PaymentPanel
                sale={sale}
                isLoading={isLoading}
                error={saleError}
                onAddPayment={addPayment}
                onCompleteSale={completeSale}
              />
            ) : (
              <div className="flex h-full items-center justify-center text-sm text-muted-foreground">
                Adicione produtos para habilitar o pagamento.
              </div>
            )}
          </div>
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

      <ProductLookupDialog
        open={lookupOpen}
        onOpenChange={setLookupOpen}
        onSelect={(code) => addItem("INTERNAL_CODE", code)}
      />
    </div>
  );
}
