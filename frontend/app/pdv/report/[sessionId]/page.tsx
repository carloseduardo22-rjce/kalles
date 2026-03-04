"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Store } from "lucide-react";
import { SessionSummary } from "@/features/cash-register/components/session-summary";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ErrorAlert } from "@/shared/components/error-alert";
import type { CloseSessionResponse } from "@/features/cash-register/types";
import { cashRegisterService } from "@/features/cash-register/services/cash-register.service";

export default function ReportPage() {
  const router = useRouter();
  const { sessionId } = useParams<{ sessionId: string }>();

  const [data, setData] = useState<CloseSessionResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Try sessionStorage first (set by pdv/page.tsx after close)
    const cached = sessionStorage.getItem(`kalles:report:${sessionId}`);
    if (cached) {
      try {
        setData(JSON.parse(cached) as CloseSessionResponse);
        sessionStorage.removeItem(`kalles:report:${sessionId}`);
        setIsLoading(false);
        return;
      } catch {
        // fall through to API call
      }
    }

    // Fallback: load report from API
    cashRegisterService
      .getReport(sessionId)
      .then((summary) => {
        setData({
          sessionId,
          codigoCaixa: "—",
          nomeOperador: "—",
          valorInicial: 0,
          abertura: new Date().toISOString(),
          fechamento: new Date().toISOString(),
          status: "CLOSED",
          resumo: summary,
        });
      })
      .catch((err) =>
        setError(
          err instanceof Error
            ? err.message
            : "Não foi possível carregar o relatório.",
        ),
      )
      .finally(() => setIsLoading(false));
  }, [sessionId]);

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <LoadingSpinner size="lg" label="Carregando relatório…" />
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-background p-6">
        <ErrorAlert error={error ?? "Relatório não disponível."} />
        <Button variant="outline" onClick={() => router.push("/open-session")}>
          <Store className="mr-2 h-4 w-4" />
          Nova sessão
        </Button>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-6 bg-background p-6">
      <SessionSummary data={data} />
      <Button onClick={() => router.push("/open-session")}>
        <Store className="mr-2 h-4 w-4" />
        Abrir nova sessão
      </Button>
    </div>
  );
}
