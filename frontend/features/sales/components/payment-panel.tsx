"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  CreditCard,
  Banknote,
  Smartphone,
  Landmark,
  QrCode,
} from "lucide-react";
import { QRCodeSVG } from "qrcode.react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ErrorAlert } from "@/shared/components/error-alert";
import { formatCurrency, formatPaymentMethod } from "@/shared/utils/formatters";
import { api } from "@/shared/services/api";
import type { PaymentMethod, SaleResponse } from "../types";

const amountSchema = z.object({
  amount: z
    .string()
    .min(1, "Informe o valor")
    .refine(
      (v) =>
        !isNaN(parseFloat(v.replace(",", "."))) &&
        parseFloat(v.replace(",", ".")) > 0,
      "Valor deve ser maior que zero",
    ),
});
type AmountForm = z.infer<typeof amountSchema>;

interface PaymentPanelProps {
  sale: SaleResponse;
  isLoading: boolean;
  error: string | null;
  cashRegisterCode?: string; // ← Add this for MercadoPago integration
  onAddPayment: (method: PaymentMethod, amount: number) => Promise<void>;
  onCompleteSale: () => Promise<void>;
}

const PAYMENT_METHODS: {
  value: PaymentMethod;
  label: string;
  icon: React.ReactNode;
}[] = [
  {
    value: "CASH",
    label: "Dinheiro",
    icon: <Banknote className="h-4 w-4" />,
  },
  {
    value: "PIX",
    label: "Pix",
    icon: <Smartphone className="h-4 w-4" />,
  },
  {
    value: "CREDIT_CARD",
    label: "Crédito",
    icon: <CreditCard className="h-4 w-4" />,
  },
  {
    value: "DEBIT_CARD",
    label: "Débito",
    icon: <Landmark className="h-4 w-4" />,
  },
];

