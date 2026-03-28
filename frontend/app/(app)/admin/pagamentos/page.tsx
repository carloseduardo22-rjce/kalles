"use client";

import { useState, useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
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
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Store, Terminal, Loader2, Lock, List, PlusCircle, Link as LinkIcon } from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { mercadopagoService } from "@/features/admin/services/mercadopago.service";

interface StoreFormData {
  companyId: string;
  name: string;
  streetName: string;
  streetNumber: string;
  cityName: string;
  stateName: string;
  latitude: number | "";
  longitude: number | "";
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
  const [activeTab, setActiveTab] = useState<"listar" | "criar">("listar");
  const [newIntegrationStep, setNewIntegrationStep] = useState<"oauth" | "store" | "pos">(
    "oauth",
  );

  // Em produção, isso viraria de variáveis de ambiente (.env)
  const MP_APP_ID = process.env.NEXT_PUBLIC_MP_APP_ID || "448684586415948";
  const REDIRECT_URI =
    process.env.NEXT_PUBLIC_MP_REDIRECT_URI ||
    "https://08e8-2804-1494-dbb-aa00-f8ed-896b-5037-a22f.ngrok-free.app/admin/pagamentos/mp-callback";

  // O state é usado para passarmos o ID do Tenant/Dono do sistema e validar o callback
  const [tenantId, setTenantId] = useState<string>("");

  const mpAuthUrl = `https://auth.mercadopago.com/authorization?client_id=${MP_APP_ID}&response_type=code&platform_id=mp&state=${tenantId}&redirect_uri=${REDIRECT_URI}`;

  const [loading, setLoading] = useState(false);
  const [storeConfigured, setStoreConfigured] = useState(false);
  const [cashRegisters, setCashRegisters] = useState<
    { id: string; code: string; description: string }[]
  >([]);

  useEffect(() => {
    fetch("/api/cash-registers")
      .then(async (res) => {
        const text = await res.text();
        return text ? JSON.parse(text) : [];
      })
      .then((data) => {
        if (!Array.isArray(data)) return;
        const registers = data.map((cr: any) => ({
          id: cr.cashRegisterId,
          code: cr.code,
          description: cr.description,
        }));
        setCashRegisters(registers);
      })
      .catch((err) => console.error("Error fetching cash registers", err));

    // Busca o tenantId do usuário logado
    fetch("/api/auth/me")
      .then((res) => (res.ok ? res.json() : null))
      .then((data) => {
        if (data?.tenantId) setTenantId(data.tenantId);
      })
      .catch(console.error);
  }, []);

  // Verifica se a conta do MP já está conectada ao entrar na aba "criar".
  // Se já estiver, pula direto para o passo "Estabelecimento".
  useEffect(() => {
    if (activeTab !== "criar") return;

    fetch("/api/mercadopago/stores/my-status")
      .then((res) => (res.ok ? res.json() : null))
      .then((data) => {
        if (data?.companyExists) {
          setNewIntegrationStep("store");
        } else {
          setNewIntegrationStep("oauth");
        }
      })
      .catch(() => setNewIntegrationStep("oauth"));
  }, [activeTab]);

  const [storeForm, setStoreForm] = useState<StoreFormData>({
    companyId: "",
    name: "",
    streetName: "",
    streetNumber: "",
    cityName: "",
    stateName: "",
    latitude: "",
    longitude: "",
  });

  const [posForm, setPosForm] = useState<PosFormData>({
    caixaId: "",
    name: "",
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
      const payload = {
        externalId: storeForm.companyId,
        name: storeForm.name,
        streetName: storeForm.streetName,
        streetNumber: storeForm.streetNumber,
        cityName: storeForm.cityName,
        stateName: storeForm.stateName,
        latitude: storeForm.latitude,
        longitude: storeForm.longitude,
        tenantId: tenantId,
      };

      const res = await fetch("/api/mercadopago/stores", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => null);
        const errorMessage = errorData?.message || "";

        if (
          errorMessage.toLowerCase().includes("already exists") ||
          errorMessage.toLowerCase().includes("external_id")
        ) {
          throw new Error(
            `Já existe uma loja criada com aquele external id que no nosso front referenciamos como "ID Interno da Empresa".`,
          );
        }

        throw new Error(errorMessage || "Erro desconhecido ao criar loja");
      }

      toast.success("Loja registrada no Mercado Pago com sucesso!");
      setStoreConfigured(true);
      setNewIntegrationStep("pos");
    } catch (error: any) {
      toast.error(error.message, {
        duration: 5000,
      });
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
        caixaId: posForm.caixaId.replace("-", ""),
        name: posForm.name,
      };

      console.log(payload);

