export interface CashSession {
  id: string
  operatorId: string
  operatorName: string
  initialCash: number
  openedAt: string
  status: "open" | "closed"
}

export interface Product {
  id: string
  internalCode: string
  barcode: string
  description: string
  price: number
  unit: string
}

export interface SaleItem {
  id: string
  product: Product
  quantity: number
  unitPrice: number
  subtotal: number
}

export interface Sale {
  id: string
  sessionId: string
  items: SaleItem[]
  subtotal: number
  discount?: number
  total: number
  status: "in-progress" | "completed" | "cancelled"
  payment?: Payment
  createdAt: string
  completedAt?: string
}

export type PaymentMethod = "cash" | "debit" | "credit" | "pix"

export interface PaymentMethodEntry {
  method: PaymentMethod
  amount: number
}

export interface Payment {
  methods: PaymentMethodEntry[]
  totalPaid: number
  totalDue: number
  change: number
  status: "pending" | "approved" | "rejected"
  timestamp: string
}

export interface CashClosingSummary {
  sessionId: string
  operatorId: string
  operatorName: string
  openedAt: string
  closedAt: string
  initialCash: number
  totalSales: number
  salesCount: number
  cashPayments: number
  cardPayments: number
  expectedCash: number
  actualCash: number
  difference: number
}

export type OperatorPermissionLevel = "basic" | "supervisor" | "manager"

export interface Operator {
  id: string
  name: string
  permissionLevel: OperatorPermissionLevel
  canRemoveItems: boolean
}

export interface ItemRemovalLog {
  id: string
  saleId: string
  item: SaleItem
  removedQuantity: number
  requestedBy: string
  requestedByName: string
  authorizedBy?: string
  authorizedByName?: string
  timestamp: string
  reason?: string
}

export interface SaleCancellationLog {
  id: string
  saleId: string
  requestedBy: string
  requestedByName: string
  authorizedBy?: string
  authorizedByName?: string
  timestamp: string
  reason?: string
}
