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
      "Receba vendas no cartão pela maquininha vinculada ao caixa.",
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
    "Vincule uma maquininha a cada caixa que recebera pagamentos no cartao.",
    "Ao cobrar no PDV, o pedido aparece diretamente na maquininha vinculada.",
    "A venda sera confirmada automaticamente quando o pagamento for aprovado.",
  ],
};
