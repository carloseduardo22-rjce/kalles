import type { Product } from "./types"

export const MOCK_PRODUCTS: Product[] = [
  {
    id: "PROD-001",
    internalCode: "1001",
    barcode: "7891234567890",
    description: "Arroz Branco 5kg",
    price: 25.9,
    unit: "UN",
  },
  {
    id: "PROD-002",
    internalCode: "1002",
    barcode: "7891234567891",
    description: "Feijão Preto 1kg",
    price: 8.5,
    unit: "UN",
  },
  {
    id: "PROD-003",
    internalCode: "1003",
    barcode: "7891234567892",
    description: "Óleo de Soja 900ml",
    price: 7.9,
    unit: "UN",
  },
  {
    id: "PROD-004",
    internalCode: "1004",
    barcode: "7891234567893",
    description: "Açúcar Refinado 1kg",
    price: 4.2,
    unit: "UN",
  },
  {
    id: "PROD-005",
    internalCode: "1005",
    barcode: "7891234567894",
    description: "Café Torrado 500g",
    price: 15.9,
    unit: "UN",
  },
  {
    id: "PROD-006",
    internalCode: "1006",
    barcode: "7891234567895",
    description: "Leite Integral 1L",
    price: 5.4,
    unit: "UN",
  },
  {
    id: "PROD-007",
    internalCode: "1007",
    barcode: "7891234567896",
    description: "Macarrão Espaguete 500g",
    price: 4.8,
    unit: "UN",
  },
  {
    id: "PROD-008",
    internalCode: "1008",
    barcode: "7891234567897",
    description: "Molho de Tomate 340g",
    price: 3.2,
    unit: "UN",
  },
  {
    id: "PROD-009",
    internalCode: "1009",
    barcode: "7891234567898",
    description: "Farinha de Trigo 1kg",
    price: 6.5,
    unit: "UN",
  },
  {
    id: "PROD-010",
    internalCode: "1010",
    barcode: "7891234567899",
    description: "Sal Refinado 1kg",
    price: 2.8,
    unit: "UN",
  },
]

export function findProductByCode(code: string): Product | undefined {
  const normalizedCode = code.trim()
  return MOCK_PRODUCTS.find((product) => product.internalCode === normalizedCode || product.barcode === normalizedCode)
}

export function searchProductsByDescription(query: string): Product[] {
  const normalizedQuery = query.toLowerCase().trim()
  if (!normalizedQuery) return []

  return MOCK_PRODUCTS.filter((product) => product.description.toLowerCase().includes(normalizedQuery))
}

export const MOCK_OPERATORS = [
  {
    id: "OP001",
    name: "João Silva",
    permissionLevel: "basic" as const,
    canCancelSale: false,
  },
  {
    id: "OP002",
    name: "Maria Santos",
    permissionLevel: "basic" as const,
    canCancelSale: false,
  },
  {
    id: "OP003",
    name: "Pedro Costa",
    permissionLevel: "supervisor" as const,
    canCancelSale: true,
  },
]
