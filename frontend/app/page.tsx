"use client"

import { useState } from "react"
import { CashRegisterOpen } from "@/components/cash-register-open"
import { PdvSales } from "@/components/pdv-sales"
import { CashClosingDialog } from "@/components/cash-closing-dialog"
import type { CashSession, Sale, CashClosingSummary } from "@/lib/types"

export default function HomePage() {
  const [activeSession, setActiveSession] = useState<CashSession | null>(null)
  const [sales, setSales] = useState<Sale[]>([])
  const [closingDialogOpen, setClosingDialogOpen] = useState(false)
  const [closingSummary, setClosingSummary] = useState<CashClosingSummary | null>(null)

  const handleSessionOpen = (session: CashSession) => {
    setActiveSession(session)
    setSales([])
  }

  const handleCloseSessionRequest = () => {
    setClosingDialogOpen(true)
  }

  const handleCloseSession = (summary: CashClosingSummary) => {
    setClosingSummary(summary)
    setClosingDialogOpen(false)
    setActiveSession(null)
    setSales([])
  }

  const handleSaleComplete = (sale: Sale) => {
    setSales((prev) => [...prev, sale])
  }

  if (activeSession) {
    return (
      <>
        <PdvSales session={activeSession} onSaleComplete={handleSaleComplete} />
        {closingDialogOpen && (
          <CashClosingDialog
            open={closingDialogOpen}
            onOpenChange={setClosingDialogOpen}
            session={activeSession}
            sales={sales}
            onCloseSession={handleCloseSession}
          />
        )}
      </>
    )
  }

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-4">
      <div className="w-full max-w-2xl">
        <CashRegisterOpen
          onSessionOpen={handleSessionOpen}
          activeSession={activeSession}
          onCloseSession={handleCloseSessionRequest}
        />
        {closingSummary && (
          <div className="mt-6 p-6 bg-muted rounded-lg border">
            <h3 className="text-lg font-bold mb-4">Último Fechamento</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Operador</span>
                <span className="font-semibold">{closingSummary.operatorName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Vendas Realizadas</span>
                <span className="font-semibold">{closingSummary.salesCount}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Total de Vendas</span>
                <span className="font-semibold">R$ {closingSummary.totalSales.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Valor Esperado</span>
                <span className="font-semibold">R$ {closingSummary.expectedCash.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Valor Real</span>
                <span className="font-semibold">R$ {closingSummary.actualCash.toFixed(2)}</span>
              </div>
              <div className="flex justify-between pt-2 border-t">
                <span className="font-bold">
                  {closingSummary.difference === 0 ? "Caixa Bateu" : closingSummary.difference > 0 ? "Sobra" : "Falta"}
                </span>
                <span
                  className={`font-bold ${closingSummary.difference === 0 ? "text-emerald-600" : closingSummary.difference > 0 ? "text-blue-600" : "text-red-600"}`}
                >
                  R$ {Math.abs(closingSummary.difference).toFixed(2)}
                </span>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
