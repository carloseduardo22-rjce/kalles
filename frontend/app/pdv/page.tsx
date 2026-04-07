"use client";

import { useEffect, useState, useRef, useCallback } from "react";
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
import type { ProductSearchHandle } from "@/features/sales/components/product-search";

const STORAGE_KEY = "kalles:active-session";
const LAYOUT_STORAGE_KEY = "kalles:pdv-layout";

type LayoutState = { leftWidth: number; centerWidth: number; bottomHeight: number };
const DEFAULT_LAYOUT: LayoutState = { leftWidth: 25, centerWidth: 45, bottomHeight: 25 };
const MIN_LEFT_WIDTH = 15;
const MAX_LEFT_WIDTH = 40;
const MIN_CENTER_WIDTH = 30;
const MAX_CENTER_WIDTH = 60;
const MIN_BOTTOM_HEIGHT = 15;
const MAX_BOTTOM_HEIGHT = 50;

const INTERACTIVE_FOCUS_SELECTOR = [
  "input",
  "textarea",
  "select",
  "button",
  "a[href]",
  "label",
  "[contenteditable='true']",
  "[role='button']",
  "[role='dialog']",
  "[role='menu']",
  "[role='listbox']",
  "[role='option']",
  "[role='tab']",
  "[role='switch']",
  "[role='checkbox']",
  "[role='radio']",
  "[data-state='open']",
  "[tabindex]:not([tabindex='-1'])",
].join(", ");

