import { api } from "@/shared/services/api";
import type {
  PaymentExecutionResult,
  PaymentProviderAdapter,
  TerminalPaymentCommand,
  TerminalPaymentResult,
} from "../types";

const PROVIDER = "STONE" as const;

export const stoneProvider: PaymentProviderAdapter = {
  id: PROVIDER,
  presentation: {
    displayName: "Stone Connect",
    shortName: "Stone",
    accentColor: "#111111",
    description:
      "Fluxos transacionais Connect 2.0 preparados no backend payment, com foco em terminal e webhooks.",
  },
  capabilities: {
    accountLink: false,
    storeManagement: false,
    pointManagement: false,
    terminalActivation: false,
    qrCodeAtPdv: false,
    terminalChargeAtPdv: true,
  },
  auth: {
    mode: "none",
  },
  processTerminalPayment: async (
    command: TerminalPaymentCommand,
  ): Promise<TerminalPaymentResult> => {
    const response = await api.post<PaymentExecutionResult>(
      "/api/payments/process",
      {
        provider: PROVIDER,
        flow: "TERMINAL",
        externalReference: command.externalReference,
        amount: command.amount,
        targetId: command.targetId,
        methodType: command.methodType,
        metadata: {
          stoneFlow: "DIRECT",
          ...command.metadata,
        },
      },
    );

    return {
      provider: PROVIDER,
      orderId: response.providerOrderId,
      providerPaymentId: response.providerPaymentId,
      status: response.status,
      metadata: response.metadata,
    };
  },
  operationalNotes: [
    "O backend ja suporta pedidos, fechamento, impressao e webhooks da Stone Connect 2.0.",
    "A tela de onboarding do terminal ainda precisa do mapeamento operacional de terminal por caixa no frontend.",
    "A experiencia de venda em terminal Stone sera acoplada a este adapter, sem espalhar regras da API externa pelas telas.",
  ],
};
