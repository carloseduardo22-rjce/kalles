"use client";

import { useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Monitor,
  Users,
  AlertCircle,
  Loader2,
  LockOpen,
  Lock,
  CalendarClock,
  User,
  BadgeDollarSign,
  RefreshCw,
} from "lucide-react";
import { toast } from "sonner";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";

import { CreateCashRegisterDialog } from "@/features/cash-register/components/create-cash-register-dialog";
import { cashRegisterService } from "@/features/cash-register/services/cash-register.service";
import type {
  CashRegisterStatusResponse,
  OperatorResponse,
} from "@/features/cash-register/types";
import { formatCurrency } from "@/shared/utils/formatters";

/* ────────────────────────────────────────────────
   Helpers
──────────────────────────────────────────────── */
function formatDatetime(iso: string | null): string {
  if (!iso) return "—";
  return new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(iso));
}

/* ────────────────────────────────────────────────
   Sub-components
──────────────────────────────────────────────── */
function EmptyState() {
  return (
    <div className="col-span-full flex flex-col items-center justify-center py-24 text-center">
      <Monitor className="mb-4 size-16 text-muted-foreground/20" />
      <p className="text-lg font-semibold text-muted-foreground">
        Nenhum caixa encontrado
      </p>
      <p className="mt-1 max-w-sm text-sm text-muted-foreground/60">
        Verifique se há caixas cadastrados e ativos no sistema.
      </p>
    </div>
  );
}

function ErrorState({ refetch }: { refetch: () => void }) {
  return (
    <div className="col-span-full flex flex-col items-center justify-center py-24 text-center">
      <AlertCircle className="mb-4 size-14 text-destructive/50" />
      <p className="text-base font-semibold text-muted-foreground">
        Erro ao carregar os caixas
      </p>
      <p className="mt-1 text-sm text-muted-foreground/60">
        Verifique sua conexão e tente novamente.
      </p>
      <Button variant="outline" size="sm" className="mt-4" onClick={refetch}>
        <RefreshCw className="mr-2 size-4" />
        Tentar novamente
      </Button>
    </div>
  );
}

interface RegisterCardProps {
  register: CashRegisterStatusResponse;
  onOpen: (reg: CashRegisterStatusResponse) => void;
}

function RegisterCard({ register, onOpen }: RegisterCardProps) {
  const isOpen = register.hasActiveSession;

  return (
    <Card
      className={`transition-shadow hover:shadow-md ${
        isOpen ? "border-emerald-500/40" : "border-border"
      }`}
    >
      <CardHeader className="flex flex-row items-start justify-between gap-2 pb-3">
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            <Monitor className="size-4 shrink-0 text-muted-foreground" />
            <CardTitle className="text-base">{register.description}</CardTitle>
          </div>
          <p className="mt-0.5 text-xs text-muted-foreground">
            Código{" "}
            <span className="font-mono font-semibold">{register.code}</span>
          </p>
        </div>

        <Badge
          variant={isOpen ? "default" : "secondary"}
          className={`shrink-0 text-xs ${
            isOpen ? "bg-emerald-600 text-white hover:bg-emerald-700" : ""
          }`}
        >
          {isOpen ? (
            <>
              <LockOpen className="mr-1 size-3" />
              Aberto
            </>
          ) : (
            <>
              <Lock className="mr-1 size-3" />
              Fechado
            </>
          )}
        </Badge>
      </CardHeader>

      <CardContent className="space-y-3">
        {isOpen ? (
          /* ── Active session info ── */
          <div className="space-y-2 rounded-md bg-muted/50 p-3 text-xs">
            <div className="flex items-center gap-2 text-muted-foreground">
              <User className="size-3.5 shrink-0" />
              <span className="font-medium text-foreground">
                {register.activeOperatorName ?? "—"}
              </span>
            </div>
            <div className="flex items-center gap-2 text-muted-foreground">
              <BadgeDollarSign className="size-3.5 shrink-0" />
              <span>
                Valor inicial:{" "}
                <span className="font-semibold text-foreground">
                  {register.initialAmount != null
                    ? formatCurrency(register.initialAmount)
                    : "—"}
                </span>
              </span>
            </div>
            <div className="flex items-center gap-2 text-muted-foreground">
              <CalendarClock className="size-3.5 shrink-0" />
              <span>Abertura: {formatDatetime(register.openedAt)}</span>
            </div>
          </div>
        ) : (
          /* ── No session placeholder ── */
          <div className="flex items-center gap-2 rounded-md bg-muted/30 p-3 text-xs text-muted-foreground">
            <AlertCircle className="size-3.5 shrink-0" />
            <span>Nenhuma sessão ativa neste caixa.</span>
          </div>
        )}

        <Separator />

        <Button
          size="sm"
          variant={isOpen ? "outline" : "default"}
          className="w-full"
          onClick={() => onOpen(register)}
          disabled={isOpen || !register.paymentIntegrationConfigured}
          title={
            !isOpen && !register.paymentIntegrationConfigured
              ? "É necessário configurar a integração de pagamento primeiro."
              : undefined
          }
        >
          {isOpen
            ? "Sessão em andamento"
            : !register.paymentIntegrationConfigured
              ? "Pagamento não configurado"
              : "Abrir sessão"}
        </Button>
      </CardContent>
    </Card>
  );
}

