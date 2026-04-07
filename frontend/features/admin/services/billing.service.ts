import { api } from "@/shared/services/api";

export interface BillingCheckoutSessionResponse {
  sessionId: string;
  clientSecret: string;
  url: string | null;
  status: string;
}

export interface BillingPortalSessionResponse {
  url: string;
}

export const billingService = {
  createCheckoutSession: (returnUrl: string) =>
    api.post<BillingCheckoutSessionResponse>("/api/billing/checkout-sessions", {
      returnUrl,
    }),

  createPortalSession: (returnUrl: string) =>
    api.post<BillingPortalSessionResponse>("/api/billing/portal-sessions", {
      returnUrl,
    }),
};
