"use client"

import { useState } from "react"
import { CreditCard, Banknote, Smartphone, Check } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import type { Payment, PaymentMethod, PaymentMethodEntry } from "@/lib/types"

interface PaymentDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  totalDue: number
  onPaymentComplete: (payment: Payment) => void
}

const PAYMENT_METHODS: { method: PaymentMethod; label: string; icon: typeof CreditCard }[] = [
  { method: "cash", label: "Dinheiro", icon: Banknote },
  { method: "debit", label: "Débito", icon: CreditCard },
  { method: "credit", label: "Crédito", icon: CreditCard },
  { method: "pix", label: "PIX", icon: Smartphone },
]

export function PaymentDialog({ open, onOpenChange, totalDue, onPaymentComplete }: PaymentDialogProps) {
  const [payments, setPayments] = useState<PaymentMethodEntry[]>([])
  const [selectedMethod, setSelectedMethod] = useState<PaymentMethod | null>(null)
  const [amount, setAmount] = useState("")

  const totalPaid = payments.reduce((sum, p) => sum + p.amount, 0)
  const remaining = Math.max(0, totalDue - totalPaid)
  const change = Math.max(0, totalPaid - totalDue)
  const canComplete = totalPaid >= totalDue

  const handleAddPayment = () => {
    if (!selectedMethod || !amount) return

    const amountValue = Number.parseFloat(amount)
    if (Number.isNaN(amountValue) || amountValue <= 0) return

    setPayments([...payments, { method: selectedMethod, amount: amountValue }])
    setAmount("")
    setSelectedMethod(null)
  }

  const handleRemovePayment = (index: number) => {
    setPayments(payments.filter((_, i) => i !== index))
  }

  const handleComplete = () => {
    const payment: Payment = {
      methods: payments,
      totalPaid,
      totalDue,
      change,
      status: "approved",
      timestamp: new Date().toISOString(),
    }
    onPaymentComplete(payment)

    // Reset state
    setPayments([])
    setSelectedMethod(null)
    setAmount("")
  }

  const handleQuickCash = () => {
    setSelectedMethod("cash")
    setAmount(totalDue.toFixed(2))
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle className="text-2xl">Finalizar Pagamento</DialogTitle>
        </DialogHeader>

        <div className="space-y-6">
          {/* Total due */}
          <div className="bg-primary text-primary-foreground rounded-lg p-6">
            <div className="flex justify-between items-center">
              <span className="text-xl">Total a Pagar</span>
              <span className="text-4xl font-bold">R$ {totalDue.toFixed(2)}</span>
            </div>
          </div>

          {/* Payment method selection */}
          <div className="space-y-3">
            <Label className="text-base">Selecione a Forma de Pagamento</Label>
            <div className="grid grid-cols-4 gap-2">
              {PAYMENT_METHODS.map(({ method, label, icon: Icon }) => (
                <Button
                  key={method}
                  type="button"
                  variant={selectedMethod === method ? "default" : "outline"}
                  className="h-20 flex-col gap-2"
                  onClick={() => setSelectedMethod(method)}
                >
                  <Icon className="size-6" />
                  <span>{label}</span>
                </Button>
              ))}
            </div>
          </div>

          {/* Amount input */}
          <div className="space-y-3">
            <Label htmlFor="amount" className="text-base">
              Valor
            </Label>
            <div className="flex gap-2">
              <Input
                id="amount"
                type="text"
                placeholder="0.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="h-12 text-xl"
                disabled={!selectedMethod}
              />
              <Button type="button" onClick={handleAddPayment} disabled={!selectedMethod || !amount} className="h-12">
                Adicionar
              </Button>
              {selectedMethod === "cash" && payments.length === 0 && (
                <Button type="button" onClick={handleQuickCash} variant="secondary" className="h-12">
                  Valor Exato
                </Button>
              )}
            </div>
          </div>

          {/* Payment list */}
          {payments.length > 0 && (
            <div className="space-y-2 border rounded-lg p-4">
              <Label className="text-base">Pagamentos</Label>
              {payments.map((payment, index) => (
                <div key={index} className="flex items-center justify-between bg-muted p-3 rounded">
                  <div className="flex items-center gap-3">
                    <span className="font-medium">
                      {PAYMENT_METHODS.find((m) => m.method === payment.method)?.label}
                    </span>
                    <span className="text-xl font-bold">R$ {payment.amount.toFixed(2)}</span>
                  </div>
                  <Button type="button" variant="ghost" size="sm" onClick={() => handleRemovePayment(index)}>
                    Remover
                  </Button>
                </div>
              ))}
            </div>
          )}

          {/* Summary */}
          <div className="space-y-2 border-t pt-4">
            <div className="flex justify-between text-lg">
              <span>Total Pago</span>
              <span className="font-bold">R$ {totalPaid.toFixed(2)}</span>
            </div>
            {remaining > 0 && (
              <div className="flex justify-between text-lg text-destructive">
                <span>Faltam</span>
                <span className="font-bold">R$ {remaining.toFixed(2)}</span>
              </div>
            )}
            {change > 0 && (
              <div className="flex justify-between text-xl text-emerald-600">
                <span className="font-bold">Troco</span>
                <span className="font-bold">R$ {change.toFixed(2)}</span>
              </div>
            )}
          </div>
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            Cancelar
          </Button>
          <Button type="button" onClick={handleComplete} disabled={!canComplete} size="lg" className="text-lg px-8">
            <Check className="mr-2 size-5" />
            Confirmar Pagamento
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
