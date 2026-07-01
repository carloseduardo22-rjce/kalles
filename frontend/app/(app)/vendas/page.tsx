"use client";

import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  Download,
  Loader2,
  RefreshCw,
  Search,
  ShoppingBag,
} from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { saleService } from "@/features/sales/services/sale.service";
import type { SaleHistoryResponse, SaleState } from "@/features/sales/types";
import {
  formatCurrency,
  formatDate,
  formatPaymentMethod,
  formatSaleState,
} from "@/shared/utils/formatters";

type SaleHistoryFilterState = SaleState | "ALL";

const STATE_OPTIONS: Array<{ value: SaleHistoryFilterState; label: string }> = [
  { value: "ALL", label: "Todos" },
  { value: "COMPLETED", label: "Concluidas" },
  { value: "CANCELED", label: "Canceladas" },
  { value: "PAID", label: "Pagas" },
  { value: "OPEN", label: "Em aberto" },
];

function toIsoDate(date: Date): string {
  return [
    date.getFullYear(),
    String(date.getMonth() + 1).padStart(2, "0"),
    String(date.getDate()).padStart(2, "0"),
  ].join("-");
}

function firstDayOfMonth(): string {
  const now = new Date();
  return toIsoDate(new Date(now.getFullYear(), now.getMonth(), 1));
}

function today(): string {
  return toIsoDate(new Date());
}

function stateBadgeVariant(state: SaleState): "default" | "secondary" | "destructive" | "outline" {
  if (state === "COMPLETED") return "default";
  if (state === "CANCELED") return "destructive";
  if (state === "PAID") return "secondary";
  return "outline";
}

function paymentSummary(sale: SaleHistoryResponse): string {
  const confirmed = sale.payments.filter((payment) => payment.confirmed);
  if (confirmed.length === 0) {
    return "Sem pagamento";
  }

  return confirmed
    .map((payment) => `${formatPaymentMethod(payment.method)} ${formatCurrency(payment.amount - payment.changeAmount)}`)
    .join(", ");
}

function itemSummary(sale: SaleHistoryResponse): string {
  if (sale.items.length === 0) {
    return "Sem itens";
  }

  return sale.items
    .map((item) => `${item.quantity}x ${item.productName}`)
    .join(", ");
}

function saleTotals(sales: SaleHistoryResponse[]) {
  return sales.reduce(
    (acc, sale) => {
      acc.total += sale.total;
      if (sale.state === "COMPLETED") acc.completed += 1;
      if (sale.state === "CANCELED") acc.canceled += 1;
      return acc;
    },
    { total: 0, completed: 0, canceled: 0 },
  );
}