export default function PdvPage() {
  const router = useRouter();
  const productSearchRef = useRef<ProductSearchHandle | null>(null);

  // Layout Management State
  const containerRef = useRef<HTMLDivElement>(null);
  const [savedLayout, setSavedLayout] = useState<LayoutState>(DEFAULT_LAYOUT);
  const [isEditingLayout, setIsEditingLayout] = useState(false);

  // Load saved layout from local storage
  useEffect(() => {
    try {
      const storedLayout = localStorage.getItem(LAYOUT_STORAGE_KEY);
      if (storedLayout) {
        setSavedLayout(JSON.parse(storedLayout));
      }
    } catch (e) {}
  }, []);

  const handleMouseDownVertical = useCallback(
    (e: React.MouseEvent) => {
      e.preventDefault();
      if (!isEditingLayout || !containerRef.current) return;
      const container = containerRef.current;

      const onMouseMove = (moveEvent: MouseEvent) => {
        const containerRect = container.getBoundingClientRect();
        let newWidth =
          ((moveEvent.clientX - containerRect.left) / containerRect.width) *
          100;
        newWidth = Math.max(MIN_LEFT_WIDTH, Math.min(newWidth, MAX_LEFT_WIDTH));
        container.style.setProperty("--left-width", `${newWidth}%`);
      };

      const onMouseUp = () => {
        document.removeEventListener("mousemove", onMouseMove);
        document.removeEventListener("mouseup", onMouseUp);
      };

      document.addEventListener("mousemove", onMouseMove);
      document.addEventListener("mouseup", onMouseUp);
    },
    [isEditingLayout],
  );

  const handleSaveLayout = () => {
    if (containerRef.current) {
      const newLeft = parseFloat(containerRef.current.style.getPropertyValue("--left-width"));
      const newCenter = parseFloat(containerRef.current.style.getPropertyValue("--center-width"));
      const layoutToSave = {
        leftWidth: newLeft || savedLayout.leftWidth,
        centerWidth: newCenter || savedLayout.centerWidth,
        bottomHeight: savedLayout.bottomHeight,
      };
      setSavedLayout(layoutToSave);
      localStorage.setItem(LAYOUT_STORAGE_KEY, JSON.stringify(layoutToSave));
    }
    setIsEditingLayout(false);
  };

  const handleCancelLayout = () => {
    if (containerRef.current) {
      containerRef.current.style.setProperty("--left-width", `${savedLayout.leftWidth}%`);
      containerRef.current.style.setProperty("--center-width", `${savedLayout.centerWidth}%`);
    }
    setIsEditingLayout(false);
  };

  const handleRestoreLayout = () => {
    if (containerRef.current) {
      containerRef.current.style.setProperty("--left-width", `${DEFAULT_LAYOUT.leftWidth}%`);
      containerRef.current.style.setProperty("--center-width", `${DEFAULT_LAYOUT.centerWidth}%`);
    }
    setSavedLayout(DEFAULT_LAYOUT);
    localStorage.removeItem(LAYOUT_STORAGE_KEY);
    setIsEditingLayout(false);
  };

  const [sessionData, setSessionData] = useState<ActiveSession | null>(null);
  const [hydrated, setHydrated] = useState(false);
  const [logoUrl, setLogoUrl] = useState<string | null>(null);

  const [removalItem, setRemovalItem] = useState<SaleItemResponse | null>(null);
  const [cancelOpen, setCancelOpen] = useState(false);
  const [discountItem, setDiscountItem] = useState<SaleItemResponse | null>(
    null,
  );
  const [lookupOpen, setLookupOpen] = useState(false);
  const hasSecondaryOverlay =
    lookupOpen || !!removalItem || cancelOpen || !!discountItem;

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
        const openRegisters = registers.filter(
          (r) => r.hasActiveSession && r.activeSessionId != null,
        );
        const openRegister =
          openRegisters.find((r) => r.paymentIntegrationConfigured) ||
          openRegisters[0];
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
    refreshSale,
    clearError: clearSaleError,
    resetSale,
  } = useSale(sessionToken);

  useEffect(() => {
    if (hydrated && sessionToken && sale === null && !saleError) {
      createSale();
    }
  }, [hydrated, sessionToken, sale, saleError, createSale]);

  const focusProductSearch = useCallback(() => {
    if (isEditingLayout || hasSecondaryOverlay) return;
    productSearchRef.current?.focus();
  }, [hasSecondaryOverlay, isEditingLayout]);

  useEffect(() => {
    if (!hydrated || isEditingLayout || hasSecondaryOverlay) return;
    const frameId = window.requestAnimationFrame(() => {
      focusProductSearch();
    });
    return () => window.cancelAnimationFrame(frameId);
  }, [focusProductSearch, hasSecondaryOverlay, hydrated, isEditingLayout]);

  useEffect(() => {
    function handlePointerDown(event: PointerEvent) {
      if (isEditingLayout || hasSecondaryOverlay) return;
      const target = event.target;
      if (!(target instanceof Element)) return;
      if (target.closest(INTERACTIVE_FOCUS_SELECTOR)) return;

      window.requestAnimationFrame(() => {
        focusProductSearch();
      });
    }

    document.addEventListener("pointerdown", handlePointerDown, true);
    return () => {
      document.removeEventListener("pointerdown", handlePointerDown, true);
    };
  }, [focusProductSearch, hasSecondaryOverlay, isEditingLayout]);

  useEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === "F2") {
        e.preventDefault();
        setLookupOpen(true);
      } else if (e.key === "F4") {
        e.preventDefault();
        router.push("/produtos");
      } else if (e.key === "F7") {
        e.preventDefault();
        if (sale && sale.state === "OPEN" && sale.items.length > 0) {
          const last = sale.items[sale.items.length - 1];
          decrementItem(last.productInternalCode);
        }
      } else if (e.key === "F8") {
        e.preventDefault();
        if (sale && sale.state === "OPEN" && sale.items.length > 0) {
          const last = sale.items[sale.items.length - 1];
          addItem("INTERNAL_CODE", last.productInternalCode);
        }
      }
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [router, sale, addItem, decrementItem, completeSale]);

  if (!hydrated || !sessionData) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <LoadingSpinner size="lg" label="Carregando sessão…" />
      </div>
    );
  }

  async function handleCloseSessionWithPayload(payload: {
    authorizedOperatorCode: string;
    countedCashAmount: number;
  }) {
    const result = await closeSession(payload);
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
          {sale && <SaleStateBadge state={sale.state} />}
        </div>

        <div className="flex items-center gap-2">
          {!isEditingLayout ? (
            <>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setIsEditingLayout(true)}
                className="mr-2"
              >
                Editar Layout
              </Button>
              <CloseSessionAuthorizedDialog
                isLoading={sessionLoading}
                error={sessionError}
                initialAmount={sessionData.initialAmount}
                onConfirm={handleCloseSessionWithPayload}
              />
            </>
          ) : (
            <div className="flex items-center gap-1 mr-2">
              <Button variant="ghost" size="sm" onClick={handleRestoreLayout}>
                Restaurar
              </Button>
              <Button
                variant="destructive"
                size="sm"
                onClick={handleCancelLayout}
              >
                Cancelar
              </Button>
              <Button
                variant="default"
                size="sm"
                onClick={handleSaveLayout}
                className="bg-green-600 hover:bg-green-700 text-white"
              >
                Salvar
              </Button>
            </div>
          )}
        </div>
      </header>

      {/* Main grid */}
      <div
        ref={containerRef}
        className="flex-1 w-full relative overflow-hidden bg-background"
        style={
          {
            display: "grid",
            gridTemplateColumns: `var(--left-width, 25%) var(--center-width, 45%) minmax(0, 1fr)`,
            gridTemplateRows: `minmax(0, 1fr) auto`,
            gridTemplateAreas: `
            "marketing products payment"
            "marketing totals payment"
          `,
            "--left-width": `${savedLayout.leftWidth}%`,
            "--center-width": `${savedLayout.centerWidth}%`,
          } as React.CSSProperties
        }
      >
        {/* Camada de Overlay visual para indicar o Modo de Edição */}
        {isEditingLayout && (
          <div className="absolute inset-0 pointer-events-none ring-4 ring-blue-500 ring-inset z-50 transition-all bg-blue-50/5" />
        )}

        {/* SECTION: Marketing (1º Quadrante - Logo Kalles) */}
        <section
          className={`flex flex-col relative overflow-hidden ${isEditingLayout ? "border-2 border-dashed border-purple-400 opacity-90" : "border-r"}`}
          style={{ gridArea: "marketing" }}
        >
          <div className="flex-1 flex items-center justify-center p-4 bg-gradient-to-b from-slate-50 to-white dark:from-slate-950 dark:to-slate-900">
            <img 
              src="/kalles-logo-palavra.jpeg" 
              alt="Kalles" 
              className="max-w-full max-h-full object-contain drop-shadow-lg" 
            />
          </div>
        </section>

        {/* Resizer Vertical 1 (Marketing <-> Products) */}
        {isEditingLayout && (
          <div
            onMouseDown={handleMouseDownVertical}
            className="w-4 bg-purple-500 hover:bg-purple-600 cursor-col-resize flex flex-col items-center justify-center opacity-80 z-40"
            style={{ gridColumn: "2 / 2", gridRow: "1 / 3", marginLeft: "-6px" }}
          >
            <div className="w-1 h-8 bg-white rounded-full"></div>
          </div>
        )}

        {/* SECTION: Products */}
        <section
          className={`flex flex-col relative overflow-hidden bg-background ${isEditingLayout ? "border-2 border-dashed border-blue-400 opacity-90" : "border-b border-r"}`}
          style={{ gridArea: "products" }}
        >
          <div className="flex flex-1 flex-col gap-3 overflow-y-auto p-4">
            {(!sale || sale.state === "OPEN") && (
              <ProductSearch
                ref={productSearchRef}
                isLoading={isLoading}
                onAddItem={addItem}
              />
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
        </section>

        {/* SECTION: Totals */}
        <section
          className={`flex flex-col overflow-hidden bg-card ${isEditingLayout ? "border-2 border-dashed border-green-400 opacity-90" : "border-r"}`}
          style={{ gridArea: "totals" }}
        >
          {sale && !isSaleComplete ? (
            <div className="flex-1 flex flex-col justify-end space-y-2 p-5 w-full">
              <div className="flex justify-between text-base text-muted-foreground">
                <span>Subtotal</span>
                <span className="font-medium">
                  {formatCurrency(sale.subtotal)}
                </span>
              </div>
              {sale.fidelityDiscountApplied > 0 && (
                <div className="flex justify-between text-base text-green-600">
                  <span className="flex items-center gap-2">
                    <Gift className="h-4 w-4" />
                    Desconto fidelidade
                  </span>
                  <span>- {formatCurrency(sale.fidelityDiscountApplied)}</span>
                </div>
              )}
              <div className="flex justify-between text-2xl font-bold pt-2 border-t">
                <span>Total</span>
                <span>{formatCurrency(sale.total)}</span>
              </div>
              {/* Cancel sale button */}
              {sale.state === "OPEN" && sale.items.length > 0 && (
                <Button
                  variant="ghost"
                  size="sm"
                  className="mt-1 w-full text-base font-medium text-destructive hover:bg-destructive/10 hover:text-destructive"
                  onClick={() => setCancelOpen(true)}
                >
                  <XCircle className="mr-2 h-4 w-4" />
                  Cancelar venda
                </Button>
              )}
            </div>
          ) : (
            <div className="flex-1 bg-card/50"></div>
          )}
        </section>

        {/* Resizer Vertical 2 (Products <-> Payment) */}
        {isEditingLayout && (
          <div
            onMouseDown={handleMouseDownVertical}
            className="w-4 bg-blue-500 hover:bg-blue-600 cursor-col-resize flex flex-col items-center justify-center opacity-80 z-40"
            style={{
              gridColumn: "3 / 3",
              gridRow: "1 / 3",
              marginLeft: "-6px",
            }}
          >
            <div className="w-1 h-8 bg-white rounded-full"></div>
          </div>
        )}

        {/* SECTION: Payment */}
        <section
          className={`flex flex-col overflow-hidden bg-background relative ${isEditingLayout ? "border-2 border-dashed border-orange-400 opacity-90" : ""}`}
          style={{ gridArea: "payment" }}
        >
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
          <div className="flex flex-1 flex-col overflow-y-auto p-4 relative z-0">
            {sale &&
            !isSaleComplete &&
            (sale.state === "OPEN" ||
              sale.state === "PAYMENT_IN_PROGRESS" ||
              sale.state === "PAID") ? (
              <PaymentPanel
                sale={sale}
                isLoading={isLoading}
                error={saleError}
                cashRegisterCode={sessionData.cashRegisterCode}
                onAddPayment={addPayment}
                onCompleteSale={completeSale}
                onRefreshSale={refreshSale}
                onStartNewSale={handleNewSale}
              />
            ) : (
              <div className="flex h-full items-center justify-center text-sm text-muted-foreground">
                Adicione produtos para habilitar o pagamento.
              </div>
            )}
          </div>

          {/* Ferramentas de Automação - Bottom of 3º Quadrante */}
          {(!sale || sale.state === "OPEN") && (
            <div className="shrink-0 border-t bg-card px-4 py-3">
              <div className="grid grid-cols-3 gap-2">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  className="w-full text-xs h-9 font-semibold"
                  onClick={() => setLookupOpen(true)}
                >
                  <kbd className="rounded border bg-muted px-1 py-0.5 font-mono text-[10px] mr-1.5">F2</kbd>
                  Consulta
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  className="w-full text-xs h-9 font-semibold hover:bg-destructive/10 hover:text-destructive hover:border-destructive/30"
                  onClick={() => {
                    const items = sale?.items ?? [];
                    if (items.length > 0) {
                      const lastItem = items[items.length - 1];
                      decrementItem(lastItem.productInternalCode);
                    }
                  }}
                  disabled={!sale || sale.items.length === 0 || isLoading}
                >
                  <kbd className="rounded border bg-muted px-1 py-0.5 font-mono text-[10px] mr-1.5">F7</kbd>
                  - Decrementar
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  className="w-full text-xs h-9 font-semibold hover:bg-primary/10 hover:text-primary hover:border-primary/30"
                  onClick={() => {
                    const items = sale?.items ?? [];
                    if (items.length > 0) {
                      const lastItem = items[items.length - 1];
                      addItem("INTERNAL_CODE", lastItem.productInternalCode);
                    }
                  }}
                  disabled={!sale || sale.items.length === 0 || isLoading}
                >
                  <kbd className="rounded border bg-muted px-1 py-0.5 font-mono text-[10px] mr-1.5">F8</kbd>
                  + Incrementar
                </Button>
              </div>
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

      <ProductLookupDialog
        open={lookupOpen}
        onOpenChange={setLookupOpen}
        onSelect={(code) => addItem("INTERNAL_CODE", code)}
      />
    </div>
  );
}
