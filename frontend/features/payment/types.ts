export type PaymentProviderId = "MERCADO_PAGO" | "STONE";

export type PaymentProviderLinkMode = "oauth" | "none";

export interface PaymentProviderCapabilities {
  accountLink: boolean;
  storeManagement: boolean;
  pointManagement: boolean;
  terminalActivation: boolean;
  qrCodeAtPdv: boolean;
  terminalChargeAtPdv: boolean;
}

export interface PaymentProviderPresentation {
  displayName: string;
  shortName: string;
  accentColor: string;
  description: string;
  logoSrc?: string;
}

export interface PaymentProviderAuthConfig {
  mode: PaymentProviderLinkMode;
  callbackPath?: string;
  startAuthorization?: () => Promise<string>;
}

export interface PaymentProviderStatus {
  provider: PaymentProviderId;
  linked: boolean;
}

export interface PaymentStoreStatus {
  provider: PaymentProviderId;
  companyExists: boolean;
  hasStoreRegistered: boolean;
  externalReference: string | null;
  providerStoreId: string | null;
}

export interface PaymentStore {
  providerStoreId: string;
  name: string;
  externalReference: string;
  createdAt?: string | null;
  metadata?: Record<string, unknown> | null;
}

export interface PaymentPoint {
  providerPointId: string;
  name: string;
  providerStoreId: string;
  externalReference?: string | null;
  externalStoreReference?: string | null;
  fixedAmount?: boolean | null;
  status?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  metadata?: Record<string, unknown> | null;
}

export interface PaymentTerminal {
  id: string;
  pointId: string;
  storeId: string;
  externalPointId: string;
  operationMode: string;
}

export interface PaymentTerminalMapping {
  id: string;
  cashRegisterId: string;
  provider: PaymentProviderId;
  terminalSerial: string;
  active: boolean;
}

export interface PaymentExecutionResult {
  provider: PaymentProviderId;
  providerOrderId: string;
  providerPaymentId: string | null;
  status: string;
  metadata?: Record<string, unknown> | null;
}

export interface QrPaymentResult {
  provider: PaymentProviderId;
  orderId: string;
  qrData: string;
}

export interface TerminalPaymentCommand {
  externalReference: string;
  amount: number;
  targetId: string;
  methodType: "CREDIT_CARD" | "DEBIT_CARD" | "VOUCHER_CARD";
  metadata?: Record<string, unknown>;
}

export interface TerminalPaymentResult {
  provider: PaymentProviderId;
  orderId: string;
  providerPaymentId: string | null;
  status: string;
  metadata?: Record<string, unknown> | null;
}

export interface PaymentProviderAdapter {
  id: PaymentProviderId;
  presentation: PaymentProviderPresentation;
  capabilities: PaymentProviderCapabilities;
  auth: PaymentProviderAuthConfig;
  operationalNotes?: string[];
  getProviderStatus?: () => Promise<PaymentProviderStatus>;
  getCurrentStoreStatus?: () => Promise<PaymentStoreStatus>;
  linkAccount?: (authorizationCode: string, state: string) => Promise<void>;
  createStore?: (
    companyId: string,
    externalReference: string,
  ) => Promise<PaymentStore>;
  createPoint?: (
    cashRegisterId: string,
    externalReference?: string,
  ) => Promise<PaymentPoint>;
  listStores?: () => Promise<PaymentStore[]>;
  listPoints?: () => Promise<PaymentPoint[]>;
  listTerminals?: (
    storeId: string,
    pointId: string,
  ) => Promise<PaymentTerminal[]>;
  activatePointOfSale?: (
    storeId: string,
    pointId: string,
    terminalSerial: string,
  ) => Promise<void>;
  processQrPayment?: (
    externalReference: string,
    amount: number,
    targetId: string,
  ) => Promise<QrPaymentResult>;
  processTerminalPayment?: (
    command: TerminalPaymentCommand,
  ) => Promise<TerminalPaymentResult>;
}
