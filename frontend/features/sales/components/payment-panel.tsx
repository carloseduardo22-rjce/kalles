"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { CreditCard, Banknote, Smartphone, Landmark } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ErrorAlert } from "@/shared/components/error-alert";
import { formatCurrency, formatPaymentMethod } from "@/shared/utils/formatters";
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
  onAddPayment,
  onCompleteSale,
}: PaymentPanelProps) {
  const [method, setMethod] = useState<PaymentMethod>("CASH");
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
        {canPay && sale.amountDue > 0 && (
          <div className="flex justify-between text-base font-bold text-destructive">
            <span>A pagar</span>
            <span>{formatCurrency(sale.amountDue)}</span>
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

          {/* Amount */}
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
                    sale.amountDue > 0
                      ? formatCurrency(sale.amountDue).replace("R$\u00a0", "")
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
    </div>
  );
}
