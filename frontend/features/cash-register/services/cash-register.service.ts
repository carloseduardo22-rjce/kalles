import { api } from "@/shared/services/api";
import type {
  CloseSessionResponse,
  OpenSessionRequest,
  SessionResponse,
  SessionSummaryResponse,
} from "../types";

const BASE = "/api/cash-register-sessions";

export const cashRegisterService = {
  openSession: (body: OpenSessionRequest): Promise<SessionResponse> =>
    api.post<SessionResponse>(`${BASE}/open`, body),

  closeSession: (sessionId: string): Promise<CloseSessionResponse> =>
    api.post<CloseSessionResponse>(`${BASE}/${sessionId}/close`),

  getReport: (sessionId: string): Promise<SessionSummaryResponse> =>
    api.get<SessionSummaryResponse>(`${BASE}/${sessionId}/report`),
};
