"use client"
import { useForm } from "react-hook-form"
import { CheckCircle2, DollarSign, User, Lock, Unlock } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import type { CashSession } from "@/lib/types"

interface FormData {
  operatorId: string
  initialCash: string
}

interface CashRegisterOpenProps {
  onSessionOpen: (session: CashSession) => void
  activeSession: CashSession | null
  onCloseSession: () => void
}

const MOCK_OPERATORS = [
  { id: "OP001", name: "João Silva" },
  { id: "OP002", name: "Maria Santos" },
  { id: "OP003", name: "Pedro Costa" },
  { id: "OP004", name: "Ana Oliveira" },
]

export function CashRegisterOpen({ onSessionOpen, activeSession, onCloseSession }: CashRegisterOpenProps) {
  const form = useForm<FormData>({
    defaultValues: {
      operatorId: "",
      initialCash: "",
    },
  })

  const onSubmit = (data: FormData) => {
    const operator = MOCK_OPERATORS.find((op) => op.id === data.operatorId)

    const session: CashSession = {
      id: `SESSION-${Date.now()}`,
      operatorId: data.operatorId,
      operatorName: operator?.name || "Unknown",
      initialCash: Number.parseFloat(data.initialCash),
      openedAt: new Date().toISOString(),
      status: "open",
    }

    onSessionOpen(session)
  }

  const handleCloseSession = () => {
    onCloseSession()
    form.reset()
  }

  // Active session view - minimal and clear
  if (activeSession) {
    return (
      <Card className="border-2 border-emerald-500 shadow-2xl">
        <CardHeader className="bg-emerald-500 text-white pb-8 pt-8">
          <div className="flex items-center justify-center gap-3">
            <Unlock className="size-10" />
            <CardTitle className="text-3xl">Caixa Aberto</CardTitle>
          </div>
        </CardHeader>
        <CardContent className="pt-8 pb-8">
          <div className="space-y-6">
            <div className="bg-muted rounded-xl p-6 space-y-4">
              <div className="text-center border-b border-border pb-4">
                <p className="text-sm text-muted-foreground mb-1">Operador</p>
                <p className="text-2xl font-bold">{activeSession.operatorName}</p>
                <p className="text-sm text-muted-foreground font-mono">{activeSession.operatorId}</p>
              </div>

              <div className="text-center border-b border-border pb-4">
                <p className="text-sm text-muted-foreground mb-1">Valor Inicial</p>
                <p className="text-4xl font-bold text-emerald-600">R$ {activeSession.initialCash.toFixed(2)}</p>
              </div>

              <div className="text-center">
                <p className="text-sm text-muted-foreground mb-1">Abertura</p>
                <p className="text-xl font-semibold">{new Date(activeSession.openedAt).toLocaleTimeString("pt-BR")}</p>
                <p className="text-xs text-muted-foreground mt-1">
                  {new Date(activeSession.openedAt).toLocaleDateString("pt-BR")}
                </p>
              </div>
            </div>

            <Button onClick={handleCloseSession} variant="destructive" size="lg" className="w-full h-14 text-lg">
              <Lock className="mr-2 size-5" />
              Fechar Caixa
            </Button>
          </div>
        </CardContent>
      </Card>
    )
  }

  // Opening session form - large and easy to use
  return (
    <Card className="shadow-2xl">
      <CardHeader className="bg-primary text-primary-foreground pb-8 pt-8">
        <div className="flex items-center justify-center gap-3">
          <Lock className="size-10" />
          <CardTitle className="text-3xl">Abertura de Caixa</CardTitle>
        </div>
      </CardHeader>
      <CardContent className="pt-8 pb-8">
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
            <FormField
              control={form.control}
              name="operatorId"
              rules={{
                required: "Selecione o operador",
              }}
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="text-lg">Operador</FormLabel>
                  <Select onValueChange={field.onChange} defaultValue={field.value}>
                    <FormControl>
                      <SelectTrigger className="w-full h-14 text-lg">
                        <SelectValue placeholder="Selecione seu código" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {MOCK_OPERATORS.map((operator) => (
                        <SelectItem key={operator.id} value={operator.id} className="text-lg py-3">
                          <div className="flex items-center gap-3">
                            <User className="size-5" />
                            <span>
                              {operator.id} - {operator.name}
                            </span>
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormDescription className="text-base">Selecione seu código de operador</FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="initialCash"
              rules={{
                required: "Informe o valor inicial",
                pattern: {
                  value: /^\d+(\.\d{1,2})?$/,
                  message: "Digite um valor válido (ex: 100.00)",
                },
                validate: (value) => {
                  const num = Number.parseFloat(value)
                  if (num < 0) return "Valor não pode ser negativo"
                  if (num > 50000) return "Valor não pode exceder R$ 50.000"
                  return true
                },
              }}
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="text-lg">Valor Inicial (R$)</FormLabel>
                  <FormControl>
                    <div className="relative">
                      <DollarSign className="text-muted-foreground absolute top-1/2 left-4 size-6 -translate-y-1/2" />
                      <Input type="text" placeholder="0.00" className="pl-14 h-14 text-2xl font-semibold" {...field} />
                    </div>
                  </FormControl>
                  <FormDescription className="text-base">Informe o valor em dinheiro no caixa</FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />

            <Button type="submit" size="lg" className="w-full h-16 text-xl">
              <CheckCircle2 className="mr-2 size-6" />
              Abrir Caixa
            </Button>
          </form>
        </Form>
      </CardContent>
    </Card>
  )
}
