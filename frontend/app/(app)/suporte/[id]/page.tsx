"use client";

import { useQuery } from "@tanstack/react-query";
import { useParams, useRouter } from "next/navigation";
import {
  ArrowLeft,
  LifeBuoy,
  User,
  UserCheck,
  Tag,
  Clock,
  MessageSquare,
  MessageCircle,
  StickyNote,
  Shield,
  CalendarClock,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ErrorAlert } from "@/shared/components/error-alert";
import { ticketService } from "@/features/support/services/ticket.service";
import { TicketStatusBadge } from "@/features/support/components/ticket-status-badge";
import { TicketPriorityBadge } from "@/features/support/components/ticket-priority-badge";
import { formatDate } from "@/shared/utils/formatters";
import type {
  InteractionResponse,
  InteractionType,
} from "@/features/support/types";

/* ─── Interaction type config ───────────────────────────────────────────── */
const INTERACTION_CONFIG: Record<
  InteractionType,
  { icon: React.ReactNode; label: string; bgClass: string; borderClass: string }
> = {
  CUSTOMER_MESSAGE: {
    icon: <MessageCircle className="h-4 w-4 text-sky-600" />,
    label: "Cliente",
    bgClass: "bg-sky-50",
    borderClass: "border-sky-200",
  },
  AGENT_MESSAGE: {
    icon: <MessageSquare className="h-4 w-4 text-emerald-600" />,
    label: "Atendente",
    bgClass: "bg-emerald-50",
    borderClass: "border-emerald-200",
  },
  INTERNAL_NOTE: {
    icon: <StickyNote className="h-4 w-4 text-amber-500" />,
    label: "Nota interna",
    bgClass: "bg-amber-50",
    borderClass: "border-amber-200",
  },
};

/* ─── Single interaction bubble ─────────────────────────────────────────── */
function InteractionBubble({
  interaction,
}: {
  interaction: InteractionResponse;
}) {
  const config = INTERACTION_CONFIG[interaction.type];

  return (
    <div
      className={`rounded-lg border p-3.5 text-sm ${config.bgClass} ${config.borderClass}`}
    >
      <div className="mb-2 flex items-center justify-between gap-2">
        <div className="flex items-center gap-1.5 font-medium text-foreground">
          {config.icon}
          <span>{config.label}</span>
        </div>
        <span className="text-xs text-muted-foreground">
          {formatDate(interaction.createdAt)}
        </span>
      </div>
      <p className="whitespace-pre-wrap leading-relaxed text-foreground/90">
        {interaction.content}
      </p>
    </div>
  );
}

/* ─── Metadata row ──────────────────────────────────────────────────────── */
function MetaRow({
  icon,
  label,
  children,
}: {
  icon: React.ReactNode;
  label: string;
  children: React.ReactNode;
}) {
  return (
    <div className="flex items-start gap-2.5 py-2 text-sm">
      <div className="mt-0.5 shrink-0 text-muted-foreground">{icon}</div>
      <div className="min-w-0">
        <p className="text-xs font-medium text-muted-foreground">{label}</p>
        <div className="mt-0.5">{children}</div>
      </div>
    </div>
  );
}

