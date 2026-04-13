"use client";

import { useCallback, useEffect, useState } from "react";
import type {
  ActiveSession,
  CloseSessionRequest,
  CloseSessionResponse,
} from "../types";
import { cashRegisterService } from "../services/cash-register.service";
import { ApiError } from "@/shared/services/api";
import {
  getSessionScopedItem,
  removeSessionScopedItem,
  setSessionScopedItem,
} from "@/shared/utils/session-storage";

const STORAGE_KEY = "kalles:active-session";

function readSession(): ActiveSession | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = getSessionScopedItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as ActiveSession) : null;
  } catch {
    return null;
  }
}

function saveSession(session: ActiveSession): void {
  setSessionScopedItem(STORAGE_KEY, JSON.stringify(session));
}

function clearSession(): void {
  removeSessionScopedItem(STORAGE_KEY);
}

interface UseSessionReturn {
  session: ActiveSession | null;
  isLoading: boolean;
  error: string | null;
  openSession: (
    cashRegisterCode: string,
    operatorCode: string,
    initialAmount: number,
  ) => Promise<void>;
  closeSession: (
    request: CloseSessionRequest,
  ) => Promise<CloseSessionResponse | null>;
  clearError: () => void;
}

export function useSession(): UseSessionReturn {
  const [session, setSession] = useState<ActiveSession | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Hydrate from localStorage on mount
  useEffect(() => {
    setSession(readSession());
  }, []);

  const openSession = useCallback(
    async (
      cashRegisterCode: string,
      operatorCode: string,
      initialAmount: number,
    ) => {
      setIsLoading(true);
      setError(null);
      try {
        const response = await cashRegisterService.openSession({
          cashRegisterCode,
          operatorCode,
          initialAmount,
        });

        const active: ActiveSession = {
          sessionId: response.sessionId,
          operatorId: response.operatorId,
          cashRegisterCode: response.cashRegisterCode,
          operatorName: response.operatorName,
          initialAmount: response.initialAmount,
          openedAt: response.openedAt,
        };

        saveSession(active);
        setSession(active);
      } catch (err) {
        setError(
          err instanceof ApiError ? err.message : "Falha ao abrir sessão.",
        );
        throw err;
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  const closeSession = useCallback(
    async (request: CloseSessionRequest): Promise<CloseSessionResponse | null> => {
      if (!session) return null;
      setIsLoading(true);
      setError(null);
      try {
        const result = await cashRegisterService.closeSession(session.sessionId, request);
        clearSession();
        setSession(null);
        return result;
      } catch (err) {
        setError(
          err instanceof ApiError ? err.message : "Falha ao fechar sessão.",
        );
        throw err;
      } finally {
        setIsLoading(false);
      }
    },
    [session],
  );

  const clearError = useCallback(() => setError(null), []);

  return { session, isLoading, error, openSession, closeSession, clearError };
}
