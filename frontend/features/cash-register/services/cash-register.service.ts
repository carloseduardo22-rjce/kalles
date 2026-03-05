import { api } from "@/shared/services/api";
import type {
  CashRegisterStatusResponse,
  CloseSessionResponse,
  OpenSessionRequest,
  OperatorResponse,
  SessionResponse,
  SessionSummaryResponse,
} from "../types";

const BASE = "/api/cash-register-sessions";
const REGISTERS_BASE = "/api/cash-registers";

export const cashRegisterService = {
  openSession: (body: OpenSessionRequest): Promise<SessionResponse> =>
    api.post<SessionResponse>(`${BASE}/open`, body),

  closeSession: (sessionId: string): Promise<CloseSessionResponse> =>
    api.post<CloseSessionResponse>(`${BASE}/${sessionId}/close`),

  getReport: (sessionId: string): Promise<SessionSummaryResponse> =>
    api.get<SessionSummaryResponse>(`${BASE}/${sessionId}/report`),

  /** Lista caixas ativos com status da sessão corrente (tela do ADMIN). */
  listCashRegisters: (): Promise<CashRegisterStatusResponse[]> =>
    api.get<CashRegisterStatusResponse[]>(REGISTERS_BASE),

  /** Lista operadores disponíveis para vinculação. */
  listOperators: (): Promise<OperatorResponse[]> =>
    api.get<OperatorResponse[]>(`${REGISTERS_BASE}/operators`),
};
