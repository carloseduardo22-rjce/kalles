"use client";

import { Button } from "@/components/ui/button";
import {
  InputOTP,
  InputOTPGroup,
  InputOTPSlot,
} from "@/components/ui/input-otp";
import { api } from "@/shared/services/api";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useState, useEffect, Suspense } from "react";
import { toast } from "sonner";
import { ArrowLeft, MailOpen } from "lucide-react";
import { setSessionScopedItem } from "@/shared/utils/session-storage";

function VerifyContent() {
  const [code, setCode] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isResending, setIsResending] = useState(false);

  const searchParams = useSearchParams();
  const email = searchParams.get("email") || "";
  const tenantId = searchParams.get("tenantId") || "";
  const router = useRouter();

  useEffect(() => {
    if (!email) {
      router.replace("/register");
    }
  }, [email, router]);

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    if (code.length !== 6) {
      toast.error("O código deve ter 6 dígitos.");
      return;
    }

    try {
      setIsLoading(true);
      await api.post("/api/auth/verify", {
        email,
        code,
        tenantId: tenantId || undefined,
      });
      if (tenantId) {
        setSessionScopedItem(`@kalles:tenantId:${email}`, tenantId);
      }
      toast.success("E-mail verificado com sucesso!");
      router.push("/caixas"); // Redirects to the authenticated area
    } catch (error: any) {
      toast.error(error.message || "Código inválido ou expirado.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleResend = async () => {
    try {
      setIsResending(true);
      const params = new URLSearchParams({ email });
      if (tenantId) {
        params.set("tenantId", tenantId);
      }
      await api.post(`/api/auth/resend-code?${params.toString()}`);
      toast.success("Novo código enviado para o seu e-mail.");
    } catch (error: any) {
      toast.error(error.message || "Erro ao reenviar o código.");
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="w-full max-w-5xl bg-white rounded-3xl shadow-xl overflow-hidden flex flex-col md:flex-row min-h-150 p-2">
      {/* Left Side */}
      <div className="w-full md:w-5/12 p-8 md:p-12 lg:p-16 flex flex-col justify-center border-b md:border-b-0 md:border-r border-gray-100">
        <div className="max-w-sm w-full mx-auto space-y-8">
          <div className="h-16 w-16 bg-blue-100 text-blue-600 rounded-2xl flex items-center justify-center shadow-sm">
            <MailOpen className="w-8 h-8" />
          </div>

          <div className="space-y-3">
            <h1 className="text-4xl sm:text-5xl font-semibold tracking-tight">
              Verifique seu e-mail
            </h1>
            <p className="text-gray-500 text-lg">
              Enviamos um código de 6 dígitos para o e-mail: <br />
              <span className="font-medium text-gray-900">{email}</span>
            </p>
          </div>
        </div>
      </div>

      {/* Right Side */}
      <div className="w-full md:w-7/12 p-8 md:p-12 lg:p-16 flex flex-col justify-center">
        <div className="max-w-md w-full mx-auto">
          <form className="space-y-8" onSubmit={handleVerify}>
            <div className="flex flex-col items-center justify-center space-y-4">
              <label className="text-sm font-medium text-gray-700">
                Digite o código de verificação
              </label>
              <InputOTP
                maxLength={6}
                value={code}
                onChange={(v) => setCode(v)}
                disabled={isLoading}
              >
                <InputOTPGroup>
                  <InputOTPSlot index={0} className="w-12 h-14 text-2xl" />
                  <InputOTPSlot index={1} className="w-12 h-14 text-2xl" />
                  <InputOTPSlot index={2} className="w-12 h-14 text-2xl" />
                  <InputOTPSlot index={3} className="w-12 h-14 text-2xl" />
                  <InputOTPSlot index={4} className="w-12 h-14 text-2xl" />
                  <InputOTPSlot index={5} className="w-12 h-14 text-2xl" />
                </InputOTPGroup>
              </InputOTP>
            </div>

            <div className="space-y-4 pt-4">
              <Button
                type="submit"
                className="w-full h-12 text-base font-medium rounded-xl"
                disabled={isLoading || code.length !== 6}
              >
                {isLoading ? "Verificando..." : "Confirmar e-mail"}
              </Button>

              <div className="flex justify-center">
                <Button
                  type="button"
                  variant="ghost"
                  onClick={handleResend}
                  disabled={isResending}
                  className="text-blue-600 hover:text-blue-700"
                >
                  {isResending ? "Enviando..." : "Reenviar código"}
                </Button>
              </div>
            </div>

            <div className="pt-8 flex justify-center border-t border-gray-100">
              <Link
                href="/login"
                className="flex items-center text-sm text-gray-500 hover:text-gray-900 font-medium"
              >
                <ArrowLeft className="w-4 h-4 mr-2" />
                Voltar para o login
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default function VerifyPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50/50 p-4">
      <Suspense
        fallback={
          <div className="p-8 text-center text-gray-500">Carregando...</div>
        }
      >
        <VerifyContent />
      </Suspense>
    </div>
  );
}