export default function VendasPage() {
  const [startDate, setStartDate] = useState(firstDayOfMonth);
  const [endDate, setEndDate] = useState(today);
  const [state, setState] = useState<SaleHistoryFilterState>("ALL");
  const [isExporting, setIsExporting] = useState(false);

  const {
    data: sales = [],
    error,
    isLoading,
    isFetching,
    refetch,
  } = useQuery({
    queryKey: ["sales-history", startDate, endDate, state],
    queryFn: () => saleService.listHistory(startDate, endDate, state),
    staleTime: 30_000,
  });

  const totals = useMemo(() => saleTotals(sales), [sales]);

  async function exportHistory() {
    setIsExporting(true);
    try {
      const blob = await saleService.exportHistory(startDate, endDate, state);
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `historico-vendas-${startDate}-${endDate}.xlsx`;
      link.click();
      URL.revokeObjectURL(url);
      toast.success("Arquivo gerado");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Nao foi possivel exportar");
    } finally {
      setIsExporting(false);
    }
  }

  return (
    <div className="flex h-full flex-col" data-testid="sales-history-page">
      <div className="border-b bg-card px-6 py-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="text-base font-semibold">Historico de Vendas</h1>
            <p className="text-xs text-muted-foreground">
              Consulte vendas, itens, pagamentos e exporte o periodo selecionado.
            </p>
          </div>
          <Button
            onClick={exportHistory}
            disabled={isExporting || isLoading}
            className="gap-2"
            data-testid="sales-history-export"
          >
            {isExporting ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Download className="h-4 w-4" />
            )}
            Exportar Excel
          </Button>
        </div>
      </div>

      <div className="flex-1 overflow-auto p-6">
        <div className="mb-4 grid gap-3 rounded-md border bg-card p-4 md:grid-cols-[1fr_1fr_1fr_auto]">
          <div className="space-y-1.5">
            <Label htmlFor="sales-history-start">Inicio</Label>
            <Input
              id="sales-history-start"
              data-testid="sales-history-start"
              type="date"
              value={startDate}
              onChange={(event) => setStartDate(event.target.value)}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="sales-history-end">Fim</Label>
            <Input
              id="sales-history-end"
              data-testid="sales-history-end"
              type="date"
              value={endDate}
              onChange={(event) => setEndDate(event.target.value)}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="sales-history-state">Status</Label>
            <select
              id="sales-history-state"
              data-testid="sales-history-state"
              value={state}
              onChange={(event) => setState(event.target.value as SaleHistoryFilterState)}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
            >
              {STATE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div className="flex items-end gap-2">
            <Button
              variant="outline"
              onClick={() => refetch()}
              disabled={isFetching}
              className="gap-2"
              data-testid="sales-history-refresh"
            >
              {isFetching ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <RefreshCw className="h-4 w-4" />
              )}
              Atualizar
            </Button>
          </div>
        </div>

        <div className="mb-4 grid gap-3 sm:grid-cols-3">
          <Card>
            <CardContent className="flex items-center gap-3 p-4">
              <ShoppingBag className="h-5 w-5 text-primary" />
              <div>
                <p className="text-xs text-muted-foreground">Total vendido</p>
                <p className="text-lg font-semibold tabular-nums">
                  {formatCurrency(totals.total)}
                </p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4">
              <p className="text-xs text-muted-foreground">Concluidas</p>
              <p className="text-lg font-semibold tabular-nums">
                {totals.completed}
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4">
              <p className="text-xs text-muted-foreground">Canceladas</p>
              <p className="text-lg font-semibold tabular-nums">
                {totals.canceled}
              </p>
            </CardContent>
          </Card>
        </div>

        {error ? (
          <div className="rounded-md border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
            {error instanceof Error ? error.message : "Nao foi possivel carregar as vendas"}
          </div>
        ) : isLoading ? (
          <div className="flex items-center gap-2 rounded-md border bg-card p-6 text-sm text-muted-foreground">
            <Loader2 className="h-4 w-4 animate-spin" />
            Carregando vendas
          </div>
        ) : sales.length === 0 ? (
          <div className="flex flex-col items-center justify-center rounded-md border border-dashed bg-card p-12 text-center">
            <Search className="mb-3 h-8 w-8 text-muted-foreground" />
            <p className="text-sm font-medium">Nenhuma venda encontrada</p>
            <p className="mt-1 text-xs text-muted-foreground">
              Ajuste o periodo ou o status para ampliar a busca.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto rounded-md border bg-card">
            <table className="w-full min-w-[980px] text-sm">
              <thead className="border-b bg-muted/50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">
                    Abertura
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">
                    Venda
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">
                    Status
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">
                    Itens
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">
                    Pagamentos
                  </th>
                  <th className="px-4 py-3 text-right text-xs font-semibold uppercase text-muted-foreground">
                    Desconto
                  </th>
                  <th className="px-4 py-3 text-right text-xs font-semibold uppercase text-muted-foreground">
                    Total
                  </th>
                </tr>
              </thead>
              <tbody>
                {sales.map((sale, index) => (
                  <tr
                    key={sale.id}
                    data-testid="sales-history-row"
                    className={index % 2 === 0 ? "border-b" : "border-b bg-muted/20"}
                  >
                    <td className="px-4 py-3 whitespace-nowrap">
                      {formatDate(sale.openedAt)}
                    </td>
                    <td className="px-4 py-3">
                      <p className="font-mono text-xs">{sale.id}</p>
                      <p className="mt-1 text-xs text-muted-foreground">
                        Sessao {sale.sessionToken}
                      </p>
                    </td>
                    <td className="px-4 py-3">
                      <Badge variant={stateBadgeVariant(sale.state)}>
                        {formatSaleState(sale.state)}
                      </Badge>
                    </td>
                    <td className="max-w-[240px] px-4 py-3 text-muted-foreground">
                      {itemSummary(sale)}
                    </td>
                    <td className="max-w-[240px] px-4 py-3 text-muted-foreground">
                      {paymentSummary(sale)}
                    </td>
                    <td className="px-4 py-3 text-right tabular-nums">
                      {formatCurrency(sale.fidelityDiscountApplied)}
                    </td>
                    <td className="px-4 py-3 text-right font-semibold tabular-nums">
                      {formatCurrency(sale.total)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
