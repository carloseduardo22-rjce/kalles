import { api } from "@/shared/services/api";
import type { MpStore, MpPos } from "./mercadopago.types";

export const mercadopagoService = {
  // Busca as lojas fÃ­sicas vinculadas na conta do Mercado Pago
  listStores: (): Promise<MpStore[]> =>
    api.get<MpStore[]>("/api/mercadopago/stores"),

  // Busca todos os PDVs (caixas) vinculados e os filtramos por loja no front ou back
  listPos: (): Promise<MpPos[]> => api.get<MpPos[]>("/api/mercadopago/pos"),
};
