"use client";

import { useQuery } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { categoryService } from "@/features/support/services/category.service";
import type { OpenTicketRequest } from "@/features/support/types";

interface OpenTicketDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: OpenTicketRequest) => void;
  isPending: boolean;
}

export function OpenTicketDialog({
  open,
  onOpenChange,
  onSubmit,
  isPending,
}: OpenTicketDialogProps) {
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<OpenTicketRequest>();

  const { data: categories = [], isLoading: loadingCategories } = useQuery({
    queryKey: ["support-categories"],
    queryFn: () => categoryService.listAll(),
    enabled: open,
  });

  const selectedCategoryId = watch("categoryId");

  function handleClose(value: boolean) {
    if (!value) reset();
    onOpenChange(value);
  }

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Abrir novo chamado</DialogTitle>
          <DialogDescription>
            Descreva o problema ou dúvida. Nossa equipe entrará em contato em
            breve.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Personal info */}
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <Label htmlFor="userName">Seu nome *</Label>
              <Input
                id="userName"
                {...register("userName", { required: "Nome é obrigatório" })}
                placeholder="Nome completo"
              />
              {errors.userName && (
                <p className="text-xs text-destructive">
                  {errors.userName.message}
                </p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="userEmail">E-mail *</Label>
              <Input
                id="userEmail"
                type="email"
                {...register("userEmail", {
                  required: "E-mail é obrigatório",
                  pattern: {
                    value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                    message: "E-mail inválido",
                  },
                })}
                placeholder="seu@email.com"
              />
              {errors.userEmail && (
                <p className="text-xs text-destructive">
                  {errors.userEmail.message}
                </p>
              )}
            </div>
          </div>

          {/* Ticket info */}
          <div className="space-y-1.5">
            <Label htmlFor="title">Título *</Label>
            <Input
              id="title"
              {...register("title", {
                required: "Título é obrigatório",
                maxLength: {
                  value: 255,
                  message: "Título deve ter no máximo 255 caracteres",
                },
              })}
              placeholder="Resumo breve do problema"
            />
            {errors.title && (
              <p className="text-xs text-destructive">{errors.title.message}</p>
            )}
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="description">Descrição *</Label>
            <Textarea
              id="description"
              rows={4}
              {...register("description", {
                required: "Descrição é obrigatória",
              })}
              placeholder="Descreva o problema com o máximo de detalhes possível…"
            />
            {errors.description && (
              <p className="text-xs text-destructive">
                {errors.description.message}
              </p>
            )}
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="categoryId">Categoria *</Label>
            <Select
              value={selectedCategoryId}
              onValueChange={(val) => setValue("categoryId", val)}
              disabled={loadingCategories}
            >
              <SelectTrigger id="categoryId">
                <SelectValue
                  placeholder={
                    loadingCategories
                      ? "Carregando…"
                      : "Selecione uma categoria"
                  }
                />
              </SelectTrigger>
              <SelectContent>
                {categories.map((cat) => (
                  <SelectItem key={cat.id} value={cat.id}>
                    {cat.name}
                    {cat.subcategory ? ` — ${cat.subcategory}` : ""}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.categoryId && (
              <p className="text-xs text-destructive">
                Categoria é obrigatória
              </p>
            )}
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => handleClose(false)}
              disabled={isPending}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending || loadingCategories}>
              {isPending ? <LoadingSpinner size="sm" /> : "Abrir chamado"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
