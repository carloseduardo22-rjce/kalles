"use client";

import { useState } from "react";
import { steps } from "./steps";
import { useQuery } from "@tanstack/react-query";
import { mercadopagoService } from "@/features/admin/services/mercadopago.service";
import {
    Tooltip,
    TooltipContent,
    TooltipTrigger,
} from "@/components/ui/tooltip"


export default function IntegrarMaquininhaPage() {
    const [currentStep, setCurrentStep] = useState(1);

    const { data: stores = [] } = useQuery({
        queryKey: ["mp-stores"],
        queryFn: mercadopagoService.listStores,
    });

    const step = steps.find((s) => s.id === currentStep);

    return (
        <div className="flex h-screen">
            <div className="w-1/4 bg-gray-100 p-4 border-r">
                <h2 className="text-lg font-semibold mb-4">
                    Integração da Maquininha
                </h2>

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
                        ${isDisabled
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
                                        Não existe lojas e caixas configurados por favor navegue até 'Configurar pagamentos' para configurar a loja e os caixas anexadas a ela.
                                    </TooltipContent>
                                )}
                            </Tooltip>
                        );
                    })}


                </ul>
            </div>

            <div className="flex-1 p-8">
                <h1 className="text-2xl font-bold mb-4">{step?.title}</h1>

                <p className="mb-6 text-gray-700 whitespace-pre-line">{step?.content}</p>

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
                                currentStep === steps.length || currentStep === 2 && stores.length === 0
                            }
                            onClick={() => setCurrentStep((prev) => prev + 1)}
                            className="px-4 py-2 bg-blue-500 text-white rounded disabled:opacity-50"
                        >
                            Próximo
                        </button>
                    </div>
                </div>
                {
                    currentStep === 2 && stores.length === 0 && (
                        <p className="text-red-500 mt-5 ">
                            Não foi possível encontrar lojas e caixas associadas à sua conta do Mercado Pago. Por favor, verifique se você tem lojas e caixas criadas no Mercado Pago.
                            Para isso, navegue até <span className="font-bold text-blue-500">'Configurar pagamentos'</span> no menu lateral.
                        </p>
                    )
                }
            </div>
        </div>
    );
}