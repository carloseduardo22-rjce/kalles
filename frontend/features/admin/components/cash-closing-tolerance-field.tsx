"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { cashClosingToleranceService } from "@/features/admin/services/cash-closing-tolerance.service";

export function CashClosingToleranceField({ companyId }: { companyId: string }) {
  const queryKey = ["cash-closing-tolerance", companyId];
  const fieldId = `tolerance-${companyId}`;
  const queryClient = useQueryClient();
  const [draft, setDraft] = useState<string | null>(null);

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

  const amount = draft ?? tolerance.data?.amount.toFixed(2) ?? "";

  function handleSave() {
    const value = Number(amount);
    if (!amount.trim() || Number.isNaN(value) || value < 0) {
      toast.error("Informe uma tolerância igual ou maior que zero.");
      return;
    }
    save.mutate(value);
  }

  return (
    <div className="space-y-1.5">
      <Label htmlFor={fieldId}>Tolerância no fechamento de caixa (R$)</Label>
      <div className="flex gap-2">
        <Input
          id={fieldId}
          type="number"
          min="0"
          step="0.01"
          value={amount}
          onChange={(e) => setDraft(e.target.value)}
          disabled={tolerance.isLoading || tolerance.isError}
          autoComplete="off"
          data-testid={fieldId}
        />
        <Button
          type="button"
          variant="outline"
          onClick={handleSave}
          disabled={save.isPending || tolerance.isError || !amount.trim()}
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
