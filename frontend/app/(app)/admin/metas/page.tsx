"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import {
  useMutation,
  useQueries,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import {
  Pencil,
  Play,
  Plus,
  RefreshCw,
  Square,
  Trash2,
  TrendingUp,
} from "lucide-react";
import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
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
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Progress } from "@/components/ui/progress";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { EmptyState } from "@/shared/components/empty-state";
import { ErrorAlert } from "@/shared/components/error-alert";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { goalService } from "@/features/admin/services/goal.service";
import type {
  GoalAssessmentResult,
  GoalRequest,
  GoalResponse,
  GoalStatus,
  Periodicity,
} from "@/features/admin/types";
import { formatCurrency } from "@/shared/utils/formatters";

const CELEBRATED_KEY = "kalles:celebrated-goals";

const CONFETTI_COLORS = [
  "#f43f5e",
  "#3b82f6",
  "#22c55e",
  "#f59e0b",
  "#8b5cf6",
  "#06b6d4",
  "#ec4899",
  "#84cc16",
  "#f97316",
];

const FALL_ANIMATIONS = [
  "confetti-fall-a",
  "confetti-fall-b",
  "confetti-fall-c",
] as const;

function formatLocalDate(dateStr: string): string {
  return new Intl.DateTimeFormat("pt-BR", { dateStyle: "medium" }).format(
    new Date(dateStr + "T00:00:00"),
  );
}

function goalStatusLabel(status: GoalStatus): string {
  const labels: Record<GoalStatus, string> = {
    DRAFT: "Rascunho",
    ACTIVE: "Ativa",
    CLOSED: "Encerrada",
  };
  return labels[status];
}

function goalStatusVariant(
  status: GoalStatus,
): "default" | "secondary" | "outline" {
  if (status === "ACTIVE") return "default";
  if (status === "CLOSED") return "secondary";
  return "outline";
}

function periodicityLabel(p: Periodicity): string {
  return p === "WEEKLY" ? "Semanal" : "Mensal";
}

// ─── Balloon SVG ──────────────────────────────────────────────────────────────

function BalloonSvg({ color, size }: { color: string; size: number }) {
  return (
    <svg
      width={size}
      height={Math.round(size * 1.7)}
      viewBox="0 0 40 68"
      fill="none"
      aria-hidden
    >
      <ellipse cx="20" cy="20" rx="17" ry="19" fill={color} />
      <path d="M17 38 L20 44 L23 38 Z" fill={color} />
      <path
        d="M20 44 Q13 54 20 64"
        stroke={color}
        strokeWidth="1.5"
        strokeLinecap="round"
        fill="none"
      />
      <ellipse
        cx="13"
        cy="12"
        rx="4.5"
        ry="6"
        fill="white"
        fillOpacity="0.22"
      />
    </svg>
  );
}

// ─── Confetti & Balloons ──────────────────────────────────────────────────────

interface ConfettiParticle {
  id: number;
  left: number;
  delay: number;
  duration: number;
  color: string;
  width: number;
  height: number;
  borderRadius: string;
  animation: string;
  rotation: number;
}

interface BalloonParticle {
  id: number;
  left: number;
  delay: number;
  duration: number;
  color: string;
  size: number;
  rotation: number;
}

