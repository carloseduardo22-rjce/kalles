"use client";

import { useState, useEffect } from "react";
import {
  Gift,
  Search,
  UserCheck,
  Star,
  RefreshCw,
  CheckCircle,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { clientService } from "@/features/admin/services/client.service";
import { fidelityService } from "@/features/admin/services/fidelity.service";
import { ApiError } from "@/shared/services/api";
import type { ClientResponse } from "@/features/admin/types";
import type {
  FidelityResponse,
  FidelityPolicyResponse,
} from "@/features/admin/types";
import type { SaleResponse } from "@/features/sales/types";
import { formatCurrency } from "@/shared/utils/formatters";

type PanelStatus =
  | "idle"
  | "loading"
  | "not_found"
  | "not_enrolled"
  | "enrolling"
  | "enrolled";

interface FidelityPdvPanelProps {
  sale: SaleResponse | null;
  disabled?: boolean;
  onAssociateClient: (clientId: string) => Promise<void>;
  onApplyFidelityDiscount: () => Promise<void>;
}

function normalizeCpf(cpf: string): string {
  return cpf.replace(/\D/g, "");
}

export function FidelityPdvPanel({
  sale,
  disabled,
  onAssociateClient,
  onApplyFidelityDiscount,
}: FidelityPdvPanelProps) {
  const [cpfInput, setCpfInput] = useState("");
  const [status, setStatus] = useState<PanelStatus>("idle");
  const [client, setClient] = useState<ClientResponse | null>(null);
  const [fidelity, setFidelity] = useState<FidelityResponse | null>(null);
  const [activePolicy, setActivePolicy] =
    useState<FidelityPolicyResponse | null>(null);
  const [errMsg, setErrMsg] = useState<string | null>(null);
  const [applying, setApplying] = useState(false);

  // Reset panel when sale changes (new sale started)
  useEffect(() => {
    setCpfInput("");
    setStatus("idle");
    setClient(null);
    setFidelity(null);
    setErrMsg(null);
  }, [sale?.id]);

  async function handleSearch() {
    const normalized = normalizeCpf(cpfInput);
    if (normalized.length < 11) {
      setErrMsg("CPF inválido.");
      return;
    }
    setStatus("loading");
    setErrMsg(null);
    try {
      const clients = await clientService.listAll();
      const found = clients.find((c) => normalizeCpf(c.cpf) === normalized);
      if (!found) {
        setStatus("not_found");
        return;
      }
      setClient(found);

      let fidelityData: FidelityResponse | null = null;
      try {
        fidelityData = await fidelityService.getByClientId(found.id);
      } catch (err) {
        if (!(err instanceof ApiError && err.status === 404)) throw err;
      }

      if (!fidelityData) {
        setStatus("not_enrolled");
        return;
      }

      setFidelity(fidelityData);

      try {
        const policy = await fidelityService.getActivePolicy();
        setActivePolicy(policy);
      } catch {
        // policy is optional for display
      }

      // Associate this client with the current sale
      await onAssociateClient(found.id);
      setStatus("enrolled");
    } catch {
      setErrMsg("Erro ao buscar cliente. Tente novamente.");
      setStatus("idle");
    }
  }

  async function handleEnroll() {
    if (!client) return;
    setStatus("enrolling");
    setErrMsg(null);
    try {
      const newFidelity = await fidelityService.enroll(client.id);
      setFidelity(newFidelity);
      await onAssociateClient(client.id);
      setStatus("enrolled");
    } catch {
      setErrMsg("Erro ao inscrever cliente.");
      setStatus("not_enrolled");
    }
  }

  async function handleApplyDiscount() {
    setApplying(true);
    setErrMsg(null);
    try {
      await onApplyFidelityDiscount();
    } catch {
      setErrMsg("Erro ao aplicar desconto.");
    } finally {
      setApplying(false);
    }
  }

  function handleReset() {
    setCpfInput("");
    setStatus("idle");
    setClient(null);
    setFidelity(null);
    setErrMsg(null);
  }

  const hasDiscount = (fidelity?.availableDiscount ?? 0) > 0;
  const discountAlreadyApplied = (sale?.fidelityDiscountApplied ?? 0) > 0;

  return (
    <div className="rounded-lg border bg-card p-3 space-y-2.5">
      <div className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
        <Gift className="h-4 w-4 text-primary" />
        <span>Fidelidade</span>
        {sale?.clientId && status === "enrolled" && (
          <Badge variant="secondary" className="ml-auto text-xs">
            <CheckCircle className="mr-1 h-3 w-3 text-green-500" />
            Vinculado
          </Badge>
        )}
      </div>

      {/* Search form */}
      {(status === "idle" || status === "not_found") && (
        <div className="space-y-2">
          <div className="flex gap-2">
            <Input
              placeholder="CPF do cliente"
              value={cpfInput}
              onChange={(e) => {
                setCpfInput(e.target.value);
                setErrMsg(null);
              }}
              onKeyDown={(e) =>
                e.key === "Enter" && !disabled && handleSearch()
              }
              disabled={disabled}
              className="text-sm h-8"
            />
            <Button
              size="sm"
              variant="secondary"
              className="h-8 px-3 shrink-0"
              onClick={handleSearch}
              disabled={disabled || !cpfInput.trim()}
            >
              <Search className="h-3.5 w-3.5" />
            </Button>
          </div>
          {status === "not_found" && (
            <p className="text-xs text-destructive">
              CPF não encontrado. Verifique ou continue sem fidelidade.
            </p>
          )}
          {errMsg && <p className="text-xs text-destructive">{errMsg}</p>}
        </div>
      )}

      {/* Loading */}
      {status === "loading" && (
        <p className="text-xs text-muted-foreground animate-pulse">
          Buscando cliente…
        </p>
      )}

      {/* Not enrolled */}
      {(status === "not_enrolled" || status === "enrolling") && client && (
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <UserCheck className="h-4 w-4 text-muted-foreground" />
            <span className="text-sm font-medium truncate">{client.name}</span>
          </div>
          <p className="text-xs text-muted-foreground">
            Não inscrito no programa de fidelidade.
          </p>
          <div className="flex gap-2">
            <Button
              size="sm"
              className="h-7 text-xs"
              onClick={handleEnroll}
              disabled={status === "enrolling" || disabled}
            >
              <Gift className="mr-1.5 h-3 w-3" />
              {status === "enrolling" ? "Inscrevendo…" : "Inscrever"}
            </Button>
            <Button
              size="sm"
              variant="ghost"
              className="h-7 text-xs text-muted-foreground"
              onClick={handleReset}
            >
              Pular
            </Button>
          </div>
          {errMsg && <p className="text-xs text-destructive">{errMsg}</p>}
        </div>
      )}

      {/* Enrolled */}
      {status === "enrolled" && client && fidelity && (
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <UserCheck className="h-4 w-4 text-green-500" />
            <span className="text-sm font-medium truncate">{client.name}</span>
            <Button
              size="sm"
              variant="ghost"
              className="ml-auto h-6 w-6 p-0 text-muted-foreground"
              onClick={handleReset}
              title="Mudar cliente"
            >
              <RefreshCw className="h-3 w-3" />
            </Button>
          </div>

          {hasDiscount && !discountAlreadyApplied && (
            <div className="rounded-md bg-green-50 border border-green-200 p-2 space-y-1.5">
              <p className="text-xs font-medium text-green-800">
                <Star className="inline mr-1 h-3 w-3" />
                {formatCurrency(fidelity.availableDiscount)} de desconto
                disponível
              </p>
              <Button
                size="sm"
                className="h-7 w-full text-xs bg-green-600 hover:bg-green-700"
                onClick={handleApplyDiscount}
                disabled={applying || disabled}
              >
                {applying ? "Aplicando…" : "Aplicar desconto"}
              </Button>
            </div>
          )}

          {discountAlreadyApplied && (
            <div className="rounded-md bg-green-50 border border-green-200 p-2">
              <p className="text-xs font-medium text-green-800">
                <CheckCircle className="inline mr-1 h-3 w-3" />
                Desconto de {formatCurrency(sale!.fidelityDiscountApplied)}{" "}
                aplicado
              </p>
            </div>
          )}

          {!hasDiscount && !discountAlreadyApplied && activePolicy && (
            <div className="space-y-1">
              <div className="flex justify-between text-xs text-muted-foreground">
                <span>
                  <Star className="inline mr-1 h-3 w-3 text-amber-400" />
                  {fidelity.points} / {activePolicy.objectivePoints} pts
                </span>
                <span>
                  {formatCurrency(activePolicy.configuredDiscount)} ao completar
                </span>
              </div>
              <Progress
                value={(fidelity.points / activePolicy.objectivePoints) * 100}
                className="h-1.5"
              />
            </div>
          )}

          {errMsg && <p className="text-xs text-destructive">{errMsg}</p>}
        </div>
      )}
    </div>
  );
}
