import { api } from "@/shared/services/api";
import { removeSessionScopedItem } from "@/shared/utils/session-storage";
import type { AuthMeResponse } from "@/shared/types";

const SIGNED_IN_KEYS = ["@kalles:activeCompanyId", "kalles:active-session"];

export const authService = {
  me: (): Promise<AuthMeResponse> => api.get<AuthMeResponse>("/api/auth/me"),

  async logout(): Promise<void> {
    try {
      await api.post("/api/auth/logout");
    } finally {
      SIGNED_IN_KEYS.forEach(removeSessionScopedItem);
      window.location.replace("/login");
    }
  },
};
