"use client";

import { Suspense, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import {
  LayoutDashboard,
  CreditCard,
  ShoppingBag,
  History,
  AlertCircle,
  CheckCircle2,
  XCircle,
  Banknote,
  Loader2,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { cashRegisterService } from "@/features/cash-register/services/cash-register.service";
import { formatCurrency, formatPaymentMethod } from "@/shared/utils/formatters";
import type { ActiveSession } from "@/features/cash-register/types";

const STORAGE_KEY = "kalles:active-session";

const PAYMENT_COLORS: Record<string, string> = {
  CASH: "bg-emerald-500",
  PIX: "bg-blue-500",
  CREDIT_CARD: "bg-violet-500",
  DEBIT_CARD: "bg-amber-500",
};

/* ─── Stat card ─── */
function StatCard({
  label,
  value,
  icon,
  accent,
}: {
  label: string;
  value: React.ReactNode;
  icon: React.ReactNode;
  accent?: "primary" | "destructive" | "muted";
}) {
  const accentClass =
    accent === "primary"
      ? "text-primary"
      : accent === "destructive"
        ? "text-destructive"
        : "text-muted-foreground";
  return (
    <Card>
      <CardContent className="flex items-center gap-4 p-5">
        <div className={`${accentClass} shrink-0`}>{icon}</div>
        <div className="min-w-0">
          <p className="text-xs text-muted-foreground">{label}</p>
          <p className={`truncate text-2xl font-bold ${accentClass}`}>
            {value}
          </p>
        </div>
      </CardContent>
    </Card>
  );
}

/* ─── Coming soon placeholder ─── */
function ComingSoon({
  icon,
  title,
  description,
}: {
  icon: React.ReactNode;
  title: string;
  description: string;
}) {
  return (
    <div className="flex flex-col items-center justify-center py-24 text-center">
      <div className="mb-4 text-muted-foreground/30">{icon}</div>
      <p className="text-lg font-semibold text-muted-foreground">{title}</p>
      <p className="mt-1 max-w-sm text-sm text-muted-foreground/70">
        {description}
      </p>
      <Badge variant="outline" className="mt-4">
        Em breve
      </Badge>
    </div>
  );
}

/* ─── Main content (inner — needs useSearchParams) ─── */
function RelatoriosContent() {
  const searchParams = useSearchParams();
  const tab = searchParams.get("tab") ?? "resumo";

  const [session, setSession] = useState<ActiveSession | null>(null);

  useEffect(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      try {
        setSession(JSON.parse(raw) as ActiveSession);
      } catch {
        /* ignore */
      }
    }
  }, []);

  const { data: summary, isLoading } = useQuery({
    queryKey: ["session-report", session?.sessionId],
    queryFn: () => cashRegisterService.getReport(session!.sessionId),
    enabled: !!session?.sessionId,
    staleTime: 30_000,
  });

  /* ── Tab: Resumo do Turno ── */
  if (tab === "resumo") {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-xl font-semibold">Resumo do Turno</h2>
          <p className="mt-0.5 text-sm text-muted-foreground">
            Desempenho da sessão de caixa atual.
          </p>
        </div>

        {!session ? (
          <div className="flex items-center gap-2 rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-700">
            <AlertCircle className="h-4 w-4 shrink-0" />
            Nenhuma sessão de caixa ativa encontrada. Abra uma sessão no PDV
            para visualizar o relatório.
          </div>
        ) : isLoading ? (
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Loader2 className="h-4 w-4 animate-spin" />
            Carregando dados…
          </div>
        ) : summary ? (
          <>
            {/* KPI cards */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <StatCard
                label="Total vendido"
                value={formatCurrency(summary.totalVendido)}
                icon={<Banknote className="h-8 w-8" />}
                accent="primary"
              />
              <StatCard
                label="Vendas concluídas"
                value={summary.vendasConcluidas}
                icon={<CheckCircle2 className="h-8 w-8" />}
                accent="primary"
              />
              <StatCard
                label="Vendas canceladas"
                value={summary.vendasCanceladas}
                icon={<XCircle className="h-8 w-8" />}
                accent="destructive"
              />
            </div>

            {/* Payment methods breakdown */}
            {Object.keys(summary.totalPorMetodoPagamento).length > 0 && (
              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm font-medium text-muted-foreground">
                    Por método de pagamento
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  {Object.entries(summary.totalPorMetodoPagamento).map(
                    ([method, amount]) => {
                      const pct =
                        summary.totalVendido > 0
                          ? (amount / summary.totalVendido) * 100
                          : 0;
                      return (
                        <div key={method} className="space-y-1">
                          <div className="flex items-center justify-between text-sm">
                            <span className="font-medium">
                              {formatPaymentMethod(method)}
                            </span>
                            <span className="tabular-nums text-muted-foreground">
                              {formatCurrency(amount)}{" "}
                              <span className="text-xs">
                                ({pct.toFixed(1)}%)
                              </span>
                            </span>
                          </div>
                          <div className="h-2 overflow-hidden rounded-full bg-muted">
                            <div
                              className={`h-full rounded-full transition-all ${PAYMENT_COLORS[method] ?? "bg-primary"}`}
                              style={{ width: `${pct}%` }}
                            />
                          </div>
                        </div>
                      );
                    },
                  )}
                </CardContent>
              </Card>
            )}

            <div className="text-xs text-muted-foreground">
              Sessão: <span className="font-mono">{session.sessionId}</span> —{" "}
              {session.cashRegisterCode} — {session.operatorName}
            </div>
          </>
        ) : null}
      </div>
    );
  }

  /* ── Tab: Meios de Pagamento ── */
  if (tab === "pagamentos") {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-xl font-semibold">Meios de Pagamento</h2>
          <p className="mt-0.5 text-sm text-muted-foreground">
            Distribuição dos pagamentos recebidos na sessão atual.
          </p>
        </div>

        {!session ? (
          <div className="flex items-center gap-2 rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-700">
            <AlertCircle className="h-4 w-4 shrink-0" />
            Nenhuma sessão ativa. Abra uma sessão no PDV.
          </div>
        ) : isLoading ? (
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Loader2 className="h-4 w-4 animate-spin" />
            Carregando…
          </div>
        ) : summary &&
          Object.keys(summary.totalPorMetodoPagamento).length > 0 ? (
          <div className="grid gap-4 sm:grid-cols-2">
            {Object.entries(summary.totalPorMetodoPagamento).map(
              ([method, amount]) => {
                const pct =
                  summary.totalVendido > 0
                    ? (amount / summary.totalVendido) * 100
                    : 0;
                const bar = PAYMENT_COLORS[method] ?? "bg-primary";
                return (
                  <Card key={method}>
                    <CardContent className="p-5">
                      <div className="flex items-start justify-between">
                        <div>
                          <p className="text-sm font-medium">
                            {formatPaymentMethod(method)}
                          </p>
                          <p className="mt-1 text-2xl font-bold">
                            {formatCurrency(amount)}
                          </p>
                        </div>
                        <Badge variant="secondary">{pct.toFixed(1)}%</Badge>
                      </div>
                      <div className="mt-4 h-2 overflow-hidden rounded-full bg-muted">
                        <div
                          className={`h-full rounded-full ${bar}`}
                          style={{ width: `${pct}%` }}
                        />
                      </div>
                    </CardContent>
                  </Card>
                );
              },
            )}
          </div>
        ) : (
          <p className="text-sm text-muted-foreground">
            Nenhum pagamento registrado nesta sessão.
          </p>
        )}
      </div>
    );
  }

  /* ── Tab: Por Produto ── */
  if (tab === "por-produto") {
    return (
      <ComingSoon
        icon={<ShoppingBag className="h-16 w-16" />}
        title="Relatório por Produto"
        description="Exibirá os produtos mais vendidos, quantidade total vendida por item e receita gerada por cada produto na sessão."
      />
    );
  }

  /* ── Tab: Histórico ── */
  if (tab === "historico") {
    return (
      <ComingSoon
        icon={<History className="h-16 w-16" />}
        title="Histórico de Sessões"
        description="Exibirá todas as sessões de caixa anteriores com totais, operadores e comparativos de período."
      />
    );
  }

  return null;
}

/* ─── Page ─── */
export default function RelatoriosPage() {
  return (
    <div className="flex h-full flex-col">
      {/* Page header */}
      <div className="border-b bg-card px-6 py-4">
        <h1 className="text-base font-semibold">Relatórios</h1>
        <p className="text-xs text-muted-foreground">
          Acompanhe o desempenho do seu caixa em tempo real.
        </p>
      </div>

      {/* Content */}
      <div className="flex-1 overflow-auto p-6">
        <Suspense
          fallback={
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Loader2 className="h-4 w-4 animate-spin" />
              Carregando…
            </div>
          }
        >
          <RelatoriosContent />
        </Suspense>
      </div>
    </div>
  );
}
