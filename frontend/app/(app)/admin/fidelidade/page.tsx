"use client";

import { useState } from "react";
import {
  useQuery,
  useMutation,
  useQueryClient,
  useQueries,
} from "@tanstack/react-query";
import {
  Gift,
  Plus,
  RefreshCw,
  Search,
  UserPlus,
  Star,
  Users,
} from "lucide-react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import { Card, CardContent } from "@/components/ui/card";
import {
  ChartContainer,
  ChartTooltip,
  ChartLegend,
  ChartLegendContent,
} from "@/components/ui/chart";
import type { ChartConfig } from "@/components/ui/chart";
import { PieChart, Pie } from "recharts";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ErrorAlert } from "@/shared/components/error-alert";
import { EmptyState } from "@/shared/components/empty-state";
import { fidelityService } from "@/features/admin/services/fidelity.service";
import { clientService } from "@/features/admin/services/client.service";
import { formatCurrency, formatDate } from "@/shared/utils/formatters";
import type {
  FidelityPolicyRequest,
  FidelityResponse,
  ClientResponse,
} from "@/features/admin/types";
const enrollmentChartConfig = {
  enrolled: { label: "Inscritos", color: "#10b981" },
  notEnrolled: {
    label: "Não inscritos",
    color: "#e5e7eb",
  },
} satisfies ChartConfig;
/* ─── Policy Form ─────────────────────────────────────────────────────────── */

function PolicyForm({
  onSubmit,
  isPending,
  onCancel,
}: {
  onSubmit: (data: FidelityPolicyRequest) => void;
  isPending: boolean;
  onCancel: () => void;
}) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FidelityPolicyRequest>();

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-1.5">
        <Label htmlFor="objectivePoints">Meta de Pontos *</Label>
        <Input
          id="objectivePoints"
          type="number"
          min={1}
          {...register("objectivePoints", {
            required: "Meta de pontos é obrigatória",
            valueAsNumber: true,
            min: { value: 1, message: "Deve ser ao menos 1 ponto" },
          })}
          placeholder="Ex: 100"
        />
        <p className="text-xs text-muted-foreground">
          Quantidade de pontos que o cliente precisa acumular para obter o
          desconto.
        </p>
        {errors.objectivePoints && (
          <p className="text-xs text-destructive">
            {errors.objectivePoints.message}
          </p>
        )}
      </div>

      <div className="space-y-1.5">
        <Label htmlFor="configuredDiscount">Desconto Configurado (R$) *</Label>
        <Input
          id="configuredDiscount"
          type="number"
          min={0.01}
          step={0.01}
          {...register("configuredDiscount", {
            required: "Desconto é obrigatório",
            valueAsNumber: true,
            min: { value: 0.01, message: "Desconto deve ser positivo" },
          })}
          placeholder="Ex: 20.00"
        />
        <p className="text-xs text-muted-foreground">
          Valor de desconto em reais que o cliente recebe ao atingir a meta.
        </p>
        {errors.configuredDiscount && (
          <p className="text-xs text-destructive">
            {errors.configuredDiscount.message}
          </p>
        )}
      </div>

      <div className="space-y-1.5">
        <Label htmlFor="valuePoint">Valor por Ponto (R$) *</Label>
        <Input
          id="valuePoint"
          type="number"
          min={0.01}
          step={0.01}
          {...register("valuePoint", {
            required: "Valor por ponto é obrigatório",
            valueAsNumber: true,
            min: { value: 0.01, message: "Valor deve ser positivo" },
          })}
          placeholder="Ex: 1.00"
        />
        <p className="text-xs text-muted-foreground">
          Valor em reais de cada compra que equivale a 1 ponto.
        </p>
        {errors.valuePoint && (
          <p className="text-xs text-destructive">
            {errors.valuePoint.message}
          </p>
        )}
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
          {isPending ? <LoadingSpinner size="sm" /> : "Salvar Política"}
        </Button>
      </DialogFooter>
    </form>
  );
}

/* ─── Client Fidelity Row ─────────────────────────────────────────────────── */

