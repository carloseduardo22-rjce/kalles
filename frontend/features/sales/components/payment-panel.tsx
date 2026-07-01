"use client";

import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  CreditCard,
  Banknote,
  Smartphone,
  Landmark,
  QrCode,
  CheckCircle2,
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
import type { PaymentMethod, SaleResponse } from "../types";
import { getDefaultQrCodeProvider } from "@/features/payment/providers";

const amountSchema = z.object({
  amount: z
    .string()
    .min(1, "Informe o valor")
    .refine(
      (value) =>
        !isNaN(parseFloat(value.replace(",", "."))) &&
        parseFloat(value.replace(",", ".")) > 0,
      "Valor deve ser maior que zero",
    ),
});

type AmountForm = z.infer<typeof amountSchema>;

const ZERO_MONEY_VALUE = "0,00";

function formatCentsAsMoneyInput(cents: number): string {
  const normalizedCents = Math.max(0, cents);
  const reais = Math.floor(normalizedCents / 100);
  const centavos = normalizedCents % 100;

  return `${reais.toString()},${centavos.toString().padStart(2, "0")}`;
}

function formatMoneyInputFromText(value: string): string {
  const digits = value.replace(/\D/g, "");

  if (!digits) {
    return ZERO_MONEY_VALUE;
  }

  return formatCentsAsMoneyInput(Number(digits));
}

function parseMoneyInput(value: string): number {
  const cents = Number(value.replace(/\D/g, ""));
  return cents / 100;
}

function keepCaretAtEnd(input: HTMLInputElement) {
  window.requestAnimationFrame(() => {
    const end = input.value.length;
    input.setSelectionRange(end, end);
  });
}

interface PaymentPanelProps {
  sale: SaleResponse;
  isLoading: boolean;
  error: string | null;
  cashRegisterCode?: string;
  cashOnlyOperation?: boolean;
  onAddPayment: (method: PaymentMethod, amount: number) => Promise<void>;
  onCompleteSale: () => Promise<void>;
  onRefreshSale?: () => Promise<void>;
  onStartNewSale?: () => void;
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
    label: "Credito",
    icon: <CreditCard className="h-4 w-4" />,
  },
  {
    value: "DEBIT_CARD",
    label: "Debito",
    icon: <Landmark className="h-4 w-4" />,
  },
];

