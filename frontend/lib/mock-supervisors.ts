import type { Operator } from "@/lib/types"

export const MOCK_SUPERVISORS: (Operator & { code: string; password: string; canCancelSale: boolean })[] = [
  {
    id: "GER001",
    code: "GER001",
    name: "Carlos Silva - Gerente",
    permissionLevel: "manager",
    canRemoveItems: true,
    canCancelSale: true,
    password: "gerente123",
  },
  {
    id: "GER002",
    code: "GER002",
    name: "Maria Santos - Gerente",
    permissionLevel: "manager",
    canRemoveItems: true,
    canCancelSale: true,
    password: "gerente123",
  },
  {
    id: "FISC001",
    code: "FISC001",
    name: "João Oliveira - Fiscal",
    permissionLevel: "supervisor",
    canRemoveItems: true,
    canCancelSale: true,
    password: "fiscal123",
  },
  {
    id: "FISC002",
    code: "FISC002",
    name: "Ana Costa - Fiscal",
    permissionLevel: "supervisor",
    canRemoveItems: true,
    canCancelSale: true,
    password: "fiscal123",
  },
]

export function validateSupervisor(code: string, password: string): (Operator & { code: string }) | null {
  const supervisor = MOCK_SUPERVISORS.find((s) => s.code === code && s.password === password)
  if (!supervisor) return null
  return supervisor
}
