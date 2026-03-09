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
} from "lucide-react";
import { cn } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";
import { companySettingsService } from "@/shared/services/company-settings.service";

/* ─── Individual nav link ─── */
interface NavLinkProps {
  href: string;
  icon: React.ReactNode;
  children: React.ReactNode;
  active: boolean;
  soon?: boolean;
  sub?: boolean;
}

function NavLink({ href, icon, children, active, soon, sub }: NavLinkProps) {
  return (
    <Link
      href={soon ? "#" : href}
      className={cn(
        "flex items-center gap-2.5 py-2 text-sm transition-colors",
        sub ? "pl-8 pr-4" : "px-4",
        active
          ? "border-primary bg-primary/10 font-medium text-primary"
          : "text-muted-foreground hover:bg-muted hover:text-foreground",
        soon && "pointer-events-none opacity-40",
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

  useEffect(() => {
    setLogoUrl(companySettingsService.getLogo());
  }, []);

  const isRelatorios = pathname === "/relatorios";

  const isPdvSection =
    ["/caixas", "/pdv", "/produtos"].includes(pathname) || isRelatorios;

  const isAdminSection = pathname.startsWith("/admin");

  const isSupportSection = pathname.startsWith("/suporte");

  return (
    <aside className="flex h-screen w-56 shrink-0 flex-col border-r bg-card">
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

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto py-2">
        {/* ── PDV ── */}
        <NavGroup
          icon={<Terminal className="h-4 w-4" />}
          label="PDV"
          defaultOpen={isPdvSection}
        >
          <SubSectionLabel>Operações</SubSectionLabel>
          <NavLink
            href="/caixas"
            icon={<ShoppingCart className="h-4 w-4" />}
            active={pathname === "/caixas"}
            sub
          >
            Vendas
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
            soon
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

        <div className="mx-4 my-1 border-t" />

        {/* ── Admin ── */}
        <NavGroup
          icon={<UserCog className="h-4 w-4" />}
          label="Admin"
          defaultOpen={isAdminSection}
        >
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
            href="/admin/configuracoes"
            icon={<Settings className="h-4 w-4" />}
            active={pathname === "/admin/configuracoes"}
            sub
          >
            Configurações
          </NavLink>
        </NavGroup>

        <div className="mx-4 my-1 border-t" />

        {/* ── Suporte ── */}
        <NavGroup
          icon={<LifeBuoy className="h-4 w-4" />}
          label="Suporte"
          defaultOpen={isSupportSection}
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

      {/* Footer */}
      <div className="border-t px-4 py-3 text-[11px] text-muted-foreground/60">
        Kalles ERP v1.0
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
