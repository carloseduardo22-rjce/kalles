"use client";

import Link from "next/link";
import { usePathname, useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState } from "react";
import {
  ShoppingCart,
  Package,
  CreditCard,
  History,
  TrendingDown,
  TrendingUp,
  LayoutDashboard,
  ShoppingBag,
  Store,
  Terminal,
  Users,
  Warehouse,
  Layers,
  UserCog,
  Gift,
  Settings,
  ChevronRight,
  LifeBuoy,
  Ticket,
  Target,
  SmartphoneNfc,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";
import { companySettingsService } from "@/shared/services/company-settings.service";
import { useCompany } from "@/shared/contexts/company-context";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { UserCircle } from "lucide-react";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { cashRegisterService } from "@/features/cash-register/services/cash-register.service";

/* ─── Individual nav link ─── */
interface NavLinkProps {
  href: string;
  icon: React.ReactNode;
  children: React.ReactNode;
  active: boolean;
  soon?: boolean;
  disabled?: boolean;
  sub?: boolean;
}

function NavLink({
  href,
  icon,
  children,
  active,
  soon,
  disabled,
  sub,
}: NavLinkProps) {
  const isBlocked = Boolean(soon || disabled);

  return (
    <Link
      href={isBlocked ? "#" : href}
      onClick={(event) => {
        if (isBlocked) {
          event.preventDefault();
        }
      }}
      aria-disabled={isBlocked}
      className={cn(
        "flex items-center gap-2.5 py-2 text-sm transition-colors",
        sub ? "pl-8 pr-4" : "px-4",
        active
          ? "border-primary bg-primary/10 font-medium text-primary"
          : "text-muted-foreground hover:bg-muted hover:text-foreground",
        isBlocked && "opacity-50 cursor-not-allowed",
      )}
    >
      <span className="shrink-0">{icon}</span>
      <span className="flex-1 truncate">{children}</span>
      {soon && (
        <Badge
          variant="outline"
          className="h-4 px-1.5 py-0 text-[9px] font-normal"
        >
          breve
        </Badge>
      )}
    </Link>
  );
}

/* ─── Sub-section label ─── */
function SubSectionLabel({ children }: { children: React.ReactNode }) {
  return (
    <p className="pl-8 pr-4 pb-0.5 pt-2.5 text-[9px] font-semibold uppercase tracking-widest text-muted-foreground/50">
      {children}
    </p>
  );
}

/* ─── Collapsible nav group ─── */
interface NavGroupProps {
  icon: React.ReactNode;
  label: string;
  defaultOpen?: boolean;
  children: React.ReactNode;
}

function NavGroup({
  icon,
  label,
  defaultOpen = false,
  children,
}: NavGroupProps) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <div>
      <button
        onClick={() => setOpen((o) => !o)}
        className="flex w-full items-center gap-2.5 px-4 py-2.5 text-sm font-medium transition-colors hover:bg-muted hover:text-foreground text-foreground/80"
      >
        <span className="shrink-0">{icon}</span>
        <span className="flex-1 truncate text-left">{label}</span>
        <ChevronRight
          className={cn(
            "h-3.5 w-3.5 shrink-0 text-muted-foreground transition-transform duration-200",
            open && "rotate-90",
          )}
        />
      </button>

      {open && <div className="pb-1">{children}</div>}
    </div>
  );
}

