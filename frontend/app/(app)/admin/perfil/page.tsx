"use client";

import { useQuery } from "@tanstack/react-query";
import { useCompany } from "@/shared/contexts/company-context";
import { authService } from "@/shared/services/auth.service";
import type { AccountRole } from "@/shared/types";
import { Mail, Building, Shield } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { LogoutButton } from "@/components/logout-button";

const ROLE_LABELS: Record<AccountRole, string> = {
  ADMIN: "Administrador",
  OPERATOR: "Operador",
};

function initials(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  const first = parts[0]?.[0] ?? "";
  const last = parts.length > 1 ? parts[parts.length - 1][0] : "";
  return (first + last).toUpperCase();
}

export default function PerfilPage() {
  const { activeCompanyId, companies } = useCompany();
  const currentCompany = companies.find((c) => c.id === activeCompanyId);
  const { data: me, isLoading, isError } = useQuery({
    queryKey: ["auth-me"],
    queryFn: authService.me,
  });

  return (
    <div className="container mx-auto max-w-4xl py-10 space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-bold tracking-tight">Meu Perfil</h1>
        <p className="text-muted-foreground">
          Dados da conta com que você entrou no sistema.
        </p>
      </div>

      {isLoading ? (
        <LoadingSpinner label="Carregando perfil…" />
      ) : isError || !me ? (
        <p className="text-sm text-destructive">
          Não foi possível carregar os dados da conta.
        </p>
      ) : (
        <div className="grid gap-6 md:grid-cols-3">
          <div className="md:col-span-1 space-y-6">
            <Card className="border-0 shadow-md">
              <CardContent className="pt-6 flex flex-col items-center text-center space-y-4">
                <Avatar className="h-32 w-32 ring-4 ring-primary/10">
                  <AvatarFallback className="text-4xl bg-primary/5 text-primary">
                    {initials(me.name)}
                  </AvatarFallback>
                </Avatar>

                <div className="space-y-1">
                  <h2 className="text-xl font-bold">{me.name}</h2>
                  <div className="flex items-center justify-center gap-1.5 text-sm text-muted-foreground">
                    <Mail className="h-3.5 w-3.5" />
                    {me.email}
                  </div>
                </div>

                <Badge variant="secondary" className="mt-2 bg-primary/10 text-primary hover:bg-primary/20">
                  <Shield className="mr-1 h-3 w-3" />
                  {ROLE_LABELS[me.role]}
                </Badge>
              </CardContent>
            </Card>
          </div>

          <div className="md:col-span-2 space-y-6">
            <Card className="border-0 shadow-md">
              <CardHeader className="pb-3 text-sm font-semibold uppercase tracking-wider text-muted-foreground">
                Vínculo Atual
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-start gap-3">
                  <Building className="h-5 w-5 text-muted-foreground shrink-0 mt-0.5" />
                  <div>
                    <p className="font-medium text-sm">Empresa Ativa</p>
                    <p className="text-sm text-muted-foreground">
                      {currentCompany ? currentCompany.name : "Nenhuma empresa selecionada"}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card className="border-0 shadow-md">
              <CardHeader>
                <CardTitle>Sessão</CardTitle>
                <CardDescription>
                  Encerra o acesso neste navegador. O caixa pareado continua pareado.
                </CardDescription>
              </CardHeader>
              <CardContent>
                <LogoutButton />
              </CardContent>
            </Card>
          </div>
        </div>
      )}
    </div>
  );
}