export function PaymentPanel({
  sale,
  isLoading,
  error,
  cashRegisterCode,
  onAddPayment,
  onCompleteSale,
}: PaymentPanelProps) {
  const [method, setMethod] = useState<PaymentMethod>("CASH");

  // MercadoPago PIX QR Code variables
  const [qrData, setQrData] = useState<string | null>(null);
  const [showQrDialog, setShowQrDialog] = useState(false);
  const [isGeneratingQr, setIsGeneratingQr] = useState(false);
  const [pendingPixAmount, setPendingPixAmount] = useState<number>(0);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AmountForm>({
    resolver: zodResolver(amountSchema),
  });

  // PAID = backend settled the balance, amountDue is 0 AND state is PAID
  const isPaid = sale.state === "PAID";
  const canPay = sale.state === "OPEN" || sale.state === "PAYMENT_IN_PROGRESS";

  async function handlePay(data: AmountForm) {
    const amount = parseFloat(data.amount.replace(",", "."));
    await onAddPayment(method, amount);
    reset();
  }

  async function handlePixPayment(amount: number) {
    if (cashRegisterCode) {
      // Generate dynamic QR Code for MercadoPago
      setIsGeneratingQr(true);
      try {
        const response = await api.post<{ orderId: string; qrData: string }>(
          "/api/mercadopago/orders",
          {
            pedidoIdErp: sale.id,
            amount: amount,
            caixaId: cashRegisterCode.replace("-", ""),
          },
        );
        setQrData(response.qrData);
        setPendingPixAmount(amount);
        setShowQrDialog(true);
      } catch (err) {
        console.error("Failed to generate QR Code", err);
      } finally {
        setIsGeneratingQr(false);
        reset({ amount: "" });
      }
    } else {
      // If there's no cash register code, just add normal payment without MP
      await onAddPayment("PIX", amount);
      reset({ amount: "" });
    }
  }

  const effectiveAmount = sale.amountDue > 0 ? sale.amountDue : sale.total;

  return (
    <div className="space-y-4">
      {/* Summary */}
      <div className="space-y-1.5 rounded-lg border bg-card p-3">
        <div className="flex justify-between text-sm">
          <span className="text-muted-foreground">Subtotal</span>
          <span>{formatCurrency(sale.subtotal)}</span>
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-muted-foreground">Total</span>
          <span className="font-semibold">{formatCurrency(sale.total)}</span>
        </div>
        {canPay && effectiveAmount > 0 && (
          <div className="flex justify-between text-base font-bold text-destructive">
            <span>A pagar</span>
            <span>{formatCurrency(effectiveAmount)}</span>
          </div>
        )}
        {isPaid && (
          <div className="flex justify-between text-sm font-medium text-green-600 dark:text-green-400">
            <span>Pagamento completo ✓</span>
          </div>
        )}
      </div>

      {/* Payments already registered */}
      {sale.payments.length > 0 && (
        <div className="space-y-1">
          <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
            Pagamentos
          </p>
          {sale.payments.map((p) => (
            <div
              key={p.id}
              className="flex justify-between text-xs text-muted-foreground"
            >
              <span>{formatPaymentMethod(p.method)}</span>
              <span>
                {formatCurrency(p.amount)}
                {p.changeAmount > 0 && (
                  <span className="ml-1 text-amber-600">
                    (troco: {formatCurrency(p.changeAmount)})
                  </span>
                )}
              </span>
            </div>
          ))}
        </div>
      )}

      <Separator />

      {/* Payment input — only when still needs payment */}
      {canPay && (
        <form onSubmit={handleSubmit(handlePay)} className="space-y-3">
          {/* Method selector */}
          <div className="grid grid-cols-4 gap-1.5">
            {PAYMENT_METHODS.map((m) => (
              <Button
                key={m.value}
                type="button"
                variant={method === m.value ? "default" : "outline"}
                size="sm"
                className="flex flex-col h-auto py-2 gap-1 text-xs"
                onClick={() => setMethod(m.value)}
              >
                {m.icon}
                {m.label}
              </Button>
            ))}
          </div>

          {/* Amount / Submit */}
          {method !== "CASH" ? (
            <div className="space-y-3 pt-2">
              <div className="flex justify-between items-center bg-muted/60 p-3 rounded-lg border text-sm">
                <span className="text-muted-foreground">
                  Valor via {formatPaymentMethod(method)}
                </span>
                <span className="font-semibold text-base">
                  {formatCurrency(effectiveAmount)}
                </span>
              </div>

              {method === "PIX" ? (
                <Button
                  type="button"
                  className="w-full"
                  onClick={() => handlePixPayment(effectiveAmount)}
                  disabled={isLoading || isGeneratingQr || effectiveAmount <= 0}
                >
                  {isLoading || isGeneratingQr ? (
                    <LoadingSpinner size="sm" label="" />
                  ) : (
                    "Gerar QR Code Pix"
                  )}
                </Button>
              ) : (
                <Button
                  type="button"
                  className="w-full"
                  onClick={() => {
                    const amount = effectiveAmount;
                    onAddPayment(method, amount);
                    reset();
                  }}
                  disabled={isLoading || effectiveAmount <= 0}
                >
                  {isLoading ? <LoadingSpinner size="sm" label="" /> : "Pagar"}
                </Button>
              )}
            </div>
          ) : (
            <div className="space-y-1">
              <Label htmlFor="payment-amount" className="text-xs">
                Valor (R$)
              </Label>
              <div className="flex gap-2">
                <div className="flex-1 space-y-1">
                  <Input
                    id="payment-amount"
                    {...register("amount")}
                    placeholder={
                      effectiveAmount > 0
                        ? formatCurrency(effectiveAmount).replace(
                            "R$\u00a0",
                            "",
                          )
                        : "0,00"
                    }
                    inputMode="decimal"
                    autoComplete="off"
                  />
                  {errors.amount && (
                    <p className="text-xs text-destructive">
                      {errors.amount.message}
                    </p>
                  )}
                </div>
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? <LoadingSpinner size="sm" label="" /> : "Pagar"}
                </Button>
              </div>
            </div>
          )}
        </form>
      )}

      {/* Complete button */}
      {isPaid && sale.state !== "COMPLETED" && (
        <Button
          className="w-full"
          size="lg"
          onClick={onCompleteSale}
          disabled={isLoading}
        >
          {isLoading ? (
            <LoadingSpinner size="sm" label="" />
          ) : (
            "Concluir Venda ✓"
          )}
        </Button>
      )}

      {error && <ErrorAlert error={error} className="mt-2" />}

      {/* MercadoPago PIX QR Code Dialog */}
      <Dialog open={showQrDialog} onOpenChange={setShowQrDialog}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <QrCode className="h-5 w-5" />
              Pagamento via Mercado Pago
            </DialogTitle>
            <DialogDescription>
              Escaneie o QR Code abaixo com o aplicativo do Mercado Pago para
              pagar o valor de{" "}
              <strong className="text-foreground">
                R$ {pendingPixAmount.toFixed(2).replace(".", ",")}
              </strong>
              .
            </DialogDescription>
          </DialogHeader>
          <div className="flex flex-col items-center justify-center p-6 bg-muted/30 rounded-lg border border-border/50">
            {qrData ? (
              <div className="bg-white p-4 rounded-xl shadow-sm">
                <QRCodeSVG value={qrData} size={200} level="H" />
              </div>
            ) : (
              <div className="flex flex-col items-center gap-2 text-muted-foreground p-8">
                <LoadingSpinner size="md" label="Processando..." />
              </div>
            )}
            <p className="mt-4 text-xs text-muted-foreground text-center">
              O modelo de QR Code configurado é dinâmico. O valor a ser cobrado
              será preenchido automaticamente ao escanear.
            </p>
          </div>
          <DialogFooter className="sm:justify-between">
            <Button variant="outline" onClick={() => setShowQrDialog(false)}>
              Cancelar ou Voltar
            </Button>
            <Button
              onClick={async () => {
                // Simulate that the payment was processed after they scanned
                setShowQrDialog(false);
                await onAddPayment("PIX", pendingPixAmount);
              }}
            >
              Simular Confirmação Webhook
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
