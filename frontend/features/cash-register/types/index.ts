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
  totalEmDinheiro: number;
  saldoEsperadoEmCaixa: number;
  valorInformadoEmCaixa: number | null;
  diferencaEmCaixa: number | null;
}

export interface CloseSessionResponse {
  sessionId: string;
  codigoCaixa: string;
  nomeOperador: string;
  codigoOperadorAutorizador: string | null;
  nomeOperadorAutorizador: string | null;
  valorInicial: number;
  abertura: string;
  fechamento: string;
  status: SessionStatus;
  resumo: SessionSummaryResponse;
}

export interface CloseSessionRequest {
  authorizedOperatorCode: string;
  countedCashAmount: number;
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

/** Status de um caixa com dados da sessão ativa (se existir) */
export interface CashRegisterStatusResponse {
  cashRegisterId: string;
  code: string;
  description: string;
  active: boolean;
  hasActiveSession: boolean;
  activeSessionId: string | null;
  activeOperatorName: string | null;
  initialAmount: number | null;
  openedAt: string | null;
  paymentIntegrationConfigured: boolean;
}

/** Operador disponível para vinculação a uma sessão */
export interface OperatorResponse {
  id: string;
  name: string;
  code: string;
  permissionLevel: string | null;
}