/* ─── Inner sidebar (needs useSearchParams → must be inside Suspense) ─── */
function SidebarInner() {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const tab = searchParams.get("tab") ?? "resumo";
  const [logoUrl, setLogoUrl] = useState<string | null>(null);
  const [hasIntegrationPrereqs, setHasIntegrationPrereqs] = useState(true);

  const { activeCompanyId, companies, setActiveCompany } = useCompany();

  useEffect(() => {
    setLogoUrl(companySettingsService.getLogo());

    async function checkPrereqs() {
      if (companies && companies.length === 0) {
        setHasIntegrationPrereqs(false);
        return;
      }
      try {
        const registers = await cashRegisterService.listCashRegisters();
        setHasIntegrationPrereqs(registers.length > 0);
      } catch {
        setHasIntegrationPrereqs(false);
      }
    }
    checkPrereqs();
  }, [companies]);

  const isRelatorios = pathname === "/relatorios";

  const isAdminSection = pathname.startsWith("/admin");

  return (
    <aside
      className="flex h-screen w-56 shrink-0 flex-col border-r bg-card"
      data-onboarding="sidebar"
    >
      {/* Brand */}
      <Link
        href="/"
        className="flex items-center gap-2.5 border-b px-4 py-4 transition-colors hover:bg-muted/50"
      >
        {logoUrl ? (
          <img
            src={logoUrl}
            alt="Logo"
            className="h-6 w-6 rounded object-contain"
          />
        ) : (
          <Store className="h-5 w-5 text-primary" />
        )}
        <span className="text-sm font-semibold">Kalles ERP</span>
      </Link>

      {/* Seletor de Filial (Apenas para Admin em rotas administrativas) */}
      {isAdminSection && (
        <div className="px-4 py-3 border-b">
          <Select
            value={activeCompanyId || ""}
            onValueChange={(val) => setActiveCompany(val)}
          >
            <SelectTrigger className="w-full h-8 text-xs">
              <SelectValue placeholder="Selecione a Filial" />
            </SelectTrigger>
            <SelectContent>
              {companies.map((company) => (
                <SelectItem
                  key={company.id}
                  value={company.id}
                  className="text-xs"
                >
                  {company.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <div className="mt-2 rounded-md border border-primary/20 bg-primary/5 px-2 py-1.5">
            <p className="text-[10px] uppercase tracking-wide text-muted-foreground">
              Filial ativa
            </p>
            <p className="text-xs font-semibold text-primary truncate">
              {activeCompanyId
                ? (companies.find((company) => company.id === activeCompanyId)
                    ?.name ?? "Filial selecionada")
                : "Nenhuma filial selecionada"}
            </p>
          </div>
        </div>
      )}

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto py-2">
        {/* ── PDV ── */}
        <NavGroup
          icon={<Terminal className="h-4 w-4" />}
          label="PDV"
          defaultOpen={true}
        >
          <SubSectionLabel>Operações</SubSectionLabel>
          <NavLink
            href="/caixas"
            icon={<ShoppingCart className="h-4 w-4" />}
            active={pathname === "/caixas"}
            sub
          >
            Caixas
          </NavLink>
          <NavLink
            href="/pdv"
            icon={<Terminal className="h-4 w-4" />}
            active={pathname === "/pdv"}
            sub
          >
            Terminal
          </NavLink>
          <NavLink
            href="/produtos"
            icon={<Package className="h-4 w-4" />}
            active={pathname === "/produtos"}
            sub
          >
            Catálogo
          </NavLink>

          <SubSectionLabel>Relatórios</SubSectionLabel>
          <NavLink
            href="/relatorios?tab=resumo"
            icon={<LayoutDashboard className="h-4 w-4" />}
            active={isRelatorios && tab === "resumo"}
            sub
          >
            Resumo do Turno
          </NavLink>
          <NavLink
            href="/relatorios?tab=pagamentos"
            icon={<CreditCard className="h-4 w-4" />}
            active={isRelatorios && tab === "pagamentos"}
            sub
          >
            Meios de Pagamento
          </NavLink>
          <NavLink
            href="/relatorios?tab=lucro-fornecedores"
            icon={<TrendingDown className="h-4 w-4" />}
            active={isRelatorios && tab === "lucro-fornecedores"}
            sub
          >
            Lucro x Fornecedor
          </NavLink>
          <NavLink
            href="/relatorios?tab=por-produto"
            icon={<ShoppingBag className="h-4 w-4" />}
            active={isRelatorios && tab === "por-produto"}
            sub
            soon
          >
            Por Produto
          </NavLink>
          <NavLink
            href="/relatorios?tab=historico"
            icon={<History className="h-4 w-4" />}
            active={isRelatorios && tab === "historico"}
            sub
          >
            Histórico
          </NavLink>

          <SubSectionLabel>Financeiro</SubSectionLabel>
          <NavLink
            href="#"
            icon={<TrendingUp className="h-4 w-4" />}
            active={false}
            sub
            soon
          >
            A Receber
          </NavLink>
          <NavLink
            href="#"
            icon={<TrendingDown className="h-4 w-4" />}
            active={false}
            sub
            soon
          >
            A Pagar
          </NavLink>
        </NavGroup>

        {/* ── Admin ── */}
        <NavGroup
          icon={<UserCog className="h-4 w-4" />}
          label="Admin"
          defaultOpen={true}
        >
          <NavLink
            href="/admin/lojas"
            icon={<Store className="h-4 w-4" />}
            active={pathname === "/admin/lojas"}
            sub
          >
            Lojas
          </NavLink>
          <NavLink
            href="/admin/operadores"
            icon={<UserCog className="h-4 w-4" />}
            active={pathname === "/admin/operadores"}
            sub
          >
            Operadores
          </NavLink>
          <NavLink
            href="/admin/produtos"
            icon={<Package className="h-4 w-4" />}
            active={pathname === "/admin/produtos"}
            sub
          >
            Produtos
          </NavLink>
          <NavLink
            href="/admin/clientes"
            icon={<Users className="h-4 w-4" />}
            active={pathname === "/admin/clientes"}
            sub
          >
            Clientes
          </NavLink>
          <NavLink
            href="/admin/depositos"
            icon={<Warehouse className="h-4 w-4" />}
            active={pathname === "/admin/depositos"}
            sub
          >
            Depósitos
          </NavLink>
          <NavLink
            href="/admin/estoque"
            icon={<Layers className="h-4 w-4" />}
            active={pathname === "/admin/estoque"}
            sub
          >
            Estoque
          </NavLink>
          <NavLink
            href="/admin/fidelidade"
            icon={<Gift className="h-4 w-4" />}
            active={pathname === "/admin/fidelidade"}
            sub
          >
            Fidelidade
          </NavLink>
          <NavLink
            href="/admin/metas"
            icon={<Target className="h-4 w-4" />}
            active={pathname === "/admin/metas"}
            sub
          >
            Metas
          </NavLink>
          <NavLink
            href="/admin/notas"
            icon={<Ticket className="h-4 w-4" />}
            active={pathname === "/admin/notas"}
            sub
          >
            Bloco de Notas
          </NavLink>
          {/* ── Configurar pagamentos ── */}
          <NavGroup
            icon={<CreditCard className="h-4 w-4" />}
            label="Configurar pagamentos"
            defaultOpen={
              pathname.includes("/admin/pagamentos") ||
              pathname.includes("/admin/assinatura") ||
              pathname.includes("/admin/integrar-maquininha")
            }
          >
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger asChild>
                  <div>
                    <NavLink
                      href="/admin/pagamentos"
                      icon={<Store className="h-4 w-4" />}
                      active={pathname === "/admin/pagamentos"}
                      disabled={!hasIntegrationPrereqs}
                      sub
                    >
                      Mercado Pago e PDV
                    </NavLink>
                  </div>
                </TooltipTrigger>
                {!hasIntegrationPrereqs && (
                  <TooltipContent side="right">
                    <p>Necessário criar Lojas e Caixas primeiramente.</p>
                  </TooltipContent>
                )}
              </Tooltip>
            </TooltipProvider>
            <NavLink
              href="/admin/assinatura"
              icon={<CreditCard className="h-4 w-4" />}
              active={pathname.startsWith("/admin/assinatura")}
              sub
            >
              Assinatura do ERP
            </NavLink>
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger asChild>
                  <div>
                    <NavLink
                      href="/admin/integrar-maquininha"
                      icon={<SmartphoneNfc className="h-4 w-4" />}
                      active={pathname === "/admin/integrar-maquininha"}
                      disabled={!hasIntegrationPrereqs}
                      sub
                    >
                      Integrar maquininha a PDV
                    </NavLink>
                  </div>
                </TooltipTrigger>
                {!hasIntegrationPrereqs && (
                  <TooltipContent side="right">
                    <p>Necessário criar Lojas e Caixas primeiramente.</p>
                  </TooltipContent>
                )}
              </Tooltip>
            </TooltipProvider>
          </NavGroup>

          {/* ── Configuracoes ── */}
          <NavLink
            href="/admin/configuracoes"
            icon={<Settings className="h-4 w-4" />}
            active={pathname === "/admin/configuracoes"}
          >
            Configurações
          </NavLink>
        </NavGroup>

        {/* ── Suporte ── */}
        <NavGroup
          icon={<LifeBuoy className="h-4 w-4" />}
          label="Suporte"
          defaultOpen={true}
        >
          <NavLink
            href="/suporte"
            icon={<Ticket className="h-4 w-4" />}
            active={pathname.startsWith("/suporte")}
            sub
          >
            Chamados
          </NavLink>
        </NavGroup>
      </nav>

      {/* Conta e Perfil */}
      <div className="border-t p-3">
        <Link
          href="/admin/perfil"
          className={cn(
            "flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors",
            pathname === "/admin/perfil"
              ? "bg-muted font-medium text-foreground"
              : "text-muted-foreground hover:bg-muted hover:text-foreground",
          )}
        >
          <UserCircle className="h-5 w-5" />
          <div className="flex flex-col flex-1 overflow-hidden">
            <span className="truncate leading-tight">Minha Conta</span>
            <span className="text-[10px] text-muted-foreground/70 truncate">
              Ver Perfil
            </span>
          </div>
        </Link>
      </div>

      {/* Footer */}
      <div className="border-t px-4 py-3 text-[11px] text-muted-foreground/60">
        Kalles ERP
      </div>
    </aside>
  );
}

/* ─── Public export wrapped in Suspense ─── */
export function NavSidebar() {
  return (
    <Suspense
      fallback={
        <aside className="flex h-screen w-56 shrink-0 flex-col border-r bg-card" />
      }
    >
      <SidebarInner />
    </Suspense>
  );
}
