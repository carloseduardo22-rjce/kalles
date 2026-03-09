"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  LifeBuoy,
  Plus,
  RefreshCw,
  Search,
  Tag,
  UserCircle,
  ChevronRight,
  Filter,
} from "lucide-react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { ErrorAlert } from "@/shared/components/error-alert";
import { EmptyState } from "@/shared/components/empty-state";
import { ticketService } from "@/features/support/services/ticket.service";
import { OpenTicketDialog } from "@/features/support/components/open-ticket-dialog";
import { TicketStatusBadge } from "@/features/support/components/ticket-status-badge";
import { TicketPriorityBadge } from "@/features/support/components/ticket-priority-badge";
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

/* ─── Ticket row ────────────────────────────────────────────────────────── */
function TicketRow({
  ticket,
  onClick,
}: {
  ticket: TicketResponse;
  onClick: () => void;
}) {
  return (
    <tr
      onClick={onClick}
      className="cursor-pointer border-b last:border-0 hover:bg-accent transition-colors"
    >
      <td className="px-4 py-3">
        <p className="text-sm font-medium text-foreground line-clamp-1">
          {ticket.title}
        </p>
        <p className="mt-0.5 text-xs text-muted-foreground">
          {ticket.category.name}
          {ticket.category.subcategory
            ? ` — ${ticket.category.subcategory}`
            : ""}
        </p>
      </td>

      <td className="hidden px-4 py-3 sm:table-cell">
        <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
          <UserCircle className="h-3.5 w-3.5 shrink-0" />
          <span className="max-w-35 truncate">{ticket.user.name}</span>
        </div>
      </td>

      <td className="px-4 py-3">
        <TicketStatusBadge status={ticket.status} />
      </td>

      <td className="hidden px-4 py-3 md:table-cell">
        <TicketPriorityBadge priority={ticket.priority} />
      </td>

      <td className="hidden px-4 py-3 text-xs text-muted-foreground lg:table-cell">
        {ticket.agent ? (
          <span>{ticket.agent.name}</span>
        ) : (
          <span className="italic opacity-60">Sem atendente</span>
        )}
      </td>

      <td className="hidden px-4 py-3 text-xs text-muted-foreground xl:table-cell">
        {ticket.sla.startedAt ? formatDate(ticket.sla.startedAt) : "—"}
      </td>

      <td className="px-4 py-3">
        <ChevronRight className="h-4 w-4 text-muted-foreground" />
      </td>
    </tr>
  );
}

/* ─── Page ──────────────────────────────────────────────────────────────── */
export default function SuportePage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<TicketStatus | "ALL">("ALL");
  const [openDialog, setOpenDialog] = useState(false);

  const {
    data: tickets = [],
    isLoading,
    error,
    refetch,
    isFetching,
  } = useQuery({
    queryKey: ["support-tickets", statusFilter],
    queryFn: () =>
      ticketService.listAll(statusFilter !== "ALL" ? statusFilter : undefined),
  });

  const { mutate: openTicket, isPending } = useMutation({
    mutationFn: (data: OpenTicketRequest) => ticketService.open(data),
    onSuccess: (ticket) => {
      toast.success("Chamado aberto com sucesso!", {
        description: `Protocolo: ${ticket.id.slice(0, 8).toUpperCase()}`,
      });
      setOpenDialog(false);
      queryClient.invalidateQueries({ queryKey: ["support-tickets"] });
    },
    onError: (err: Error) => {
      toast.error("Falha ao abrir chamado", { description: err.message });
    },
  });

  const filtered = tickets.filter(
    (t) =>
      !search ||
      t.title.toLowerCase().includes(search.toLowerCase()) ||
      t.user.name.toLowerCase().includes(search.toLowerCase()) ||
      t.user.email.toLowerCase().includes(search.toLowerCase()),
  );

  return (
    <div className="flex h-full flex-col overflow-hidden">
      {/* Header */}
      <header className="border-b bg-card px-4 py-3 shadow-sm">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <LifeBuoy className="h-5 w-5 text-muted-foreground" />
            <div>
              <h1 className="text-sm font-semibold leading-none">Suporte</h1>
              <p className="mt-0.5 text-xs text-muted-foreground">
                Acompanhe e abra chamados de suporte
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
              <RefreshCw
                className={`h-4 w-4 ${isFetching ? "animate-spin" : ""}`}
              />
            </Button>
            <Button size="sm" onClick={() => setOpenDialog(true)}>
              <Plus className="mr-1.5 h-4 w-4" />
              Abrir chamado
            </Button>
          </div>
        </div>
      </header>

      {/* Filter bar */}
      <div className="border-b bg-muted/30 px-4 py-3">
        <div className="flex items-center gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Buscar por título, nome ou e-mail…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="h-8 pl-8 text-sm"
            />
          </div>
          <div className="flex items-center gap-1.5 text-muted-foreground">
            <Filter className="h-3.5 w-3.5 shrink-0" />
          </div>
          <Select
            value={statusFilter}
            onValueChange={(val) =>
              setStatusFilter(val as TicketStatus | "ALL")
            }
          >
            <SelectTrigger className="h-8 w-48 text-sm">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {STATUS_OPTIONS.map((opt) => (
                <SelectItem key={opt.value} value={opt.value}>
                  {opt.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 overflow-auto">
        {isLoading ? (
          <div className="flex h-full items-center justify-center">
            <LoadingSpinner size="lg" label="Carregando chamados…" />
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
                : 'Clique em "Abrir chamado" para registrar uma nova solicitação.'
            }
          />
        ) : (
          <table className="w-full text-sm">
            <thead className="sticky top-0 z-10 border-b bg-muted shadow-sm">
              <tr>
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-muted-foreground">
                  Chamado
                </th>
                <th className="hidden px-4 py-2.5 text-left text-xs font-semibold text-muted-foreground sm:table-cell">
                  Solicitante
                </th>
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-muted-foreground">
                  Status
                </th>
                <th className="hidden px-4 py-2.5 text-left text-xs font-semibold text-muted-foreground md:table-cell">
                  Prioridade
                </th>
                <th className="hidden px-4 py-2.5 text-left text-xs font-semibold text-muted-foreground lg:table-cell">
                  Atendente
                </th>
                <th className="hidden px-4 py-2.5 text-left text-xs font-semibold text-muted-foreground xl:table-cell">
                  Abertura
                </th>
                <th className="px-4 py-2.5" />
              </tr>
            </thead>
            <tbody className="divide-y">
              {filtered.map((ticket, i) => (
                <TicketRow
                  key={ticket.id}
                  ticket={ticket}
                  onClick={() => router.push(`/suporte/${ticket.id}`)}
                />
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Footer */}
      <footer className="border-t bg-card px-4 py-2 text-xs text-muted-foreground">
        {filtered.length} chamado{filtered.length !== 1 ? "s" : ""} exibido
        {filtered.length !== 1 ? "s" : ""}
        {tickets.length !== filtered.length && ` de ${tickets.length} no total`}
      </footer>

      {/* Dialog */}
      <OpenTicketDialog
        open={openDialog}
        onOpenChange={setOpenDialog}
        onSubmit={openTicket}
        isPending={isPending}
      />
    </div>
  );
}
