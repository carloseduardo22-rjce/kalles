export interface MpStore {
  id: string | number;
  name: string;
  external_id: string;
  date_creation?: string;
  terminals?: MpPos[];
}

export interface MpPos {
  id: number | string;
  name: string;
  fixed_amount?: boolean;
  store_id: number | string;
  external_store_id?: string;
  external_id?: string;
  qr_code?: string;
  status?: string;
  date_created?: string;
  date_last_updated?: string;
}

export interface MpTerminal {
  id: string;
  posId: string;
  storeId: string;
  externalPosId: string;
  operationMode: string;
}

export interface MpStoreStatus {
  provider: "MERCADO_PAGO";
  companyExists: boolean;
  hasStoreRegistered: boolean;
  externalReference: string | null;
  providerStoreId: string | null;
}

export interface MpProviderStatus {
  provider: "MERCADO_PAGO";
  linked: boolean;
}
