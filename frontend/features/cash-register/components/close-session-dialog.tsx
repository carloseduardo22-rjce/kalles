"use client";

import { useState } from "react";
import { Lock } from "lucide-react";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ErrorAlert } from "@/shared/components/error-alert";

interface CloseSessionDialogProps {
  isLoading: boolean;
  error: string | null;
  onConfirm: () => Promise<void>;
}

export function CloseSessionDialog({
  isLoading,
  error,
  onConfirm,
}: CloseSessionDialogProps) {
  const [open, setOpen] = useState(false);

  async function handleConfirm() {
    await onConfirm();
    setOpen(false);
  }

  return (
    <AlertDialog open={open} onOpenChange={setOpen}>
      <AlertDialogTrigger asChild>
        <Button variant="outline" size="sm">
          <Lock className="mr-2 h-4 w-4" />
          Fechar Caixa
        </Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Fechar sessão de caixa?</AlertDialogTitle>
          <AlertDialogDescription>
            Você receberá um resumo financeiro com todas as vendas realizadas
            nesta sessão. Esta ação não pode ser desfeita.
          </AlertDialogDescription>
        </AlertDialogHeader>

        {error && (
          <ErrorAlert
            error={error}
            title="Não foi possível fechar o caixa"
            className="mt-2"
          />
        )}

        <AlertDialogFooter>
          <AlertDialogCancel disabled={isLoading}>Cancelar</AlertDialogCancel>
          <AlertDialogAction
            onClick={(e) => {
              e.preventDefault();
              handleConfirm();
            }}
            disabled={isLoading}
            className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
          >
            {isLoading ? <LoadingSpinner size="sm" label="" /> : "Fechar Caixa"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
