package dev.kalles.sale.payment.application.service;

import dev.kalles.sale.payment.application.port.in.GetPaymentProviderAccountStatusUseCase;
import dev.kalles.sale.payment.application.port.in.LinkPaymentProviderAccountUseCase;
import dev.kalles.sale.payment.application.port.in.command.LinkPaymentProviderAccountCommand;
import dev.kalles.sale.payment.application.port.out.PaymentAccountRepository;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentProviderAccount;
import dev.kalles.sale.payment.domain.PaymentProviderAuthorization;
import dev.kalles.sale.payment.exception.PaymentTenantContextException;
import dev.kalles.sale.security.context.TenantContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentProviderAccountService implements
        LinkPaymentProviderAccountUseCase,
        GetPaymentProviderAccountStatusUseCase {

    private final PaymentProviderPortFactory portFactory;
    private final PaymentAccountRepository paymentAccountRepository;

    public PaymentProviderAccountService(
            PaymentProviderPortFactory portFactory,
            PaymentAccountRepository paymentAccountRepository
    ) {
        this.portFactory = portFactory;
        this.paymentAccountRepository = paymentAccountRepository;
    }

    @Override
    public void execute(LinkPaymentProviderAccountCommand command) {
        UUID tenantId = UUID.fromString(command.state());
        PaymentProviderAuthorization authorization = portFactory.providerAccount(command.provider())
                .exchangeAuthorizationCode(command.authorizationCode());

        PaymentProviderAccount account = paymentAccountRepository.findByTenantIdAndProvider(tenantId, command.provider())
                .orElseGet(() -> new PaymentProviderAccount(
                        tenantId,
                        command.provider(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ));

        paymentAccountRepository.save(account.withAuthorization(authorization));
    }

    @Override
    public boolean execute(PaymentProvider provider) {
        UUID tenantId = getCurrentTenantId();
        return paymentAccountRepository.findByTenantIdAndProvider(tenantId, provider)
                .map(PaymentProviderAccount::isLinked)
                .orElse(false);
    }

    private UUID getCurrentTenantId() {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new PaymentTenantContextException("Tenant context is required for this operation");
        }
        return tenantId;
    }
}
