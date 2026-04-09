import { api } from "@/shared/services/api";
import type {
  PaymentExecutionResult,
  PaymentPoint,
  PaymentProviderAdapter,
  PaymentProviderStatus,
  PaymentStore,
  PaymentStoreStatus,
  PaymentTerminal,
  QrPaymentResult,
} from "../types";

const PROVIDER = "MERCADO_PAGO" as const;
const DEFAULT_APP_ID = "448684586415948";
const DEFAULT_REDIRECT_URI =
  "https://2dbd-2804-1494-dbb-aa00-ad55-f249-5eaf-70fa.ngrok-free.app/admin/pagamentos/mp-callback";

export const mercadoPagoProvider: PaymentProviderAdapter = {
  id: PROVIDER,
  presentation: {
    displayName: "Mercado Pago",
    shortName: "MP",
    accentColor: "#009EE3",
    description:
      "Onboarding completo de conta, loja, POS e QR dinamico pelo contrato generico de payment.",
    logoSrc: "/mercado-pago-logo.png",
  },
  capabilities: {
    accountLink: true,
    storeManagement: true,
    pointManagement: true,
    terminalActivation: true,
    qrCodeAtPdv: true,
    terminalChargeAtPdv: false,
  },
  auth: {
    mode: "oauth",
    callbackPath: "/admin/pagamentos/mp-callback",
    buildAuthorizationUrl: (tenantId: string) => {
      const appId = process.env.NEXT_PUBLIC_MP_APP_ID || DEFAULT_APP_ID;
      const redirectUri =
        process.env.NEXT_PUBLIC_MP_REDIRECT_URI || DEFAULT_REDIRECT_URI;

      return `https://auth.mercadopago.com/authorization?client_id=${appId}&response_type=code&platform_id=mp&state=${tenantId}&redirect_uri=${redirectUri}`;
    },
  },
  getProviderStatus: (): Promise<PaymentProviderStatus> =>
    api.get<PaymentProviderStatus>(`/api/payment-providers/${PROVIDER}/status`),
  getCurrentStoreStatus: async (): Promise<PaymentStoreStatus> => {
    try {
      return await api.get<PaymentStoreStatus>(
        `/api/payment-stores/${PROVIDER}/my-status`,
      );
    } catch {
      return {
        provider: PROVIDER,
        companyExists: false,
        hasStoreRegistered: false,
        externalReference: null,
        providerStoreId: null,
      };
    }
  },
  linkAccount: (authorizationCode: string, state: string): Promise<void> =>
    api.post<void>("/api/payment-providers/link", {
      provider: PROVIDER,
      authorizationCode,
      state,
    }),
  createStore: (companyId: string, externalReference: string) =>
    api.post<PaymentStore>("/api/payment-stores", {
      provider: PROVIDER,
      companyId,
      externalReference,
    }),
  createPoint: (cashRegisterId: string, externalReference?: string) =>
    api.post<PaymentPoint>("/api/payment-points", {
      provider: PROVIDER,
      cashRegisterId,
      externalReference,
    }),
  listStores: (): Promise<PaymentStore[]> =>
    api.get<PaymentStore[]>(`/api/payment-stores?provider=${PROVIDER}`),
  listPoints: (): Promise<PaymentPoint[]> =>
    api.get<PaymentPoint[]>(`/api/payment-points?provider=${PROVIDER}`),
  listTerminals: (
    storeId: string,
    pointId: string,
  ): Promise<PaymentTerminal[]> =>
    api.get<PaymentTerminal[]>(
      `/api/payment-terminals?provider=${PROVIDER}&storeId=${storeId}&pointId=${pointId}`,
    ),
  activatePointOfSale: (
    storeId: string,
    pointId: string,
    terminalSerial: string,
  ): Promise<void> =>
    api.post<void>("/api/payment-terminals/activate-point-of-sale", {
      provider: PROVIDER,
      storeId,
      pointId,
      terminalSerial,
    }),
  processQrPayment: async (
    externalReference: string,
    amount: number,
    targetId: string,
  ): Promise<QrPaymentResult> => {
    const response = await api.post<PaymentExecutionResult>(
      "/api/payments/process",
      {
        provider: PROVIDER,
        flow: "QR_CODE",
        externalReference,
        amount,
        targetId,
      },
    );

    return {
      provider: PROVIDER,
      orderId: response.providerOrderId,
      qrData: String(response.metadata?.qrData ?? ""),
    };
  },
  operationalNotes: [
    "Usa a borda generica de payment para conta, loja, POS, terminais e QR dinamico.",
    "Continua sendo o provider pronto para o fluxo PIX por QR no PDV atual.",
  ],
};
