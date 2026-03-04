import { InboxIcon } from "lucide-react";
import { cn } from "@/lib/utils";

interface EmptyStateProps {
  title?: string;
  description?: string;
  className?: string;
  icon?: React.ReactNode;
}

export function EmptyState({
  title = "Nenhum item encontrado",
  description,
  className,
  icon,
}: EmptyStateProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center gap-2 py-12 text-center text-muted-foreground",
        className,
      )}
    >
      {icon ?? <InboxIcon className="h-10 w-10 opacity-40" />}
      <p className="text-sm font-medium">{title}</p>
      {description && <p className="text-xs">{description}</p>}
    </div>
  );
}