/* ────────────────────────────────────────────────
   Open-session dialog
──────────────────────────────────────────────── */
interface OpenSessionDialogProps {
  register: CashRegisterStatusResponse | null;
  operators: OperatorResponse[];
  isLoadingOperators: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

function OpenSessionDialog({
  register,
  operators,
  isLoadingOperators,
  onClose,
  onSuccess,
}: OpenSessionDialogProps) {
  const [operatorCode, setOperatorCode] = useState<string>("");
  const [initialAmount, setInitialAmount] = useState<string>("");
  const [submitting, setSubmitting] = useState(false);

  const isOpen = !!register;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!register || !operatorCode) return;

    const amount = parseFloat(initialAmount.replace(",", "."));
    if (isNaN(amount) || amount < 0) {
      toast.error("Informe um valor inicial válido.");
      return;
    }

    setSubmitting(true);
    try {
      await cashRegisterService.openSession({
        cashRegisterCode: register.code,
        operatorCode,
        initialAmount: amount,
      });
      toast.success(`Sessão aberta no caixa ${register.code} com sucesso!`);
      onSuccess();
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : "Erro ao abrir a sessão.";
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  }

  function handleOpenChange(open: boolean) {
    if (!open) {
      setOperatorCode("");
      setInitialAmount("");
      onClose();
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <LockOpen className="size-5 text-emerald-600" />
            Abrir sessão
          </DialogTitle>
          {register && (
            <DialogDescription>
              Caixa{" "}
              <span className="font-mono font-semibold">{register.code}</span> —{" "}
              {register.description}
            </DialogDescription>
          )}
        </DialogHeader>

        <form
          id="open-session-form"
          onSubmit={handleSubmit}
          className="space-y-4 pt-2"
        >
          {/* Operator */}
          <div className="space-y-1.5">
            <Label htmlFor="operator-select">
              Operador <span className="text-destructive">*</span>
            </Label>
            {isLoadingOperators ? (
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Loader2 className="size-4 animate-spin" />
                Carregando operadores…
              </div>
            ) : (
              <Select value={operatorCode} onValueChange={setOperatorCode}>
                <SelectTrigger id="operator-select">
                  <SelectValue placeholder="Selecione um operador" />
                </SelectTrigger>
                <SelectContent>
                  {operators.length === 0 ? (
                    <SelectItem value="__none__" disabled>
                      Nenhum operador disponível
                    </SelectItem>
                  ) : (
                    operators.map((op) => (
                      <SelectItem key={op.id} value={op.code}>
                        <span className="font-mono text-xs text-muted-foreground mr-1.5">
                          {op.code}
                        </span>
                        {op.name}
                      </SelectItem>
                    ))
                  )}
                </SelectContent>
              </Select>
            )}
          </div>

          {/* Initial amount */}
          <div className="space-y-1.5">
            <Label htmlFor="initial-amount">Valor inicial (R$)</Label>
            <Input
              id="initial-amount"
              type="number"
              min="0"
              step="0.01"
              placeholder="0,00"
              value={initialAmount}
              onChange={(e) => setInitialAmount(e.target.value)}
            />
            <p className="text-xs text-muted-foreground">
              Deixe em branco ou zero para iniciar sem fundo de caixa.
            </p>
          </div>
        </form>

        <DialogFooter>
          <Button variant="ghost" onClick={onClose} disabled={submitting}>
            Cancelar
          </Button>
          <Button
            type="submit"
            form="open-session-form"
            disabled={!operatorCode || submitting}
          >
            {submitting && <Loader2 className="mr-2 size-4 animate-spin" />}
            {submitting ? "Abrindo…" : "Abrir sessão"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/* ────────────────────────────────────────────────
   Page
──────────────────────────────────────────────── */
export default function CaixasPage() {
  const queryClient = useQueryClient();
  const [selectedRegister, setSelectedRegister] =
    useState<CashRegisterStatusResponse | null>(null);

  /* ── Queries ── */
  const {
    data: registers,
    isLoading: isLoadingRegisters,
    isError: isErrorRegisters,
    refetch: refetchRegisters,
  } = useQuery({
    queryKey: ["cash-registers"],
    queryFn: cashRegisterService.listCashRegisters,
  });

  const { data: operators = [], isLoading: isLoadingOperators } = useQuery({
    queryKey: ["operators"],
    queryFn: cashRegisterService.listOperators,
    enabled: !!selectedRegister && !selectedRegister.hasActiveSession,
    staleTime: 30_000,
  });

  /* ── Summary stats ── */
  const totalOpen = registers?.filter((r) => r.hasActiveSession).length ?? 0;
  const totalClosed = registers?.filter((r) => !r.hasActiveSession).length ?? 0;

  /* ── Handlers ── */
  function handleOpenDialog(reg: CashRegisterStatusResponse) {
    if (!reg.hasActiveSession && reg.paymentIntegrationConfigured) {
      setSelectedRegister(reg);
    } else if (!reg.paymentIntegrationConfigured) {
      toast.error("Integração de pagamento não configurada para este caixa.");
    }
  }

  function handleDialogSuccess() {
    setSelectedRegister(null);
    queryClient.invalidateQueries({ queryKey: ["cash-registers"] });
  }

  /* ── Render ── */
  return (
    <div className="flex flex-col gap-6 p-6" data-onboarding="caixas-page">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">
            Gerenciamento de Caixas
          </h1>
          <p className="text-sm text-muted-foreground">
            Visualize e gerencie as sessões dos caixas registradores.
          </p>
        </div>
        <CreateCashRegisterDialog />
      </div>

      {/* Summary row */}
      {!isLoadingRegisters && !isErrorRegisters && registers && (
        <div
          className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4"
          data-onboarding="caixas-summary"
        >
          <Card>
            <CardContent className="flex items-center gap-3 p-4">
              <Monitor className="size-8 shrink-0 text-muted-foreground" />
              <div>
                <p className="text-xs text-muted-foreground">Total de caixas</p>
                <p className="text-2xl font-bold">{registers.length}</p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="flex items-center gap-3 p-4">
              <LockOpen className="size-8 shrink-0 text-emerald-600" />
              <div>
                <p className="text-xs text-muted-foreground">Abertos</p>
                <p className="text-2xl font-bold text-emerald-600">
                  {totalOpen}
                </p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="flex items-center gap-3 p-4">
              <Lock className="size-8 shrink-0 text-muted-foreground" />
              <div>
                <p className="text-xs text-muted-foreground">Fechados</p>
                <p className="text-2xl font-bold">{totalClosed}</p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="flex items-center gap-3 p-4">
              <Users className="size-8 shrink-0 text-blue-600" />
              <div>
                <p className="text-xs text-muted-foreground">
                  Operadores ativos
                </p>
                <p className="text-2xl font-bold text-blue-600">{totalOpen}</p>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      <Separator />

      {/* Grid */}
      <div
        className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
        data-onboarding="caixas-content"
      >
        {isLoadingRegisters ? (
          <div className="col-span-full flex items-center justify-center py-24">
            <Loader2 className="size-8 animate-spin text-muted-foreground" />
          </div>
        ) : isErrorRegisters ? (
          <ErrorState refetch={refetchRegisters} />
        ) : !registers || registers.length === 0 ? (
          <EmptyState />
        ) : (
          registers.map((reg) => (
            <RegisterCard
              key={reg.cashRegisterId}
              register={reg}
              onOpen={handleOpenDialog}
            />
          ))
        )}
      </div>

      {/* Open-session dialog */}
      <OpenSessionDialog
        register={selectedRegister}
        operators={operators}
        isLoadingOperators={isLoadingOperators}
        onClose={() => setSelectedRegister(null)}
        onSuccess={handleDialogSuccess}
      />
    </div>
  );
}
