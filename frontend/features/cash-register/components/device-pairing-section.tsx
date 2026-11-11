"use client";

import { useCallback, useEffect, useState } from "react";
import {
  Check,
  Copy,
  KeyRound,
  Loader2,
  MonitorSmartphone,
  Unlink,
} from "lucide-react";
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

async function copyText(text: string): Promise<void> {
  if (navigator.clipboard && window.isSecureContext) {
    await navigator.clipboard.writeText(text);
    return;
  }
  const field = document.createElement("textarea");
  field.value = text;
  field.setAttribute("readonly", "");
  field.style.position = "fixed";
  field.style.opacity = "0";
  document.body.appendChild(field);
  field.select();
  const copied = document.execCommand("copy");
  document.body.removeChild(field);
  if (!copied) {
    throw new Error("copy failed");
  }
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
  const [copied, setCopied] = useState(false);

  const handleCopy = useCallback(async (code: string) => {
    try {
      await copyText(code);
      setCopied(true);
      toast.success("Código copiado.");
    } catch {
      toast.error("Não foi possível copiar. Selecione o código e copie manualmente.");
    }
  }, []);

  useEffect(() => {
    if (!pairingCode) return;
    const code = pairingCode.code;
    function onKeyDown(event: KeyboardEvent) {
      const isCopyShortcut =
        (event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "c";
      if (isCopyShortcut && !window.getSelection()?.toString()) {
        event.preventDefault();
        handleCopy(code);
      }
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [pairingCode, handleCopy]);

  useEffect(() => {
    if (!copied) return;
    const timer = window.setTimeout(() => setCopied(false), 2000);
    return () => window.clearTimeout(timer);
  }, [copied]);

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
      setCopied(false);
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
              <Button
                data-testid="copy-pairing-code"
                size="sm"
                variant="outline"
                onClick={() => handleCopy(pairingCode.code)}
              >
                {copied ? (
                  <Check className="mr-1 size-3.5" />
                ) : (
                  <Copy className="mr-1 size-3.5" />
                )}
                {copied ? "Copiado" : "Copiar"}
              </Button>
              <p className="text-sm text-muted-foreground">
                Válido até {formatTime(pairingCode.expiresAt)}. Informe este
                código no navegador do caixa, na tela de abertura de sessão. Ele
                só pode ser usado uma vez. Ctrl+C também copia o código.
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
