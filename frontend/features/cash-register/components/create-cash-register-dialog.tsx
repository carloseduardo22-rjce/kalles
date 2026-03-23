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
  const [code, setCode] = useState("");
  const [description, setDescription] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const queryClient = useQueryClient();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    if (!code.trim() || !description.trim()) {
      toast.error("Por favor, preencha o código e a descrição.");
      return;
    }

    setIsSubmitting(true);
    try {
      await cashRegisterService.createCashRegister({
        code: code.trim(),
        description: description.trim(),
      });
      toast.success("Caixa criado com sucesso!");
      setOpen(false);
      setCode("");
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
      setCode("");
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
            Preencha os detalhes para registrar um novo caixa no sistema.
          </DialogDescription>
        </DialogHeader>

        <form
          id="create-cash-register-form"
          onSubmit={handleSubmit}
          className="space-y-4 pt-2"
        >
          <div className="space-y-1.5">
            <Label htmlFor="code">
              Código do Caixa <span className="text-destructive">*</span>
            </Label>
            <Input
              id="code"
              placeholder="Ex: CX-01"
              value={code}
              onChange={(e) => setCode(e.target.value.toUpperCase())}
              disabled={isSubmitting}
            />
          </div>
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
