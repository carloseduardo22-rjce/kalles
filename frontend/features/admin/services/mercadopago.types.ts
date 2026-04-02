export interface MpStore {
  id: number;
  name: string;
  external_id: string;
  date_creation?: string;
  terminals?: MpPos[];
}

export interface MpPos {
  id: number;
  name: string;
  fixed_amount: boolean;
  store_id: number | string;
  external_store_id: string;
  external_id: string;
  qr_code?: string;
}

export interface MpTerminal {
  id: string;
  posId: string;
  storeId: string;
  externalPosId: string;
  operationMode: string;
}
