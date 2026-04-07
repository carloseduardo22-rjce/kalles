"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { CreditCard, ExternalLink, ShieldCheck, Sparkles } from "lucide-react";
import { toast } from "sonner";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { billingService } from "@/features/admin/services/billing.service";
import { ApiError } from "@/shared/services/api";

export default function BillingSubscriptionPage() {
  const [isOpeningPortal, setIsOpeningPortal] = useState(false);
  const [returnUrl, setReturnUrl] = useState("");

  useEffect(() => {
    setReturnUrl(`${window.location.origin}/admin/assinatura`);
  }, []);

  const handleOpenPortal = async () => {
    try {
      setIsOpeningPortal(true);
      const portalSession = await billingService.createPortalSession(returnUrl);
      window.location.assign(portalSession.url);
    } catch (error) {
      const message =
        error instanceof ApiError
          ? error.message
          : "Nao foi possivel abrir o portal de assinatura.";
      toast.error(message);
    } finally {
      setIsOpeningPortal(false);
    }
  };

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top,#e8f6ff_0%,#f7fbff_45%,#ffffff_100%)] p-4 pt-6 md:p-8">
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-6">
        <div className="flex flex-col gap-3">
          <div className="inline-flex w-fit items-center gap-2 rounded-full border border-sky-200 bg-white/80 px-3 py-1 text-xs font-semibold uppercase tracking-[0.24em] text-sky-700 shadow-sm">
            <Sparkles className="h-3.5 w-3.5" />
            Assinatura do ERP
          </div>
          <h1 className="text-3xl font-semibold tracking-tight text-slate-900 md:text-4xl">
            Gerencie a cobranca recorrente do Kalles com Stripe
          </h1>
          <p className="max-w-3xl text-sm leading-6 text-slate-600 md:text-base">
            Esta area cuida da assinatura mensal do seu ERP. O checkout abre em modo embutido
            dentro da propria aplicacao, e o portal da Stripe permite atualizar pagamento,
            consultar cobrancas e gerenciar a assinatura.
          </p>
        </div>

        <div className="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
          <Card className="border-sky-100 bg-white/90 shadow-sm">
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-slate-900">
                <CreditCard className="h-5 w-5 text-sky-600" />
                Iniciar assinatura mensal
              </CardTitle>
              <CardDescription>
                Abra o embedded checkout da Stripe sem sair do painel administrativo.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Alert className="border-sky-200 bg-sky-50/70">
                <ShieldCheck className="h-4 w-4 text-sky-700" />
                <AlertTitle className="text-sky-900">Fluxo recomendado</AlertTitle>
                <AlertDescription className="text-sky-800">
                  O backend cria a sessao de assinatura e os webhooks mantem o estado local
                  sincronizado com a Stripe.
                </AlertDescription>
              </Alert>

              <div className="flex flex-col gap-3 sm:flex-row">
                <Button asChild size="lg" className="bg-sky-600 hover:bg-sky-700">
                  <Link href="/admin/assinatura/checkout">Abrir checkout embutido</Link>
                </Button>
                <Button
                  size="lg"
                  variant="outline"
                  onClick={handleOpenPortal}
                  disabled={isOpeningPortal || !returnUrl}
                >
                  <ExternalLink className="mr-2 h-4 w-4" />
                  {isOpeningPortal ? "Abrindo portal..." : "Gerenciar no portal Stripe"}
                </Button>
              </div>
            </CardContent>
          </Card>

          <Card className="border-slate-200 bg-slate-950 text-slate-50 shadow-sm">
            <CardHeader>
              <CardTitle className="text-slate-50">O que esta conectado aqui</CardTitle>
              <CardDescription className="text-slate-300">
                O frontend agora consome os endpoints do backend que voce acabou de integrar.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-3 text-sm text-slate-200">
              <p>
                <code>POST /api/billing/checkout-sessions</code> cria a sessao de assinatura para o embedded checkout.
              </p>
              <p>
                <code>POST /api/billing/portal-sessions</code> devolve a URL do Customer Portal da Stripe.
              </p>
              <p>
                <code>POST /api/billing/webhook</code> continua sendo o ponto que sincroniza renovacao, falha de pagamento e cancelamento.
              </p>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
