"use client";

import Link from "next/link";
import { usePathname, useSearchParams } from "next/navigation";
import { Suspense } from "react";
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
} from "lucide-react";
import { cn } from "@/lib/utils";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";

/* ─── Individual nav link ─── */
interface NavLinkProps {
  href: string;
  icon: React.ReactNode;
  children: React.ReactNode;
  active: boolean;
  soon?: boolean;
}

function NavLink({ href, icon, children, active, soon }: NavLinkProps) {
  return (
    <Link
      href={soon ? "#" : href}
      className={cn(
        "flex items-center gap-2.5 px-4 py-2 text-sm transition-colors",
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

/* ─── Section label ─── */
function SectionLabel({ children }: { children: React.ReactNode }) {
  return (
    <p className="px-4 pb-1 pt-3 text-[10px] font-semibold uppercase tracking-widest text-muted-foreground/70">
      {children}
    </p>
  );
}

/* ─── Inner sidebar (needs useSearchParams → must be inside Suspense) ─── */
function SidebarInner() {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const tab = searchParams.get("tab") ?? "resumo";

  const isRelatorios = pathname === "/relatorios";

  return (
    <aside className="flex h-screen w-56 shrink-0 flex-col border-r bg-card">
      {/* Brand */}
      <Link
        href="/"
        className="flex items-center gap-2.5 border-b px-4 py-4 transition-colors hover:bg-muted/50"
      >
        <Store className="h-5 w-5 text-primary" />
        <span className="text-sm font-semibold">Kalles ERP</span>
      </Link>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto py-2">
        {/* ── PDV ── */}
        <SectionLabel>PDV</SectionLabel>
        <NavLink
          href="/caixas"
          icon={<ShoppingCart className="h-4 w-4" />}
          active={pathname === "/caixas"}
        >
          Vendas
        </NavLink>
        <NavLink
          href="/pdv"
          icon={<Terminal className="h-4 w-4" />}
          active={pathname === "/pdv"}
        >
          Terminal
        </NavLink>
        <NavLink
          href="/produtos"
          icon={<Package className="h-4 w-4" />}
          active={pathname === "/produtos"}
        >
          Catálogo
        </NavLink>

        <Separator className="my-2" />

        {/* ── Relatórios (under PDV) ── */}
        <NavLink
          href="/relatorios?tab=resumo"
          icon={<LayoutDashboard className="h-4 w-4" />}
          active={isRelatorios && tab === "resumo"}
        >
          Resumo do Turno
        </NavLink>
        <NavLink
          href="/relatorios?tab=pagamentos"
          icon={<CreditCard className="h-4 w-4" />}
          active={isRelatorios && tab === "pagamentos"}
        >
          Meios de Pagamento
        </NavLink>
        <NavLink
          href="/relatorios?tab=por-produto"
          icon={<ShoppingBag className="h-4 w-4" />}
          active={isRelatorios && tab === "por-produto"}
          soon
        >
          Por Produto
        </NavLink>
        <NavLink
          href="/relatorios?tab=historico"
          icon={<History className="h-4 w-4" />}
          active={isRelatorios && tab === "historico"}
          soon
        >
          Histórico
        </NavLink>

        <Separator className="my-2" />

        {/* ── Financeiro (under PDV) ── */}
        <NavLink
          href="#"
          icon={<TrendingUp className="h-4 w-4" />}
          active={false}
          soon
        >
          A Receber
        </NavLink>
        <NavLink
          href="#"
          icon={<TrendingDown className="h-4 w-4" />}
          active={false}
          soon
        >
          A Pagar
        </NavLink>

        <Separator className="my-2" />

        {/* ── Admin ── */}
        <SectionLabel>Admin</SectionLabel>
        <NavLink
          href="/admin/operadores"
          icon={<UserCog className="h-4 w-4" />}
          active={pathname === "/admin/operadores"}
        >
          Operadores
        </NavLink>
        <NavLink
          href="/admin/produtos"
          icon={<Package className="h-4 w-4" />}
          active={pathname === "/admin/produtos"}
        >
          Produtos
        </NavLink>
        <NavLink
          href="/admin/clientes"
          icon={<Users className="h-4 w-4" />}
          active={pathname === "/admin/clientes"}
        >
          Clientes
        </NavLink>
        <NavLink
          href="/admin/depositos"
          icon={<Warehouse className="h-4 w-4" />}
          active={pathname === "/admin/depositos"}
        >
          Depósitos
        </NavLink>
        <NavLink
          href="/admin/estoque"
          icon={<Layers className="h-4 w-4" />}
          active={pathname === "/admin/estoque"}
        >
          Estoque
        </NavLink>
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
