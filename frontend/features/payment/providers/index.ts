import type { PaymentProviderAdapter, PaymentProviderId } from "../types";
import { mercadoPagoProvider } from "./mercado-pago-provider";
import { stoneProvider } from "./stone-provider";

const providers: PaymentProviderAdapter[] = [
  mercadoPagoProvider,
  stoneProvider,
];

export const paymentProviders = providers;

export function getPaymentProvider(
  providerId: PaymentProviderId,
): PaymentProviderAdapter {
  const provider = providers.find((entry) => entry.id === providerId);
  if (!provider) {
    throw new Error(`Payment provider not registered: ${providerId}`);
  }

  return provider;
}

export function getDefaultQrCodeProvider(): PaymentProviderAdapter | null {
  return providers.find((provider) => provider.capabilities.qrCodeAtPdv) ?? null;
}

export function getProvidersWithTerminalCharge(): PaymentProviderAdapter[] {
  return providers.filter((provider) => provider.capabilities.terminalChargeAtPdv);
}
