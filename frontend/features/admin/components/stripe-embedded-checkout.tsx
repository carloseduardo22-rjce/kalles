"use client";

import { EmbeddedCheckout, EmbeddedCheckoutProvider } from "@stripe/react-stripe-js";
import { loadStripe } from "@stripe/stripe-js";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { billingService } from "@/features/admin/services/billing.service";

const stripePublishableKey = process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY ?? "";
const stripePromise = stripePublishableKey ? loadStripe(stripePublishableKey) : null;

interface StripeEmbeddedCheckoutProps {
  returnUrl: string;
}

export function StripeEmbeddedCheckout({
  returnUrl,
}: StripeEmbeddedCheckoutProps) {
  if (!stripePromise) {
    return (
      <Alert variant="destructive">
        <AlertTitle>Stripe nao configurada no frontend</AlertTitle>
        <AlertDescription>
          Defina a variavel NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY para carregar o checkout embutido.
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <EmbeddedCheckoutProvider
      stripe={stripePromise}
      options={{
        fetchClientSecret: async () => {
          const session = await billingService.createCheckoutSession(returnUrl);
          return session.clientSecret;
        },
      }}
    >
      <EmbeddedCheckout />
    </EmbeddedCheckoutProvider>
  );
}
