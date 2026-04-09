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
import {
  Store,
  Terminal,
  Loader2,
  Lock,
  List,
  PlusCircle,
  Link as LinkIcon,
  Repeat,
} from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { LoadingSpinner } from "@/shared/components/loading-spinner";
import { paymentMercadoPagoService as mercadopagoService } from "@/features/admin/services/payment-mercadopago.service";
import {
  getPaymentProvider,
  paymentProviders,
} from "@/features/payment/providers";
import type { PaymentProviderId } from "@/features/payment/types";
import { api } from "@/shared/services/api";
import { useCompany } from "@/shared/contexts/company-context";

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
  const { activeCompany, activeCompanyId } = useCompany();
  const [selectedProviderId, setSelectedProviderId] =
    useState<PaymentProviderId>("MERCADO_PAGO");
  const [activeTab, setActiveTab] = useState<"listar" | "criar">("listar");
  const [newIntegrationStep, setNewIntegrationStep] = useState<
    "oauth" | "store" | "pos"
  >("oauth");
  const selectedProvider = getPaymentProvider(selectedProviderId);

  // Em produção, isso viraria de variáveis de ambiente (.env)
  const MP_APP_ID = process.env.NEXT_PUBLIC_MP_APP_ID || "448684586415948";
  const REDIRECT_URI =
    process.env.NEXT_PUBLIC_MP_REDIRECT_URI ||
    "https://2dbd-2804-1494-dbb-aa00-ad55-f249-5eaf-70fa.ngrok-free.app/admin/pagamentos/mp-callback";

  // O state é usado para passarmos o ID do Tenant/Dono do sistema e validar o callback
  const [tenantId, setTenantId] = useState<string>("");

  const providerAuthUrl =
    selectedProvider.auth.mode === "oauth" && tenantId
      ? selectedProvider.auth.buildAuthorizationUrl?.(tenantId) ?? ""
      : "";

  const [loading, setLoading] = useState(false);
  const [storeConfigured, setStoreConfigured] = useState(false);
  const [cashRegisters, setCashRegisters] = useState<
    { id: string; code: string; description: string }[]
  >([]);

  useEffect(() => {
    api
      .get<any[]>("/api/cash-registers")
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
  // Se já estiver, decidimos se seguimos para loja ou terminais.
  useEffect(() => {
    if (selectedProviderId !== "MERCADO_PAGO" || activeTab !== "criar") return;

    Promise.all([
      mercadopagoService.getProviderStatus(),
      mercadopagoService.getCurrentStoreStatus(),
    ])
      .then(([providerStatus, storeStatus]) => {
        if (!providerStatus.linked) {
          setStoreConfigured(false);
          setNewIntegrationStep("oauth");
          return;
        }

        const hasConfiguredStore =
          storeStatus.hasStoreRegistered || !!storeStatus.providerStoreId;

        setStoreConfigured(hasConfiguredStore);
        setNewIntegrationStep(hasConfiguredStore ? "pos" : "store");
      })
      .catch(() => setNewIntegrationStep("oauth"));
  }, [activeTab, selectedProviderId]);

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

  const handleCreateStore = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      if (!activeCompanyId) {
        throw new Error("Selecione uma empresa ativa antes de vincular a loja.");
      }

      await mercadopagoService.createStore(activeCompanyId, storeForm.companyId);

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
      const selectedRegister = cashRegisters.find(
        (cashRegister) => cashRegister.code === posForm.caixaId,
      );

      if (!selectedRegister) {
        throw new Error("Selecione um caixa válido para vincular.");
      }

      await mercadopagoService.createPos(
        selectedRegister.id,
        selectedRegister.code.replace(/-/g, ""),
      );

      toast.success("Caixa/POS vinculado com sucesso!");
      setPosForm({ caixaId: "", name: "" }); // Reset
    } catch (error: any) {
      toast.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  const { data: stores = [], isLoading: isLoadingStores } = useQuery({
    queryKey: ["payment-provider-stores", selectedProviderId],
    queryFn: mercadopagoService.listStores,
    enabled: selectedProviderId === "MERCADO_PAGO",
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
    <div
      className={`flex-1 min-h-screen p-4 pt-6 md:p-8 ${
        selectedProviderId === "STONE" ? "bg-slate-950" : "bg-[#009EE3]"
      }`}
    >
      <div className="flex items-center gap-4 mb-2">
        <h2 className="text-3xl font-bold tracking-tight text-white drop-shadow-md">
          Integração de Pagamentos
        </h2>
        {selectedProvider.presentation.logoSrc ? (
          <div className="bg-white p-3 rounded-xl shadow-sm">
            <Image
              src={selectedProvider.presentation.logoSrc}
              alt={selectedProvider.presentation.displayName}
              width={160}
              height={40}
              className="object-contain"
              priority
            />
          </div>
        ) : (
          <div className="rounded-xl bg-white/10 px-4 py-3 text-sm font-semibold text-white shadow-sm backdrop-blur-sm">
            {selectedProvider.presentation.shortName}
          </div>
        )}
      </div>

      {selectedProviderId !== "MERCADO_PAGO" && (
        <p className="text-white/90 w-full mb-8 font-medium">
          {selectedProvider.presentation.description}
        </p>
      )}

      {selectedProviderId === "MERCADO_PAGO" && (
        <p className="text-white/90 w-full mb-8 font-medium">
        Gerencie as suas integrações com o Mercado Pago para aceitar pagamentos
        via Pix (QrCode Dinâmico) e Cartões (Crédito, Débito e Vouchers -
        Alimentação/Refeição). Você pode visualizar as lojas configuradas ou
        criar novas integrações.
        </p>
      )}

      <div className="mb-8 grid gap-3 md:grid-cols-2">
        {paymentProviders.map((provider) => {
          const isSelected = provider.id === selectedProviderId;

          return (
            <button
              key={provider.id}
              type="button"
              onClick={() => setSelectedProviderId(provider.id)}
              className={`rounded-2xl border p-4 text-left transition-all ${
                isSelected
                  ? "border-white bg-white text-slate-950 shadow-lg"
                  : "border-white/30 bg-white/10 text-white hover:bg-white/15"
              }`}
            >
              <div className="flex items-center justify-between gap-4">
                <div>
                  <p className="text-sm font-semibold">
                    {provider.presentation.displayName}
                  </p>
                  <p
                    className={`mt-1 text-sm ${
                      isSelected ? "text-slate-600" : "text-white/75"
                    }`}
                  >
                    {provider.presentation.description}
                  </p>
                </div>
                <div
                  className={`rounded-full px-3 py-1 text-xs font-semibold ${
                    isSelected
                      ? "bg-slate-100 text-slate-700"
                      : "bg-white/15 text-white"
                  }`}
                >
                  {provider.capabilities.accountLink ? "Onboarding" : "Preview"}
                </div>
              </div>
            </button>
          );
        })}
      </div>

      <Card className="mb-8 border-0 bg-white/95 shadow-md">
        <CardContent className="flex flex-col gap-4 p-5 md:flex-row md:items-center md:justify-between">
          <div className="space-y-1">
            <p className="text-sm font-semibold text-slate-900">
              Assinatura recorrente do ERP
            </p>
            <p className="text-sm text-slate-600">
              A cobranca mensal do Kalles via Stripe agora fica em uma area
              dedicada, com checkout embutido e portal do cliente.
            </p>
          </div>

          <Button
            asChild
            variant="outline"
            className="border-sky-200 text-sky-700 hover:bg-sky-50"
          >
            <Link href="/admin/assinatura">
              <Repeat className="mr-2 h-4 w-4" />
              Abrir assinatura Stripe
            </Link>
          </Button>
        </CardContent>
      </Card>

      {selectedProviderId === "STONE" && (
        <div className="grid gap-6 pb-20">
          <Card className="border-0 shadow-md">
            <CardHeader>
              <CardTitle>{selectedProvider.presentation.displayName}</CardTitle>
              <CardDescription>
                O frontend ja conhece este provider pelo contrato generico de
                payment, mas a camada visual de onboarding ainda nao foi ligada
                ao fluxo operacional do caixa.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Alert className="border-slate-200 bg-slate-50 text-slate-800">
                <AlertTitle className="font-semibold">Status atual</AlertTitle>
                <AlertDescription>
                  O backend payment ja suporta pedidos, fechamento, impressao e
                  webhooks da Stone Connect 2.0. O proximo passo no frontend e
                  mapear terminal por caixa para o PDV acionar o adapter Stone
                  sem conhecer detalhes da API externa.
                </AlertDescription>
              </Alert>

              <div className="space-y-2">
                {selectedProvider.operationalNotes?.map((note) => (
                  <div
                    key={note}
                    className="rounded-lg border bg-slate-50 px-4 py-3 text-sm text-slate-700"
                  >
                    {note}
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {selectedProviderId !== "STONE" && (
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
                <p className="mt-4">
                  Buscando lojas e caixas no Mercado Pago...
                </p>
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
              ))
            )}
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
                    Vincule sua conta do Mercado Pago para receber pagamentos de
                    suas vendas e criar seus caixas.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {tenantId ? (
                    <Button
                      asChild
                      className="w-auto bg-[#009EE3] hover:bg-[#0089C7] text-white"
                    >
                      <Link href={providerAuthUrl}>
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
                    A empresa ativa do Kalles fornece os dados cadastrais da
                    loja. Aqui você informa a referência usada na integração.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {!activeCompanyId && (
                    <Alert className="mb-6 border-amber-500/50 bg-amber-500/10 text-amber-700">
                      <AlertTitle className="font-semibold">
                        Empresa ativa obrigatória
                      </AlertTitle>
                      <AlertDescription>
                        Selecione a empresa ativa antes de criar a loja no
                        Mercado Pago.
                      </AlertDescription>
                    </Alert>
                  )}

                  {activeCompany && (
                    <Alert className="mb-6 border-sky-500/30 bg-sky-50 text-sky-900">
                      <AlertTitle className="font-semibold">
                        Empresa ativa
                      </AlertTitle>
                      <AlertDescription>
                        A integração será criada usando os dados cadastrais de{" "}
                        <strong>{activeCompany.name}</strong>.
                      </AlertDescription>
                    </Alert>
                  )}

                  <form onSubmit={handleCreateStore} className="space-y-6">
                    <div className="grid gap-4 md:grid-cols-2">
                      <div className="space-y-2">
                        <Label htmlFor="companyId">
                          Referência externa da loja
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
                          Identificador único que o provedor usará para a loja.
                          Exemplo: LOJ001.
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
                        />
                      </div>
                    </div>

                    <div className="space-y-4 pt-4 border-t">
                      <h4 className="text-sm font-medium">Localização</h4>
                      <p className="text-sm text-muted-foreground">
                        Esses campos ficam apenas como conferência visual nesta
                        etapa. O cadastro oficial usado na integração vem da
                        empresa ativa.
                      </p>
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
                      <Button type="submit" disabled={loading || !activeCompanyId}>
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
                          Referência da loja vinculada
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
                                name: cr
                                  ? `${cr.description} - ${cr.code}`
                                  : prev.name,
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
                            Nome gerado para o ponto de pagamento
                          </Label>
                          <Input
                            id="posName"
                            placeholder="Selecione um caixa para visualizar"
                            value={posForm.name}
                            readOnly
                            className="bg-muted/50"
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
      )}
    </div>
  );
}
