"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
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
import { cashRegisterService } from "@/features/cash-register/services/cash-register.service";
import type { SessionResponse } from "@/features/cash-register/types";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { api } from "@/shared/services/api";

export default function RootPage() {
  const router = useRouter();
  const [resumableSession, setResumableSession] =
    useState<SessionResponse | null>(null);

  useEffect(() => {
    async function chooseLandingPage() {
      const me = await api
        .get<{ role: string }>("/api/auth/me")
        .catch(() => null);
      if (me && me.role !== "ADMIN") {
        router.replace("/pdv");
        return;
      }
      const session = me
        ? await cashRegisterService.findSessionOnThisDevice()
        : null;
      if (session) {
        setResumableSession(session);
        return;
      }
      router.replace("/caixas");
    }

    chooseLandingPage();
  }, [router]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <LoadingSpinner size="lg" label="Carregando…" />
      <AlertDialog open={!!resumableSession}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Você tem um caixa aberto</AlertDialogTitle>
            <AlertDialogDescription>
              Sua sessão no caixa {resumableSession?.cashRegisterCode} continua
              aberta. Para onde você quer ir?
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => router.replace("/caixas")}>
              Ir para Caixas
            </AlertDialogCancel>
            <AlertDialogAction onClick={() => router.replace("/pdv")}>
              Ir para o PDV
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
