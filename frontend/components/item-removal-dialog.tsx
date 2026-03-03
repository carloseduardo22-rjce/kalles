"use client"

import { useState } from "react"
import { AlertTriangle, Plus, Minus } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import type { SaleItem, ItemRemovalLog } from "@/lib/types"
import { validateSupervisor, MOCK_SUPERVISORS } from "@/lib/mock-supervisors"

interface ItemRemovalDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  item: SaleItem
  currentOperatorId: string
  currentOperatorName: string
  saleId: string
  onRemove: (log: ItemRemovalLog) => void
}

export function ItemRemovalDialog({
  open,
  onOpenChange,
  item,
  currentOperatorId,
  currentOperatorName,
  saleId,
  onRemove,
}: ItemRemovalDialogProps) {
  const [quantityToRemove, setQuantityToRemove] = useState(item.quantity)
  const [supervisorId, setSupervisorId] = useState("")
  const [password, setPassword] = useState("")
  const [reason, setReason] = useState("")
  const [error, setError] = useState("")
  const [isValidating, setIsValidating] = useState(false)

  const resetForm = () => {
    setQuantityToRemove(item.quantity)
    setSupervisorId("")
    setPassword("")
    setReason("")
    setError("")
  }

  const handleClose = () => {
    resetForm()
    onOpenChange(false)
  }

  const incrementQuantity = () => {
    setQuantityToRemove((prev) => Math.min(prev + 1, item.quantity))
  }

  const decrementQuantity = () => {
    setQuantityToRemove((prev) => Math.max(prev - 1, 1))
  }

  const handleAuthorize = () => {
    setError("")

    if (quantityToRemove <= 0 || quantityToRemove > item.quantity) {
      setError("Quantidade inválida")
      return
    }

    if (!supervisorId.trim()) {
      setError("Digite o código do gerente/fiscal")
      return
    }

    if (!password.trim()) {
      setError("Digite a senha")
      return
    }

    setIsValidating(true)

    // Simulate async validation
    setTimeout(() => {
      const authorizer = validateSupervisor(supervisorId, password)

      if (!authorizer) {
        setError("Código ou senha inválidos")
        setIsValidating(false)
        return
      }

      // Create removal log
      const removalLog: ItemRemovalLog = {
        id: `REMOVAL-${Date.now()}`,
        saleId,
        item,
        removedQuantity: quantityToRemove,
        requestedBy: currentOperatorId,
        requestedByName: currentOperatorName,
        authorizedBy: authorizer.id,
        authorizedByName: authorizer.name,
        timestamp: new Date().toISOString(),
        reason: reason.trim() || undefined,
      }

      onRemove(removalLog)
      handleClose()
      setIsValidating(false)
    }, 500)
  }

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-destructive">
            <AlertTriangle className="size-5" />
            Autorização Necessária
          </DialogTitle>
          <DialogDescription>
            Para remover item(ns), é necessária a autorização de um gerente ou fiscal.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          {/* Item info */}
          <div className="p-3 bg-muted rounded-md space-y-2">
            <p className="text-sm font-medium">Item a ser removido:</p>
            <p className="font-semibold">{item.product.description}</p>
            <div className="flex justify-between text-sm">
              <span>Quantidade disponível: {item.quantity}</span>
              <span className="font-semibold">Preço unit.: R$ {item.unitPrice.toFixed(2)}</span>
            </div>

            <div className="pt-2 border-t">
              <Label className="text-sm mb-2 block">Quantidade a remover:</Label>
              <div className="flex items-center justify-center gap-3">
                <Button
                  size="sm"
                  variant="outline"
                  className="size-10 p-0 bg-transparent"
                  onClick={decrementQuantity}
                  disabled={quantityToRemove <= 1 || isValidating}
                >
                  <Minus className="size-4" />
                </Button>
                <span className="text-2xl font-bold w-16 text-center">{quantityToRemove}</span>
                <Button
                  size="sm"
                  variant="outline"
                  className="size-10 p-0 bg-transparent"
                  onClick={incrementQuantity}
                  disabled={quantityToRemove >= item.quantity || isValidating}
                >
                  <Plus className="size-4" />
                </Button>
              </div>
              <p className="text-center text-sm text-muted-foreground mt-2">
                Valor a remover: R$ {(quantityToRemove * item.unitPrice).toFixed(2)}
              </p>
            </div>
          </div>

          {/* Operator info */}
          <div className="text-sm text-muted-foreground">
            <p>
              Solicitado por: <span className="font-medium text-foreground">{currentOperatorName}</span> (
              {currentOperatorId})
            </p>
          </div>

          {/* Authorization form */}
          <div className="space-y-3 pt-2">
            <div className="space-y-2">
              <Label htmlFor="supervisor-id">Código do Gerente/Fiscal</Label>
              <Input
                id="supervisor-id"
                placeholder="Digite o código"
                value={supervisorId}
                onChange={(e) => setSupervisorId(e.target.value)}
                disabled={isValidating}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Senha</Label>
              <Input
                id="password"
                type="password"
                placeholder="Digite a senha"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={isValidating}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="reason">Motivo da Remoção (opcional)</Label>
              <Textarea
                id="reason"
                placeholder="Descreva o motivo da remoção"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                disabled={isValidating}
                rows={3}
              />
            </div>

            {/* Supervisors reference */}
            <details className="text-xs">
              <summary className="cursor-pointer text-muted-foreground hover:text-foreground">
                Ver gerentes/fiscais disponíveis
              </summary>
              <div className="mt-2 space-y-1 pl-3">
                {MOCK_SUPERVISORS.map((sup) => (
                  <div key={sup.id} className="flex justify-between">
                    <span className="font-mono">{sup.id}</span>
                    <span className="text-muted-foreground">{sup.name}</span>
                  </div>
                ))}
              </div>
            </details>
          </div>

          {error && (
            <div className="p-3 bg-destructive/10 text-destructive rounded-md text-sm font-medium flex items-center gap-2">
              <AlertTriangle className="size-4" />
              {error}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={handleClose} disabled={isValidating}>
            Cancelar
          </Button>
          <Button onClick={handleAuthorize} disabled={isValidating} variant="destructive">
            {isValidating ? "Validando..." : "Autorizar Remoção"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
