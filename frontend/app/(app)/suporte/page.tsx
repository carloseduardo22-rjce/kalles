"use client";

import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ChevronRight,
  Filter,
  LifeBuoy,
  Plus,
  RefreshCw,
  Search,
  UserCircle,
} from "lucide-react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ErrorAlert } from "@/shared/components/error-alert";
import { EmptyState } from "@/shared/components/empty-state";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { TicketPriorityBadge } from "@/features/support/components/ticket-priority-badge";
import { TicketStatusBadge } from "@/features/support/components/ticket-status-badge";
import { OpenTicketDialog } from "@/features/support/components/open-ticket-dialog";
import { ticketService } from "@/features/support/services/ticket.service";
import { formatDate } from "@/shared/utils/formatters";
import type {
  OpenTicketRequest,
  TicketResponse,
  TicketStatus,
} from "@/features/support/types";

const STATUS_OPTIONS: { value: TicketStatus | "ALL"; label: string }[] = [
  { value: "ALL", label: "Todos os status" },
  { value: "OPEN", label: "Aberto" },
  { value: "IN_PROGRESS", label: "Em atendimento" },
  { value: "WAITING_FOR_CUSTOMER", label: "Aguardando cliente" },
  { value: "RESOLVED", label: "Resolvido" },
  { value: "CLOSED", label: "Fechado" },
];

function TicketRow({
  ticket,
  onClick,
  showRequester,
}: {
  ticket: TicketResponse;
  onClick: () => void;
  showRequester: boolean;
}) {
  return (
    <tr
      onClick={onClick}
      className="cursor-pointer border-b transition-colors hover:bg-accent"
    >
      <td className="px-4 py-3">
        <p className="line-clamp-1 text-sm font-medium text-foreground">
          {ticket.title}
        </p>
        <p className="mt-0.5 text-xs text-muted-foreground">
          {ticket.category.name}
          {ticket.category.subcategory ? ` - ${ticket.category.subcategory}` : ""}
        </p>
      </td>

      <td className="hidden px-4 py-3 sm:table-cell">
        {showRequester ? (
          <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
            <UserCircle className="h-3.5 w-3.5 shrink-0" />
            <span className="max-w-35 truncate">{ticket.user.name}</span>
          </div>
        ) : (
          <span className="text-xs text-muted-foreground">{ticket.user.email}</span>
        )}
      </td>

      <td className="px-4 py-3">
        <TicketStatusBadge status={ticket.status} />
      </td>

      <td className="hidden px-4 py-3 md:table-cell">
        <TicketPriorityBadge priority={ticket.priority} />
      </td>

      <td className="hidden px-4 py-3 text-xs text-muted-foreground lg:table-cell">
        {ticket.agent ? ticket.agent.name : <span className="italic opacity-60">Sem atendente</span>}
      </td>

      <td className="hidden px-4 py-3 text-xs text-muted-foreground xl:table-cell">
        {ticket.sla.startedAt ? formatDate(ticket.sla.startedAt) : "-"}
      </td>

      <td className="px-4 py-3">
        <ChevronRight className="h-4 w-4 text-muted-foreground" />
      </td>
    </tr>
  );
}

