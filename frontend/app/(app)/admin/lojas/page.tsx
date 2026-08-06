"use client";

import { useEffect, useState } from "react";
import { Store, Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { driver } from "driver.js";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { api } from "@/shared/services/api";
import { useCompany } from "@/shared/contexts/company-context";

type CompanyResponse = {
  id: string;
  name: string;
  streetName: string;
  streetNumber: string;
  cityName: string;
  stateName: string;
  latitude: number;
  longitude: number;
};

export default function LojasPage() {
  const { loadCompanies, activeCompany } = useCompany();
  const [companies, setCompanies] = useState<CompanyResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);

  const [form, setForm] = useState({
    name: "",
    streetName: "",
    streetNumber: "",
    cityName: "",
    stateName: "",
    latitude: 0,
    longitude: 0,
  });

  const fetchCompanies = async () => {
    setIsLoading(true);
    try {
      const data = await api.get<CompanyResponse[]>("/api/companies");
      setCompanies(data);
      loadCompanies(
        data.map((company) => ({ id: company.id, name: company.name })),
      );
      return data;
    } catch {
      toast.error("Erro ao buscar as lojas.");
      return [];
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCompanies().then((data) => {
      // If no stores, trigger onboarding
      if (false && data && data.length === 0) {
        setTimeout(() => {
          const driverObj = driver({
            showProgress: true,
            animate: true,
            allowClose: false,
            doneBtnText: "Vamos lá!",
            nextBtnText: "Próximo →",
            prevBtnText: "← Voltar",
            steps: [
              {
                element: "#tour-welcome",
                popover: {
                  title: "Bem-vindo ao Setup inicial do Kalles",
                  description:
                    "Aqui é onde você começa a configurar o seu negócio no sistema.",
                  side: "bottom",
                  align: "start",
                },
              },
              {
                element: "#tour-create-form",
                popover: {
                  title: "Crie sua primeira Loja",
                  description:
                    "O Kalles ERP é multi-lojas nativamente. Você precisa criar a primeira (Matriz) para começar a operar, lançar produtos e usar o PDV.",
                  side: "right",
                  align: "center",
                },
              },
            ],
          });
          driverObj.drive();
        }, 800);
      }
    });
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsCreating(true);
    try {
      await api.post("/api/companies", form);
      toast.success("Loja criada com sucesso!");
      setForm({
        name: "",
        streetName: "",
        streetNumber: "",
        cityName: "",
        stateName: "",
        latitude: 0,
        longitude: 0,
      });
      fetchCompanies();
    } catch {
      toast.error("Erro ao criar a loja.");
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <div
      className="container mx-auto max-w-5xl py-8 space-y-8"
      data-onboarding="stores-page"
    >
      <div
        className="flex items-center justify-between"
        data-onboarding="stores-header"
      >
        <div>
          <h1
            id="tour-welcome"
            className="text-3xl font-bold flex items-center gap-2"
          >
            <Store className="h-8 w-8 text-primary" />
            Lojas
          </h1>
          <p className="text-muted-foreground mt-1">
            Gerencie as filiais físicas do seu sistema.
          </p>
        </div>
      </div>

      <div className="rounded-md border border-primary/20 bg-primary/5 px-4 py-2 text-sm">
        <span className="font-semibold text-primary">Filial ativa:</span>{" "}
        <span>{activeCompany?.name ?? "Nenhuma filial selecionada"}</span>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card id="tour-create-form" data-onboarding="stores-form">
          <CardHeader>
            <CardTitle>Nova Loja</CardTitle>
            <CardDescription>
              Cadastre uma nova filial para o seu ambiente Kalles.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleCreate} className="space-y-4">
              <div className="space-y-2">
                <Label>Nome da Filial</Label>
                <Input
                  required
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  placeholder="Ex: Loja Matriz São Paulo"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Rua/Av</Label>
                  <Input
                    required
                    value={form.streetName}
                    onChange={(e) =>
                      setForm({ ...form, streetName: e.target.value })
                    }
                  />
                </div>
                <div className="space-y-2">
                  <Label>Número</Label>
                  <Input
                    required
                    value={form.streetNumber}
                    onChange={(e) =>
                      setForm({ ...form, streetNumber: e.target.value })
                    }
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Cidade</Label>
                  <Input
                    required
                    value={form.cityName}
                    onChange={(e) =>
                      setForm({ ...form, cityName: e.target.value })
                    }
                  />
                </div>
                <div className="space-y-2">
                  <Label>Estado</Label>
                  <Input
                    required
                    value={form.stateName}
                    onChange={(e) =>
                      setForm({ ...form, stateName: e.target.value })
                    }
                    placeholder="SP"
                  />
                </div>
              </div>

              <Button type="submit" disabled={isCreating} className="w-full">
                {isCreating ? (
                  <Loader2 className="h-4 w-4 animate-spin mr-2" />
                ) : (
                  <Plus className="h-4 w-4 mr-2" />
                )}
                Cadastrar Loja
              </Button>
            </form>
          </CardContent>
        </Card>

        <div className="space-y-4" data-onboarding="stores-content">
          <h2 className="text-xl font-semibold">Lojas Cadastradas</h2>
          {isLoading ? (
            <div className="flex justify-center p-8">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : companies.length === 0 ? (
            <div className="rounded-md border border-dashed p-8 text-center text-muted-foreground">
              Nenhuma loja cadastrada.
            </div>
          ) : (
            companies.map((co) => (
              <Card key={co.id}>
                <CardContent className="p-4 flex gap-4 items-center">
                  <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                    <Store className="h-6 w-6 text-primary" />
                  </div>
                  <div>
                    <h3 className="font-semibold">{co.name}</h3>
                    <p className="text-sm text-muted-foreground">
                      {co.streetName}, {co.streetNumber} - {co.cityName}/
                      {co.stateName}
                    </p>
                  </div>
                </CardContent>
              </Card>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
