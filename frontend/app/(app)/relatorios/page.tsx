"use client";

import { Suspense, useState } from "react";
import { useSearchParams } from "next/navigation";
import { useQuery, useQueries } from "@tanstack/react-query";
import type { DateRange } from "react-day-picker";
import {
  ShoppingBag,
  AlertCircle,
  CheckCircle2,
  XCircle,
  Banknote,
  Loader2,
  Store,
  CalendarIcon,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
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
import type { SessionSummaryResponse } from "@/features/cash-register/types";
import {
  formatCurrency,
  formatDate,
  formatPaymentMethod,
} from "@/shared/utils/formatters";
import { cn } from "@/lib/utils";

/* ─── Types ─── */
type FilterMode = "active" | "date" | "range";

interface SessionItem {
  sessionId: string;
  code: string;
  operatorName: string;
  openedAt: string;
  closedAt: string | null;
  status: "OPEN" | "CLOSED";
  initialAmount: number;
  report: SessionSummaryResponse;
}

/* ─── Helpers ─── */
function toIsoDate(d: Date): string {
  return [
    d.getFullYear(),
    String(d.getMonth() + 1).padStart(2, "0"),
    String(d.getDate()).padStart(2, "0"),
  ].join("-");
}

function shortDate(d: Date): string {
  return d.toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

/* ─── Colors ─── */
const PAYMENT_HEX: Record<string, string> = {
  CASH: "#10b981",
  PIX: "#0ea5e9",
  CREDIT_CARD: "#8b5cf6",
  DEBIT_CARD: "#f97316",
};

const PAYMENT_COLORS: Record<string, string> = {
  CASH: "bg-emerald-500",
  PIX: "bg-sky-500",
  CREDIT_CARD: "bg-violet-500",
  DEBIT_CARD: "bg-orange-500",
};

/* ─── Chart configs ─── */
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

/* ─── Coming soon ─── */
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

/* ─── Session card ─── */
function SessionCard({ item }: { item: SessionItem }) {
  return (
    <Card>
      <CardHeader className="pb-2">
        <div className="flex items-center gap-2">
          <Store className="h-4 w-4 text-primary" />
          <CardTitle className="text-sm">{item.code}</CardTitle>
          <Badge
            variant={item.status === "OPEN" ? "secondary" : "outline"}
            className="ml-auto text-xs"
          >
            {item.status === "OPEN" ? "Aberto" : "Fechado"}
          </Badge>
        </div>
        <p className="text-xs text-muted-foreground">
          {item.operatorName}
          {item.openedAt ? ` · abertura ${formatDate(item.openedAt)}` : ""}
        </p>
        {item.closedAt && (
          <p className="text-xs text-muted-foreground">
            Fechamento: {formatDate(item.closedAt)}
          </p>
        )}
      </CardHeader>
      <CardContent className="space-y-3 pt-0">
        <div className="grid grid-cols-3 gap-2 text-center text-xs">
          <div className="rounded bg-muted p-2">
            <p className="text-base font-bold text-primary">
              {formatCurrency(item.report.totalVendido)}
            </p>
            <p className="text-muted-foreground">Total</p>
          </div>
          <div className="rounded bg-muted p-2">
            <p className="text-base font-bold text-primary">
              {item.report.vendasConcluidas}
            </p>
            <p className="text-muted-foreground">Concluídas</p>
          </div>
          <div className="rounded bg-muted p-2">
            <p className="text-base font-bold text-destructive">
              {item.report.vendasCanceladas}
            </p>
            <p className="text-muted-foreground">Canceladas</p>
          </div>
        </div>

        {Object.keys(item.report.totalPorMetodoPagamento).length > 0 && (
          <div className="space-y-1.5">
            <p className="text-[10px] font-semibold uppercase tracking-widest text-muted-foreground/60">
              Pagamentos
            </p>
            {Object.entries(item.report.totalPorMetodoPagamento).map(
              ([method, amount]) => {
                const pct =
                  item.report.totalVendido > 0
                    ? (amount / item.report.totalVendido) * 100
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
                        style={{ width: `${pct}%`, backgroundColor: color }}
                      />
                    </div>
                  </div>
                );
              },
            )}
          </div>
        )}

        <p className="text-[10px] text-muted-foreground/50">
          Fundo inicial: {formatCurrency(item.initialAmount)}
        </p>
      </CardContent>
    </Card>
  );
}

/* ─── Filter bar ─── */
const FILTER_MODES: { value: FilterMode; label: string }[] = [
  { value: "active", label: "Sessões ativas" },
  { value: "date", label: "Data específica" },
  { value: "range", label: "Intervalo" },
];

function FilterBar({
  mode,
  onModeChange,
  selectedDate,
  onSelectDate,
  dateRange,
  onSelectDateRange,
}: {
  mode: FilterMode;
  onModeChange: (m: FilterMode) => void;
  selectedDate: Date;
  onSelectDate: (d: Date) => void;
  dateRange: DateRange | undefined;
  onSelectDateRange: (r: DateRange | undefined) => void;
}) {
  const rangeLabel =
    dateRange?.from && dateRange.to
      ? `${shortDate(dateRange.from)} → ${shortDate(dateRange.to)}`
      : dateRange?.from
        ? shortDate(dateRange.from)
        : "Selecionar período";

  return (
    <div className="flex flex-wrap items-center gap-2">
      <div className="flex rounded-lg border bg-muted/50 p-0.5">
        {FILTER_MODES.map((m) => (
          <button
            key={m.value}
            onClick={() => onModeChange(m.value)}
            className={cn(
              "rounded-md px-3 py-1.5 text-xs font-medium transition-colors",
              mode === m.value
                ? "bg-background text-foreground shadow-sm"
                : "text-muted-foreground hover:text-foreground",
            )}
          >
            {m.label}
          </button>
        ))}
      </div>

      {mode === "date" && (
        <Popover>
          <PopoverTrigger asChild>
            <Button variant="outline" size="sm" className="h-8 gap-1.5 text-xs">
              <CalendarIcon className="h-3.5 w-3.5" />
              {shortDate(selectedDate)}
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-auto p-0" align="end">
            <Calendar
              mode="single"
              selected={selectedDate}
              onSelect={(d) => d && onSelectDate(d)}
              disabled={{ after: new Date() }}
              initialFocus
            />
          </PopoverContent>
        </Popover>
      )}

      {mode === "range" && (
        <Popover>
          <PopoverTrigger asChild>
            <Button variant="outline" size="sm" className="h-8 gap-1.5 text-xs">
              <CalendarIcon className="h-3.5 w-3.5" />
              {rangeLabel}
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-auto p-0" align="end">
            <Calendar
              mode="range"
              selected={dateRange}
              onSelect={onSelectDateRange}
              disabled={{ after: new Date() }}
              initialFocus
              numberOfMonths={2}
            />
          </PopoverContent>
        </Popover>
      )}
    </div>
  );
}

/* ─── Main content (needs useSearchParams → inside Suspense) ─── */
function RelatoriosContent() {
  const searchParams = useSearchParams();
  const tab = searchParams.get("tab") ?? "resumo";

  const [filterMode, setFilterMode] = useState<FilterMode>("active");
  const [selectedDate, setSelectedDate] = useState<Date>(() => new Date());
  const [dateRange, setDateRange] = useState<DateRange | undefined>({
    from: new Date(),
  });

  /* ── Active mode ── */
  const { data: cashRegisters = [], isLoading: loadingRegisters } = useQuery({
    queryKey: ["all-cash-registers"],
    queryFn: () => cashRegisterService.listCashRegisters(),
    staleTime: 30_000,
    refetchInterval: filterMode === "active" ? 60_000 : false,
    enabled: filterMode === "active",
  });

  const activeSessions = cashRegisters.filter(
    (cr) => cr.hasActiveSession && cr.activeSessionId,
  );

  const reportQueries = useQueries({
    queries: activeSessions.map((cr) => ({
      queryKey: ["session-report", cr.activeSessionId],
      queryFn: () => cashRegisterService.getReport(cr.activeSessionId!),
      staleTime: 30_000,
      enabled: filterMode === "active",
    })),
  });

  /* ── Date / Range mode ── */
  const rangeStart =
    filterMode === "date"
      ? toIsoDate(selectedDate)
      : filterMode === "range" && dateRange?.from
        ? toIsoDate(dateRange.from)
        : null;

  const rangeEnd =
    filterMode === "date"
      ? toIsoDate(selectedDate)
      : filterMode === "range" && dateRange?.to
        ? toIsoDate(dateRange.to)
        : filterMode === "range" && dateRange?.from
          ? toIsoDate(dateRange.from)
          : null;

  const { data: historicSessions = [], isLoading: loadingHistoric } = useQuery({
    queryKey: ["sessions-by-range", rangeStart, rangeEnd],
    queryFn: () =>
      cashRegisterService.listSessionsByDateRange(rangeStart!, rangeEnd!),
    enabled: filterMode !== "active" && !!rangeStart && !!rangeEnd,
    staleTime: 60_000,
  });

  /* ── Loading ── */
  const isLoading =
    filterMode === "active"
      ? loadingRegisters || reportQueries.some((q) => q.isLoading)
      : loadingHistoric;

  /* ── Unified session items ── */
  const sessionItems: SessionItem[] =
    filterMode === "active"
      ? activeSessions.flatMap((cr, i) => {
          const report = reportQueries[i]?.data;
          if (!report) return [];
          const item: SessionItem = {
            sessionId: cr.activeSessionId!,
            code: cr.code,
            operatorName: cr.activeOperatorName ?? "—",
            openedAt: cr.openedAt!,
            closedAt: null,
            status: "OPEN",
            initialAmount: cr.initialAmount ?? 0,
            report,
          };
          return [item];
        })
      : historicSessions.map((s) => ({
          sessionId: s.sessionId,
          code: s.codigoCaixa,
          operatorName: s.nomeOperador,
          openedAt: s.abertura,
          closedAt: s.fechamento,
          status: s.status,
          initialAmount: s.valorInicial,
          report: s.resumo,
        }));

  /* ── Aggregate ── */
  const aggregate = sessionItems.reduce(
    (acc, item) => {
      acc.vendasConcluidas += item.report.vendasConcluidas;
      acc.vendasCanceladas += item.report.vendasCanceladas;
      acc.totalVendido += item.report.totalVendido;
      for (const [method, amount] of Object.entries(
        item.report.totalPorMetodoPagamento,
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

  const paymentDonutData = Object.entries(aggregate.totalPorMetodo).map(
    ([method, amount]) => ({
      method,
      value: amount,
      fill: `var(--color-${method})`,
    }),
  );

  const sessionBarData = sessionItems.map((item) => ({
    caixa: item.code,
    totalVendido: item.report.totalVendido,
  }));

  /* ── Descriptions ── */
  const periodDesc =
    filterMode === "active"
      ? "Sessões de caixa abertas agora."
      : filterMode === "date"
        ? selectedDate.toLocaleDateString("pt-BR", { dateStyle: "long" })
        : dateRange?.from && dateRange.to
          ? `${shortDate(dateRange.from)} → ${shortDate(dateRange.to)}`
          : dateRange?.from
            ? shortDate(dateRange.from)
            : "Selecione um período";

  const emptyMsg =
    filterMode === "active"
      ? "Nenhuma sessão de caixa ativa no momento."
      : "Nenhuma sessão encontrada para o período selecionado.";

  const filterBar = (
    <FilterBar
      mode={filterMode}
      onModeChange={setFilterMode}
      selectedDate={selectedDate}
      onSelectDate={setSelectedDate}
      dateRange={dateRange}
      onSelectDateRange={setDateRange}
    />
  );

  const loadingNode = (
    <div className="flex items-center gap-2 text-sm text-muted-foreground">
      <Loader2 className="h-4 w-4 animate-spin" />
      Carregando dados…
    </div>
  );

  const emptyNode = (
    <div className="flex items-center gap-2 rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-700">
      <AlertCircle className="h-4 w-4 shrink-0" />
      {emptyMsg}
    </div>
  );

  /* ══ Tab: Resumo ══ */
  if (tab === "resumo") {
    return (
      <div className="space-y-6">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h2 className="text-xl font-semibold">Resumo do Turno</h2>
            <p className="mt-0.5 text-sm text-muted-foreground">{periodDesc}</p>
          </div>
          {filterBar}
        </div>

        {isLoading ? (
          loadingNode
        ) : sessionItems.length === 0 ? (
          emptyNode
        ) : (
          <>
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

            {paymentDonutData.length > 0 && (
              <div
                className={`grid gap-4 ${sessionItems.length > 1 ? "sm:grid-cols-2" : ""}`}
              >
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

                {sessionItems.length > 1 && (
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

            <div>
              <p className="mb-3 text-xs font-semibold uppercase tracking-widest text-muted-foreground/70">
                Sessões ({sessionItems.length})
              </p>
              <div className="grid gap-4 sm:grid-cols-2">
                {sessionItems.map((item) => (
                  <SessionCard key={item.sessionId} item={item} />
                ))}
              </div>
            </div>
          </>
        )}
      </div>
    );
  }

  /* ══ Tab: Meios de Pagamento ══ */
  if (tab === "pagamentos") {
    return (
      <div className="space-y-6">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h2 className="text-xl font-semibold">Meios de Pagamento</h2>
            <p className="mt-0.5 text-sm text-muted-foreground">{periodDesc}</p>
          </div>
          {filterBar}
        </div>

        {isLoading ? (
          loadingNode
        ) : sessionItems.length === 0 ? (
          emptyNode
        ) : Object.keys(aggregate.totalPorMetodo).length === 0 ? (
          <p className="text-sm text-muted-foreground">
            Nenhum pagamento registrado nas sessões do período.
          </p>
        ) : (
          <>
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

            {sessionItems.length > 1 && (
              <div>
                <Separator className="mb-4" />
                <p className="mb-3 text-xs font-semibold uppercase tracking-widest text-muted-foreground/70">
                  Por sessão
                </p>
                <div className="space-y-4">
                  {sessionItems.map((item) => {
                    if (
                      Object.keys(item.report.totalPorMetodoPagamento)
                        .length === 0
                    )
                      return null;
                    return (
                      <Card key={item.sessionId}>
                        <CardHeader className="pb-2">
                          <CardTitle className="flex items-center gap-2 text-sm">
                            <Store className="h-4 w-4 text-primary" />
                            {item.code}
                            <span className="text-xs font-normal text-muted-foreground">
                              — {item.operatorName}
                            </span>
                          </CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-3 pt-0">
                          {Object.entries(
                            item.report.totalPorMetodoPagamento,
                          ).map(([method, amount]) => {
                            const pct =
                              item.report.totalVendido > 0
                                ? (amount / item.report.totalVendido) * 100
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
                          })}
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

  /* ══ Tab: Por Produto ══ */
  if (tab === "por-produto") {
    return (
      <ComingSoon
        icon={<ShoppingBag className="h-16 w-16" />}
        title="Relatório por Produto"
        description="Exibirá os produtos mais vendidos, quantidade total vendida por item e receita gerada por cada produto na sessão."
      />
    );
  }

  /* ══ Tab: Histórico ══ */
  if (tab === "historico") {
    return (
      <div className="space-y-6">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h2 className="text-xl font-semibold">Histórico de Sessões</h2>
            <p className="mt-0.5 text-sm text-muted-foreground">{periodDesc}</p>
          </div>
          {filterBar}
        </div>

        {isLoading ? (
          loadingNode
        ) : sessionItems.length === 0 ? (
          emptyNode
        ) : (
          <div className="grid gap-4 sm:grid-cols-2">
            {sessionItems.map((item) => (
              <SessionCard key={item.sessionId} item={item} />
            ))}
          </div>
        )}
      </div>
    );
  }

  return null;
}

/* ─── Page ─── */
export default function RelatoriosPage() {
  return (
    <div className="flex h-full flex-col">
      <div className="border-b bg-card px-6 py-4">
        <h1 className="text-base font-semibold">Relatórios</h1>
        <p className="text-xs text-muted-foreground">
          Visualize o desempenho por sessão — ativas agora, por data ou por
          período.
        </p>
      </div>

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