function ConfettiEffect({
  active,
  onDone,
}: {
  active: boolean;
  onDone: () => void;
}) {
  const { confetti, balloons } = useMemo(() => {
    const confetti: ConfettiParticle[] = Array.from({ length: 100 }, (_, i) => {
      const isCircle = Math.random() > 0.4;
      const size = 7 + Math.floor(Math.random() * 9);
      return {
        id: i,
        left: Math.random() * 100,
        delay: Math.random() * 2,
        duration: 3 + Math.random() * 3,
        color:
          CONFETTI_COLORS[Math.floor(Math.random() * CONFETTI_COLORS.length)],
        width: size,
        height: isCircle ? size : size * 1.6,
        borderRadius: isCircle ? "50%" : "2px",
        animation:
          FALL_ANIMATIONS[Math.floor(Math.random() * FALL_ANIMATIONS.length)],
        rotation: Math.floor(Math.random() * 360),
      };
    });

    const balloons: BalloonParticle[] = Array.from({ length: 12 }, (_, i) => ({
      id: 100 + i,
      left: 5 + Math.random() * 90,
      delay: Math.random() * 2.5,
      duration: 4 + Math.random() * 3,
      color:
        CONFETTI_COLORS[Math.floor(Math.random() * CONFETTI_COLORS.length)],
      size: 36 + Math.floor(Math.random() * 24),
      rotation: Math.floor((Math.random() - 0.5) * 20),
    }));

    return { confetti, balloons };
  }, []);

  useEffect(() => {
    if (!active) return;
    const timer = setTimeout(onDone, 7000);
    return () => clearTimeout(timer);
  }, [active, onDone]);

  if (!active) return null;

  return (
    <div
      aria-hidden
      className="pointer-events-none fixed inset-0 z-50 overflow-hidden"
    >
      {confetti.map((p) => (
        <div
          key={p.id}
          style={{
            position: "absolute",
            left: `${p.left}%`,
            top: "-20px",
            width: p.width,
            height: p.height,
            background: p.color,
            borderRadius: p.borderRadius,
            transform: `rotate(${p.rotation}deg)`,
            animation: `${p.animation} ${p.duration}s ${p.delay}s linear forwards`,
          }}
        />
      ))}
      {balloons.map((b) => (
        <div
          key={b.id}
          style={{
            position: "absolute",
            left: `${b.left}%`,
            bottom: "0px",
            transform: `rotate(${b.rotation}deg)`,
            animation: `balloon-rise ${b.duration}s ${b.delay}s ease-out forwards`,
          }}
        >
          <BalloonSvg color={b.color} size={b.size} />
        </div>
      ))}
    </div>
  );
}

// ─── Celebration Overlay ──────────────────────────────────────────────────────

function CelebrationOverlay({
  active,
  onDismiss,
}: {
  active: boolean;
  onDismiss: () => void;
}) {
  useEffect(() => {
    if (!active) return;
    const timer = setTimeout(onDismiss, 5000);
    return () => clearTimeout(timer);
  }, [active, onDismiss]);

  if (!active) return null;

  return (
    <div
      className="pointer-events-auto fixed inset-0 z-60 flex items-center justify-center bg-black/50 backdrop-blur-sm"
      onClick={onDismiss}
      role="dialog"
      aria-label="Meta atingida"
    >
      <div
        className="mx-4 max-w-sm rounded-2xl bg-background p-8 text-center shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="mb-3 text-6xl">🏆</div>
        <h2 className="mb-3 text-2xl font-bold text-green-600 dark:text-green-400">
          Meta Atingida!
        </h2>
        <p className="text-muted-foreground">
          Parabéns por atingir a meta! Que você consiga atingir muito mais
          metas!
        </p>
        <Button className="mt-6 w-full" onClick={onDismiss}>
          Fechar
        </Button>
      </div>
    </div>
  );
}

// ─── Goal Form ────────────────────────────────────────────────────────────────

interface GoalFormValues {
  targetValue: number;
  periodicity: Periodicity;
  startDate: string;
  endDate: string;
}

