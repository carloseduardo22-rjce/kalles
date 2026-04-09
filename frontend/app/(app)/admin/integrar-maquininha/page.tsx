"use client";

import { useState } from "react";
import Link from "next/link";
import { steps } from "./steps";
import { useQuery } from "@tanstack/react-query";
import { paymentMercadoPagoService as mercadopagoService } from "@/features/admin/services/payment-mercadopago.service";
import {
  getPaymentProvider,
  paymentProviders,
} from "@/features/payment/providers";
import type { PaymentProviderId } from "@/features/payment/types";
import type {
  MpStore,
  MpPos,
} from "@/features/admin/services/mercadopago.types";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";

export default function IntegrarMaquininhaPage() {
  const [selectedProviderId, setSelectedProviderId] =
    useState<PaymentProviderId>("MERCADO_PAGO");
  const [currentStep, setCurrentStep] = useState(1);
  const [selectedStore, setSelectedStore] = useState("");
  const [selectedPos, setSelectedPos] = useState("");
  const [serial, setSerial] = useState("");
  const [configuringState, setConfiguringState] = useState("");
  const [isConfigured, setIsConfigured] = useState(false);
  const selectedProvider = getPaymentProvider(selectedProviderId);

  const { data: stores = [], isLoading } = useQuery({
    queryKey: ["payment-provider-stores", selectedProviderId],
    queryFn: async () =>
      selectedProviderId === "MERCADO_PAGO"
        ? mercadopagoService.listStores()
        : [],
  });

  const hasStoresWithPos = stores.some(
    (store: MpStore) => store.terminals && store.terminals.length > 0,
  );
  const isBlocked = !isLoading && !hasStoresWithPos;

  const handleConfigure = async () => {
    if (!selectedStore || !selectedPos || !serial) return;
    setIsConfigured(false);
    try {
      setConfiguringState("Ativando modo PDV...");
      await mercadopagoService.activatePdv(selectedStore, selectedPos, serial);

      setConfiguringState("Integrando sistema de pagamentos...");
      await new Promise((r) => setTimeout(r, 1500)); // Simulando automação da persistencia

      setConfiguringState("Configurando notificações webhook...");
      await new Promise((r) => setTimeout(r, 1500)); // Simulando automação de webhooks

      setIsConfigured(true);
    } catch (error) {
      console.error(error);
    } finally {
      setConfiguringState("");
    }
  };

  const step = steps.find((s) => s.id === currentStep);

  return (
    <div className="flex h-screen">
      <div className="w-1/4 bg-gray-100 p-4 border-r">
        <h2 className="text-lg font-semibold mb-4">Integração da Maquininha</h2>

        <ul className="space-y-2">
          {steps.map((s) => {
            const isDisabled = isBlocked;

            return (
              <Tooltip key={s.id}>
                <li>
                  <TooltipTrigger asChild>
                    <button
                      onClick={() => !isDisabled && setCurrentStep(s.id)}
                      className={`w-full text-left p-3 rounded transition 
                          ${currentStep === s.id ? "bg-blue-500 text-white" : "bg-white"}
                          ${
                            isDisabled
                              ? "opacity-50 cursor-not-allowed"
                              : "hover:bg-gray-200 cursor-pointer"
                          }`}
                    >
                      <div className="flex items-center gap-2">
                        <span className="font-bold">{s.id}.</span>
                        <span>{s.title}</span>
                      </div>
                    </button>
                  </TooltipTrigger>
                </li>

                {isDisabled && (
                  <TooltipContent side="right" className="max-w-[200px]">
                    Não existem lojas e caixas configurados, por favor navegue
                    até{" "}
                    <Link
                      href="/admin/pagamentos"
                      className="text-blue-400 font-bold hover:underline"
                    >
                      Criar lojas e PDV
                    </Link>{" "}
                    para configurar.
                  </TooltipContent>
                )}
              </Tooltip>
            );
          })}
        </ul>
      </div>

      <div className="flex-1 p-8 relative">
        <div className="mb-6 grid gap-3 md:grid-cols-2">
          {paymentProviders.map((provider) => {
            const isSelected = provider.id === selectedProviderId;

            return (
              <button
                key={provider.id}
                type="button"
                onClick={() => setSelectedProviderId(provider.id)}
                className={`rounded-2xl border p-4 text-left transition-all ${
                  isSelected
                    ? "border-slate-900 bg-slate-900 text-white shadow-lg"
                    : "border-slate-200 bg-white hover:border-slate-300"
                }`}
              >
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <p className="text-sm font-semibold">
                      {provider.presentation.displayName}
                    </p>
                    <p
                      className={`mt-1 text-sm ${
                        isSelected ? "text-slate-200" : "text-slate-500"
                      }`}
                    >
                      {provider.presentation.description}
                    </p>
                  </div>
                  <div
                    className={`rounded-full px-3 py-1 text-xs font-semibold ${
                      isSelected
                        ? "bg-white/10 text-white"
                        : "bg-slate-100 text-slate-600"
                    }`}
                  >
                    {provider.capabilities.terminalActivation ? "Ativo" : "Preview"}
                  </div>
                </div>
              </button>
            );
          })}
        </div>
        {selectedProviderId === "STONE" ? (
          <div className="rounded-2xl border border-slate-200 bg-slate-50 p-6">
            <h1 className="text-2xl font-bold text-slate-900">
              {selectedProvider.presentation.displayName}
            </h1>
            <p className="mt-2 text-sm text-slate-600">
              Esta tela ja usa o contrato generico de providers, mas o
              onboarding visual da Stone ainda nao foi ligado ao fluxo
              operacional do caixa.
            </p>

            <div className="mt-4 space-y-2">
              {selectedProvider.operationalNotes?.map((note) => (
                <div
                  key={note}
                  className="rounded-lg border bg-white px-4 py-3 text-sm text-slate-700"
                >
                  {note}
                </div>
              ))}
            </div>
          </div>
        ) : (
        <>
        {isLoading && (
          <div className="absolute inset-0 z-10 flex items-center justify-center bg-white/50">
            <span className="text-gray-500 font-medium">Carregando...</span>
          </div>
        )}

        {isBlocked && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-md">
            <p className="text-red-600 font-medium">
              Atenção: Não foi possível encontrar lojas e caixas associadas à
              sua conta do Mercado Pago.
            </p>
            <p className="text-red-500 mt-2 text-sm">
              A integração da maquininha só pode ser realizada após a criação de
              pelo menos uma loja e um caixa. Por favor, navegue até{" "}
              <Link
                href="/admin/pagamentos"
                className="text-blue-600 font-bold hover:underline"
              >
                Criar lojas e PDV
              </Link>{" "}
              no menu lateral.
            </p>
          </div>
        )}

        <div className={isBlocked ? "opacity-50 pointer-events-none" : ""}>
          <h1 className="text-2xl font-bold mb-4">{step?.title}</h1>

          <div className="mb-6 text-gray-700">{step?.content}</div>

          {currentStep === 3 && stores.length > 0 && (
            <div className="mb-6 space-y-4 max-w-sm">
              {isConfigured ? (
                <div className="p-6 bg-green-50 border border-green-200 text-green-900 rounded-lg shadow-sm">
                  <div className="flex items-center gap-3 mb-4">
                    <div className="w-10 h-10 rounded-full bg-green-500 flex items-center justify-center text-white text-xl shadow-sm">
                      âœ“
                    </div>
                    <h3 className="font-bold text-xl">
                      Tudo configurado e pronto!
                    </h3>
                  </div>
                  <p className="text-sm leading-relaxed mb-6">
                    Sua maquininha foi ativada com sucesso em{" "}
                    <strong>Modo PDV</strong> e o processamento de pagamentos
                    direto no terminal foi ativado. As notificações webhooks
                    estão configuradas corretamente e a maquininha já estão
                    totalmente integrada ao nosso sistema, pronta para operar e
                    transacionar na tele de PDV.
                  </p>
                  <button
                    className="bg-green-600 text-white font-semibold py-2 px-4 rounded w-full hover:bg-green-700 transition shadow-sm"
                    onClick={() => {
                      setIsConfigured(false);
                      setSerial("");
                    }}
                  >
                    Configurar outra maquininha
                  </button>
                </div>
              ) : (
                <>
                  <div>
                    <label className="block text-sm font-medium mb-1">
                      Selecione a Loja
                    </label>
                    <select
                      className="w-full border rounded p-2"
                      value={selectedStore}
                      onChange={(e) => setSelectedStore(e.target.value)}
                    >
                      <option value="">-- Selecione --</option>
                      {stores.map((store: MpStore) => (
                        <option key={store.id} value={store.id}>
                          {store.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-1">
                      Selecione o Caixa
                    </label>
                    <select
                      className="w-full border rounded p-2"
                      disabled={!selectedStore}
                      value={selectedPos}
                      onChange={(e) => setSelectedPos(e.target.value)}
                    >
                      <option value="">-- Selecione --</option>
                      {stores
                        .find((s: MpStore) => String(s.id) === selectedStore)
                        ?.terminals?.map((pos: MpPos) => (
                          <option key={pos.id} value={pos.id}>
                            {pos.name}
                          </option>
                        )) || []}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-1">
                      Número de Série da Maquininha
                    </label>
                    <input
                      type="text"
                      className="w-full border rounded p-2"
                      placeholder="Ex: N950NCB801293324"
                      value={serial}
                      onChange={(e) => setSerial(e.target.value)}
                    />
                  </div>
                  <button
                    onClick={handleConfigure}
                    disabled={
                      !selectedStore ||
                      !selectedPos ||
                      !serial ||
                      !!configuringState
                    }
                    className="bg-green-600 text-white px-4 py-2 rounded font-semibold disabled:opacity-50"
                  >
                    {configuringState || "Começar configuração"}
                  </button>
                </>
              )}
            </div>
          )}

          <div className="flex gap-4">
            <button
              disabled={currentStep === 1}
              onClick={() => setCurrentStep((prev) => prev - 1)}
              className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50"
            >
              Voltar
            </button>

            <div className="flex gap-2">
              <button
                disabled={currentStep === steps.length || isBlocked}
                onClick={() => setCurrentStep((prev) => prev + 1)}
                className="px-4 py-2 bg-blue-500 text-white rounded disabled:opacity-50"
              >
                Próximo
              </button>
            </div>
          </div>
        </div>
        </>
        )}
      </div>
    </div>
  );
}
