"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Users,
  Plus,
  Pencil,
  Trash2,
  RefreshCw,
  Search,
  Gift,
} from "lucide-react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
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
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ErrorAlert } from "@/shared/components/error-alert";
import { clientService } from "@/features/admin/services/client.service";
import { fidelityService } from "@/features/admin/services/fidelity.service";
import type {
  ClientRequest,
  ClientResponse,
  FidelityResponse,
} from "@/features/admin/types";

/* ─── Form ─────────────────────────────────────────────────────────────── */
function ClientForm({
  defaultValues,
  onSubmit,
  isPending,
  onCancel,
}: {
  defaultValues?: Partial<ClientRequest>;
  onSubmit: (data: ClientRequest) => void;
  isPending: boolean;
  onCancel: () => void;
}) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ClientRequest>({ defaultValues: defaultValues ?? {} });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div className="col-span-2 space-y-1.5">
          <Label htmlFor="name">Nome *</Label>
          <Input
            id="name"
            {...register("name", { required: "Nome é obrigatório" })}
            placeholder="Nome completo"
          />
          {errors.name && (
            <p className="text-xs text-destructive">{errors.name.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="cpf">CPF *</Label>
          <Input
            id="cpf"
            {...register("cpf", { required: "CPF é obrigatório" })}
            placeholder="000.000.000-00"
          />
          {errors.cpf && (
            <p className="text-xs text-destructive">{errors.cpf.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="birthDate">Data de Nascimento</Label>
          <Input id="birthDate" type="date" {...register("birthDate")} />
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="cellphone">Celular</Label>
          <Input
            id="cellphone"
            {...register("cellphone")}
            placeholder="(00) 00000-0000"
          />
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="rg">RG</Label>
          <Input
            id="rg"
            {...register("rg")}
            placeholder="Documento de identidade"
          />
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="nameFather">Nome do Pai</Label>
          <Input
            id="nameFather"
            {...register("nameFather")}
            placeholder="Nome do pai"
          />
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="nameMother">Nome da Mãe</Label>
          <Input
            id="nameMother"
            {...register("nameMother")}
            placeholder="Nome da mãe"
          />
        </div>

        <div className="col-span-2 space-y-1.5">
          <Label htmlFor="observations">Observações</Label>
          <Textarea
            id="observations"
            {...register("observations")}
            placeholder="Informações adicionais…"
            rows={3}
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
export default function ClientesPage() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<ClientResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<ClientResponse | null>(null);

  const {
    data: clients = [],
    isLoading,
    error,
    refetch,
    isFetching,
  } = useQuery({
    queryKey: ["admin-clients"],
    queryFn: () => clientService.listAll(),
  });

  const clientIds = clients.map((c) => c.id);

  const { data: fidelityMap = new Map<string, FidelityResponse | null>() } =
    useQuery({
      queryKey: ["clients-fidelity", clientIds.join(",")],
      queryFn: async () => {
        const results = await Promise.allSettled(
          clientIds.map((id) => fidelityService.getByClientId(id)),
        );
        const map = new Map<string, FidelityResponse | null>();
        clientIds.forEach((id, i) => {
          const r = results[i];
          map.set(id, r.status === "fulfilled" ? r.value : null);
        });
        return map;
      },
      enabled: clientIds.length > 0,
      staleTime: 60_000,
    });

  const createMutation = useMutation({
    mutationFn: (data: ClientRequest) => clientService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-clients"] });
      setCreateOpen(false);
      toast.success("Cliente cadastrado com sucesso.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao cadastrar cliente.",
      );
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: ClientRequest }) =>
      clientService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-clients"] });
      setEditTarget(null);
      toast.success("Cliente atualizado com sucesso.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao atualizar cliente.",
      );
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => clientService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-clients"] });
      setDeleteTarget(null);
      toast.success("Cliente removido.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao remover cliente.",
      );
    },
  });

  const filtered = search.trim()
    ? clients.filter((c) => {
        const q = search.toLowerCase();
        return (
          c.name.toLowerCase().includes(q) ||
          c.cpf.includes(q) ||
          (c.cellphone ?? "").includes(q)
        );
      })
    : clients;

  return (
    <div className="flex h-full flex-col overflow-hidden">
      {/* ─── Header ─── */}
      <header className="flex items-center gap-3 border-b bg-card px-4 py-3 shadow-sm">
        <Users className="h-5 w-5 text-primary" />
        <div>
          <h1 className="text-sm font-semibold leading-none">Clientes</h1>
          <p className="mt-0.5 text-xs text-muted-foreground">
            {isLoading
              ? "Carregando…"
              : `${clients.length} cliente(s) cadastrado(s)`}
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
            Novo Cliente
          </Button>
        </div>
      </header>

      {/* ─── Search ─── */}
      <div className="border-b bg-muted/30 px-4 py-3">
        <div className="relative max-w-xl">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por nome, CPF ou celular…"
            className="bg-background pl-9"
          />
        </div>
      </div>

      {/* ─── Content ─── */}
      <div className="flex-1 overflow-auto">
        {isLoading ? (
          <div className="flex h-40 items-center justify-center">
            <LoadingSpinner size="lg" label="Carregando clientes…" />
          </div>
        ) : error ? (
          <ErrorAlert error={error} title="Erro ao carregar clientes" />
        ) : filtered.length === 0 ? (
          <div className="flex h-40 flex-col items-center justify-center gap-2 text-muted-foreground">
            <Users className="h-10 w-10 opacity-20" />
            <p className="text-sm font-medium">
              {search
                ? "Nenhum cliente encontrado"
                : "Nenhum cliente cadastrado"}
            </p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="border-b bg-muted/50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Nome
                </th>
                <th className="w-36 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  CPF
                </th>
                <th className="w-36 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Celular
                </th>
                <th className="w-44 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Fidelidade
                </th>
                <th className="w-24 px-4 py-3" />
              </tr>
            </thead>
            <tbody>
              {filtered.map((client, i) => (
                <tr
                  key={client.id}
                  className={`border-t transition-colors hover:bg-accent ${i % 2 !== 0 ? "bg-muted/20" : ""}`}
                >
                  <td className="px-4 py-3 font-medium">{client.name}</td>
                  <td className="px-4 py-3 font-mono text-xs text-muted-foreground">
                    {client.cpf}
                  </td>
                  <td className="px-4 py-3 text-xs text-muted-foreground">
                    {client.cellphone ?? <span className="opacity-40">—</span>}
                  </td>
                  <td className="px-4 py-3">
                    {(() => {
                      if (!fidelityMap.has(client.id)) {
                        return (
                          <span className="text-xs text-muted-foreground opacity-40">
                            —
                          </span>
                        );
                      }
                      const f = fidelityMap.get(client.id) ?? null;
                      if (f === null) {
                        return (
                          <Badge
                            variant="outline"
                            className="text-xs font-normal"
                          >
                            Não inscrito
                          </Badge>
                        );
                      }
                      if (f.availableDiscount > 0) {
                        return (
                          <Badge className="border border-green-200 bg-green-100 text-xs font-normal text-green-700 hover:bg-green-100">
                            <Gift className="mr-1 h-3 w-3" />
                            R$ {f.availableDiscount.toFixed(2)} disponível
                          </Badge>
                        );
                      }
                      return (
                        <Badge
                          variant="secondary"
                          className="text-xs font-normal"
                        >
                          <Gift className="mr-1 h-3 w-3 opacity-60" />
                          {f.points} pts
                        </Badge>
                      );
                    })()}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-1">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-7 w-7"
                        onClick={() => setEditTarget(client)}
                        title="Editar"
                      >
                        <Pencil className="h-3.5 w-3.5" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-7 w-7 text-destructive hover:text-destructive"
                        onClick={() => setDeleteTarget(client)}
                        title="Excluir"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* ─── Create Dialog ─── */}
      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>Novo Cliente</DialogTitle>
            <DialogDescription>
              Preencha os dados do cliente. Campos marcados com * são
              obrigatórios.
            </DialogDescription>
          </DialogHeader>
          <ClientForm
            onSubmit={(data) => createMutation.mutate(data)}
            isPending={createMutation.isPending}
            onCancel={() => setCreateOpen(false)}
          />
        </DialogContent>
      </Dialog>

      {/* ─── Edit Dialog ─── */}
      <Dialog
        open={!!editTarget}
        onOpenChange={(o) => !o && setEditTarget(null)}
      >
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>Editar Cliente</DialogTitle>
            <DialogDescription>Atualize os dados do cliente.</DialogDescription>
          </DialogHeader>
          {editTarget && (
            <ClientForm
              defaultValues={editTarget}
              onSubmit={(data) =>
                updateMutation.mutate({ id: editTarget.id, data })
              }
              isPending={updateMutation.isPending}
              onCancel={() => setEditTarget(null)}
            />
          )}
        </DialogContent>
      </Dialog>

      {/* ─── Delete Dialog ─── */}
      <AlertDialog
        open={!!deleteTarget}
        onOpenChange={(o) => !o && setDeleteTarget(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Excluir cliente?</AlertDialogTitle>
            <AlertDialogDescription>
              Tem certeza que deseja excluir{" "}
              <strong>{deleteTarget?.name}</strong>? Esta ação não pode ser
              desfeita.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={deleteMutation.isPending}>
              Cancelar
            </AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteMutation.isPending}
              onClick={() =>
                deleteTarget && deleteMutation.mutate(deleteTarget.id)
              }
            >
              {deleteMutation.isPending ? (
                <LoadingSpinner size="sm" />
              ) : (
                "Excluir"
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
