package dev.kalles.payment.application.service;

import dev.kalles.payment.application.port.in.GetPaymentProviderAccountStatusUseCase;
import dev.kalles.payment.application.port.in.LinkPaymentProviderAccountUseCase;
import dev.kalles.payment.application.port.in.command.LinkPaymentProviderAccountCommand;
import dev.kalles.payment.application.port.out.PaymentAccountRepository;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentProviderAccount;
import dev.kalles.payment.domain.PaymentProviderAuthorization;
import dev.kalles.security.context.TenantContextHolder;
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
        PaymentProviderAuthorization authorization = portFactory.providerAccount(command.provider())
                .exchangeAuthorizationCode(command.authorizationCode());

        PaymentProviderAccount account = paymentAccountRepository.findByTenantIdAndProvider(command.tenantId(), command.provider())
                .orElseGet(() -> new PaymentProviderAccount(
                        command.tenantId(),
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
        UUID tenantId = TenantContextHolder.requireTenantId();
        return paymentAccountRepository.findByTenantIdAndProvider(tenantId, provider)
                .map(PaymentProviderAccount::isLinked)
                .orElse(false);
    }
}