/* ─── Page ──────────────────────────────────────────────────────────────── */
export default function TicketDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();

  const {
    data: ticket,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["support-ticket", params.id],
    queryFn: () => ticketService.findById(params.id),
    enabled: !!params.id,
  });

  if (isLoading) {
    return (
      <div className="flex h-full items-center justify-center">
        <LoadingSpinner size="lg" label="Carregando chamado…" />
      </div>
    );
  }

  if (error || !ticket) {
    return (
      <div className="p-4">
        <ErrorAlert error={error ?? new Error("Chamado não encontrado")} />
      </div>
    );
  }

  const protocol = ticket.id.slice(0, 8).toUpperCase();

  return (
    <div className="flex h-full flex-col overflow-hidden">
      {/* Header */}
      <header className="border-b bg-card px-4 py-3 shadow-sm">
        <div className="flex items-start gap-3">
          <Button
            variant="ghost"
            size="icon"
            className="mt-0.5 shrink-0"
            onClick={() => router.push("/suporte")}
            title="Voltar para chamados"
          >
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <LifeBuoy className="h-4 w-4 shrink-0 text-muted-foreground" />
              <h1
                className="truncate text-sm font-semibold"
                title={ticket.title}
              >
                {ticket.title}
              </h1>
            </div>
            <p className="mt-1 text-xs text-muted-foreground">
              Protocolo:{" "}
              <span className="font-mono font-medium">{protocol}</span>
            </p>
          </div>
          <div className="flex shrink-0 flex-wrap items-center gap-2">
            <TicketStatusBadge status={ticket.status} />
            <TicketPriorityBadge priority={ticket.priority} />
          </div>
        </div>
      </header>

      {/* Body */}
      <div className="flex-1 overflow-auto">
        <div className="mx-auto max-w-5xl gap-6 px-4 py-6 lg:grid lg:grid-cols-3">
          {/* ── Left: description + timeline ── */}
          <div className="space-y-6 lg:col-span-2">
            {/* Description */}
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-sm font-semibold">
                  Descrição
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="whitespace-pre-wrap text-sm leading-relaxed text-foreground/90">
                  {ticket.description}
                </p>
              </CardContent>
            </Card>

            {/* Interactions */}
            <div>
              <h2 className="mb-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                Histórico de interações
              </h2>
              {ticket.interactions.length === 0 ? (
                <div className="flex flex-col items-center justify-center gap-2 rounded-lg border border-dashed py-10 text-center text-muted-foreground">
                  <MessageSquare className="h-8 w-8 opacity-40" />
                  <p className="text-sm">Nenhuma interação ainda.</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {ticket.interactions.map((interaction, idx) => (
                    <InteractionBubble key={idx} interaction={interaction} />
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* ── Right: metadata ── */}
          <div className="mt-6 lg:mt-0">
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-sm font-semibold">
                  Informações do chamado
                </CardTitle>
              </CardHeader>
              <CardContent className="divide-y">
                <MetaRow
                  icon={<User className="h-4 w-4" />}
                  label="Solicitante"
                >
                  <p className="text-sm font-medium">{ticket.user.name}</p>
                  <p className="text-xs text-muted-foreground">
                    {ticket.user.email}
                  </p>
                </MetaRow>

                <MetaRow
                  icon={<UserCheck className="h-4 w-4" />}
                  label="Atendente"
                >
                  {ticket.agent ? (
                    <>
                      <p className="text-sm font-medium">{ticket.agent.name}</p>
                      <p className="text-xs text-muted-foreground font-mono">
                        {ticket.agent.employeeId}
                      </p>
                    </>
                  ) : (
                    <p className="text-sm italic text-muted-foreground">
                      Aguardando atribuição
                    </p>
                  )}
                </MetaRow>

                <MetaRow icon={<Tag className="h-4 w-4" />} label="Categoria">
                  <p className="text-sm font-medium">{ticket.category.name}</p>
                  {ticket.category.subcategory && (
                    <p className="text-xs text-muted-foreground">
                      {ticket.category.subcategory}
                    </p>
                  )}
                </MetaRow>

                <MetaRow
                  icon={<Shield className="h-4 w-4" />}
                  label="Prioridade"
                >
                  <TicketPriorityBadge priority={ticket.priority} />
                </MetaRow>

                <MetaRow
                  icon={<CalendarClock className="h-4 w-4" />}
                  label="SLA"
                >
                  {ticket.sla.active ? (
                    <>
                      <Badge className="bg-emerald-600 text-white hover:bg-emerald-700">
                        Ativo
                      </Badge>
                      {ticket.sla.startedAt && (
                        <p className="mt-1 text-xs text-muted-foreground">
                          Iniciado em {formatDate(ticket.sla.startedAt)}
                        </p>
                      )}
                    </>
                  ) : (
                    <Badge variant="secondary">Inativo</Badge>
                  )}
                </MetaRow>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
