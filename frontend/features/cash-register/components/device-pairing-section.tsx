"use client";

import { useState } from "react";
import { KeyRound, Loader2, MonitorSmartphone, Unlink } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { formatDate } from "@/shared/utils/formatters";
import { cashRegisterService } from "../services/cash-register.service";
import type { CashRegisterStatusResponse, PairingCodeResponse } from "../types";

interface DevicePairingSectionProps {
  register: CashRegisterStatusResponse;
  onChanged: () => void;
}

function formatTime(iso: string): string {
  return new Intl.DateTimeFormat("pt-BR", {
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(iso));
}

export function DevicePairingSection({
  register,
  onChanged,
}: DevicePairingSectionProps) {
  const [pairingCode, setPairingCode] = useState<PairingCodeResponse | null>(
    null,
  );
  const [confirmRevoke, setConfirmRevoke] = useState(false);
  const [busy, setBusy] = useState(false);

  async function handleGenerate() {
    setBusy(true);
    try {
      setPairingCode(
        await cashRegisterService.generatePairingCode(register.cashRegisterId),
      );
    } catch (err) {
      toast.error(
        err instanceof Error ? err.message : "Erro ao gerar o código.",
      );
    } finally {
      setBusy(false);
    }
  }

  async function handleRevoke() {
    setBusy(true);
    try {
      await cashRegisterService.revokePairing(register.cashRegisterId);
      toast.success(`Pareamento do caixa ${register.code} revogado.`);
      onChanged();
    } catch (err) {
      toast.error(
        err instanceof Error ? err.message : "Erro ao revogar o pareamento.",
      );
    } finally {
      setBusy(false);
      setConfirmRevoke(false);
    }
  }

  function handleCodeDialogChange(open: boolean) {
    if (!open) {
      setPairingCode(null);
      onChanged();
    }
  }

  return (
    <div className="space-y-2">
      <div className="flex items-center gap-2 text-xs text-muted-foreground">
        <MonitorSmartphone className="size-3.5 shrink-0" />
        <span>
          {register.pairedAt
            ? `Pareado em ${formatDate(register.pairedAt)}`
            : "Nenhum dispositivo pareado"}
        </span>
      </div>

      <div className="flex flex-wrap gap-2">
        <Button
          data-testid={`generate-pairing-code-${register.code}`}
          size="sm"
          variant="outline"
          onClick={handleGenerate}
          disabled={busy}
        >
          {busy ? (
            <Loader2 className="mr-1 size-3.5 animate-spin" />
          ) : (
            <KeyRound className="mr-1 size-3.5" />
          )}
          Gerar código
        </Button>
        {register.pairedAt && (
          <Button
            data-testid={`revoke-pairing-${register.code}`}
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() => setConfirmRevoke(true)}
            disabled={busy}
          >
            <Unlink className="mr-1 size-3.5" />
            Revogar
          </Button>
        )}
      </div>

      <Dialog open={!!pairingCode} onOpenChange={handleCodeDialogChange}>
        <DialogContent className="sm:max-w-sm">
          <DialogHeader>
            <DialogTitle>Código de pareamento</DialogTitle>
            <DialogDescription>
              Caixa{" "}
              <span className="font-mono font-semibold">{register.code}</span>{" "}
              — {register.description}
            </DialogDescription>
          </DialogHeader>
          {pairingCode && (
            <div className="space-y-3 text-center">
              <p
                data-testid="pairing-code-value"
                className="select-all font-mono text-4xl font-bold tracking-widest"
              >
                {pairingCode.code}
              </p>
              <p className="text-sm text-muted-foreground">
                Válido até {formatTime(pairingCode.expiresAt)}. Informe este
                código no navegador do caixa, na tela de abertura de sessão. Ele
                só pode ser usado uma vez.
              </p>
            </div>
          )}
          <DialogFooter>
            <Button onClick={() => handleCodeDialogChange(false)}>
              Concluir
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog open={confirmRevoke} onOpenChange={setConfirmRevoke}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              Revogar pareamento do caixa {register.code}?
            </AlertDialogTitle>
            <AlertDialogDescription>
              O navegador pareado não conseguirá mais abrir nem operar sessão
              neste caixa.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={busy}>Voltar</AlertDialogCancel>
            <AlertDialogAction onClick={handleRevoke} disabled={busy}>
              Revogar
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
