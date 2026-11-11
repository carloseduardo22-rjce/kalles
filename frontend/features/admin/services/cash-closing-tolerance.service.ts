import { api } from "@/shared/services/api";

export interface CashClosingToleranceResponse {
  amount: number;
}

const toleranceUrl = (companyId: string) =>
  `/api/companies/${companyId}/cash-closing-tolerance`;

export const cashClosingToleranceService = {
  get: (companyId: string): Promise<CashClosingToleranceResponse> =>
    api.get<CashClosingToleranceResponse>(toleranceUrl(companyId)),

  update: (
    companyId: string,
    amount: number,
  ): Promise<CashClosingToleranceResponse> =>
    api.put<CashClosingToleranceResponse>(toleranceUrl(companyId), { amount }),
};
