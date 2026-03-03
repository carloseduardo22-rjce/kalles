"use client"

import { useState } from "react"
import { DollarSign, Lock } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import type { CashSession, Sale, CashClosingSummary } from "@/lib/types"

interface CashClosingDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  session: CashSession
  sales: Sale[]
  onCloseSession: (summary: CashClosingSummary) => void
}

export function CashClosingDialog({ open, onOpenChange, session, sales, onCloseSession }: CashClosingDialogProps) {
  const [actualCash, setActualCash] = useState("")

  const completedSales = sales.filter((s) => s.status === "completed")
  const totalSales = completedSales.reduce((sum, sale) => sum + sale.total, 0)
  const salesCount = completedSales.length

  const cashPayments = completedSales.reduce((sum, sale) => {
    if (!sale.payment) return sum
    const cashEntries = sale.payment.methods.filter((m) => m.method === "cash")
    return sum + cashEntries.reduce((s, e) => s + e.amount, 0)
  }, 0)

  const cardPayments = completedSales.reduce((sum, sale) => {
    if (!sale.payment) return sum
    const cardEntries = sale.payment.methods.filter((m) => m.method !== "cash")
    return sum + cardEntries.reduce((s, e) => s + e.amount, 0)
  }, 0)

  const cashGiven = completedSales.reduce((sum, sale) => {
    if (!sale.payment) return sum
    return sum + sale.payment.change
  }, 0)

  const expectedCash = session.initialCash + cashPayments - cashGiven

  const actualCashValue = Number.parseFloat(actualCash) || 0
  const difference = actualCashValue - expectedCash

  const handleClose = () => {
    const summary: CashClosingSummary = {
      sessionId: session.id,
      operatorId: session.operatorId,
      operatorName: session.operatorName,
      openedAt: session.openedAt,
      closedAt: new Date().toISOString(),
      initialCash: session.initialCash,
      totalSales,
      salesCount,
      cashPayments,
      cardPayments,
      expectedCash,
      actualCash: actualCashValue,
      difference,
    }

    onCloseSession(summary)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle className="text-2xl flex items-center gap-2">
            <Lock className="size-6" />
            Fechamento de Caixa
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-6">
          {/* Session info */}
          <div className="bg-muted rounded-lg p-4 space-y-2">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Operador</span>
              <span className="font-semibold">
                {session.operatorName} ({session.operatorId})
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Abertura</span>
              <span className="font-semibold">{new Date(session.openedAt).toLocaleString("pt-BR")}</span>
            </div>
          </div>

          {/* Sales summary */}
          <div className="space-y-3">
            <Label className="text-lg font-semibold">Resumo de Vendas</Label>
            <div className="grid grid-cols-2 gap-3">
              <div className="border rounded-lg p-4">
                <p className="text-sm text-muted-foreground mb-1">Quantidade de Vendas</p>
                <p className="text-3xl font-bold">{salesCount}</p>
              </div>
              <div className="border rounded-lg p-4">
                <p className="text-sm text-muted-foreground mb-1">Total de Vendas</p>
                <p className="text-3xl font-bold text-emerald-600">R$ {totalSales.toFixed(2)}</p>
              </div>
            </div>
          </div>

          {/* Cash calculation */}
          <div className="space-y-3">
            <Label className="text-lg font-semibold">Cálculo de Caixa</Label>
            <div className="border rounded-lg p-4 space-y-2">
              <div className="flex justify-between">
                <span>Valor Inicial</span>
                <span className="font-semibold">R$ {session.initialCash.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-emerald-600">
                <span>+ Pagamentos em Dinheiro</span>
                <span className="font-semibold">R$ {cashPayments.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-destructive">
                <span>- Troco Dado</span>
                <span className="font-semibold">R$ {cashGiven.toFixed(2)}</span>
              </div>
              <div className="flex justify-between pt-2 border-t text-lg">
                <span className="font-bold">Valor Esperado em Caixa</span>
                <span className="font-bold">R$ {expectedCash.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-muted-foreground text-sm">
                <span>Pagamentos em Cartão/PIX</span>
                <span>R$ {cardPayments.toFixed(2)}</span>
              </div>
            </div>
          </div>

          {/* Actual cash input */}
          <div className="space-y-3">
            <Label htmlFor="actualCash" className="text-lg font-semibold">
              Valor Real em Caixa
            </Label>
            <div className="relative">
              <DollarSign className="text-muted-foreground absolute top-1/2 left-4 size-6 -translate-y-1/2" />
              <Input
                id="actualCash"
                type="text"
                placeholder="0.00"
                value={actualCash}
                onChange={(e) => setActualCash(e.target.value)}
                className="pl-14 h-14 text-2xl font-semibold"
              />
            </div>
          </div>

          {/* Difference */}
          {actualCash && (
            <div
              className={`rounded-lg p-4 ${difference === 0 ? "bg-emerald-50 border border-emerald-200" : difference > 0 ? "bg-blue-50 border border-blue-200" : "bg-red-50 border border-red-200"}`}
            >
              <div className="flex justify-between items-center">
                <span className="text-lg font-semibold">
                  {difference === 0 ? "Caixa Bateu" : difference > 0 ? "Sobra" : "Falta"}
                </span>
                <span
                  className={`text-3xl font-bold ${difference === 0 ? "text-emerald-600" : difference > 0 ? "text-blue-600" : "text-red-600"}`}
                >
                  R$ {Math.abs(difference).toFixed(2)}
                </span>
              </div>
            </div>
          )}
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            Cancelar
          </Button>
          <Button type="button" onClick={handleClose} disabled={!actualCash} size="lg" className="text-lg px-8">
            <Lock className="mr-2 size-5" />
            Fechar Caixa
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
