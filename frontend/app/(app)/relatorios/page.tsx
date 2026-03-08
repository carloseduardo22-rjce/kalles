"use client";

import { Suspense } from "react";
import { useSearchParams } from "next/navigation";
import { useQuery, useQueries } from "@tanstack/react-query";
import {
  ShoppingBag,
  History,
  AlertCircle,
  CheckCircle2,
  XCircle,
  Banknote,
  Loader2,
  Store,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import {
  ChartContainer,
  ChartTooltip,
  ChartLegend,
  ChartLegendContent,
} from "@/components/ui/chart";
import type { ChartConfig } from "@/components/ui/chart";
import {
  PieChart,
  Pie,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
} from "recharts";
import { cashRegisterService } from "@/features/cash-register/services/cash-register.service";
import {
  formatCurrency,
  formatDate,
  formatPaymentMethod,
} from "@/shared/utils/formatters";

/* Hex colors for payment methods – used in both charts and mini-bars */
const PAYMENT_HEX: Record<string, string> = {
  CASH: "#10b981", // emerald-500
  PIX: "#0ea5e9", // sky-500
  CREDIT_CARD: "#8b5cf6", // violet-500
  DEBIT_CARD: "#f97316", // orange-500
};

const PAYMENT_COLORS: Record<string, string> = {
  CASH: "bg-emerald-500",
  PIX: "bg-sky-500",
  CREDIT_CARD: "bg-violet-500",
  DEBIT_CARD: "bg-orange-500",
};

const paymentChartConfig = {
  value: { label: "Valor" },
  CASH: { label: "Dinheiro", color: PAYMENT_HEX.CASH },
  PIX: { label: "PIX", color: PAYMENT_HEX.PIX },
  CREDIT_CARD: { label: "Crédito", color: PAYMENT_HEX.CREDIT_CARD },
  DEBIT_CARD: { label: "Débito", color: PAYMENT_HEX.DEBIT_CARD },
} satisfies ChartConfig;

const sessionBarConfig = {
  totalVendido: { label: "Total Vendido", color: "#6366f1" },
} satisfies ChartConfig;

/* ─── Custom chart tooltips ─── */
function PaymentPieTooltip({
  active,
  payload,
}: {
  active?: boolean;
  payload?: Array<{ value: number; payload: { fill: string; method: string } }>;
}) {
  if (!active || !payload?.length) return null;
  const item = payload[0];
  const label =
    (
      paymentChartConfig[
        item.payload.method as keyof typeof paymentChartConfig
      ] as { label?: string }
    )?.label ?? item.payload.method;
  return (
    <div className="grid min-w-32 items-start gap-1.5 rounded-lg border border-border/50 bg-background px-2.5 py-1.5 text-xs shadow-xl">
      <div className="flex items-center gap-2">
        <div
          className="h-2 w-2 shrink-0 rounded-[2px]"
          style={{ backgroundColor: item.payload.fill }}
        />
        <span className="text-muted-foreground">{label}</span>
        <span className="ml-auto font-mono font-medium tabular-nums">
          {formatCurrency(item.value)}
        </span>
      </div>
    </div>
  );
}

function SessionBarTooltip({
  active,
  payload,
  label,
}: {
  active?: boolean;
  payload?: Array<{ dataKey: string; value: number; fill: string }>;
  label?: string;
}) {
  if (!active || !payload?.length) return null;
  return (
    <div className="grid min-w-32 items-start gap-1.5 rounded-lg border border-border/50 bg-background px-2.5 py-1.5 text-xs shadow-xl">
      <p className="font-medium">{label}</p>
      {payload.map((item) => (
        <div key={item.dataKey} className="flex items-center gap-2">
          <div
            className="h-2 w-2 shrink-0 rounded-[2px]"
            style={{ backgroundColor: item.fill }}
          />
          <span className="text-muted-foreground">
            {(
              sessionBarConfig[
                item.dataKey as keyof typeof sessionBarConfig
              ] as { label?: string }
            )?.label ?? item.dataKey}
          </span>
          <span className="ml-auto font-mono font-medium tabular-nums">
            {formatCurrency(item.value)}
          </span>
        </div>
      ))}
    </div>
  );
}

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

  /* Fetch all cash registers + find those with active sessions */
  const { data: cashRegisters = [], isLoading: loadingRegisters } = useQuery({
    queryKey: ["all-cash-registers"],
    queryFn: () => cashRegisterService.listCashRegisters(),
    staleTime: 30_000,
    refetchInterval: 60_000,
  });

  const activeSessions = cashRegisters.filter(
    (cr) => cr.hasActiveSession && cr.activeSessionId,
  );

  /* Fetch report for every active session in parallel */
  const reportQueries = useQueries({
    queries: activeSessions.map((cr) => ({
      queryKey: ["session-report", cr.activeSessionId],
      queryFn: () => cashRegisterService.getReport(cr.activeSessionId!),
      staleTime: 30_000,
    })),
  });

  const loadingReports = reportQueries.some((q) => q.isLoading);
  const isLoading = loadingRegisters || loadingReports;

  /* Pair each active cash register with its report */
  const sessionData = activeSessions.map((cr, i) => ({
    register: cr,
    report: reportQueries[i]?.data ?? null,
  }));

  /* Aggregate totals across all sessions */
  const aggregate = sessionData.reduce(
    (acc, { report }) => {
      if (!report) return acc;
      acc.vendasConcluidas += report.vendasConcluidas;
      acc.vendasCanceladas += report.vendasCanceladas;
      acc.totalVendido += report.totalVendido;
      for (const [method, amount] of Object.entries(
        report.totalPorMetodoPagamento,
      )) {
        acc.totalPorMetodo[method] = (acc.totalPorMetodo[method] ?? 0) + amount;
      }
      return acc;
    },
    {
      vendasConcluidas: 0,
      vendasCanceladas: 0,
      totalVendido: 0,
      totalPorMetodo: {} as Record<string, number>,
    },
  );

  /* Chart data */
  const paymentDonutData = Object.entries(aggregate.totalPorMetodo).map(
    ([method, amount]) => ({
      method,
      value: amount,
      fill: `var(--color-${method})`,
    }),
  );

  const sessionBarData = sessionData.map(({ register, report }) => ({
    caixa: register.code,
    totalVendido: report?.totalVendido ?? 0,
  }));

  /* ── Tab: Resumo do Turno ── */
  if (tab === "resumo") {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-xl font-semibold">Resumo do Turno</h2>
          <p className="mt-0.5 text-sm text-muted-foreground">
            Desempenho de todas as sessões de caixa abertas.
          </p>
        </div>

        {isLoading ? (
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Loader2 className="h-4 w-4 animate-spin" />
            Carregando dados…
          </div>
        ) : activeSessions.length === 0 ? (
          <div className="flex items-center gap-2 rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-700">
            <AlertCircle className="h-4 w-4 shrink-0" />
            Nenhuma sessão de caixa ativa no momento.
          </div>
        ) : (
          <>
            {/* KPI aggregate cards */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <StatCard
                label="Total vendido"
                value={formatCurrency(aggregate.totalVendido)}
                icon={<Banknote className="h-8 w-8" />}
                accent="primary"
              />
              <StatCard
                label="Vendas concluídas"
                value={aggregate.vendasConcluidas}
                icon={<CheckCircle2 className="h-8 w-8" />}
                accent="primary"
              />
              <StatCard
                label="Vendas canceladas"
                value={aggregate.vendasCanceladas}
                icon={<XCircle className="h-8 w-8" />}
                accent="destructive"
              />
            </div>

            {/* Payment distribution + session comparison charts */}
            {paymentDonutData.length > 0 && (
              <div
                className={`grid gap-4 ${activeSessions.length > 1 ? "sm:grid-cols-2" : ""}`}
              >
                {/* Donut – payment method distribution */}
                <Card>
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium text-muted-foreground">
                      Distribuição por Pagamento
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <ChartContainer
                      config={paymentChartConfig}
                      className="mx-auto aspect-square max-h-55"
                    >
                      <PieChart>
                        <ChartTooltip
                          cursor={false}
                          content={<PaymentPieTooltip />}
                        />
                        <Pie
                          data={paymentDonutData}
                          dataKey="value"
                          nameKey="method"
                          innerRadius={65}
                          strokeWidth={4}
                          stroke="transparent"
                        />
                        <ChartLegend
                          content={<ChartLegendContent nameKey="method" />}
                        />
                      </PieChart>
                    </ChartContainer>
                  </CardContent>
                </Card>

                {/* Bar – session total comparison (only with multiple sessions) */}
                {activeSessions.length > 1 && (
                  <Card>
                    <CardHeader className="pb-2">
                      <CardTitle className="text-sm font-medium text-muted-foreground">
                        Comparativo de Sessões
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ChartContainer
                        config={sessionBarConfig}
                        className="h-55"
                      >
                        <BarChart
                          data={sessionBarData}
                          layout="vertical"
                          margin={{ left: 8, right: 24, top: 4, bottom: 4 }}
                        >
                          <CartesianGrid
                            strokeDasharray="3 3"
                            horizontal={false}
                          />
                          <XAxis
                            type="number"
                            tickFormatter={(v: number) =>
                              v >= 1000
                                ? `R$${(v / 1000).toFixed(1)}k`
                                : `R$${v}`
                            }
                            tick={{ fontSize: 10 }}
                          />
                          <YAxis
                            dataKey="caixa"
                            type="category"
                            tick={{ fontSize: 11 }}
                            width={65}
                          />
                          <ChartTooltip
                            cursor={false}
                            content={<SessionBarTooltip />}
                          />
                          <Bar
                            dataKey="totalVendido"
                            fill="var(--color-totalVendido)"
                            radius={[0, 4, 4, 0]}
                            barSize={32}
                          />
                        </BarChart>
                      </ChartContainer>
                    </CardContent>
                  </Card>
                )}
              </div>
            )}

            {/* Per-session cards */}
            <div>
              <p className="mb-3 text-xs font-semibold uppercase tracking-widest text-muted-foreground/70">
                Sessões ativas ({activeSessions.length})
              </p>
              <div className="grid gap-4 sm:grid-cols-2">
                {sessionData.map(({ register, report }) => (
                  <Card key={register.cashRegisterId}>
                    <CardHeader className="pb-2">
                      <div className="flex items-center gap-2">
                        <Store className="h-4 w-4 text-primary" />
                        <CardTitle className="text-sm">
                          {register.code}
                        </CardTitle>
                        <Badge variant="secondary" className="ml-auto text-xs">
                          Aberto
                        </Badge>
                      </div>
                      <p className="text-xs text-muted-foreground">
                        {register.activeOperatorName}
                        {register.openedAt
                          ? ` · desde ${formatDate(register.openedAt)}`
                          : ""}
                      </p>
                    </CardHeader>
                    <CardContent className="space-y-3 pt-0">
                      {report ? (
                        <>
                          <div className="grid grid-cols-3 gap-2 text-center text-xs">
                            <div className="rounded bg-muted p-2">
                              <p className="text-base font-bold text-primary">
                                {formatCurrency(report.totalVendido)}
                              </p>
                              <p className="text-muted-foreground">Total</p>
                            </div>
                            <div className="rounded bg-muted p-2">
                              <p className="text-base font-bold text-primary">
                                {report.vendasConcluidas}
                              </p>
                              <p className="text-muted-foreground">
                                Concluídas
                              </p>
                            </div>
                            <div className="rounded bg-muted p-2">
                              <p className="text-base font-bold text-destructive">
                                {report.vendasCanceladas}
                              </p>
                              <p className="text-muted-foreground">
                                Canceladas
                              </p>
                            </div>
                          </div>
                          {Object.keys(report.totalPorMetodoPagamento).length >
                            0 && (
                            <div className="space-y-1.5">
                              <p className="text-[10px] font-semibold uppercase tracking-widest text-muted-foreground/60">
                                Pagamentos
                              </p>
                              {Object.entries(
                                report.totalPorMetodoPagamento,
                              ).map(([method, amount]) => {
                                const pct =
                                  report.totalVendido > 0
                                    ? (amount / report.totalVendido) * 100
                                    : 0;
                                const color = PAYMENT_HEX[method] ?? "#6366f1";
                                return (
                                  <div key={method} className="space-y-0.5">
                                    <div className="flex justify-between text-[11px]">
                                      <span className="text-muted-foreground">
                                        {formatPaymentMethod(method)}
                                      </span>
                                      <span className="tabular-nums font-medium">
                                        {formatCurrency(amount)}
                                      </span>
                                    </div>
                                    <div className="h-1.5 overflow-hidden rounded-full bg-muted">
                                      <div
                                        className="h-full rounded-full transition-all"
                                        style={{
                                          width: `${pct}%`,
                                          backgroundColor: color,
                                        }}
                                      />
                                    </div>
                                  </div>
                                );
                              })}
                            </div>
                          )}
                        </>
                      ) : (
                        <div className="flex items-center gap-2 text-xs text-muted-foreground">
                          <Loader2 className="h-3 w-3 animate-spin" />
                          Carregando…
                        </div>
                      )}
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          </>
        )}
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
            Distribuição dos pagamentos recebidos nas sessões de caixa ativas.
          </p>
        </div>

        {isLoading ? (
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Loader2 className="h-4 w-4 animate-spin" />
            Carregando…
          </div>
        ) : activeSessions.length === 0 ? (
          <div className="flex items-center gap-2 rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-700">
            <AlertCircle className="h-4 w-4 shrink-0" />
            Nenhuma sessão de caixa ativa no momento.
          </div>
        ) : Object.keys(aggregate.totalPorMetodo).length === 0 ? (
          <p className="text-sm text-muted-foreground">
            Nenhum pagamento registrado nas sessões ativas.
          </p>
        ) : (
          <>
            {/* Donut – aggregate payment distribution */}
            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Distribuição Geral
                </CardTitle>
              </CardHeader>
              <CardContent>
                <ChartContainer
                  config={paymentChartConfig}
                  className="mx-auto aspect-square max-h-60"
                >
                  <PieChart>
                    <ChartTooltip
                      cursor={false}
                      content={<PaymentPieTooltip />}
                    />
                    <Pie
                      data={paymentDonutData}
                      dataKey="value"
                      nameKey="method"
                      innerRadius={72}
                      strokeWidth={4}
                      stroke="transparent"
                    />
                    <ChartLegend
                      content={<ChartLegendContent nameKey="method" />}
                    />
                  </PieChart>
                </ChartContainer>
              </CardContent>
            </Card>

            {/* Per-session breakdown (when more than one session) */}
            {activeSessions.length > 1 && (
              <div>
                <Separator className="mb-4" />
                <p className="mb-3 text-xs font-semibold uppercase tracking-widest text-muted-foreground/70">
                  Por sessão
                </p>
                <div className="space-y-4">
                  {sessionData.map(({ register, report }) => {
                    if (
                      !report ||
                      Object.keys(report.totalPorMetodoPagamento).length === 0
                    )
                      return null;
                    return (
                      <Card key={register.cashRegisterId}>
                        <CardHeader className="pb-2">
                          <CardTitle className="flex items-center gap-2 text-sm">
                            <Store className="h-4 w-4 text-primary" />
                            {register.code}
                            <span className="text-xs font-normal text-muted-foreground">
                              — {register.activeOperatorName}
                            </span>
                          </CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-3 pt-0">
                          {Object.entries(report.totalPorMetodoPagamento).map(
                            ([method, amount]) => {
                              const pct =
                                report.totalVendido > 0
                                  ? (amount / report.totalVendido) * 100
                                  : 0;
                              return (
                                <div key={method} className="space-y-1">
                                  <div className="flex justify-between text-sm">
                                    <span>{formatPaymentMethod(method)}</span>
                                    <span className="text-muted-foreground">
                                      {formatCurrency(amount)} ({pct.toFixed(1)}
                                      %)
                                    </span>
                                  </div>
                                  <div className="h-1.5 overflow-hidden rounded-full bg-muted">
                                    <div
                                      className={`h-full rounded-full ${PAYMENT_COLORS[method] ?? "bg-primary"}`}
                                      style={{ width: `${pct}%` }}
                                    />
                                  </div>
                                </div>
                              );
                            },
                          )}
                        </CardContent>
                      </Card>
                    );
                  })}
                </div>
              </div>
            )}
          </>
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