      const res = await fetch("/api/mercadopago/pos", {
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
      setPosForm({ caixaId: "", name: "" }); // Reset
    } catch (error: any) {
      toast.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  const { data: stores = [], isLoading: isLoadingStores } = useQuery({
    queryKey: ["mp-stores"],
    queryFn: mercadopagoService.listStores,
  });

  const integrationStores = stores.map((store) => ({
    id: store.id.toString(),
    name: store.name,
    externalId: store.external_id,
    mpStoreId: store.id.toString(),
    terminals: (store.terminals || []).map((pos) => ({
      id: pos.external_id || pos.id.toString(),
      name: pos.name,
    })),
  }));

  const isLoadingData = isLoadingStores;

  return (
    <div className="flex-1 min-h-screen bg-[#009EE3] p-4 md:p-8 pt-6">
      <div className="flex items-center gap-4 mb-2">
        <h2 className="text-3xl font-bold tracking-tight text-white drop-shadow-md">
          Integração de Pagamentos
        </h2>
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
      </div>

      <p className="text-white/90 w-full mb-8 font-medium">
        Gerencie as suas integrações com o Mercado Pago para aceitar pagamentos
        via Pix (QrCode Dinâmico). Você pode visualizar as lojas configuradas ou
        criar novas integrações.
      </p>

      <Tabs
        value={activeTab}
        onValueChange={(val) => setActiveTab(val as "listar" | "criar")}
        className="w-full mt-6"
      >
        <TabsList className="bg-white/20 text-white border border-white/30 backdrop-blur-sm mb-6 h-12 p-1">
          <TabsTrigger
            value="listar"
            className="data-[state=active]:bg-white data-[state=active]:text-[#009EE3] font-medium h-full px-6 flex gap-2 items-center rounded-sm transition-all"
          >
            <List className="h-4 w-4" />
            Lojas e Terminais Configurados
          </TabsTrigger>
          <TabsTrigger
            value="criar"
            className="data-[state=active]:bg-white data-[state=active]:text-[#009EE3] font-medium h-full px-6 flex gap-2 items-center rounded-sm transition-all"
          >
            <PlusCircle className="h-4 w-4" />
            Nova Integração (Loja/POS)
          </TabsTrigger>
        </TabsList>

        <TabsContent value="listar" className="pb-20">
          <div className="grid gap-6">
            {isLoadingData ? (
              <div className="flex flex-col items-center justify-center p-12 text-center text-white">
                <LoadingSpinner />
                <p className="mt-4">Buscando lojas e caixas no Mercado Pago...</p>
              </div>
            ) : integrationStores.length === 0 ? (
              <Card className="border-0 shadow-md">
                <CardContent className="flex flex-col items-center justify-center p-12 text-center text-zinc-500">
                  <Store className="h-16 w-16 mb-4 text-zinc-300" />
                  <p className="text-lg font-medium">Nenhuma loja vinculada.</p>
                  <p>Mude para a aba de ConfiguraçÃ£o para criar a sua loja.</p>
                </CardContent>
              </Card>
            ) : (
              integrationStores.map((store) => (
                <Card
                  key={store.id}
                  className="border-0 p-0 gap-0 shadow-md overflow-hidden"
                >
                  <CardHeader className="bg-zinc-100/50 border-b [.border-b]:pb-0 px-6 py-2">
                    <div className="flex justify-between items-start">
                      <div className="flex flex-col gap-1">
                        <CardTitle className="text-xl flex items-center gap-2">
                          <Store className="h-5 w-5 text-zinc-600" />
                          {store.name}
                        </CardTitle>
                        <CardDescription className="flex items-center gap-4">
                          <span>
                            <strong>ID Interno:</strong> {store.externalId}
                          </span>
                          <span>
                            <strong>ID Mercado Pago:</strong> {store.mpStoreId}
                          </span>
                        </CardDescription>
                      </div>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          setActiveTab("criar");
                          setStoreConfigured(true);
                          setNewIntegrationStep("pos");
                          setStoreForm((prev) => ({
                            ...prev,
                            companyId: store.externalId,
                          }));
                        }}
                      >
                        <PlusCircle className="h-4 w-4 mr-2" />
                        Adicionar Terminal
                      </Button>
                    </div>
                  </CardHeader>
                  <CardContent className="p-6 pt-4">
                    <h4 className="text-sm font-semibold text-zinc-500 mb-3 flex items-center gap-2">
                      <Terminal className="h-4 w-4" />
                      Terminais (Caixas) Vinculados
                    </h4>
                    {store.terminals.length > 0 ? (
                      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {store.terminals.map((terminal) => (
                          <div
                            key={terminal.id}
                            className="flex flex-col p-4 border rounded-lg bg-zinc-50/50"
                          >
                            <span className="font-medium">{terminal.name}</span>
                            <span className="text-xs text-muted-foreground mt-1">
                              ID: {terminal.id}
                            </span>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <p className="text-sm text-muted-foreground">
                        Nenhum terminal vinculado a esta loja.
                      </p>
                    )}
                  </CardContent>
                </Card>
              )))}
          </div>
        </TabsContent>

        <TabsContent value="criar">
          {/* Tabs / Stepper Header for Creation */}
          <div className="flex gap-4 border-b border-white/30 pb-4 mb-6">
            <button
              onClick={() => setNewIntegrationStep("oauth")}
              className={`flex items-center gap-2 pb-2 px-1 border-b-4 font-medium transition-all ${newIntegrationStep === "oauth"
                ? "border-white text-white drop-shadow-md"
                : "border-transparent text-white/70 hover:text-white"
                }`}
            >
              <LinkIcon className="h-4 w-4" />
              <span>1. Conectar Conta</span>
            </button>
            <button
              onClick={() => setNewIntegrationStep("store")}
              className={`flex items-center gap-2 pb-2 px-1 border-b-4 font-medium transition-all ${newIntegrationStep === "store"
                ? "border-white text-white drop-shadow-md"
                : "border-transparent text-white/70 hover:text-white"
                }`}
            >
              <Store className="h-4 w-4" />
              <span>2. Estabelecimento</span>
            </button>
            <button
              onClick={() => {
                if (storeConfigured) setNewIntegrationStep("pos");
              }}
              disabled={!storeConfigured}
              className={`flex items-center gap-2 pb-2 px-1 border-b-4 font-medium transition-all ${newIntegrationStep === "pos"
                ? "border-white text-white drop-shadow-md"
                : "border-transparent text-white/50"
                } ${!storeConfigured ? "cursor-not-allowed opacity-60" : "hover:text-white"}`}
            >
              <Terminal className="h-4 w-4" />
              <span>3. Terminais (Caixas)</span>
              {!storeConfigured && <Lock className="h-3 w-3 ml-1" />}
            </button>
          </div>

          <div className="w-full pb-20">
            {newIntegrationStep === "oauth" && (
              <Card className="border-0 shadow-md">
                <CardHeader>
                  <CardTitle>Conectar Conta Mercado Pago</CardTitle>
                  <CardDescription>
                    Vincule sua conta do Mercado Pago para receber pagamentos de suas
                    vendas e criar seus caixas.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {tenantId ? (
                    <Button
                      asChild
                      className="w-auto bg-[#009EE3] hover:bg-[#0089C7] text-white"
                    >
                      <Link href={mpAuthUrl}>
                        <LinkIcon className="mr-2 h-4 w-4" />
                        Conectar Mercado Pago
                      </Link>
                    </Button>
                  ) : (
                    <Button
                      disabled
                      className="w-auto bg-[#009EE3]/70 text-white cursor-not-allowed"
                    >
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Preparando conexão...
                    </Button>
                  )}
                </CardContent>
              </Card>
            )}
            {newIntegrationStep === "store" && (
              <Card className="border-0 shadow-md">
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
                        />
                        <p
                          id="companyId-description"
                          className="text-sm text-muted-foreground"
                        >
                          Identificador externo da loja para o sistema
                          integrador. Pode conter qualquer valor alfanumérico de
                          até 60 caracteres e deve ser único para cada loja. Por
                          exemplo, LOJ001.
                        </p>
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
                              handleUpdateStoreField(
                                "streetName",
                                e.target.value,
                              )
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
                                e.target.value === ""
                                  ? ""
                                  : parseFloat(e.target.value),
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
                                e.target.value === ""
                                  ? ""
                                  : parseFloat(e.target.value),
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

            {newIntegrationStep === "pos" && (
              <Card className="border-0 shadow-md">
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
                        Certifique-se de que configurou a Loja Física (Etapa 1)
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
                          <Label htmlFor="caixaId">
                            Selecionar Caixa (Físico)
                          </Label>
                          <Select
                            value={posForm.caixaId}
                            onValueChange={(val) => {
                              const cr = cashRegisters.find(
                                (c) => c.code === val,
                              );
                              setPosForm((prev) => ({
                                ...prev,
                                caixaId: val,
                                name: cr ? cr.description : prev.name,
                              }));
                            }}
                          >
                            <SelectTrigger>
                              <SelectValue placeholder="Selecione um caixa registrado..." />
                            </SelectTrigger>
                            <SelectContent>
                              {cashRegisters.map((cr) => (
                                <SelectItem key={cr.code} value={cr.code}>
                                  {cr.code} - {cr.description}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="posName">
                            Nome do Caixa no Mercado Pago
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
        </TabsContent>
      </Tabs>
    </div>
  );
}
