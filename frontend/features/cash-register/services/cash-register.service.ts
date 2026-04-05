import { api } from "@/shared/services/api";
import type {
  CloseSessionRequest,
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

  closeSession: (
    sessionId: string,
    body: CloseSessionRequest,
  ): Promise<CloseSessionResponse> =>
    api.post<CloseSessionResponse>(`${BASE}/${sessionId}/close`, body),

  getReport: (sessionId: string): Promise<SessionSummaryResponse> =>
    api.get<SessionSummaryResponse>(`${BASE}/${sessionId}/report`),

  getSessionDetails: (sessionId: string): Promise<CloseSessionResponse> =>
    api.get<CloseSessionResponse>(`${BASE}/${sessionId}`),

  /** Lista caixas ativos com status da sessão corrente (tela do ADMIN). */
  listCashRegisters: (): Promise<CashRegisterStatusResponse[]> =>
    api.get<CashRegisterStatusResponse[]>(REGISTERS_BASE),

  /** Cria um novo caixa. */
  createCashRegister: (body: {
    code: string;
    description: string;
  }): Promise<void> => api.post<void>(REGISTERS_BASE, body),

  /** Lista operadores disponíveis para vinculação. */
  listOperators: (): Promise<OperatorResponse[]> =>
    api.get<OperatorResponse[]>(`${REGISTERS_BASE}/operators`),

  /** Lista sessões (abertas ou fechadas) cujas aberturas ocorreram entre startDate e endDate (YYYY-MM-DD, inclusivo). */
  listSessionsByDateRange: (
    startDate: string,
    endDate: string,
  ): Promise<CloseSessionResponse[]> =>
    api.get<CloseSessionResponse[]>(
      `${BASE}?startDate=${startDate}&endDate=${endDate}`,
    ),
};
