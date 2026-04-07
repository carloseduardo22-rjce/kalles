"use client";

import { useCompany } from "@/shared/contexts/company-context";
import { UserCircle, Mail, MapPin, Building, Key, Bell, Shield, ChevronRight } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";

export default function PerfilPage() {
  const { activeCompanyId, companies } = useCompany();
  const currentCompany = companies.find((c) => c.id === activeCompanyId);

  return (
    <div className="container mx-auto max-w-4xl py-10 space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-bold tracking-tight">Meu Perfil</h1>
        <p className="text-muted-foreground">
          Gerencie suas informações pessoais, preferências e configurações de segurança.
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        {/* Coluna Principal do Perfil */}
        <div className="md:col-span-1 space-y-6">
          <Card className="border-0 shadow-md">
            <CardContent className="pt-6 flex flex-col items-center text-center space-y-4">
              <Avatar className="h-32 w-32 ring-4 ring-primary/10">
                <AvatarImage src="" alt="Avatar do usuário" />
                <AvatarFallback className="text-4xl bg-primary/5 text-primary">OP</AvatarFallback>
              </Avatar>
              
              <div className="space-y-1">
                <h2 className="text-xl font-bold">Operador Padrão</h2>
                <div className="flex items-center justify-center gap-1.5 text-sm text-muted-foreground">
                  <Mail className="h-3.5 w-3.5" />
                  operador@kalles.com
                </div>
              </div>

              <Badge variant="secondary" className="mt-2 bg-primary/10 text-primary hover:bg-primary/20">
                <Shield className="mr-1 h-3 w-3" />
                Acesso Administrativo
              </Badge>
              
              <div className="w-full pt-4">
                <Button className="w-full" variant="outline">Editar Foto</Button>
              </div>
            </CardContent>
          </Card>

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
        </div>

        {/* Coluna de Detalhes e Configurações */}
        <div className="md:col-span-2 space-y-6">
          <Card className="border-0 shadow-md">
            <CardHeader>
              <CardTitle>Informações Pessoais</CardTitle>
              <CardDescription>
                Seus dados básicos para identificação no sistema.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1 p-3 rounded-lg bg-muted/50">
                  <p className="text-xs font-medium text-muted-foreground uppercase">Nome Completo</p>
                  <p className="font-medium">Operador Padrão</p>
                </div>
                <div className="space-y-1 p-3 rounded-lg bg-muted/50">
                  <p className="text-xs font-medium text-muted-foreground uppercase">CPF</p>
                  <p className="font-medium">000.000.000-00</p>
                </div>
                <div className="space-y-1 p-3 rounded-lg bg-muted/50">
                  <p className="text-xs font-medium text-muted-foreground uppercase">Telefone</p>
                  <p className="font-medium">(00) 00000-0000</p>
                </div>
                <div className="space-y-1 p-3 rounded-lg bg-muted/50">
                  <p className="text-xs font-medium text-muted-foreground uppercase">Data de Cadastro</p>
                  <p className="font-medium">01/01/2026</p>
                </div>
              </div>
              <div className="flex justify-end pt-2">
                <Button variant="default">Atualizar Dados</Button>
              </div>
            </CardContent>
          </Card>

          <Card className="border-0 shadow-md">
            <CardHeader>
              <CardTitle>Configurações de Conta</CardTitle>
              <CardDescription>
                Gerencie sua segurança e preferências do sistema.
              </CardDescription>
            </CardHeader>
            <CardContent className="p-0">
              <div className="divide-y">
                <button className="w-full flex items-center justify-between p-4 hover:bg-muted/50 transition-colors">
                  <div className="flex items-center gap-3">
                    <div className="bg-primary/10 p-2 rounded-full">
                      <Key className="h-4 w-4 text-primary" />
                    </div>
                    <div className="text-left">
                      <p className="font-medium text-sm">Alterar Senha</p>
                      <p className="text-xs text-muted-foreground">Atualize sua senha de acesso</p>
                    </div>
                  </div>
                  <ChevronRight className="h-4 w-4 text-muted-foreground" />
                </button>

                <button className="w-full flex items-center justify-between p-4 hover:bg-muted/50 transition-colors">
                  <div className="flex items-center gap-3">
                    <div className="bg-primary/10 p-2 rounded-full">
                      <Bell className="h-4 w-4 text-primary" />
                    </div>
                    <div className="text-left">
                      <p className="font-medium text-sm">Preferências de Notificação</p>
                      <p className="text-xs text-muted-foreground">Altere como você recebe alertas</p>
                    </div>
                  </div>
                  <ChevronRight className="h-4 w-4 text-muted-foreground" />
                </button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
