import { mercadoPagoProvider } from "@/features/payment/providers/mercado-pago-provider";
import type {
  PaymentPoint,
  PaymentStore,
  PaymentTerminal,
} from "@/features/payment/types";
import type {
  MpPos,
  MpProviderStatus,
  MpStore,
  MpStoreStatus,
  MpTerminal,
} from "./mercadopago.types";

const mapPointToMpPos = (point: PaymentPoint): MpPos => ({
  id: point.providerPointId,
  name: point.name,
  fixed_amount: point.fixedAmount ?? undefined,
  store_id: point.providerStoreId,
  external_store_id: point.externalStoreReference ?? undefined,
  external_id: point.externalReference ?? undefined,
  status: point.status ?? undefined,
  date_created: point.createdAt ?? undefined,
  date_last_updated: point.updatedAt ?? undefined,
});

const mapTerminalToMpTerminal = (terminal: PaymentTerminal): MpTerminal => ({
  id: terminal.id,
  posId: terminal.pointId,
  storeId: terminal.storeId,
  externalPosId: terminal.externalPointId,
  operationMode: terminal.operationMode,
});

export const paymentMercadoPagoService = {
  provider: mercadoPagoProvider.id,

  getProviderStatus: (): Promise<MpProviderStatus> =>
    mercadoPagoProvider.getProviderStatus!() as Promise<MpProviderStatus>,

  getCurrentStoreStatus: (): Promise<MpStoreStatus> =>
    mercadoPagoProvider.getCurrentStoreStatus!() as Promise<MpStoreStatus>,

  linkAccount: (authorizationCode: string, state: string): Promise<void> =>
    mercadoPagoProvider.linkAccount!(authorizationCode, state),

  createStore: (
    companyId: string,
    externalReference: string,
  ): Promise<PaymentStore> =>
    mercadoPagoProvider.createStore!(companyId, externalReference),

  createPos: (
    cashRegisterId: string,
    externalReference?: string,
  ): Promise<PaymentPoint> =>
    mercadoPagoProvider.createPoint!(cashRegisterId, externalReference),

  processQrPayment: (
    externalReference: string,
    amount: number,
    cashRegisterCode: string,
  ) =>
    mercadoPagoProvider.processQrPayment!(
      externalReference,
      amount,
      cashRegisterCode,
    ),

  listStores: async (): Promise<MpStore[]> => {
    const [stores, points] = await Promise.all([
      mercadoPagoProvider.listStores!(),
      mercadoPagoProvider.listPoints!().catch(() => []),
    ]);

    return stores.map((store) => ({
      id: store.providerStoreId,
      name: store.name,
      external_id: store.externalReference,
      date_creation: store.createdAt ?? undefined,
      terminals: points
        .filter((point) => point.providerStoreId === store.providerStoreId)
        .map(mapPointToMpPos),
    }));
  },

  listPos: async (): Promise<MpPos[]> => {
    const points = await mercadoPagoProvider.listPoints!().catch(() => []);
    return points.map(mapPointToMpPos);
  },

  listTerminals: async (
    storeId: string,
    posId: string,
  ): Promise<MpTerminal[]> => {
    const terminals = await mercadoPagoProvider
      .listTerminals!(storeId, posId)
      .catch(() => []);

    return terminals.map(mapTerminalToMpTerminal);
  },

  activatePdv: (
    storeId: string,
    posId: string,
    terminalSerial: string,
  ): Promise<void> =>
    mercadoPagoProvider.activatePointOfSale!(storeId, posId, terminalSerial),
};