function ClientFidelityRow({
  client,
  onEnroll,
  isEnrolling,
}: {
  client: ClientResponse;
  onEnroll: (clientId: string) => void;
  isEnrolling: boolean;
}) {
  const { data: fidelity, isLoading } = useQuery<FidelityResponse | null>({
    queryKey: ["fidelity-client", client.id],
    queryFn: () =>
      fidelityService.getByClientId(client.id).catch((err) => {
        // 404 means not enrolled
        if (err?.status === 404) return null;
        throw err;
      }),
    staleTime: 60_000,
  });

  return (
    <tr className="border-t transition-colors hover:bg-accent">
      <td className="px-4 py-3 font-medium">{client.name}</td>
      <td className="px-4 py-3 font-mono text-xs text-muted-foreground">
        {client.cpf}
      </td>
      <td className="px-4 py-3">
        {isLoading ? (
          <LoadingSpinner size="sm" />
        ) : fidelity ? (
          <div className="flex items-center gap-2">
            <Badge variant="default" className="text-xs">
              Inscrito
            </Badge>
            <span className="text-xs text-muted-foreground">
              <Star className="mr-0.5 inline h-3 w-3 text-yellow-500" />
              {fidelity.points} pts
            </span>
            {fidelity.availableDiscount > 0 && (
              <span className="text-xs font-medium text-green-600">
                {formatCurrency(fidelity.availableDiscount)} disponível
              </span>
            )}
          </div>
        ) : (
          <Badge variant="outline" className="text-xs text-muted-foreground">
            Não inscrito
          </Badge>
        )}
      </td>
      <td className="px-4 py-3 text-right">
        {!isLoading && !fidelity && (
          <Button
            variant="outline"
            size="sm"
            className="h-7 text-xs"
            onClick={() => onEnroll(client.id)}
            disabled={isEnrolling}
          >
            <UserPlus className="mr-1.5 h-3.5 w-3.5" />
            Inscrever
          </Button>
        )}
      </td>
    </tr>
  );
}

/* ─── Page ──────────────────────────────────────────────────────────────── */

