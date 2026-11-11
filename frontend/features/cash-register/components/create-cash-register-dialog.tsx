"use client";

import { useState } from "react";
import { Loader2, Plus, Store } from "lucide-react";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { cashRegisterService } from "@/features/cash-register/services/cash-register.service";

export function CreateCashRegisterDialog() {
  const [open, setOpen] = useState(false);
  const [description, setDescription] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const queryClient = useQueryClient();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    if (!description.trim()) {
      toast.error("Informe a descrição do caixa.");
      return;
    }

    setIsSubmitting(true);
    try {
      const created = await cashRegisterService.createCashRegister({
        description: description.trim(),
      });
      toast.success(`Caixa ${created.code} criado.`);
      setOpen(false);
      setDescription("");
      queryClient.invalidateQueries({ queryKey: ["cash-registers"] });
    } catch (err: any) {
      const msg = err.message || "Erro ao criar caixa.";
      toast.error(msg);
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleOpenChange(newOpen: boolean) {
    if (!newOpen) {
      setDescription("");
    }
    setOpen(newOpen);
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Novo Caixa
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Store className="size-5 text-emerald-600" />
            Cadastrar Novo Caixa
          </DialogTitle>
          <DialogDescription>
            O código do caixa é gerado pelo sistema em sequência (CAIXA-01,
            CAIXA-02...). Informe só a descrição.
          </DialogDescription>
        </DialogHeader>

        <form
          id="create-cash-register-form"
          onSubmit={handleSubmit}
          className="space-y-4 pt-2"
        >
          <div className="space-y-1.5">
            <Label htmlFor="description">
              Descrição <span className="text-destructive">*</span>
            </Label>
            <Input
              id="description"
              placeholder="Ex: Caixa Principal"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              disabled={isSubmitting}
            />
          </div>
        </form>

        <DialogFooter>
          <Button
            variant="ghost"
            onClick={() => setOpen(false)}
            disabled={isSubmitting}
          >
            Cancelar
          </Button>
          <Button
            type="submit"
            form="create-cash-register-form"
            disabled={isSubmitting}
          >
            {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            {isSubmitting ? "Salvando..." : "Salvar"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
