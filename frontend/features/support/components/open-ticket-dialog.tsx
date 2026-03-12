"use client";

import { useRef, useState } from "react";
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
import { Paperclip, X } from "lucide-react";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { categoryService } from "@/features/support/services/category.service";
import type { OpenTicketRequest } from "@/features/support/types";

const CATEGORY_TRANSLATIONS: Record<string, string> = {
  System: "Sistema",
  Billing: "Financeiro",
  Account: "Conta",
  General: "Geral",
  Bug: "Bug",
  Performance: "Desempenho",
  "Feature Request": "Nova Funcionalidade",
  "Charge Dispute": "Contestação de Cobrança",
  Invoice: "Fatura",
  Access: "Acesso",
  "Password Reset": "Redefinição de Senha",
  Question: "Dúvida",
  Feedback: "Feedback",
};

function translateCategory(s: string): string {
  return CATEGORY_TRANSLATIONS[s] ?? s;
}

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
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreviewUrl, setImagePreviewUrl] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

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

  function handleRemoveImage() {
    setImageFile(null);
    if (imagePreviewUrl) URL.revokeObjectURL(imagePreviewUrl);
    setImagePreviewUrl(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  }

  function handleImageChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] ?? null;
    if (imagePreviewUrl) URL.revokeObjectURL(imagePreviewUrl);
    setImageFile(file);
    setImagePreviewUrl(file ? URL.createObjectURL(file) : null);
  }

  function handleFormSubmit(data: OpenTicketRequest) {
    onSubmit({ ...data, attachment: imageFile ?? undefined });
  }

  function handleClose(value: boolean) {
    if (!value) {
      reset();
      handleRemoveImage();
    }
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

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
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
                    {translateCategory(cat.name)}
                    {cat.subcategory
                      ? ` — ${translateCategory(cat.subcategory)}`
                      : ""}
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

          {/* Image attachment */}
          <div className="space-y-1.5">
            <Label>Anexar imagem (opcional)</Label>
            <input
              type="file"
              accept="image/*"
              ref={fileInputRef}
              onChange={handleImageChange}
              className="hidden"
            />
            {imagePreviewUrl ? (
              <div className="relative rounded-md border overflow-hidden">
                <img
                  src={imagePreviewUrl}
                  alt="Pré-visualização"
                  className="w-full max-h-40 object-contain bg-muted"
                />
                <Button
                  type="button"
                  variant="destructive"
                  size="sm"
                  className="absolute top-1.5 right-1.5 h-6 w-6 p-0"
                  onClick={handleRemoveImage}
                >
                  <X className="h-3 w-3" />
                </Button>
                <p className="px-2 py-1 text-xs text-muted-foreground truncate bg-muted/50">
                  {imageFile?.name}
                </p>
              </div>
            ) : (
              <Button
                type="button"
                variant="outline"
                className="w-full"
                onClick={() => fileInputRef.current?.click()}
              >
                <Paperclip className="h-4 w-4 mr-2" />
                Selecionar imagem
              </Button>
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