export default function FidelidadePage() {
  const queryClient = useQueryClient();
  const [policyFormOpen, setPolicyFormOpen] = useState(false);
  const [clientSearch, setClientSearch] = useState("");
  const [enrollingId, setEnrollingId] = useState<string | null>(null);

  // ── Policy queries ──
  const {
    data: activePolicy,
    isLoading: policyLoading,
    error: policyError,
    refetch: refetchPolicy,
    isFetching: policyFetching,
  } = useQuery({
    queryKey: ["fidelity-policy-active"],
    queryFn: () =>
      fidelityService.getActivePolicy().catch((err) => {
        if (err?.status === 404) return null;
        throw err;
      }),
  });

  const {
    data: policies = [],
    isLoading: historicLoading,
    refetch: refetchHistoric,
  } = useQuery({
    queryKey: ["fidelity-policies"],
    queryFn: () => fidelityService.listPolicies(),
  });

  // ── Clients query ──
  const {
    data: clients = [],
    isLoading: clientsLoading,
    error: clientsError,
    refetch: refetchClients,
    isFetching: clientsFetching,
  } = useQuery({
    queryKey: ["admin-clients-fidelity"],
    queryFn: () => clientService.listAll(),
  });

  // ── Fidelity overview (enrollment stats for chart) ──
  const fidelityOverviewQueries = useQueries({
    queries: clients.map((c) => ({
      queryKey: ["fidelity-client", c.id],
      queryFn: () =>
        fidelityService.getByClientId(c.id).catch((err) => {
          if (err?.status === 404) return null;
          throw err;
        }),
      staleTime: 60_000,
    })),
  });

  const fidelityOverviewLoaded =
    clients.length === 0 || fidelityOverviewQueries.every((q) => !q.isLoading);
  const enrolledCount = fidelityOverviewLoaded
    ? fidelityOverviewQueries.filter((q) => q.data != null).length
    : 0;
  const notEnrolledCount = fidelityOverviewLoaded
    ? fidelityOverviewQueries.filter((q) => q.data === null).length
    : 0;
  const enrollmentDonutData = [
    { name: "enrolled", value: enrolledCount, fill: "var(--color-enrolled)" },
    {
      name: "notEnrolled",
      value: notEnrolledCount,
      fill: "#e5e7eb",
    },
  ].filter((d) => d.value > 0);

  // ── Mutations ──
  const createPolicyMutation = useMutation({
    mutationFn: (data: FidelityPolicyRequest) =>
      fidelityService.createPolicy(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["fidelity-policy-active"] });
      queryClient.invalidateQueries({ queryKey: ["fidelity-policies"] });
      setPolicyFormOpen(false);
      toast.success("Política de fidelidade atualizada.");
    },
    onError: (err: unknown) => {
      toast.error(
        err instanceof Error ? err.message : "Erro ao salvar política.",
      );
    },
  });

  const enrollMutation = useMutation({
    mutationFn: (clientId: string) => fidelityService.enroll(clientId),
    onSuccess: (_, clientId) => {
      queryClient.invalidateQueries({
        queryKey: ["fidelity-client", clientId],
      });
      setEnrollingId(null);
      toast.success("Cliente inscrito no programa de fidelidade.");
    },
    onError: (err: unknown) => {
      setEnrollingId(null);
      toast.error(
        err instanceof Error ? err.message : "Erro ao inscrever cliente.",
      );
    },
  });

  function handleEnroll(clientId: string) {
    setEnrollingId(clientId);
    enrollMutation.mutate(clientId);
  }

  const filteredClients = clients.filter(
    (c) =>
      !clientSearch ||
      c.name.toLowerCase().includes(clientSearch.toLowerCase()) ||
      c.cpf.includes(clientSearch),
  );

  return (
    <div className="flex h-full flex-col overflow-hidden">
      {/* ─── Header ─── */}
      <header className="flex items-center gap-3 border-b bg-card px-4 py-3 shadow-sm">
        <Gift className="h-5 w-5 text-primary" />
        <div>
          <h1 className="text-sm font-semibold leading-none">
            Programa de Fidelidade
          </h1>
          <p className="mt-0.5 text-xs text-muted-foreground">
            Gerencie a política e os clientes inscritos
          </p>
        </div>
      </header>

      {/* ─── Tabs ─── */}
      <div className="flex-1 overflow-auto p-4">
        <Tabs defaultValue="politica">
          <TabsList className="mb-4">
            <TabsTrigger value="politica">Política</TabsTrigger>
            <TabsTrigger value="clientes">Clientes</TabsTrigger>
          </TabsList>

          {/* ── Política tab ── */}
          <TabsContent value="politica" className="space-y-4">
            {/* Active policy card */}
            <div className="rounded-lg border bg-card p-4 shadow-sm">
              <div className="mb-3 flex items-center justify-between">
                <h2 className="text-sm font-semibold">Política Ativa</h2>
                <div className="flex items-center gap-2">
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => refetchPolicy()}
                    disabled={policyFetching}
                    title="Atualizar"
                  >
                    <RefreshCw
                      className={`h-4 w-4 ${policyFetching ? "animate-spin" : ""}`}
                    />
                  </Button>
                  <Button size="sm" onClick={() => setPolicyFormOpen(true)}>
                    <Plus className="mr-1.5 h-4 w-4" />
                    Nova Política
                  </Button>
                </div>
              </div>

              {policyLoading ? (
                <div className="flex h-24 items-center justify-center">
                  <LoadingSpinner size="lg" label="Carregando política…" />
                </div>
              ) : policyError ? (
                <ErrorAlert
                  error={policyError}
                  title="Erro ao carregar política"
                />
              ) : activePolicy ? (
                <div className="grid grid-cols-3 gap-4">
                  <div className="rounded-md border bg-muted/30 p-3 text-center">
                    <p className="text-2xl font-bold text-primary">
                      {activePolicy.objectivePoints}
                    </p>
                    <p className="mt-1 text-xs text-muted-foreground">
                      Pontos para desconto
                    </p>
                  </div>
                  <div className="rounded-md border bg-muted/30 p-3 text-center">
                    <p className="text-2xl font-bold text-green-600">
                      {formatCurrency(activePolicy.configuredDiscount)}
                    </p>
                    <p className="mt-1 text-xs text-muted-foreground">
                      Desconto ao atingir meta
                    </p>
                  </div>
                  <div className="rounded-md border bg-muted/30 p-3 text-center">
                    <p className="text-2xl font-bold">
                      {formatCurrency(activePolicy.valuePoint)}
                    </p>
                    <p className="mt-1 text-xs text-muted-foreground">
                      Valor por ponto
                    </p>
                  </div>
                </div>
              ) : (
                <EmptyState
                  icon={<Gift className="h-10 w-10 opacity-20" />}
                  title="Nenhuma política ativa"
                  description="Crie uma política para ativar o programa de fidelidade."
                />
              )}
            </div>

            {/* Policy history */}
            <div className="rounded-lg border bg-card shadow-sm">
              <div className="flex items-center justify-between border-b px-4 py-3">
                <h2 className="text-sm font-semibold">
                  Histórico de Políticas
                </h2>
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => refetchHistoric()}
                  title="Atualizar"
                >
                  <RefreshCw className="h-4 w-4" />
                </Button>
              </div>
              {historicLoading ? (
                <div className="flex h-24 items-center justify-center">
                  <LoadingSpinner size="lg" label="Carregando histórico…" />
                </div>
              ) : policies.length === 0 ? (
                <EmptyState
                  title="Nenhuma política cadastrada"
                  description="O histórico aparecerá aqui após a primeira política ser criada."
                />
              ) : (
                <table className="w-full text-sm">
                  <thead className="border-b bg-muted/50">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                        Criada em
                      </th>
                      <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                        Meta (pts)
                      </th>
                      <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                        Desconto
                      </th>
                      <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                        R$/Ponto
                      </th>
                      <th className="px-4 py-3 text-center text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                        Status
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {policies.map((p, i) => (
                      <tr
                        key={p.id}
                        className={`border-t transition-colors hover:bg-accent ${i % 2 !== 0 ? "bg-muted/20" : ""}`}
                      >
                        <td className="px-4 py-3 text-xs text-muted-foreground">
                          {formatDate(p.createdAt)}
                        </td>
                        <td className="px-4 py-3 text-right font-medium">
                          {p.objectivePoints}
                        </td>
                        <td className="px-4 py-3 text-right">
                          {formatCurrency(p.configuredDiscount)}
                        </td>
                        <td className="px-4 py-3 text-right">
                          {formatCurrency(p.valuePoint)}
                        </td>
                        <td className="px-4 py-3 text-center">
                          {p.active ? (
                            <Badge variant="default" className="text-xs">
                              Ativa
                            </Badge>
                          ) : (
                            <Badge
                              variant="outline"
                              className="text-xs text-muted-foreground"
                            >
                              Inativa
                            </Badge>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </TabsContent>

          {/* ── Clientes tab ── */}
          <TabsContent value="clientes" className="space-y-4">
            {/* Enrollment overview */}
            {!clientsLoading && clients.length > 0 && (
              <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
                <Card>
                  <CardContent className="flex flex-col items-center justify-center p-4 text-center">
                    <p className="text-2xl font-bold">{clients.length}</p>
                    <p className="mt-1 text-xs text-muted-foreground">
                      Total de Clientes
                    </p>
                  </CardContent>
                </Card>
                <Card>
                  <CardContent className="flex flex-col items-center justify-center p-4 text-center">
                    <p className="text-2xl font-bold text-primary">
                      {enrolledCount}
                    </p>
                    <p className="mt-1 text-xs text-muted-foreground">
                      Inscritos
                    </p>
                  </CardContent>
                </Card>
                <Card>
                  <CardContent className="flex flex-col items-center justify-center p-4 text-center">
                    <p className="text-2xl font-bold text-muted-foreground">
                      {notEnrolledCount}
                    </p>
                    <p className="mt-1 text-xs text-muted-foreground">
                      Não inscritos
                    </p>
                  </CardContent>
                </Card>
                <Card>
                  <CardContent className="flex items-center justify-center p-2">
                    <ChartContainer
                      config={enrollmentChartConfig}
                      className="mx-auto aspect-square max-h-28"
                    >
                      <PieChart>
                        <ChartTooltip
                          cursor={false}
                          content={({ active, payload }) => {
                            if (!active || !payload?.length) return null;
                            const item = payload[0];
                            const cfg = enrollmentChartConfig[
                              item.payload
                                .name as keyof typeof enrollmentChartConfig
                            ] as { label?: string } | undefined;
                            return (
                              <div className="rounded-lg border border-border/50 bg-background px-2.5 py-1.5 text-xs shadow-xl">
                                <div className="flex items-center gap-2">
                                  <div
                                    className="h-2 w-2 shrink-0 rounded-[2px]"
                                    style={{
                                      backgroundColor: item.payload.fill,
                                    }}
                                  />
                                  <span className="text-muted-foreground">
                                    {cfg?.label ?? item.payload.name}
                                  </span>
                                  <span className="ml-auto font-bold">
                                    {item.value as number}
                                  </span>
                                </div>
                              </div>
                            );
                          }}
                        />
                        <Pie
                          data={enrollmentDonutData}
                          dataKey="value"
                          nameKey="name"
                          innerRadius={28}
                          strokeWidth={3}
                          stroke="transparent"
                        />
                        <ChartLegend
                          content={<ChartLegendContent nameKey="name" />}
                        />
                      </PieChart>
                    </ChartContainer>
                  </CardContent>
                </Card>
              </div>
            )}

            <div className="flex items-center gap-2">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  value={clientSearch}
                  onChange={(e) => setClientSearch(e.target.value)}
                  placeholder="Buscar por nome ou CPF…"
                  className="pl-9"
                />
              </div>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => refetchClients()}
                disabled={clientsFetching}
                title="Atualizar"
              >
                <RefreshCw
                  className={`h-4 w-4 ${clientsFetching ? "animate-spin" : ""}`}
                />
              </Button>
            </div>

            {clientsLoading ? (
              <div className="flex h-40 items-center justify-center">
                <LoadingSpinner size="lg" label="Carregando clientes…" />
              </div>
            ) : clientsError ? (
              <ErrorAlert
                error={clientsError}
                title="Erro ao carregar clientes"
              />
            ) : filteredClients.length === 0 ? (
              <EmptyState
                icon={<Users className="h-10 w-10 opacity-20" />}
                title="Nenhum cliente encontrado"
                description="Tente ajustar o filtro de busca."
              />
            ) : (
              <div className="rounded-md border">
                <table className="w-full text-sm">
                  <thead className="border-b bg-muted/50">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                        Nome
                      </th>
                      <th className="w-40 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                        CPF
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                        Fidelidade
                      </th>
                      <th className="w-28 px-4 py-3" />
                    </tr>
                  </thead>
                  <tbody>
                    {filteredClients.map((client) => (
                      <ClientFidelityRow
                        key={client.id}
                        client={client}
                        onEnroll={handleEnroll}
                        isEnrolling={enrollingId === client.id}
                      />
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </TabsContent>
        </Tabs>
      </div>

      {/* ─── Create Policy Dialog ─── */}
      <Dialog open={policyFormOpen} onOpenChange={setPolicyFormOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Nova Política de Fidelidade</DialogTitle>
            <DialogDescription>
              A política anterior será desativada automaticamente e esta
              substituirá as regras do programa.
            </DialogDescription>
          </DialogHeader>
          <PolicyForm
            onSubmit={(data) => createPolicyMutation.mutate(data)}
            isPending={createPolicyMutation.isPending}
            onCancel={() => setPolicyFormOpen(false)}
          />
        </DialogContent>
      </Dialog>
    </div>
  );
}
