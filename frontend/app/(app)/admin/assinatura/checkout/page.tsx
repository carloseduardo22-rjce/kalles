"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { StripeEmbeddedCheckout } from "@/features/admin/components/stripe-embedded-checkout";

export default function BillingCheckoutPage() {
  const [returnUrl, setReturnUrl] = useState("");

  useEffect(() => {
    setReturnUrl(`${window.location.origin}/admin/assinatura?checkout=complete`);
  }, []);

  return (
    <div className="min-h-screen bg-[linear-gradient(180deg,#eef7ff_0%,#ffffff_55%)] p-4 pt-6 md:p-8">
      <div className="mx-auto flex w-full max-w-5xl flex-col gap-6">
        <div className="flex items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-semibold tracking-tight text-slate-900">
              Checkout da assinatura
            </h1>
            <p className="mt-2 text-sm text-slate-600">
              Conclua a contratacao mensal do ERP sem sair do painel.
            </p>
          </div>

          <Button asChild variant="outline">
            <Link href="/admin/assinatura">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Voltar
            </Link>
          </Button>
        </div>

        <Card className="border-sky-100 bg-white/90 shadow-sm">
          <CardHeader>
            <CardTitle>Stripe Embedded Checkout</CardTitle>
            <CardDescription>
              O formulario abaixo e servido pela Stripe e usa o client secret criado pelo backend.
            </CardDescription>
          </CardHeader>
          <CardContent>
            {returnUrl ? (
              <StripeEmbeddedCheckout returnUrl={returnUrl} />
            ) : (
              <div className="flex min-h-56 items-center justify-center">
                <LoadingSpinner />
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