export function PaymentPanel({
  sale,
  isLoading,
  error,
  cashRegisterCode,
  cashOnlyOperation = false,
  onAddPayment,
  onCompleteSale,
  onRefreshSale,
  onStartNewSale,
}: PaymentPanelProps) {
  const [method, setMethod] = useState<PaymentMethod>("CASH");
  const pixProvider = getDefaultQrCodeProvider();
  const pixProviderName =
    pixProvider?.presentation.displayName ?? "provider configurado";

  const [qrData, setQrData] = useState<string | null>(null);
  const [showQrDialog, setShowQrDialog] = useState(false);
  const [isGeneratingQr, setIsGeneratingQr] = useState(false);
  const [pendingPixAmount, setPendingPixAmount] = useState<number>(0);
  const [pixPaymentStatus, setPixPaymentStatus] = useState<
    "pending" | "success"
  >("pending");

  useEffect(() => {
    if (cashOnlyOperation && method !== "CASH") {
      setMethod("CASH");
    }
  }, [cashOnlyOperation, method]);

  useEffect(() => {
    if (!showQrDialog || !onRefreshSale) return;

    const intervalId = setInterval(() => {
      onRefreshSale();
    }, 3000);

    return () => clearInterval(intervalId);
  }, [showQrDialog, onRefreshSale]);

  useEffect(() => {
    if (
      showQrDialog &&
      sale.state === "PAID" &&
      pixPaymentStatus === "pending"
    ) {
      setPixPaymentStatus("success");
    }
  }, [sale.state, showQrDialog, pixPaymentStatus]);

  useEffect(() => {
    let timeoutId: NodeJS.Timeout;
    if (pixPaymentStatus === "success" && showQrDialog) {
      timeoutId = setTimeout(async () => {
        setShowQrDialog(false);
        try {
          await onCompleteSale();
          onStartNewSale?.();
        } catch (err) {
          console.error(err);
        }
      }, 3000);
    }

    return () => {
      if (timeoutId) clearTimeout(timeoutId);
    };
  }, [pixPaymentStatus, showQrDialog, onCompleteSale, onStartNewSale]);

  useEffect(() => {
    if (
      showQrDialog &&
      sale.amountDue <= 0 &&
      sale.state !== "OPEN" &&
      sale.state !== "PAID"
    ) {
      setShowQrDialog(false);
    }
  }, [sale.state, sale.amountDue, showQrDialog]);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm<AmountForm>({
    resolver: zodResolver(amountSchema),
    defaultValues: {
      amount: ZERO_MONEY_VALUE,
    },
  });
  const amountValue = watch("amount");
  const amountField = register("amount");

  const isPaid = sale.state === "PAID";
  const canPay = sale.state === "OPEN" || sale.state === "PAYMENT_IN_PROGRESS";
  const effectiveAmount = sale.amountDue > 0 ? sale.amountDue : sale.total;
  const availablePaymentMethods = cashOnlyOperation
    ? PAYMENT_METHODS.filter((paymentMethod) => paymentMethod.value === "CASH")
    : PAYMENT_METHODS;

  async function handlePay(data: AmountForm) {
    const amount = parseMoneyInput(data.amount);
    await onAddPayment(method, amount);
    reset({ amount: ZERO_MONEY_VALUE });
  }

  function handleAmountChange(event: React.ChangeEvent<HTMLInputElement>) {
    const formattedValue = formatMoneyInputFromText(event.target.value);
    setValue("amount", formattedValue, {
      shouldDirty: true,
      shouldTouch: true,
      shouldValidate: true,
    });
    keepCaretAtEnd(event.target);
  }

  async function handlePixPayment(amount: number) {
    if (cashRegisterCode && pixProvider?.processQrPayment) {
      setPixPaymentStatus("pending");
      setIsGeneratingQr(true);
      try {
        const response = await pixProvider.processQrPayment(
          sale.sessionToken,
          amount,
          cashRegisterCode,
        );
        setQrData(response.qrData);
        setPendingPixAmount(amount);
        setShowQrDialog(true);
      } catch (err) {
        console.error("Failed to generate QR Code", err);
      } finally {
        setIsGeneratingQr(false);
        reset({ amount: ZERO_MONEY_VALUE });
      }
    } else {
      await onAddPayment("PIX", amount);
      reset({ amount: ZERO_MONEY_VALUE });
    }
  }

  return (
    <div className="space-y-4">
      {cashOnlyOperation && (
        <div
          data-testid="cash-only-banner"
          className="rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-900"
        >
          Esta sessao esta operando em modo somente dinheiro. PIX, vouchers e
          cartoes ficam indisponiveis ate a abertura de uma nova sessao com
          pagamento configurado.
        </div>
      )}

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
            <span>Pagamento completo</span>
          </div>
        )}
      </div>

      {sale.payments.length > 0 && (
        <div className="space-y-1">
          <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
            Pagamentos
          </p>
          {sale.payments.map((payment) => (
            <div
              key={payment.id}
              className="flex justify-between text-xs text-muted-foreground"
            >
              <span>{formatPaymentMethod(payment.method)}</span>
              <span>
                {formatCurrency(payment.amount)}
                {payment.changeAmount > 0 && (
                  <span className="ml-1 text-amber-600">
                    (troco: {formatCurrency(payment.changeAmount)})
                  </span>
                )}
              </span>
            </div>
          ))}
        </div>
      )}

      <Separator />

      {canPay && (
        <form onSubmit={handleSubmit(handlePay)} className="space-y-3">
          <div className="grid grid-cols-4 gap-1.5">
            {availablePaymentMethods.map((paymentMethod) => (
              <Button
                key={paymentMethod.value}
                data-testid={`payment-method-${paymentMethod.value.toLowerCase()}`}
                type="button"
                variant={method === paymentMethod.value ? "default" : "outline"}
                size="sm"
                className="flex h-auto flex-col gap-1 py-2 text-xs"
                onClick={() => setMethod(paymentMethod.value)}
              >
                {paymentMethod.icon}
                {paymentMethod.label}
              </Button>
            ))}
          </div>

          {method !== "CASH" ? (
            <div className="space-y-3 pt-2">
              <div className="flex items-center justify-between rounded-lg border bg-muted/60 p-3 text-sm">
                <span className="text-muted-foreground">
                  Valor via {formatPaymentMethod(method)}
                </span>
                <span className="text-base font-semibold">
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
                    onAddPayment(method, effectiveAmount);
                    reset({ amount: ZERO_MONEY_VALUE });
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
                    data-testid="payment-amount-input"
                    id="payment-amount"
                    name={amountField.name}
                    ref={amountField.ref}
                    onBlur={amountField.onBlur}
                    value={amountValue}
                    onChange={handleAmountChange}
                    onFocus={(event) => keepCaretAtEnd(event.target)}
                    onClick={(event) => keepCaretAtEnd(event.currentTarget)}
                    inputMode="numeric"
                    autoComplete="off"
                  />
                  {errors.amount && (
                    <p className="text-xs text-destructive">
                      {errors.amount.message}
                    </p>
                  )}
                </div>
                <Button
                  data-testid="payment-submit-cash"
                  type="submit"
                  disabled={isLoading}
                >
                  {isLoading ? <LoadingSpinner size="sm" label="" /> : "Pagar"}
                </Button>
              </div>
            </div>
          )}
        </form>
      )}

      {isPaid && sale.state !== "COMPLETED" && (
        <Button
          data-testid="complete-sale"
          className="w-full"
          size="lg"
          onClick={onCompleteSale}
          disabled={isLoading}
        >
          {isLoading ? <LoadingSpinner size="sm" label="" /> : "Concluir venda"}
        </Button>
      )}

      {error && <ErrorAlert error={error} className="mt-2" />}

      <Dialog open={showQrDialog} onOpenChange={setShowQrDialog}>
        <DialogContent
          className={`overflow-hidden transition-all duration-500 sm:max-w-md ${
            pixPaymentStatus === "success"
              ? "border-green-200 bg-green-50/50 dark:border-green-900 dark:bg-green-950/10"
              : ""
          }`}
        >
          {pixPaymentStatus === "pending" && (
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <QrCode className="h-5 w-5" />
                Pagamento via {pixProviderName}
              </DialogTitle>
              <DialogDescription>
                Escaneie o QR Code abaixo com o aplicativo do {pixProviderName}
                para pagar o valor de{" "}
                <strong className="text-foreground">
                  R$ {pendingPixAmount.toFixed(2).replace(".", ",")}
                </strong>
                .
              </DialogDescription>
            </DialogHeader>
          )}

          {pixPaymentStatus === "success" ? (
            <div className="animate-in zoom-in-95 flex min-h-[300px] flex-col items-center justify-center p-12 duration-500">
              <div className="relative mb-6">
                <div className="absolute inset-0 animate-ping rounded-full bg-green-400 opacity-20 duration-1000" />
                <div className="relative rounded-full bg-green-500 p-4 text-white shadow-lg shadow-green-500/30">
                  <CheckCircle2
                    className="animate-in fade-in zoom-in h-12 w-12 fill-none duration-500 delay-150"
                    strokeWidth={2.5}
                  />
                </div>
              </div>
              <div className="space-y-2 text-center">
                <h3 className="animate-in slide-in-from-bottom-2 text-2xl font-bold tracking-tight text-green-700 duration-500 delay-200 dark:text-green-500">
                  Pagamento confirmado!
                </h3>
                <p className="animate-in slide-in-from-bottom-2 font-medium text-green-600/80 duration-500 delay-300 dark:text-green-400/80">
                  Valor de {formatCurrency(pendingPixAmount)} recebido
                </p>
                <div className="animate-in fade-in mt-6 flex items-center justify-center gap-2 pt-4 text-sm text-muted-foreground duration-500 delay-500">
                  <LoadingSpinner size="sm" label="" />
                  Concluindo a venda e preparando o proximo caixa...
                </div>
              </div>
            </div>
          ) : (
            <div className="flex min-h-[300px] flex-col items-center justify-center rounded-lg border border-border/50 bg-muted/30 p-6">
              {qrData ? (
                <div className="rounded-xl bg-white p-4 shadow-sm">
                  <QRCodeSVG value={qrData} size={200} level="H" />
                </div>
              ) : (
                <div className="flex flex-col items-center gap-2 p-8 text-muted-foreground">
                  <LoadingSpinner size="md" label="Gerando QR Code Pix..." />
                </div>
              )}
              <p className="mt-4 text-center text-xs text-muted-foreground">
                O modelo de QR Code configurado e dinamico. O valor a ser
                cobrado sera preenchido automaticamente ao escanear.
              </p>
            </div>
          )}

          {pixPaymentStatus === "pending" && (
            <DialogFooter className="sm:justify-between">
              <Button variant="outline" onClick={() => setShowQrDialog(false)}>
                Cancelar ou voltar
              </Button>
              <Button
                onClick={async () => {
                  setShowQrDialog(false);
                  await onAddPayment("PIX", pendingPixAmount);
                }}
              >
                Simular confirmacao webhook
              </Button>
            </DialogFooter>
          )}
        </DialogContent>
      </Dialog>

    </div>
  );
}
