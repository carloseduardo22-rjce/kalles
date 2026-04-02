package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.domain.CobrancaPoint;
import dev.kalles.sale.mercadopago.domain.PointOrder;
import dev.kalles.sale.mercadopago.domain.PointOrderStatus;
import dev.kalles.sale.mercadopago.domain.ResultadoPoint;
import dev.kalles.sale.mercadopago.port.MercadoPagoOrderPort;
import dev.kalles.sale.mercadopago.port.PointOrderPersistencePort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProcessPaymentOrderUseCase {

    private final MercadoPagoOrderPort orderPort;
    private final PointOrderPersistencePort persistencePort;

    public ProcessPaymentOrderUseCase(MercadoPagoOrderPort orderPort, PointOrderPersistencePort persistencePort) {
        this.orderPort = orderPort;
        this.persistencePort = persistencePort;
    }

public ResultadoPoint startPayment(String terminalId, BigDecimal amount, String description, String externalReference, String paymentMethodType) {   
        String idempotencyKey = UUID.randomUUID().toString();

        CobrancaPoint request = new CobrancaPoint(
                externalReference,
                amount,
                terminalId,
                idempotencyKey,
                description,
                paymentMethodType
        );
        
        ResultadoPoint result = orderPort.createOrderPoint(request);
        
        PointOrder pointOrder = new PointOrder();
        pointOrder.setOrderId(result.orderId());
        pointOrder.setPaymentId(result.paymentId());
        try {
            pointOrder.setStatus(PointOrderStatus.valueOf(result.status().toUpperCase()));
        } catch (Exception e) {
            // default or handle unknown status
        }
        pointOrder.setExternalReference(externalReference);
        pointOrder.setAmount(amount);
        pointOrder.setIdempotencyKey(idempotencyKey);
        
        persistencePort.save(pointOrder);
        
        return result;
    }
}
