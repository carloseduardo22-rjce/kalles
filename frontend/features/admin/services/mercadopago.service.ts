import { api } from "@/shared/services/api";
import type { MpStore, MpPos, MpTerminal } from "./mercadopago.types";

export const mercadopagoService = {
  // Busca as lojas fÃ­sicas vinculadas na conta do Mercado Pago
  listStores: (): Promise<MpStore[]> =>
    api.get<MpStore[]>("/api/mercadopago/stores"),
  // Busca todos os PDVs (caixas) vinculados e os filtramos por loja no front ou back
  listPos: (): Promise<MpPos[]> => api.get<MpPos[]>("/api/mercadopago/pos"),
  // Busca todos os terminais vinculados
  listTerminals: (storeId: string, posId: string): Promise<MpTerminal[]> =>
    api.get<MpTerminal[]>(
      `/api/mercadopago/terminals?storeId=${storeId}&posId=${posId}`,
    ),
  activatePdv: (
    storeId: string,
    posId: string,
    terminalSerial: string,
  ): Promise<void> =>
    api.post("/api/mercadopago/terminals/activate-pdv", {
      storeId,
      posId,
      terminalSerial,
    }),
};
