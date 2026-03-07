"use client";

import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  UserCog,
  Plus,
  Pencil,
  PowerOff,
  RefreshCw,
  Camera,
} from "lucide-react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ErrorAlert } from "@/shared/components/error-alert";
import { operatorService } from "@/features/admin/services/operator.service";
import { operatorPhotoService } from "@/shared/services/company-settings.service";
import type {
  OperatorAdminResponse,
  OperatorRequest,
  PermissionLevel,
} from "@/features/admin/types";

/* ─── Helpers ────────────────────────────────────────────────────────────── */

const PERMISSION_OPTIONS: { value: PermissionLevel; label: string }[] = [
  { value: "BASIC", label: "Básico" },
  { value: "SUPERVISOR", label: "Supervisor" },
  { value: "MANAGER", label: "Gerente" },
  { value: "ADMIN", label: "Admin" },
];

const PERMISSION_LABELS: Record<string, string> = {
  BASIC: "Básico",
  SUPERVISOR: "Supervisor",
  MANAGER: "Gerente",
  ADMIN: "Admin",
};

function PermissionBadge({ level }: { level: string | null }) {
  if (!level)
    return <span className="text-xs text-muted-foreground opacity-40">—</span>;
  const variant =
    level === "ADMIN"
      ? "default"
      : level === "MANAGER"
        ? "secondary"
        : "outline";
  return (
    <Badge variant={variant} className="text-xs">
      {PERMISSION_LABELS[level] ?? level}
    </Badge>
  );
}

function OperatorAvatar({ id, name }: { id: string; name: string }) {
  const [src, setSrc] = useState<string | null>(null);

  useEffect(() => {
    setSrc(operatorPhotoService.getPhoto(id));
  }, [id]);

  if (src) {
    return (
      <img
        src={src}
        alt={name}
        className="h-7 w-7 rounded-full object-cover ring-1 ring-border"
        onError={() => setSrc(null)}
      />
    );
  }

  return (
    <div className="flex h-7 w-7 items-center justify-center rounded-full bg-primary/10 text-[10px] font-semibold text-primary ring-1 ring-border">
      {name.slice(0, 2).toUpperCase()}
    </div>
  );
}

/* ─── Form type ──────────────────────────────────────────────────────────── */

interface OperatorFormValues extends OperatorRequest {
  photoUrl?: string;
}

/* ─── Form ──────────────────────────────────────────────────────────────── */

