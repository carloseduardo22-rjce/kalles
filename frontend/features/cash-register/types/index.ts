export type SessionStatus = "OPEN" | "CLOSED";

export interface OpenSessionRequest {
  cashRegisterCode: string;
  operatorCode: string;
  initialAmount: number;
}

export interface SessionResponse {
  sessionId: string;
  operatorId: string;
  cashRegisterCode: string;
  operatorName: string;
  initialAmount: number;
  openedAt: string;
  status: SessionStatus;
}

export interface SessionSummaryResponse {
  vendasConcluidas: number;
  vendasCanceladas: number;
  totalVendido: number;
  totalPorMetodoPagamento: Record<string, number>;
}

export interface CloseSessionResponse {
  sessionId: string;
  codigoCaixa: string;
  nomeOperador: string;
  valorInicial: number;
  abertura: string;
  fechamento: string;
  status: SessionStatus;
  resumo: SessionSummaryResponse;
}

/** Persisted in localStorage */
export interface ActiveSession {
  sessionId: string;
  operatorId: string;
  cashRegisterCode: string;
  operatorName: string;
  initialAmount: number;
  openedAt: string;
}
