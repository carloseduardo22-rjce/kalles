import { Badge } from "@/components/ui/badge";
import { formatSaleState } from "@/shared/utils/formatters";
import type { SaleState } from "../types";

const variantMap: Record<
  SaleState,
  "default" | "secondary" | "destructive" | "outline"
> = {
  OPEN: "default",
  ON_HOLD: "outline",
  PAYMENT_IN_PROGRESS: "secondary",
  PAID: "secondary",
  COMPLETED: "default",
  CANCELED: "destructive",
};

interface SaleStateBadgeProps {
  state: SaleState;
}

export function SaleStateBadge({ state }: SaleStateBadgeProps) {
  return (
    <Badge variant={variantMap[state] ?? "outline"}>
      {formatSaleState(state)}
    </Badge>
  );
}
