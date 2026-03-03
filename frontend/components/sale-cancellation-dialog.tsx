"use client"

import type React from "react"

import { useState } from "react"
import { XCircle, AlertTriangle } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import type { SaleCancellationLog } from "@/lib/types"
import { MOCK_SUPERVISORS } from "@/lib/mock-supervisors"
import { MOCK_OPERATORS } from "@/lib/mock-products"

interface SaleCancellationDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  saleId: string
  currentOperatorId: string
  currentOperatorName: string
  onCancel: (log: SaleCancellationLog) => void
}

export function SaleCancellationDialog({
  open,
  onOpenChange,
  saleId,
  currentOperatorId,
  currentOperatorName,
  onCancel,
}: SaleCancellationDialogProps) {
  const [supervisorCode, setSupervisorCode] = useState("")
  const [supervisorPassword, setSupervisorPassword] = useState("")
  const [reason, setReason] = useState("")
  const [error, setError] = useState("")
  const [step, setStep] = useState<"check-permission" | "authorization">("check-permission")

  // Check if current operator can cancel sales without authorization
  const currentOperator = MOCK_OPERATORS.find((op) => op.id === currentOperatorId)
  const canCancelWithoutAuth = currentOperator?.canCancelSale || false

  const handleCheckPermission = () => {
    if (canCancelWithoutAuth) {
      // Operator has permission, cancel directly
      const log: SaleCancellationLog = {
        id: `CANCEL-${Date.now()}`,
        saleId,
        requestedBy: currentOperatorId,
        requestedByName: currentOperatorName,
        timestamp: new Date().toISOString(),
        reason: reason || undefined,
      }
      onCancel(log)
      handleClose()
    } else {
      // Need authorization
      setStep("authorization")
    }
  }

  const handleAuthorizationSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setError("")

    if (!supervisorCode || !supervisorPassword) {
      setError("Preencha todos os campos obrigatórios")
      return
    }

    const supervisor = MOCK_SUPERVISORS.find(
      (s) => s.code === supervisorCode && s.password === supervisorPassword && s.canCancelSale,
    )

    if (!supervisor) {
      setError("Código ou senha inválidos, ou supervisor não autorizado para cancelar vendas")
      return
    }

    const log: SaleCancellationLog = {
      id: `CANCEL-${Date.now()}`,
      saleId,
      requestedBy: currentOperatorId,
      requestedByName: currentOperatorName,
      authorizedBy: supervisor.code,
      authorizedByName: supervisor.name,
      timestamp: new Date().toISOString(),
      reason: reason || undefined,
    }

    onCancel(log)
    handleClose()
  }

  const handleClose = () => {
    setSupervisorCode("")
    setSupervisorPassword("")
    setReason("")
    setError("")
    setStep("check-permission")
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-destructive">
            <XCircle className="size-5" />
            Cancelar Venda
          </DialogTitle>
          <DialogDescription>
            {step === "check-permission"
              ? "Você está prestes a cancelar esta venda. Esta ação não pode ser desfeita."
              : "Autorização de gerente ou fiscal necessária para cancelar a venda."}
          </DialogDescription>
        </DialogHeader>

        {step === "check-permission" ? (
          <div className="space-y-4">
            <div className="bg-destructive/10 border border-destructive/20 rounded-md p-4 flex items-start gap-3">
              <AlertTriangle className="size-5 text-destructive flex-shrink-0 mt-0.5" />
              <div className="text-sm text-destructive">
                <p className="font-semibold">Atenção!</p>
                <p>A venda será cancelada e marcada como inválida no sistema.</p>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="reason">Motivo do Cancelamento (Opcional)</Label>
              <Textarea
                id="reason"
                placeholder="Descreva o motivo do cancelamento..."
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                rows={3}
              />
            </div>

            <div className="flex gap-2 pt-2">
              <Button variant="outline" onClick={handleClose} className="flex-1 bg-transparent">
                Voltar
              </Button>
              <Button variant="destructive" onClick={handleCheckPermission} className="flex-1">
                Confirmar Cancelamento
              </Button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleAuthorizationSubmit} className="space-y-4">
            <div className="bg-muted/50 border rounded-md p-4 text-sm">
              <p>
                <span className="font-semibold">Solicitado por:</span> {currentOperatorName} ({currentOperatorId})
              </p>
              <p className="mt-1">
                <span className="font-semibold">Venda:</span> {saleId}
              </p>
              {reason && (
                <p className="mt-1">
                  <span className="font-semibold">Motivo:</span> {reason}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="supervisor-code">
                Código do Gerente/Fiscal <span className="text-destructive">*</span>
              </Label>
              <Input
                id="supervisor-code"
                type="text"
                placeholder="Digite o código"
                value={supervisorCode}
                onChange={(e) => setSupervisorCode(e.target.value)}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="supervisor-password">
                Senha <span className="text-destructive">*</span>
              </Label>
              <Input
                id="supervisor-password"
                type="password"
                placeholder="Digite a senha"
                value={supervisorPassword}
                onChange={(e) => setSupervisorPassword(e.target.value)}
                required
              />
            </div>

            {error && (
              <div className="bg-destructive/10 text-destructive text-sm p-3 rounded-md border border-destructive/20">
                {error}
              </div>
            )}

            <div className="text-xs text-muted-foreground bg-muted/30 p-3 rounded-md">
              <p className="font-semibold mb-1">Credenciais de teste:</p>
              <p>Gerente: GER001 / senha: gerente123</p>
              <p>Fiscal: FISC001 / senha: fiscal123</p>
            </div>

            <div className="flex gap-2 pt-2">
              <Button type="button" variant="outline" onClick={handleClose} className="flex-1 bg-transparent">
                Cancelar
              </Button>
              <Button type="submit" variant="destructive" className="flex-1">
                Autorizar Cancelamento
              </Button>
            </div>
          </form>
        )}
      </DialogContent>
    </Dialog>
  )
}
