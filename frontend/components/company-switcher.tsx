"use client";

import { Store } from "lucide-react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { cn } from "@/lib/utils";
import { useCompany } from "@/shared/contexts/company-context";

interface CompanySwitcherProps {
  compact?: boolean;
  className?: string;
}

export function CompanySwitcher({
  compact = false,
  className,
}: CompanySwitcherProps) {
  const { activeCompanyId, activeCompany, companies, loading, setActiveCompany } =
    useCompany();

  if (loading) {
    return (
      <div
        className={cn(
          "rounded-md border bg-muted/40 px-3 py-2 text-xs text-muted-foreground",
          className,
        )}
      >
        Carregando lojas...
      </div>
    );
  }

  if (companies.length === 0) {
    return (
      <div
        className={cn(
          "rounded-md border border-amber-300 bg-amber-50 px-3 py-2 text-xs text-amber-900",
          className,
        )}
      >
        Cadastre uma loja para continuar.
      </div>
    );
  }

  return (
    <div className={cn("space-y-2", className)} data-onboarding="company-switcher">
      <div className="flex items-center gap-2 text-[10px] font-semibold uppercase tracking-wide text-muted-foreground">
        <Store className="h-3.5 w-3.5" />
        Loja ativa
      </div>
      <Select
        value={activeCompanyId || ""}
        onValueChange={(value) => setActiveCompany(value)}
      >
        <SelectTrigger className={cn("w-full", compact ? "h-8 text-xs" : "h-10")}>
          <SelectValue placeholder="Selecione uma loja" />
        </SelectTrigger>
        <SelectContent>
          {companies.map((company) => (
            <SelectItem key={company.id} value={company.id}>
              {company.name}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      {!compact && (
        <p className="text-xs text-muted-foreground">
          Os dados exibidos seguem a loja selecionada.
        </p>
      )}
      {compact && (
        <p className="truncate text-xs font-semibold text-primary">
          {activeCompany?.name ?? "Nenhuma loja selecionada"}
        </p>
      )}
    </div>
  );
}
