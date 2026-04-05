import { api } from "@/shared/services/api";
import type { ProfitSupplierExpenseReportResponse } from "@/features/reports/types";

const BASE = "/api/reports";

export const financialReportService = {
  getProfitVsSupplierExpenses: (
    startDate: string,
    endDate: string,
  ): Promise<ProfitSupplierExpenseReportResponse> =>
    api.get<ProfitSupplierExpenseReportResponse>(
      `${BASE}/profit-vs-supplier-expenses?startDate=${startDate}&endDate=${endDate}`,
    ),
};