function GoalFormFields({
  onSubmit,
  isPending,
  onCancel,
  defaultValues,
}: {
  onSubmit: (data: GoalFormValues) => void;
  isPending: boolean;
  onCancel: () => void;
  defaultValues?: Partial<GoalFormValues>;
}) {
  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<GoalFormValues>({ defaultValues });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-1.5">
        <Label htmlFor="targetValue">Valor Alvo (R$) *</Label>
        <Input
          id="targetValue"
          type="number"
          min={0.01}
          step={0.01}
          placeholder="Ex: 100000.00"
          {...register("targetValue", {
            required: "O valor alvo é obrigatório",
            valueAsNumber: true,
            min: { value: 0.01, message: "O valor deve ser maior que zero" },
          })}
        />
        {errors.targetValue && (
          <p className="text-xs text-destructive">
            {errors.targetValue.message}
          </p>
        )}
      </div>

      <div className="space-y-1.5">
        <Label>Periodicidade *</Label>
        <Controller
          control={control}
          name="periodicity"
          rules={{ required: "A periodicidade é obrigatória" }}
          render={({ field }) => (
            <Select value={field.value} onValueChange={field.onChange}>
              <SelectTrigger>
                <SelectValue placeholder="Selecione a periodicidade" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="WEEKLY">Semanal</SelectItem>
                <SelectItem value="MONTHLY">Mensal</SelectItem>
              </SelectContent>
            </Select>
          )}
        />
        {errors.periodicity && (
          <p className="text-xs text-destructive">
            {errors.periodicity.message}
          </p>
        )}
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-1.5">
          <Label htmlFor="startDate">Data de Início *</Label>
          <Input
            id="startDate"
            type="date"
            {...register("startDate", {
              required: "A data de início é obrigatória",
            })}
          />
          {errors.startDate && (
            <p className="text-xs text-destructive">
              {errors.startDate.message}
            </p>
          )}
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="endDate">Data de Fim *</Label>
          <Input
            id="endDate"
            type="date"
            {...register("endDate", {
              required: "A data de fim é obrigatória",
            })}
          />
          {errors.endDate && (
            <p className="text-xs text-destructive">{errors.endDate.message}</p>
          )}
        </div>
      </div>

      <DialogFooter>
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancelar
        </Button>
        <Button type="submit" disabled={isPending}>
          {isPending ? "Salvando…" : "Salvar"}
        </Button>
      </DialogFooter>
    </form>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────

export default function MetasPage() {
  const queryClient = useQueryClient();

  const [showConfetti, setShowConfetti] = useState(false);
  const [showCelebration, setShowCelebration] = useState(false);
  const [formOpen, setFormOpen] = useState(false);
  const [editingGoal, setEditingGoal] = useState<GoalResponse | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [activatingId, setActivatingId] = useState<string | null>(null);
  const [closingId, setClosingId] = useState<string | null>(null);
  const celebratedRef = useRef<Set<string>>(new Set());

  const {
    data: goals,
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["goals"],
    queryFn: () => goalService.listAll(),
  });

  const assessableGoals = useMemo(
    () =>
      (goals ?? []).filter(
        (g) => g.status === "ACTIVE" || g.status === "CLOSED",
      ),
    [goals],
  );

  const progressQueries = useQueries({
    queries: assessableGoals.map((g) => ({
      queryKey: ["goal-progress", g.id],
      queryFn: () => goalService.getProgress(g.id),
      staleTime: 60_000,
    })),
  });

  const progressMap = useMemo(() => {
    const map: Record<string, GoalAssessmentResult> = {};
    assessableGoals.forEach((g, i) => {
      const result = progressQueries[i]?.data;
      if (result) map[g.id] = result;
    });
    return map;
  }, [assessableGoals, progressQueries]);

  useEffect(() => {
    if (Object.keys(progressMap).length === 0) return;
    let stored: string[] = [];
    try {
      const raw = localStorage.getItem(CELEBRATED_KEY);
      stored = raw ? (JSON.parse(raw) as string[]) : [];
    } catch {
      // ignore
    }
    const alreadyCelebrated = new Set(stored);
    const newlyMet = Object.entries(progressMap)
      .filter(
        ([id, r]) =>
          r.gap === 0 &&
          !alreadyCelebrated.has(id) &&
          !celebratedRef.current.has(id),
      )
      .map(([id]) => id);

    if (newlyMet.length > 0) {
      setShowConfetti(true);
      setShowCelebration(true);
      newlyMet.forEach((id) => {
        celebratedRef.current.add(id);
        alreadyCelebrated.add(id);
      });
      try {
        localStorage.setItem(
          CELEBRATED_KEY,
          JSON.stringify([...alreadyCelebrated]),
        );
      } catch {
        // ignore
      }
    }
  }, [progressMap]);

  const createMutation = useMutation({
    mutationFn: (data: GoalRequest) => goalService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["goals"] });
      toast.success("Meta criada com sucesso.");
      setFormOpen(false);
    },
    onError: (e: Error) => toast.error(e.message),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: GoalRequest }) =>
      goalService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["goals"] });
      toast.success("Meta atualizada.");
      setEditingGoal(null);
    },
    onError: (e: Error) => toast.error(e.message),
  });

  const activateMutation = useMutation({
    mutationFn: (id: string) => goalService.activate(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["goals"] });
      toast.success("Meta ativada.");
      setActivatingId(null);
    },
    onError: (e: Error) => toast.error(e.message),
  });

  const closeMutation = useMutation({
    mutationFn: (id: string) => goalService.close(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["goals"] });
      toast.success("Meta encerrada.");
      setClosingId(null);
    },
    onError: (e: Error) => toast.error(e.message),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => goalService.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["goals"] });
      toast.success("Meta excluída.");
      setDeletingId(null);
    },
    onError: (e: Error) => toast.error(e.message),
  });

  const handleConfettiDone = useCallback(() => setShowConfetti(false), []);
  const handleCelebrationDismiss = useCallback(
    () => setShowCelebration(false),
    [],
  );

  return (
    <div className="flex flex-col gap-6 p-6">
      <ConfettiEffect active={showConfetti} onDone={handleConfettiDone} />
      <CelebrationOverlay
        active={showCelebration}
        onDismiss={handleCelebrationDismiss}
      />

      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <TrendingUp className="h-6 w-6 text-primary" />
          <div>
            <h1 className="text-xl font-semibold">Metas de Faturamento</h1>
            <p className="text-sm text-muted-foreground">
              Configure e acompanhe as metas globais de faturamento.
            </p>
          </div>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            aria-label="Atualizar"
          >
            <RefreshCw className="h-4 w-4" />
          </Button>
          <Button size="sm" onClick={() => setFormOpen(true)}>
            <Plus className="mr-1.5 h-4 w-4" />
            Nova Meta
          </Button>
        </div>
      </div>

      {/* Content */}
      {isLoading && <LoadingSpinner label="Carregando metas…" />}
      {error && <ErrorAlert error={error} />}

      {!isLoading && !error && (
        <Card className="p-0">
          <CardContent className="p-0">
            {!goals || goals.length === 0 ? (
              <EmptyState
                title="Nenhuma meta cadastrada"
                description="Crie a primeira meta de faturamento clicando em Nova Meta."
                icon={<TrendingUp className="h-10 w-10 opacity-40" />}
              />
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Período</TableHead>
                    <TableHead>Periodicidade</TableHead>
                    <TableHead>Valor Alvo</TableHead>
                    <TableHead>Progresso</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Ações</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {goals.map((goal) => {
                    const progress = progressMap[goal.id];
                    const pct = progress
                      ? Math.min(
                          (progress.achievedValue / goal.targetValue) * 100,
                          100,
                        )
                      : 0;
                    const isMet = progress?.gap === 0;
                    return (
                      <TableRow key={goal.id}>
                        <TableCell>
                          <span className="font-medium">
                            {formatLocalDate(goal.startDate)}
                          </span>
                          <span className="mx-1.5 text-muted-foreground">
                            →
                          </span>
                          <span className="font-medium">
                            {formatLocalDate(goal.endDate)}
                          </span>
                        </TableCell>
                        <TableCell>
                          <Badge variant="outline" className="text-xs">
                            {periodicityLabel(goal.periodicity)}
                          </Badge>
                        </TableCell>
                        <TableCell className="font-semibold">
                          {formatCurrency(goal.targetValue)}
                        </TableCell>
                        <TableCell className="min-w-40">
                          {goal.status === "DRAFT" ? (
                            <span className="text-xs text-muted-foreground">
                              —
                            </span>
                          ) : progress ? (
                            <div className="space-y-1">
                              <div className="flex justify-between text-xs text-muted-foreground">
                                <span>
                                  {formatCurrency(progress.achievedValue)}
                                </span>
                                <span
                                  className={
                                    isMet
                                      ? "font-semibold text-green-600 dark:text-green-400"
                                      : ""
                                  }
                                >
                                  {Math.round(pct)}%
                                </span>
                              </div>
                              <Progress
                                value={pct}
                                className={
                                  isMet
                                    ? "*:data-[slot=progress-indicator]:bg-green-500"
                                    : ""
                                }
                              />
                              {isMet ? (
                                <p className="text-xs font-medium text-green-600 dark:text-green-400">
                                  ✓ Meta atingida
                                </p>
                              ) : (
                                <p className="text-xs text-muted-foreground">
                                  Faltam {formatCurrency(progress.gap)}
                                </p>
                              )}
                            </div>
                          ) : (
                            <span className="text-xs text-muted-foreground">
                              Calculando…
                            </span>
                          )}
                        </TableCell>
                        <TableCell>
                          <Badge variant={goalStatusVariant(goal.status)}>
                            {goalStatusLabel(goal.status)}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className="flex justify-end gap-1">
                            {goal.status === "DRAFT" && (
                              <>
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  className="h-8 w-8"
                                  title="Editar"
                                  onClick={() => setEditingGoal(goal)}
                                >
                                  <Pencil className="h-3.5 w-3.5" />
                                </Button>
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  className="h-8 w-8 text-primary hover:text-primary"
                                  title="Ativar"
                                  onClick={() => setActivatingId(goal.id)}
                                >
                                  <Play className="h-3.5 w-3.5" />
                                </Button>
                              </>
                            )}
                            {goal.status === "ACTIVE" && (
                              <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8"
                                title="Encerrar"
                                onClick={() => setClosingId(goal.id)}
                              >
                                <Square className="h-3.5 w-3.5" />
                              </Button>
                            )}
                            <Button
                              variant="ghost"
                              size="icon"
                              className="h-8 w-8 text-destructive hover:text-destructive"
                              title="Excluir"
                              onClick={() => setDeletingId(goal.id)}
                            >
                              <Trash2 className="h-3.5 w-3.5" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      )}

      {/* Create Dialog */}
      <Dialog open={formOpen} onOpenChange={setFormOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Nova Meta de Faturamento</DialogTitle>
            <DialogDescription>
              Defina o valor alvo e o período. A meta será criada como rascunho.
            </DialogDescription>
          </DialogHeader>
          <GoalFormFields
            onSubmit={(data) => createMutation.mutate(data as GoalRequest)}
            isPending={createMutation.isPending}
            onCancel={() => setFormOpen(false)}
          />
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog
        open={!!editingGoal}
        onOpenChange={(o) => !o && setEditingGoal(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Editar Meta</DialogTitle>
            <DialogDescription>
              Atualiza o valor alvo e o período. Só é permitido em rascunho.
            </DialogDescription>
          </DialogHeader>
          {editingGoal && (
            <GoalFormFields
              onSubmit={(data) =>
                updateMutation.mutate({
                  id: editingGoal.id,
                  data: data as GoalRequest,
                })
              }
              isPending={updateMutation.isPending}
              onCancel={() => setEditingGoal(null)}
              defaultValues={{
                targetValue: editingGoal.targetValue,
                periodicity: editingGoal.periodicity,
                startDate: editingGoal.startDate,
                endDate: editingGoal.endDate,
              }}
            />
          )}
        </DialogContent>
      </Dialog>

      {/* Activate Confirmation */}
      <AlertDialog
        open={!!activatingId}
        onOpenChange={(o) => !o && setActivatingId(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Ativar meta?</AlertDialogTitle>
            <AlertDialogDescription>
              A meta será ativada. Após a ativação, o valor e o período não
              poderão ser alterados.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={() =>
                activatingId && activateMutation.mutate(activatingId)
              }
            >
              Ativar
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Close Confirmation */}
      <AlertDialog
        open={!!closingId}
        onOpenChange={(o) => !o && setClosingId(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Encerrar meta?</AlertDialogTitle>
            <AlertDialogDescription>
              A meta será encerrada e não poderá ser reativada.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={() => closingId && closeMutation.mutate(closingId)}
            >
              Encerrar
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Delete Confirmation */}
      <AlertDialog
        open={!!deletingId}
        onOpenChange={(o) => !o && setDeletingId(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Excluir meta?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta ação não pode ser desfeita.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={() => deletingId && deleteMutation.mutate(deletingId)}
            >
              Excluir
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
