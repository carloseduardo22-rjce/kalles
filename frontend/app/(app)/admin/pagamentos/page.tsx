"use client";

import { useState, useEffect } from "react";
import Image from "next/image";
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
import { Store, Terminal, Loader2, Lock } from "lucide-react";
import { toast } from "sonner";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

interface StoreFormData {
  companyId: string;
  name: string;
  streetName: string;
  streetNumber: string;
  cityName: string;
  stateName: string;
  latitude: number;
  longitude: number;
}

interface PosFormData {
  caixaId: string;
  name: string;
}

const BRAZILIAN_STATES = [
  "Acre",
  "Alagoas",
  "Amapá",
  "Amazonas",
  "Bahia",
  "Ceará",
  "Distrito Federal",
  "Espírito Santo",
  "Goiás",
  "Maranhão",
  "Mato Grosso",
  "Mato Grosso do Sul",
  "Minas Gerais",
  "Paraná",
  "Paraíba",
  "Pará",
  "Pernambuco",
  "Piauí",
  "Rio Grande do Norte",
  "Rio Grande do Sul",
  "Rio de Janeiro",
  "Rondônia",
  "Roraima",
  "Santa Catarina",
  "Sergipe",
  "São Paulo",
  "Tocantins",
];

export default function PaymentSettingsPage() {
  const [activeTab, setActiveTab] = useState<"store" | "pos">("store");
  const [loading, setLoading] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState(true);
  const [storeConfigured, setStoreConfigured] = useState(false);

  // MOCK UUIDs for demo. In a real app we'd fetch the company's UUID from the auth/context
  const mockCompanyId = "3fa85f64-5717-4562-b3fc-2c963f66afa6";

  useEffect(() => {
    async function checkStoreStatus() {
      try {
        const res = await fetch(
          `http://localhost:8080/api/mercadopago/stores/${mockCompanyId}/status`,
        );
        if (res.ok) {
          const data = await res.json();
          setStoreConfigured(data.hasStoreRegistered);
        }
      } catch (error) {
        console.error("Falha ao verificar status da loja:");
      } finally {
        setLoadingStatus(false);
      }
    }
    checkStoreStatus();
  }, [mockCompanyId]);

  const [storeForm, setStoreForm] = useState<StoreFormData>({
    companyId: mockCompanyId,
    name: "Kalles Matriz",
    streetName: "Rua Exemplo",
    streetNumber: "123",
    cityName: "São Paulo",
    stateName: "São Paulo",
    latitude: -23.55052,
    longitude: -46.633308,
  });

  const [posForm, setPosForm] = useState<PosFormData>({
    caixaId: "CAIXA-001",
    name: "Caixa Frontal",
  });

  const handleUpdateStoreField = (
    key: keyof StoreFormData,
    value: string | number,
  ) => {
    setStoreForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleUpdatePosField = (key: keyof PosFormData, value: string) => {
    setPosForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleCreateStore = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const res = await fetch("http://localhost:8080/api/mercadopago/stores", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(storeForm),
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => null);
        throw new Error(
          errorData?.message || "Erro desconhecido ao criar loja",
        );
      }

      toast.success("Loja registrada no Mercado Pago com sucesso!");
      setStoreConfigured(true);
      setActiveTab("pos");
    } catch (error: any) {
      toast.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCreatePos = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const payload = {
        companyId: storeForm.companyId,
        caixaId: posForm.caixaId,
        name: posForm.name,
      };

      const res = await fetch("http://localhost:8080/api/mercadopago/pos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => null);
        throw new Error(
          errorData?.message || "Erro desconhecido ao criar Caixa/POS",
        );
      }

      toast.success("Caixa/POS vinculado com sucesso!");
      setPosForm((prev) => ({ ...prev, caixaId: crypto.randomUUID() })); // Reset to new UUID
    } catch (error: any) {
      toast.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex-1 min-h-screen bg-[#009EE3] p-4 md:p-8 pt-6">
      <div className="flex items-center justify-between space-y-2 mb-6">
        <div className="flex items-center gap-4">
          <div className="bg-white p-3 rounded-xl shadow-sm">
            <Image
              src="/mercado-pago-logo.png"
              alt="Mercado Pago"
              width={160}
              height={40}
              className="object-contain"
              priority
            />
          </div>
          <h2 className="text-3xl font-bold tracking-tight text-white drop-shadow-md">
            Integração de Pagamentos
          </h2>
        </div>
      </div>

      <p className="text-white/90 w-full max-w-3xl mb-8 font-medium">
        Configure sua integração com o Mercado Pago para aceitar pagamentos via
        QR Code e Pix. Primeiro crie seu estabelecimento e depois associe os
        caixas reais físicos a ele.
      </p>

      {/* Tabs / Stepper Header */}
      <div className="flex gap-4 border-b border-white/30 pb-4 mt-6">
        <button
          onClick={() => setActiveTab("store")}
          className={`flex items-center gap-2 pb-2 px-1 border-b-4 font-medium transition-all ${
            activeTab === "store"
              ? "border-white text-white drop-shadow-md"
              : "border-transparent text-white/70 hover:text-white"
          }`}
        >
          <Store className="h-4 w-4" />
          <span>1. Estabelecimento</span>
        </button>
        <button
          onClick={() => {
            if (storeConfigured) setActiveTab("pos");
          }}
          disabled={!storeConfigured}
          className={`flex items-center gap-2 pb-2 px-1 border-b-4 font-medium transition-all ${
            activeTab === "pos"
              ? "border-white text-white drop-shadow-md"
              : "border-transparent text-white/50"
          } ${!storeConfigured ? "cursor-not-allowed opacity-60" : "hover:text-white"}`}
        >
          <Terminal className="h-4 w-4" />
          <span>2. Terminais (Caixas)</span>
          {!storeConfigured && <Lock className="h-3 w-3 ml-1" />}
        </button>
      </div>

      {loadingStatus ? (
        <div className="flex items-center gap-3 mt-12 text-white">
          <Loader2 className="h-6 w-6 animate-spin" />
          <span className="font-medium text-lg">
            Verificando status da integração...
          </span>
        </div>
      ) : (
        <div className="max-w-2xl mt-6 pb-20">
          {activeTab === "store" && (
            <Card>
              <CardHeader>
                <CardTitle>Dados da Loja Física</CardTitle>
                <CardDescription>
                  Seus dados serão enviados ao Mercado Pago para registro da
                  filial pagadora.
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleCreateStore} className="space-y-6">
                  <div className="grid gap-4 md:grid-cols-2">
                    <div className="space-y-2">
                      <Label htmlFor="companyId">
                        ID Interno da Empresa (Kalles)
                      </Label>
                      <Input
                        id="companyId"
                        value={storeForm.companyId}
                        onChange={(e) =>
                          handleUpdateStoreField("companyId", e.target.value)
                        }
                        readOnly
                        className="bg-muted/50"
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="name">
                        Nome da Loja (Visível no Recibo)
                      </Label>
                      <Input
                        id="name"
                        placeholder="Ex: Kalles Matriz Centro"
                        value={storeForm.name}
                        onChange={(e) =>
                          handleUpdateStoreField("name", e.target.value)
                        }
                        required
                      />
                    </div>
                  </div>

                  <div className="space-y-4 pt-4 border-t">
                    <h4 className="text-sm font-medium">Localização</h4>
                    <div className="grid gap-4 md:grid-cols-4">
                      <div className="space-y-2 md:col-span-3">
                        <Label htmlFor="streetName">Logradouro</Label>
                        <Input
                          id="streetName"
                          value={storeForm.streetName}
                          onChange={(e) =>
                            handleUpdateStoreField("streetName", e.target.value)
                          }
                          required
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="streetNumber">Número</Label>
                        <Input
                          id="streetNumber"
                          value={storeForm.streetNumber}
                          onChange={(e) =>
                            handleUpdateStoreField(
                              "streetNumber",
                              e.target.value,
                            )
                          }
                          required
                        />
                      </div>
                    </div>

                    <div className="grid gap-4 md:grid-cols-2">
                      <div className="space-y-2">
                        <Label htmlFor="cityName">Cidade</Label>
                        <Input
                          id="cityName"
                          value={storeForm.cityName}
                          onChange={(e) =>
                            handleUpdateStoreField("cityName", e.target.value)
                          }
                          required
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="stateName">Estado (Extenso)</Label>
                        <Select
                          value={storeForm.stateName}
                          onValueChange={(val) =>
                            handleUpdateStoreField("stateName", val)
                          }
                        >
                          <SelectTrigger>
                            <SelectValue placeholder="Selecione o estado..." />
                          </SelectTrigger>
                          <SelectContent>
                            {BRAZILIAN_STATES.map((st) => (
                              <SelectItem key={st} value={st}>
                                {st}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                    </div>

                    <div className="grid gap-4 md:grid-cols-2">
                      <div className="space-y-2">
                        <Label htmlFor="latitude">Latitude</Label>
                        <Input
                          id="latitude"
                          type="number"
                          step="any"
                          value={storeForm.latitude}
                          onChange={(e) =>
                            handleUpdateStoreField(
                              "latitude",
                              parseFloat(e.target.value),
                            )
                          }
                          required
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="longitude">Longitude</Label>
                        <Input
                          id="longitude"
                          type="number"
                          step="any"
                          value={storeForm.longitude}
                          onChange={(e) =>
                            handleUpdateStoreField(
                              "longitude",
                              parseFloat(e.target.value),
                            )
                          }
                          required
                        />
                      </div>
                    </div>
                  </div>

                  <div className="flex justify-end pt-4">
                    <Button type="submit" disabled={loading}>
                      {loading ? (
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      ) : (
                        <Store className="mr-2 h-4 w-4" />
                      )}
                      Criar/Vincular Loja
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          )}

          {activeTab === "pos" && (
            <Card>
              <CardHeader>
                <CardTitle>Terminais de Caixa (POS)</CardTitle>
                <CardDescription>
                  Vincule caixas físicos já cadastrados na sua loja ao Mercado
                  Pago para gerar QR Codes dinâmicos.
                </CardDescription>
              </CardHeader>
              <CardContent>
                {!storeConfigured && (
                  <Alert className="mb-6 border-amber-500/50 bg-amber-500/10 text-amber-600">
                    <AlertTitle className="font-semibold">Atenção</AlertTitle>
                    <AlertDescription>
                      Certifique-se de que configurou o a Loja Física (Etapa 1)
                      antes de criar caixas/POS.
                    </AlertDescription>
                  </Alert>
                )}

                <form onSubmit={handleCreatePos} className="space-y-6">
                  <div className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="posCompanyId">
                        ID da Sua Loja Kalles
                      </Label>
                      <Input
                        id="posCompanyId"
                        value={storeForm.companyId}
                        readOnly
                        className="bg-muted/50"
                      />
                    </div>

                    <div className="grid gap-4 md:grid-cols-2">
                      <div className="space-y-2">
                        <Label htmlFor="caixaId">ID do Caixa (Kalles)</Label>
                        <Input
                          id="caixaId"
                          placeholder="Ex: CAIXA-001"
                          value={posForm.caixaId}
                          onChange={(e) =>
                            handleUpdatePosField("caixaId", e.target.value)
                          }
                          required
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="posName">
                          Nome do Caixa (Visível para Cliente)
                        </Label>
                        <Input
                          id="posName"
                          placeholder="Ex: Caixa Frontal 01"
                          value={posForm.name}
                          onChange={(e) =>
                            handleUpdatePosField("name", e.target.value)
                          }
                          required
                        />
                      </div>
                    </div>
                  </div>

                  <div className="flex justify-end pt-4">
                    <Button type="submit" disabled={loading}>
                      {loading ? (
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      ) : (
                        <Terminal className="mr-2 h-4 w-4" />
                      )}
                      Registrar Terminal
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          )}
        </div>
      )}
    </div>
  );
}
