"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { MoneyInput } from "@/shared/components/money-input";
import { cashClosingToleranceService } from "@/features/admin/services/cash-closing-tolerance.service";

export function CashClosingToleranceField({ companyId }: { companyId: string }) {
  const queryKey = ["cash-closing-tolerance", companyId];
  const fieldId = `tolerance-${companyId}`;
  const queryClient = useQueryClient();
  const [draft, setDraft] = useState<number | null>(null);

  const tolerance = useQuery({
    queryKey,
    queryFn: () => cashClosingToleranceService.get(companyId),
  });

  const save = useMutation({
    mutationFn: (amount: number) => cashClosingToleranceService.update(companyId, amount),
    onSuccess: (response) => {
      queryClient.setQueryData(queryKey, response);
      setDraft(null);
      toast.success("Tolerância de caixa atualizada.");
    },
    onError: () => toast.error("Erro ao salvar a tolerância de caixa."),
  });

  const amount = draft ?? tolerance.data?.amount ?? 0;

  function handleSave() {
    save.mutate(amount);
  }

  return (
    <div className="space-y-1.5">
      <Label htmlFor={fieldId}>Tolerância no fechamento de caixa</Label>
      <div className="flex gap-2">
        <div className="flex-1">
          <MoneyInput
            id={fieldId}
            value={amount}
            onValueChange={setDraft}
            disabled={tolerance.isLoading || tolerance.isError}
            data-testid={fieldId}
          />
        </div>
        <Button
          type="button"
          variant="outline"
          onClick={handleSave}
          disabled={save.isPending || tolerance.isLoading || tolerance.isError}
        >
          {save.isPending && <Loader2 className="h-4 w-4 animate-spin mr-2" />}
          Salvar
        </Button>
      </div>
      {tolerance.isError ? (
        <p className="text-xs text-destructive">Não foi possível carregar a tolerância desta loja.</p>
      ) : (
        <p className="text-xs text-muted-foreground">
          Diferenças até este valor fecham o caixa sem liberação de supervisor.
        </p>
      )}
    </div>
  );
}
