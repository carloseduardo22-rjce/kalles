import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import type { CloseSessionResponse } from "../types";
import {
  formatCurrency,
  formatDate,
  formatPaymentMethod,
} from "@/shared/utils/formatters";
import { CheckCircle2 } from "lucide-react";

interface SessionSummaryProps {
  data: CloseSessionResponse;
}

export function SessionSummary({ data }: SessionSummaryProps) {
  const { resumo } = data;
  const methodEntries = Object.entries(resumo.totalPorMetodoPagamento);

  return (
    <Card className="w-full max-w-lg border-2 border-primary/30 shadow-md">
      <CardHeader className="pb-3">
        <div className="flex items-center gap-2">
          <CheckCircle2 className="h-5 w-5 text-primary" />
          <CardTitle className="text-lg">Sessão Encerrada</CardTitle>
        </div>
        <CardDescription>
          Caixa {data.codigoCaixa} — Operador {data.nomeOperador}
        </CardDescription>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Period */}
        <div className="flex justify-between text-sm text-muted-foreground">
          <span>Abertura: {formatDate(data.abertura)}</span>
          <span>Fechamento: {formatDate(data.fechamento)}</span>
        </div>

        <Separator />

        {/* Sales counts */}
        <div className="grid grid-cols-2 gap-4 text-center">
          <div className="rounded-lg bg-muted p-3">
            <p className="text-2xl font-bold text-primary">
              {resumo.vendasConcluidas}
            </p>
            <p className="text-xs text-muted-foreground">Vendas concluídas</p>
          </div>
          <div className="rounded-lg bg-muted p-3">
            <p className="text-2xl font-bold text-destructive">
              {resumo.vendasCanceladas}
            </p>
            <p className="text-xs text-muted-foreground">Vendas canceladas</p>
          </div>
        </div>

        {/* Total */}
        <div className="flex items-center justify-between rounded-lg border p-4">
          <span className="font-medium">Total vendido</span>
          <span className="text-xl font-bold text-primary">
            {formatCurrency(resumo.totalVendido)}
          </span>
        </div>

        {/* By payment method */}
        {methodEntries.length > 0 && (
          <div>
            <p className="mb-2 text-sm font-medium text-muted-foreground">
              Por método de pagamento
            </p>
            <div className="space-y-2">
              {methodEntries.map(([method, amount]) => (
                <div
                  key={method}
                  className="flex items-center justify-between text-sm"
                >
                  <Badge variant="secondary">
                    {formatPaymentMethod(method)}
                  </Badge>
                  <span className="font-medium">{formatCurrency(amount)}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Initial amount */}
        <div className="space-y-2 border-t pt-3 text-sm text-muted-foreground">
          <div className="flex justify-between">
            <span>Fundo de caixa inicial</span>
            <span>{formatCurrency(data.valorInicial)}</span>
          </div>
          <div className="flex justify-between">
            <span>Total em dinheiro</span>
            <span>{formatCurrency(resumo.totalEmDinheiro)}</span>
          </div>
          <div className="flex justify-between">
            <span>Saldo esperado em caixa</span>
            <span>{formatCurrency(resumo.saldoEsperadoEmCaixa)}</span>
          </div>
          {resumo.valorInformadoEmCaixa !== null && (
            <div className="flex justify-between">
              <span>Valor informado no fechamento</span>
              <span>{formatCurrency(resumo.valorInformadoEmCaixa)}</span>
            </div>
          )}
          {resumo.diferencaEmCaixa !== null && (
            <div className="flex justify-between">
              <span>Diferença de caixa</span>
              <span
                className={
                  resumo.diferencaEmCaixa === 0
                    ? ""
                    : resumo.diferencaEmCaixa > 0
                      ? "text-emerald-600"
                      : "text-destructive"
                }
              >
                {formatCurrency(resumo.diferencaEmCaixa)}
              </span>
            </div>
          )}
          {data.nomeOperadorAutorizador && (
            <div className="flex justify-between">
              <span>Fechamento autorizado por</span>
              <span>{data.nomeOperadorAutorizador}</span>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
