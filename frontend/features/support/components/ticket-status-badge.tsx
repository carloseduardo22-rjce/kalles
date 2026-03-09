import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import type { TicketStatus } from "@/features/support/types";

const STATUS_CONFIG: Record<
  TicketStatus,
  { label: string; className: string }
> = {
  OPEN: {
    label: "Aberto",
    className: "bg-sky-600 text-white hover:bg-sky-700",
  },
  IN_PROGRESS: {
    label: "Em atendimento",
    className: "bg-amber-500 text-white hover:bg-amber-600",
  },
  WAITING_FOR_CUSTOMER: {
    label: "Aguardando cliente",
    className: "bg-yellow-400 text-yellow-900 hover:bg-yellow-500",
  },
  RESOLVED: {
    label: "Resolvido",
    className: "bg-emerald-600 text-white hover:bg-emerald-700",
  },
  CLOSED: {
    label: "Fechado",
    className: "bg-muted text-muted-foreground border-border hover:bg-muted/80",
  },
};

interface TicketStatusBadgeProps {
  status: TicketStatus;
  className?: string;
}

export function TicketStatusBadge({
  status,
  className,
}: TicketStatusBadgeProps) {
  const config = STATUS_CONFIG[status];
  return (
    <Badge className={cn(config.className, className)}>{config.label}</Badge>
  );
}
