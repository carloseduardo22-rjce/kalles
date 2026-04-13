import type {
  FidelityPolicyRequest,
  GoalRequest,
  LocationRequest,
  OperatorRequest,
  ProductRequest,
  StockRequest,
  WarehouseRequest,
} from "@/features/admin/types";

function trimValue(value?: string | null): string {
  return value?.trim() ?? "";
}

function trimToNull(value?: string | null): string | null {
  const trimmed = trimValue(value);
  return trimmed ? trimmed : null;
}

function normalizeCode(value?: string | null): string {
  return trimValue(value).replace(/\s+/g, " ").toUpperCase();
}

export function normalizeProductRequest(data: ProductRequest): ProductRequest {
  return {
    ...data,
    name: trimValue(data.name),
    internalCode: normalizeCode(data.internalCode),
    barcode: trimToNull(data.barcode),
    description: trimToNull(data.description),
  };
}

export function normalizeOperatorRequest(data: OperatorRequest): OperatorRequest {
  return {
    ...data,
    name: trimValue(data.name),
    code: trimValue(data.code).toLowerCase(),
  };
}

export function normalizeWarehouseRequest(
  data: WarehouseRequest,
): WarehouseRequest {
  return {
    ...data,
    name: trimValue(data.name),
    address: trimToNull(data.address),
  };
}

export function normalizeLocationRequest(data: LocationRequest): LocationRequest {
  return {
    ...data,
    code: normalizeCode(data.code),
    description: trimToNull(data.description),
  };
}

export function normalizeGoalRequest(data: GoalRequest): GoalRequest {
  return {
    ...data,
    startDate: trimValue(data.startDate),
    endDate: trimValue(data.endDate),
  };
}

export function normalizeFidelityPolicyRequest(
  data: FidelityPolicyRequest,
): FidelityPolicyRequest {
  return {
    ...data,
    objectivePoints: Math.trunc(data.objectivePoints),
    valuePoint: Math.trunc(data.valuePoint),
  };
}

export function normalizeStockRequest(data: StockRequest): StockRequest {
  return {
    ...data,
    quantity: Math.trunc(data.quantity),
    unitCost:
      data.unitCost == null || Number.isNaN(data.unitCost) ? null : data.unitCost,
  };
}