function OperatorForm({
  defaultValues,
  onSubmit,
  isPending,
  onCancel,
}: {
  defaultValues?: Partial<OperatorFormValues>;
  onSubmit: (data: OperatorFormValues) => void;
  isPending: boolean;
  onCancel: () => void;
}) {
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<OperatorFormValues>({ defaultValues });

  const permLevel = watch("permissionLevel");
  const photoUrl = watch("photoUrl");

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-1.5">
        <Label htmlFor="name">Nome *</Label>
        <Input
          id="name"
          {...register("name", { required: "Nome é obrigatório" })}
          placeholder="Nome completo do operador"
        />
        {errors.name && (
          <p className="text-xs text-destructive">{errors.name.message}</p>
        )}
      </div>

      <div className="space-y-1.5">
        <Label htmlFor="code">Código de Login *</Label>
        <Input
          id="code"
          {...register("code", { required: "Código é obrigatório" })}
          placeholder="Ex: joao.silva"
          className="font-mono"
        />
        {errors.code && (
          <p className="text-xs text-destructive">{errors.code.message}</p>
        )}
      </div>

      <div className="space-y-1.5">
        <Label>Nível de Permissão *</Label>
        <Select
          value={permLevel}
          onValueChange={(v) =>
            setValue("permissionLevel", v as PermissionLevel, {
              shouldValidate: true,
            })
          }
        >
          <SelectTrigger>
            <SelectValue placeholder="Selecione um nível…" />
          </SelectTrigger>
          <SelectContent>
            {PERMISSION_OPTIONS.map((opt) => (
              <SelectItem key={opt.value} value={opt.value}>
                {opt.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {errors.permissionLevel && (
          <p className="text-xs text-destructive">
            Nível de permissão é obrigatório
          </p>
        )}
      </div>

      <div className="space-y-1.5">
        <Label htmlFor="photoUrl">
          <Camera className="mr-1 inline h-3.5 w-3.5" />
          URL da Foto (opcional)
        </Label>
        <div className="flex items-center gap-2">
          {photoUrl && (
            <img
              src={photoUrl}
              alt="Prévia"
              className="h-8 w-8 rounded-full object-cover ring-1 ring-border"
              onError={(e) => {
                (e.currentTarget as HTMLImageElement).style.display = "none";
              }}
            />
          )}
          <Input
            id="photoUrl"
            {...register("photoUrl")}
            placeholder="https://exemplo.com/foto.jpg"
          />
        </div>
      </div>

      <DialogFooter>
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isPending}
        >
          Cancelar
        </Button>
        <Button type="submit" disabled={isPending}>
          {isPending ? <LoadingSpinner size="sm" /> : "Salvar"}
        </Button>
      </DialogFooter>
    </form>
  );
}

/* ─── Page ──────────────────────────────────────────────────────────────── */

export default function OperadoresPage() {
  const queryClient = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<OperatorAdminResponse | null>(
    null,
  );
  const [deactivateTarget, setDeactivateTarget] =
    useState<OperatorAdminResponse | null>(null);

  const {
    data: operators = [],
    isLoading,
    error,
    refetch,
    isFetching,
  } = useQuery<OperatorAdminResponse[]>({
    queryKey: ["admin-operadores"],
    queryFn: () => operatorService.listAll(),
  });

  const createMutation = useMutation({
    mutationFn: ({ name, code, permissionLevel }: OperatorFormValues) =>
      operatorService.create({ name, code, permissionLevel }),
    onSuccess: (created, variables) => {
      if (variables.photoUrl?.trim()) {
        operatorPhotoService.setPhoto(created.id, variables.photoUrl.trim());
      }
      queryClient.invalidateQueries({ queryKey: ["admin-operadores"] });
      setCreateOpen(false);
      toast.success("Operador cadastrado com sucesso.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao cadastrar operador.",
      );
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: OperatorFormValues }) =>
      operatorService.update(id, {
        name: data.name,
        code: data.code,
        permissionLevel: data.permissionLevel,
      }),
    onSuccess: (_, { id, data }) => {
      if (data.photoUrl?.trim()) {
        operatorPhotoService.setPhoto(id, data.photoUrl.trim());
      } else if (data.photoUrl === "") {
        operatorPhotoService.removePhoto(id);
      }
      queryClient.invalidateQueries({ queryKey: ["admin-operadores"] });
      setEditTarget(null);
      toast.success("Operador atualizado com sucesso.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao atualizar operador.",
      );
    },
  });

  const deactivateMutation = useMutation({
    mutationFn: (id: string) => operatorService.deactivate(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-operadores"] });
      setDeactivateTarget(null);
      toast.success("Operador desativado.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao desativar operador.",
      );
    },
  });

  return (
    <div className="flex h-full flex-col overflow-hidden">
      {/* ─── Header ─── */}
      <header className="flex items-center gap-3 border-b bg-card px-4 py-3 shadow-sm">
        <UserCog className="h-5 w-5 text-primary" />
        <div>
          <h1 className="text-sm font-semibold leading-none">Operadores</h1>
          <p className="mt-0.5 text-xs text-muted-foreground">
            {isLoading
              ? "Carregando…"
              : `${operators.length} operador(es) ativo(s)`}
          </p>
        </div>
        <div className="ml-auto flex items-center gap-2">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => refetch()}
            disabled={isFetching}
            title="Atualizar"
          >
            <RefreshCw
              className={`h-4 w-4 ${isFetching ? "animate-spin" : ""}`}
            />
          </Button>
          <Button size="sm" onClick={() => setCreateOpen(true)}>
            <Plus className="mr-1.5 h-4 w-4" />
            Novo Operador
          </Button>
        </div>
      </header>

      {/* ─── Content ─── */}
      <div className="flex-1 overflow-auto p-4">
        {isLoading ? (
          <div className="flex h-40 items-center justify-center">
            <LoadingSpinner size="lg" label="Carregando operadores…" />
          </div>
        ) : error ? (
          <ErrorAlert error={error} title="Erro ao carregar operadores" />
        ) : operators.length === 0 ? (
          <div className="flex h-40 flex-col items-center justify-center gap-2 text-muted-foreground">
            <UserCog className="h-10 w-10 opacity-20" />
            <p className="text-sm font-medium">
              Nenhum operador ativo cadastrado
            </p>
          </div>
        ) : (
          <div className="rounded-md border">
            <table className="w-full text-sm">
              <thead className="border-b bg-muted/50">
                <tr>
                  <th className="w-10 px-4 py-3" />
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Nome
                  </th>
                  <th className="w-40 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Código
                  </th>
                  <th className="w-32 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Nível
                  </th>
                  <th className="w-20 px-4 py-3" />
                </tr>
              </thead>
              <tbody>
                {operators.map((op, i) => (
                  <tr
                    key={op.id}
                    className={`border-t transition-colors hover:bg-accent ${i % 2 !== 0 ? "bg-muted/20" : ""}`}
                  >
                    <td className="px-4 py-3">
                      <OperatorAvatar id={op.id} name={op.name} />
                    </td>
                    <td className="px-4 py-3 font-medium">{op.name}</td>
                    <td className="px-4 py-3 font-mono text-xs text-muted-foreground">
                      {op.code}
                    </td>
                    <td className="px-4 py-3">
                      <PermissionBadge level={op.permissionLevel} />
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-1">
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-7 w-7"
                          onClick={() => setEditTarget(op)}
                          title="Editar"
                        >
                          <Pencil className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-7 w-7 text-destructive hover:text-destructive"
                          onClick={() => setDeactivateTarget(op)}
                          title="Desativar"
                        >
                          <PowerOff className="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ─── Create Dialog ─── */}
      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Novo Operador</DialogTitle>
            <DialogDescription>
              Preencha os dados do operador a ser cadastrado.
            </DialogDescription>
          </DialogHeader>
          <OperatorForm
            onSubmit={(data) => createMutation.mutate(data)}
            isPending={createMutation.isPending}
            onCancel={() => setCreateOpen(false)}
          />
        </DialogContent>
      </Dialog>

      {/* ─── Edit Dialog ─── */}
      <Dialog
        open={!!editTarget}
        onOpenChange={(open) => !open && setEditTarget(null)}
      >
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Editar Operador</DialogTitle>
            <DialogDescription>
              Altere os dados do operador selecionado.
            </DialogDescription>
          </DialogHeader>
          {editTarget && (
            <OperatorForm
              defaultValues={{
                name: editTarget.name,
                code: editTarget.code,
                permissionLevel: editTarget.permissionLevel as PermissionLevel,
                photoUrl: operatorPhotoService.getPhoto(editTarget.id) ?? "",
              }}
              onSubmit={(data) =>
                updateMutation.mutate({ id: editTarget.id, data })
              }
              isPending={updateMutation.isPending}
              onCancel={() => setEditTarget(null)}
            />
          )}
        </DialogContent>
      </Dialog>

      {/* ─── Deactivate AlertDialog ─── */}
      <AlertDialog
        open={!!deactivateTarget}
        onOpenChange={(open) => !open && setDeactivateTarget(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Desativar operador?</AlertDialogTitle>
            <AlertDialogDescription>
              O operador <strong>{deactivateTarget?.name}</strong> será marcado
              como inativo e não poderá abrir novas sessões de caixa.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={() =>
                deactivateTarget &&
                deactivateMutation.mutate(deactivateTarget.id)
              }
            >
              {deactivateMutation.isPending ? (
                <LoadingSpinner size="sm" />
              ) : (
                "Desativar"
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
