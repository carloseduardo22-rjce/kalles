"use client";

import { useEffect, useMemo, useState, type ReactNode } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useParams, useRouter } from "next/navigation";
import {
  AlertCircle,
  ArrowLeft,
  CalendarClock,
  CheckCircle2,
  Clock,
  LifeBuoy,
  MessageCircle,
  MessageSquare,
  Send,
  Shield,
  StickyNote,
  Tag,
  User,
  UserCheck,
} from "lucide-react";
import { toast } from "sonner";

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { ErrorAlert } from "@/shared/components/error-alert";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { TicketPriorityBadge } from "@/features/support/components/ticket-priority-badge";
import { TicketStatusBadge } from "@/features/support/components/ticket-status-badge";
import { ticketService } from "@/features/support/services/ticket.service";
import { formatDate } from "@/shared/utils/formatters";
import type {
  AuthMeResponse,
  InteractionResponse,
  InteractionType,
  TicketResponse,
} from "@/features/support/types";

const INTERACTION_CONFIG: Record<
  InteractionType,
  { icon: ReactNode; label: string; bgClass: string; borderClass: string }
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

function MetaRow({
  icon,
  label,
  children,
}: {
  icon: ReactNode;
  label: string;
  children: ReactNode;
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

function InteractionBubble({
  interaction,
  isEditable,
}: {
  interaction: InteractionResponse;
  isEditable: boolean;
}) {
  const config = INTERACTION_CONFIG[interaction.type];

  return (
    <div className={`rounded-lg border p-3.5 text-sm ${config.bgClass} ${config.borderClass}`}>
      <div className="mb-2 flex items-center justify-between gap-2">
        <div className="flex items-center gap-1.5 font-medium text-foreground">
          {config.icon}
          <span>{config.label}</span>
          {isEditable ? (
            <Badge variant="secondary" className="ml-1 text-[10px] uppercase tracking-wide">
              Editavel
            </Badge>
          ) : null}
        </div>
        <span className="text-xs text-muted-foreground">{formatDate(interaction.createdAt)}</span>
      </div>
      <p className="whitespace-pre-wrap leading-relaxed text-foreground/90">{interaction.content}</p>
    </div>
  );
}

function getLatestConversationInteraction(ticket: TicketResponse | undefined) {
  if (!ticket) return null;
  const conversation = ticket.interactions.filter(
    (interaction) => interaction.type !== "INTERNAL_NOTE",
  );
  return conversation.at(-1) ?? null;
}

function getActorInteractionType(me: AuthMeResponse | undefined): InteractionType {
  return me?.role === "ADMIN" ? "AGENT_MESSAGE" : "CUSTOMER_MESSAGE";
}

function buildHelperText(params: {
  me?: AuthMeResponse;
  ticket: TicketResponse;
  latestConversation: InteractionResponse | null;
  ownType: InteractionType;
}): string {
  const { me, ticket, latestConversation, ownType } = params;

  if (!me) return "";
  if (ticket.status === "CLOSED") return "Este chamado ja foi encerrado e nao aceita novas mensagens.";
  if (ownType === "AGENT_MESSAGE" && !ticket.agent) {
    return "Atribua um atendente ao chamado antes de responder por esta tela.";
  }
  if (!latestConversation) {
    return ownType === "AGENT_MESSAGE"
      ? "Voce pode iniciar a conversa com o cliente por aqui."
      : "Voce pode complementar o chamado com uma nova mensagem.";
  }
  if (latestConversation.type === ownType) {
    return "A ultima mensagem foi sua. Edite-a abaixo antes de enviar outra.";
  }
  if (ownType === "AGENT_MESSAGE") {
    return "Responda ao cliente ou envie a pergunta final para confirmar se o problema foi solucionado.";
  }
  return "O atendente respondeu por ultimo. Voce pode enviar a proxima mensagem.";
}

export default function TicketDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const queryClient = useQueryClient();
  const [draft, setDraft] = useState("");
  const [isEditing, setIsEditing] = useState(false);

  const { data: me, isLoading: loadingMe, error: meError } = useQuery({
    queryKey: ["support-me"],
    queryFn: ticketService.me,
  });

  const {
    data: ticket,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["support-ticket", params.id],
    queryFn: () => ticketService.findById(params.id),
    enabled: !!params.id,
  });

  const ownType = getActorInteractionType(me);
  const latestConversation = useMemo(() => getLatestConversationInteraction(ticket), [ticket]);
  const canEditLatest = !!latestConversation && latestConversation.type === ownType && ticket?.status !== "CLOSED";
  const canSendNew = !!ticket && ticket.status !== "CLOSED" && latestConversation?.type !== ownType && (ownType !== "AGENT_MESSAGE" || !!ticket.agent);
  const canClose = !!ticket && me?.role === "ADMIN" && ticket.status === "RESOLVED" && latestConversation?.type === "CUSTOMER_MESSAGE";

  useEffect(() => {
    if (!ticket) return;
    if (isEditing && latestConversation && latestConversation.type === ownType) {
      setDraft(latestConversation.content);
      return;
    }
    setDraft("");
    setIsEditing(false);
  }, [ticket, latestConversation, ownType, isEditing]);

  const syncTicket = (nextTicket: TicketResponse) => {
    queryClient.setQueryData(["support-ticket", params.id], nextTicket);
    queryClient.invalidateQueries({ queryKey: ["support-tickets"] });
  };

  const sendCustomerMutation = useMutation({
    mutationFn: (content: string) => ticketService.sendCustomerMessage(params.id, { content }),
    onSuccess: (nextTicket) => {
      syncTicket(nextTicket);
      setDraft("");
      setIsEditing(false);
      toast.success("Mensagem enviada");
    },
    onError: (mutationError: Error) => {
      toast.error("Nao foi possivel enviar a mensagem", { description: mutationError.message });
    },
  });

  const editCustomerMutation = useMutation({
    mutationFn: (content: string) => ticketService.editCustomerMessage(params.id, { content }),
    onSuccess: (nextTicket) => {
      syncTicket(nextTicket);
      setDraft("");
      setIsEditing(false);
      toast.success("Mensagem atualizada");
    },
    onError: (mutationError: Error) => {
      toast.error("Nao foi possivel atualizar a mensagem", { description: mutationError.message });
    },
  });

  const sendAgentMutation = useMutation({
    mutationFn: ({ content, markAsResolved }: { content: string; markAsResolved: boolean }) =>
      ticketService.sendAgentMessage(params.id, { content, markAsResolved }),
    onSuccess: (nextTicket, variables) => {
      syncTicket(nextTicket);
      setDraft("");
      setIsEditing(false);
      toast.success(
        variables.markAsResolved ? "Pergunta final enviada ao cliente" : "Mensagem enviada",
      );
    },
    onError: (mutationError: Error) => {
      toast.error("Nao foi possivel enviar a mensagem", { description: mutationError.message });
    },
  });

  const editAgentMutation = useMutation({
    mutationFn: (content: string) => ticketService.editAgentMessage(params.id, { content }),
    onSuccess: (nextTicket) => {
      syncTicket(nextTicket);
      setDraft("");
      setIsEditing(false);
      toast.success("Mensagem atualizada");
    },
    onError: (mutationError: Error) => {
      toast.error("Nao foi possivel atualizar a mensagem", { description: mutationError.message });
    },
  });

  const closeMutation = useMutation({
    mutationFn: () => ticketService.close(params.id),
    onSuccess: (nextTicket) => {
      syncTicket(nextTicket);
      toast.success("Chamado encerrado");
    },
    onError: (mutationError: Error) => {
      toast.error("Nao foi possivel encerrar o chamado", { description: mutationError.message });
    },
  });

  const isMutating =
    sendCustomerMutation.isPending ||
    editCustomerMutation.isPending ||
    sendAgentMutation.isPending ||
    editAgentMutation.isPending ||
    closeMutation.isPending;

  const helperText = ticket
    ? buildHelperText({ me, ticket, latestConversation, ownType })
    : "";

  const protocol = ticket?.id.slice(0, 8).toUpperCase();

  const handleSubmit = (markAsResolved = false) => {
    const content = draft.trim();
    if (!content) {
      toast.error("Digite uma mensagem antes de continuar");
      return;
    }

    if (ownType === "AGENT_MESSAGE") {
      if (isEditing) {
        editAgentMutation.mutate(content);
      } else {
        sendAgentMutation.mutate({ content, markAsResolved });
      }
      return;
    }

    if (isEditing) {
      editCustomerMutation.mutate(content);
      return;
    }

    sendCustomerMutation.mutate(content);
  };

  if (loadingMe || isLoading) {
    return (
      <div className="flex h-full items-center justify-center">
        <LoadingSpinner size="lg" label="Carregando chamado..." />
      </div>
    );
  }

  if (meError || error || !ticket) {
    return (
      <div className="p-4">
        <ErrorAlert error={(meError ?? error ?? new Error("Chamado nao encontrado")) as Error} />
      </div>
    );
  }

  return (
    <div className="flex h-full flex-col overflow-hidden">
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
              <h1 className="truncate text-sm font-semibold" title={ticket.title}>
                {ticket.title}
              </h1>
            </div>
            <p className="mt-1 text-xs text-muted-foreground">
              Protocolo: <span className="font-mono font-medium">{protocol}</span>
            </p>
          </div>
          <div className="flex shrink-0 flex-wrap items-center gap-2">
            <TicketStatusBadge status={ticket.status} />
            <TicketPriorityBadge priority={ticket.priority} />
          </div>
        </div>
      </header>

      <div className="flex-1 overflow-auto">
        <div className="mx-auto max-w-5xl gap-6 px-4 py-6 lg:grid lg:grid-cols-3">
          <div className="space-y-6 lg:col-span-2">
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-sm font-semibold">Descricao inicial</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="whitespace-pre-wrap text-sm leading-relaxed text-foreground/90">
                  {ticket.description}
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-sm font-semibold">Historico de interacoes</CardTitle>
              </CardHeader>
              <CardContent>
                {ticket.interactions.length === 0 ? (
                  <div className="flex flex-col items-center justify-center gap-2 rounded-lg border border-dashed py-10 text-center text-muted-foreground">
                    <MessageSquare className="h-8 w-8 opacity-40" />
                    <p className="text-sm">Nenhuma interacao ainda.</p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {ticket.interactions.map((interaction) => (
                      <InteractionBubble
                        key={interaction.id}
                        interaction={interaction}
                        isEditable={!!latestConversation && interaction.id === latestConversation.id && canEditLatest}
                      />
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-sm font-semibold">
                  {ownType === "AGENT_MESSAGE" ? "Responder chamado" : "Enviar mensagem"}
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <Alert>
                  <AlertCircle className="h-4 w-4" />
                  <AlertTitle>Fluxo da conversa</AlertTitle>
                  <AlertDescription>{helperText}</AlertDescription>
                </Alert>

                <Textarea
                  rows={5}
                  value={draft}
                  onChange={(event) => setDraft(event.target.value)}
                  placeholder={
                    ownType === "AGENT_MESSAGE"
                      ? "Digite a resposta que sera enviada ao cliente"
                      : "Digite a proxima mensagem para o atendimento"
                  }
                  disabled={isMutating || (!isEditing && !canSendNew && !canEditLatest)}
                />

                <div className="flex flex-wrap items-center gap-2">
                  {canEditLatest && !isEditing ? (
                    <Button
                      variant="outline"
                      onClick={() => {
                        setDraft(latestConversation?.content ?? "");
                        setIsEditing(true);
                      }}
                    >
                      Editar ultima mensagem
                    </Button>
                  ) : null}

                  {isEditing ? (
                    <>
                      <Button onClick={() => handleSubmit()} disabled={isMutating || !draft.trim()}>
                        Salvar edicao
                      </Button>
                      <Button
                        variant="outline"
                        onClick={() => {
                          setIsEditing(false);
                          setDraft("");
                        }}
                        disabled={isMutating}
                      >
                        Cancelar
                      </Button>
                    </>
                  ) : (
                    <>
                      <Button onClick={() => handleSubmit(false)} disabled={isMutating || !canSendNew || !draft.trim()}>
                        <Send className="mr-1.5 h-4 w-4" />
                        Enviar mensagem
                      </Button>
                      {ownType === "AGENT_MESSAGE" ? (
                        <Button
                          variant="outline"
                          onClick={() => handleSubmit(true)}
                          disabled={isMutating || !canSendNew || !draft.trim()}
                        >
                          <CheckCircle2 className="mr-1.5 h-4 w-4" />
                          Perguntar se foi solucionado
                        </Button>
                      ) : null}
                    </>
                  )}

                  {canClose ? (
                    <Button variant="secondary" onClick={() => closeMutation.mutate()} disabled={isMutating}>
                      Encerrar chamado
                    </Button>
                  ) : null}
                </div>
              </CardContent>
            </Card>
          </div>

          <div className="mt-6 lg:mt-0">
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-sm font-semibold">Informacoes do chamado</CardTitle>
              </CardHeader>
              <CardContent className="divide-y">
                <MetaRow icon={<User className="h-4 w-4" />} label="Solicitante">
                  <p className="text-sm font-medium">{ticket.user.name}</p>
                  <p className="text-xs text-muted-foreground">{ticket.user.email}</p>
                </MetaRow>

                <MetaRow icon={<UserCheck className="h-4 w-4" />} label="Atendente">
                  {ticket.agent ? (
                    <>
                      <p className="text-sm font-medium">{ticket.agent.name}</p>
                      <p className="font-mono text-xs text-muted-foreground">{ticket.agent.employeeId}</p>
                    </>
                  ) : (
                    <p className="text-sm italic text-muted-foreground">Aguardando atribuicao</p>
                  )}
                </MetaRow>

                <MetaRow icon={<Tag className="h-4 w-4" />} label="Categoria">
                  <p className="text-sm font-medium">{ticket.category.name}</p>
                  {ticket.category.subcategory ? (
                    <p className="text-xs text-muted-foreground">{ticket.category.subcategory}</p>
                  ) : null}
                </MetaRow>

                <MetaRow icon={<Shield className="h-4 w-4" />} label="Prioridade">
                  <TicketPriorityBadge priority={ticket.priority} />
                </MetaRow>

                <MetaRow icon={<Clock className="h-4 w-4" />} label="Perfil atual">
                  <p className="text-sm font-medium">{me?.name}</p>
                  <p className="text-xs text-muted-foreground">
                    {me?.role === "ADMIN" ? "Visao de atendimento" : "Visao do cliente"}
                  </p>
                </MetaRow>

                <MetaRow icon={<CalendarClock className="h-4 w-4" />} label="SLA">
                  {ticket.sla.active ? (
                    <>
                      <Badge className="bg-emerald-600 text-white hover:bg-emerald-700">Ativo</Badge>
                      {ticket.sla.startedAt ? (
                        <p className="mt-1 text-xs text-muted-foreground">
                          Iniciado em {formatDate(ticket.sla.startedAt)}
                        </p>
                      ) : null}
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
