import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import type { Priority } from "@/features/support/types";

const PRIORITY_CONFIG: Record<Priority, { label: string; className: string }> =
  {
    LOW: {
      label: "Baixa",
      className: "border-border text-muted-foreground bg-transparent",
    },
    MEDIUM: {
      label: "Média",
      className: "bg-primary text-primary-foreground hover:bg-primary/90",
    },
    HIGH: {
      label: "Alta",
      className:
        "border border-orange-400 text-orange-600 bg-transparent hover:bg-orange-50",
    },
    CRITICAL: {
      label: "Crítica",
      className:
        "bg-destructive text-destructive-foreground hover:bg-destructive/90",
    },
  };

interface TicketPriorityBadgeProps {
  priority: Priority;
  className?: string;
}

export function TicketPriorityBadge({
  priority,
  className,
}: TicketPriorityBadgeProps) {
  const config = PRIORITY_CONFIG[priority];
  return (
    <Badge className={cn(config.className, className)}>{config.label}</Badge>
  );
}
