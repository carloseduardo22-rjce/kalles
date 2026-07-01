import { api } from "@/shared/services/api";
import type { PaymentProviderId, PaymentTerminalMapping } from "../types";

const BASE = "/api/payment-terminal-mappings";

export const paymentTerminalMappingService = {
  list: (provider: PaymentProviderId): Promise<PaymentTerminalMapping[]> =>
    api.get<PaymentTerminalMapping[]>(`${BASE}?provider=${provider}`),

  findByCashRegister: (
    cashRegisterId: string,
    provider: PaymentProviderId,
  ): Promise<PaymentTerminalMapping> =>
    api.get<PaymentTerminalMapping>(
      `${BASE}/by-cash-register?cashRegisterId=${cashRegisterId}&provider=${provider}`,
    ),

  mapTerminal: (body: {
    cashRegisterId: string;
    provider: PaymentProviderId;
    terminalSerial: string;
  }): Promise<PaymentTerminalMapping> =>
    api.post<PaymentTerminalMapping>(BASE, body),
};
