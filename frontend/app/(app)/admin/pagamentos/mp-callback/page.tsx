"use client";

import { useEffect, useState, useRef, Suspense } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { CheckCircle2, XCircle, Loader2 } from "lucide-react";


function MpCallbackContent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const [status, setStatus] = useState<"loading" | "success" | "error">(
    "loading",
  );
  const [errorMessage, setErrorMessage] = useState("");

  const hasFetched = useRef(false);

  useEffect(() => {
    if (hasFetched.current) return;
    hasFetched.current = true;

    const code = searchParams.get("code");
    const state = searchParams.get("state");
    const error = searchParams.get("error");

    if (error) {
      setStatus("error");
      setErrorMessage(
        searchParams.get("error_description") || "Usuário negou a permissão.",
      );
      return;
    }

    if (code && state) {
      // Chamada real ao back-end (proxy via Next.js rewrites)
      fetch("/api/v1/mercadopago/oauth/link", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ code, state }),
      })
        .then(async (response) => {
          if (!response.ok) {
            throw new Error("Falha ao vincular a conta");
          }
          setStatus("success");
        })
        .catch((err) => {
          console.error(err);
          setStatus("error");
          setErrorMessage(
            "Não foi possível concluir a vinculação no servidor.",
          );
        });
    } else {
      setStatus("error");
      setErrorMessage("Parâmetros inválidos retornados pelo Mercado Pago.");
    }
  }, [searchParams]);

  return (
    <Card className="w-full max-w-md text-center">
      <CardHeader>
        <CardTitle>Integração Mercado Pago</CardTitle>
        <CardDescription>Processando sua vinculação de conta</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col items-center justify-center space-y-4 py-8">
        {status === "loading" && (
          <>
            <Loader2 className="h-12 w-12 animate-spin text-primary" />
            <p className="text-sm text-gray-500">
              Trocando códigos e finalizando a conexão com sua conta.
              Aguarde...
            </p>
          </>
        )}

        {status === "success" && (
          <>
            <CheckCircle2 className="h-12 w-12 text-green-500" />
            <p className="text-sm text-gray-500">
              Conta do Mercado Pago vinculada com sucesso! Você já pode
              processar pagamentos.
            </p>
            <Button
              onClick={() => router.push("/admin/pagamentos")}
              className="mt-4"
            >
              Voltar para Configuração de Pagamento
            </Button>
          </>
        )}

        {status === "error" && (
          <>
            <XCircle className="h-12 w-12 text-red-500" />
            <p className="text-sm text-red-500 font-semibold">
              Falha na integração
            </p>
            <p className="text-sm text-gray-500">{errorMessage}</p>
            <Button
              onClick={() => router.push("/admin/pagamentos")}
              variant="outline"
              className="mt-4"
            >
              Tentar Novamente
            </Button>
          </>
        )}
      </CardContent>
    </Card>
  );
}

export default function MpCallbackPage() {
  return (
    <div className="flex h-full w-full items-center justify-center p-6">
      <Suspense fallback={<div>Carregando...</div>}>
         <MpCallbackContent />
      </Suspense>
    </div>
  );
}
