"use client";

import { useEffect, useState } from "react";
import {
  MonitorSmartphone,
  Plus,
  Loader2,
  CreditCard,
  Banknote,
} from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { api } from "@/shared/services/api";

type CompanyResponse = {
  id: string;
  name: string;
};

type CashRegisterStatusResponse = {
  cashRegisterId: string;
  code: string;
  description: string;
  active: boolean;
  hasActiveSession: boolean;
  activeSessionId: string | null;
  activeOperatorName: string | null;
  initialAmount: number | null;
  openedAt: string | null;
  paymentIntegrationConfigured: boolean;
  activeSessionCashOnlyOperation: boolean | null;
};

export default function CaixasPage() {
  const [caixas, setCaixas] = useState<CashRegisterStatusResponse[]>([]);
  const [companies, setCompanies] = useState<CompanyResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);

  const [form, setForm] = useState({
    code: "",
    description: "",
    companyId: "",
  });

  const fetchData = async () => {
    setIsLoading(true);
    try {
      const [caixasData, companiesData] = await Promise.all([
        api.get<CashRegisterStatusResponse[]>("/api/cash-registers"),
        api.get<CompanyResponse[]>("/api/companies"),
      ]);
      setCaixas(caixasData);
      setCompanies(companiesData);
    } catch {
      toast.error("Erro ao buscar dados de caixas e lojas.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsCreating(true);
    try {
      await api.post("/api/cash-registers", form);
      toast.success("Caixa criado com sucesso!");
      setForm({ code: "", description: "", companyId: "" });
      fetchData();
    } catch {
      toast.error("Erro ao criar caixa. O código pode já estar em uso.");
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <div className="container mx-auto max-w-5xl py-8 space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-2">
            <MonitorSmartphone className="h-8 w-8 text-primary" />
            Caixas Registradores (PDV)
          </h1>
          <p className="text-muted-foreground mt-1">
            Gerencie os terminais e códigos dos seus faturadores de loja.
          </p>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Novo Caixa</CardTitle>
            <CardDescription>
              Abertura de novos pontos de caixa no seu ERP local.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleCreate} className="space-y-4">
              <div className="space-y-2">
                <Label>Código Único do Caixa</Label>
                <Input
                  required
                  value={form.code}
                  onChange={(e) => setForm({ ...form, code: e.target.value })}
                  placeholder="Ex: CX01"
                />
              </div>

              <div className="space-y-2">
                <Label>Descrição</Label>
                <Input
                  required
                  value={form.description}
                  onChange={(e) =>
                    setForm({ ...form, description: e.target.value })
                  }
                  placeholder="Ex: Caixa 1 da Matriz"
                />
              </div>

              <div className="space-y-2">
                <Label>Loja (Filial Físíca)</Label>
                <Select
                  value={form.companyId}
                  onValueChange={(val) => setForm({ ...form, companyId: val })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Vincular a uma Loja" />
                  </SelectTrigger>
                  <SelectContent>
                    {companies.map((c) => (
                      <SelectItem key={c.id} value={c.id}>
                        {c.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <Button type="submit" disabled={isCreating} className="w-full">
                {isCreating ? (
                  <Loader2 className="h-4 w-4 animate-spin mr-2" />
                ) : (
                  <Plus className="h-4 w-4 mr-2" />
                )}
                Criar Caixa
              </Button>
            </form>
          </CardContent>
        </Card>

        <div className="space-y-4">
          <h2 className="text-xl font-semibold">Caixas Cadastrados</h2>
          {isLoading ? (
            <div className="flex justify-center p-8">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : caixas.length === 0 ? (
            <div className="rounded-md border border-dashed p-8 text-center text-muted-foreground">
              Nenhum caixa/PDV cadastrado no core.
            </div>
          ) : (
            <div className="space-y-3">
              {caixas.map((cx) => (
                <Card key={cx.cashRegisterId} className="bg-card">
                  <CardContent className="p-4 flex justify-between items-center">
                    <div>
                      <h3 className="font-bold flex items-center gap-2">
                        {cx.code}
                        {cx.active ? (
                          <span className="text-[10px] bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300 px-2 py-0.5 rounded shadow-sm">
                            Ativo
                          </span>
                        ) : (
                          <span className="text-[10px] bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300 px-2 py-0.5 rounded shadow-sm">
                            Inatvo
                          </span>
                        )}
                      </h3>
                      <p className="text-sm text-muted-foreground mt-0.5">
                        {cx.description}
                      </p>
                      <div className="mt-2 flex flex-wrap gap-2">
                        <span
                          className={`inline-flex items-center rounded px-2 py-0.5 text-[10px] font-medium shadow-sm ${
                            cx.paymentIntegrationConfigured
                              ? "bg-emerald-100 text-emerald-800 dark:bg-emerald-900 dark:text-emerald-300"
                              : "bg-amber-100 text-amber-800 dark:bg-amber-900 dark:text-amber-300"
                          }`}
                        >
                          {cx.paymentIntegrationConfigured ? (
                            <>
                              <CreditCard className="mr-1 h-3 w-3" />
                              Pagamento configurado
                            </>
                          ) : (
                            <>
                              <Banknote className="mr-1 h-3 w-3" />
                              Sem integração de pagamento
                            </>
                          )}
                        </span>
                        {cx.hasActiveSession &&
                          cx.activeSessionCashOnlyOperation && (
                            <span className="inline-flex items-center rounded bg-amber-100 px-2 py-0.5 text-[10px] font-medium text-amber-800 shadow-sm dark:bg-amber-900 dark:text-amber-300">
                              <Banknote className="mr-1 h-3 w-3" />
                              Sessão somente dinheiro
                            </span>
                          )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
