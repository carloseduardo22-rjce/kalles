"use client";

import { useState } from "react";
import { steps } from "./steps";
import { useQuery } from "@tanstack/react-query";
import { mercadopagoService } from "@/features/admin/services/mercadopago.service";
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
  const [currentStep, setCurrentStep] = useState(1);
  const [selectedStore, setSelectedStore] = useState("");
  const [selectedPos, setSelectedPos] = useState("");
  const [serial, setSerial] = useState("");
  const [configuringState, setConfiguringState] = useState("");
  const [isConfigured, setIsConfigured] = useState(false);

  const mockedStores: MpStore[] = [
    {
      id: 123456,
      name: "Loja Teste (Mock)",
      external_id: "store_123",
      date_creation: "2024-01-01",
      terminals: [
        {
          id: 987654,
          name: "Caixa Frontal (Mock)",
          fixed_amount: true,
          store_id: 123456,
          external_store_id: "store_123",
          external_id: "ext_123",
        },
      ],
    },
  ];

  const stores = mockedStores;

  // TODO: Remover o const stores = mockedStores e descomentar isso depois de testar
  /*
  const { data: stores = [] } = useQuery({
    queryKey: ["mp-stores"],
    queryFn: mercadopagoService.listStores,
  });
  */

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
            const isDisabled = s.id === 3 && stores.length === 0;

            return (
              <Tooltip key={s.id}>
                <TooltipTrigger asChild>
                  <li
                    onClick={() => !isDisabled && setCurrentStep(s.id)}
                    className={`p-3 rounded transition 
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
                  </li>
                </TooltipTrigger>

                {isDisabled && (
                  <TooltipContent side="right" className="max-w-[200px]">
                    Não existe lojas e caixas configurados por favor navegue até
                    'Configurar pagamentos' para configurar a loja e os caixas
                    anexadas a ela.
                  </TooltipContent>
                )}
              </Tooltip>
            );
          })}
        </ul>
      </div>

      <div className="flex-1 p-8">
        <h1 className="text-2xl font-bold mb-4">{step?.title}</h1>

        <p className="mb-6 text-gray-700 whitespace-pre-line">
          {step?.content}
        </p>

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
                  direto no terminal foi ativado. As notificações webhooks estão
                  configuradas corretamente e a maquininha já estão totalmente
                  integrada ao nosso sistema, pronta para operar e transacionar
                  na tele de PDV.
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
              disabled={
                currentStep === steps.length ||
                (currentStep === 2 && stores.length === 0)
              }
              onClick={() => setCurrentStep((prev) => prev + 1)}
              className="px-4 py-2 bg-blue-500 text-white rounded disabled:opacity-50"
            >
              Próximo
            </button>
          </div>
        </div>
        {currentStep === 2 && stores.length === 0 && (
          <p className="text-red-500 mt-5 ">
            Não foi possível encontrar lojas e caixas associadas à sua conta do
            Mercado Pago. Por favor, verifique se você tem lojas e caixas
            criadas no Mercado Pago. Para isso, navegue até{" "}
            <span className="font-bold text-blue-500">
              'Configurar pagamentos'
            </span>{" "}
            no menu lateral.
          </p>
        )}
      </div>
    </div>
  );
}