export default function SuportePage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<TicketStatus | "ALL">("ALL");
  const [page, setPage] = useState(0);
  const [openDialog, setOpenDialog] = useState(false);

  useEffect(() => {
    setPage(0);
  }, [statusFilter]);

  const { data: me, isLoading: loadingMe } = useQuery({
    queryKey: ["support-me"],
    queryFn: ticketService.me,
  });

  const {
    data: ticketPage,
    isLoading,
    error,
    refetch,
    isFetching,
  } = useQuery({
    queryKey: ["support-tickets", statusFilter, page],
    queryFn: () =>
      ticketService.listPage(
        page,
        20,
        statusFilter !== "ALL" ? statusFilter : undefined,
      ),
    enabled: !!me,
  });
  const tickets = ticketPage?.content ?? [];

  const { mutate: openTicket, isPending } = useMutation({
    mutationFn: (data: OpenTicketRequest) => ticketService.open(data),
    onSuccess: (ticket) => {
      toast.success("Chamado aberto com sucesso", {
        description: `Protocolo: ${ticket.id.slice(0, 8).toUpperCase()}`,
      });
      setOpenDialog(false);
      queryClient.invalidateQueries({ queryKey: ["support-tickets"] });
    },
    onError: (err: Error) => {
      toast.error("Falha ao abrir chamado", { description: err.message });
    },
  });

  const filtered = tickets.filter((ticket) => {
    const term = search.trim().toLowerCase();
    if (!term) return true;
    return [ticket.title, ticket.user.name, ticket.user.email]
      .filter(Boolean)
      .some((value) => value.toLowerCase().includes(term));
  });

  const isAdmin = me?.role === "ADMIN";

  if (loadingMe || (isLoading && !me)) {
    return (
      <div className="flex h-full items-center justify-center">
        <LoadingSpinner size="lg" label="Carregando suporte..." />
      </div>
    );
  }

  return (
    <div className="flex h-full flex-col overflow-hidden" data-onboarding="support-page">
      <header
        className="border-b bg-card px-4 py-3 shadow-sm"
        data-onboarding="support-header"
      >
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <LifeBuoy className="h-5 w-5 text-muted-foreground" />
            <div>
              <h1 className="text-sm font-semibold leading-none">Suporte</h1>
              <p className="mt-0.5 text-xs text-muted-foreground">
                {isAdmin
                  ? "Acompanhe os chamados da operacao e responda clientes"
                  : "Acompanhe seus chamados e converse com o atendimento"}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => refetch()}
              disabled={isFetching}
              title="Atualizar"
            >
              <RefreshCw className={`h-4 w-4 ${isFetching ? "animate-spin" : ""}`} />
            </Button>
            {!isAdmin && (
              <Button size="sm" onClick={() => setOpenDialog(true)}>
                <Plus className="mr-1.5 h-4 w-4" />
                Abrir chamado
              </Button>
            )}
          </div>
        </div>
      </header>

      <div className="border-b bg-muted/30 px-4 py-3" data-onboarding="support-filters">
        <div className="flex items-center gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder={isAdmin ? "Buscar por titulo, nome ou e-mail..." : "Buscar por titulo..."}
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              className="h-8 pl-8 text-sm"
            />
          </div>
          <Filter className="h-3.5 w-3.5 shrink-0 text-muted-foreground" />
          <Select value={statusFilter} onValueChange={(value) => setStatusFilter(value as TicketStatus | "ALL")}>
            <SelectTrigger className="h-8 w-48 text-sm">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {STATUS_OPTIONS.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="flex-1 overflow-auto" data-onboarding="support-content">
        {isLoading ? (
          <div className="flex h-full items-center justify-center">
            <LoadingSpinner size="lg" label="Carregando chamados..." />
          </div>
        ) : error ? (
          <div className="p-4">
            <ErrorAlert error={error} />
          </div>
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<LifeBuoy className="h-10 w-10 opacity-40" />}
            title="Nenhum chamado encontrado"
            description={
              search
                ? "Tente ajustar os filtros ou o termo de busca."
                : isAdmin
                  ? "Nenhum chamado esta disponivel para os filtros atuais."
                  : 'Clique em "Abrir chamado" para registrar uma nova solicitacao.'
            }
          />
        ) : (
          <table className="w-full text-sm">
            <thead className="sticky top-0 z-10 border-b bg-muted shadow-sm">
              <tr>
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-muted-foreground">Chamado</th>
                <th className="hidden px-4 py-2.5 text-left text-xs font-semibold text-muted-foreground sm:table-cell">
                  {isAdmin ? "Solicitante" : "Conta"}
                </th>
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-muted-foreground">Status</th>
                <th className="hidden px-4 py-2.5 text-left text-xs font-semibold text-muted-foreground md:table-cell">Prioridade</th>
                <th className="hidden px-4 py-2.5 text-left text-xs font-semibold text-muted-foreground lg:table-cell">Atendente</th>
                <th className="hidden px-4 py-2.5 text-left text-xs font-semibold text-muted-foreground xl:table-cell">Abertura</th>
                <th className="px-4 py-2.5" />
              </tr>
            </thead>
            <tbody className="divide-y">
              {filtered.map((ticket) => (
                <TicketRow
                  key={ticket.id}
                  ticket={ticket}
                  showRequester={isAdmin}
                  onClick={() => router.push(`/suporte/${ticket.id}`)}
                />
              ))}
            </tbody>
          </table>
        )}
      </div>

      <footer className="border-t bg-card px-4 py-2 text-xs text-muted-foreground">
        <div className="flex items-center justify-between gap-3">
          <span>
            {filtered.length} chamado{filtered.length !== 1 ? "s" : ""} exibido
            {tickets.length !== filtered.length ? ` de ${tickets.length} na pagina` : ""}
            {ticketPage ? `, ${ticketPage.totalElements} no total` : ""}
          </span>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage((current) => Math.max(current - 1, 0))}
              disabled={page === 0 || isFetching}
            >
              Anterior
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() =>
                setPage((current) =>
                  ticketPage && current + 1 < ticketPage.totalPages
                    ? current + 1
                    : current,
                )
              }
              disabled={!ticketPage || page + 1 >= ticketPage.totalPages || isFetching}
            >
              Proxima
            </Button>
          </div>
        </div>
      </footer>

      <OpenTicketDialog open={openDialog} onOpenChange={setOpenDialog} onSubmit={openTicket} isPending={isPending} />
    </div>
  );
}
