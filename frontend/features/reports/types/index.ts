export interface SupplierExpenseProductItemResponse {
  productId: string;
  productName: string;
  productInternalCode: string;
  totalQuantity: number;
  averageUnitCost: number;
  totalCost: number;
}

export interface ProfitSupplierExpenseReportResponse {
  startDate: string;
  endDate: string;
  totalSales: number;
  totalSupplierExpenses: number;
  estimatedProfit: number;
  marginPercentage: number;
  generatedAt: string;
  purchasedProducts: SupplierExpenseProductItemResponse[];
}
