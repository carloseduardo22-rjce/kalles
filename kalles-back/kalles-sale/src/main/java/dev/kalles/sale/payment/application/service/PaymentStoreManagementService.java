package dev.kalles.sale.payment.application.service;

import dev.kalles.sale.core.entity.Company;
import dev.kalles.sale.core.repository.CompanyRepository;
import dev.kalles.sale.payment.application.port.in.CreatePaymentStoreUseCase;
import dev.kalles.sale.payment.application.port.in.GetPaymentStoreStatusUseCase;
import dev.kalles.sale.payment.application.port.in.ListPaymentStoresUseCase;
import dev.kalles.sale.payment.application.port.in.command.CreatePaymentStoreCommand;
import dev.kalles.sale.payment.application.port.out.PaymentStoreRepository;
import dev.kalles.sale.payment.domain.MerchantProfile;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStore;
import dev.kalles.sale.payment.domain.PaymentStoreView;
import dev.kalles.sale.payment.exception.PaymentTenantContextException;
import dev.kalles.sale.security.context.TenantContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentStoreManagementService implements
        CreatePaymentStoreUseCase,
        GetPaymentStoreStatusUseCase,
        ListPaymentStoresUseCase {

    private final PaymentProviderPortFactory portFactory;
    private final PaymentStoreRepository paymentStoreRepository;
    private final CompanyRepository companyRepository;

    public PaymentStoreManagementService(
            PaymentProviderPortFactory portFactory,
            PaymentStoreRepository paymentStoreRepository,
            CompanyRepository companyRepository
    ) {
        this.portFactory = portFactory;
        this.paymentStoreRepository = paymentStoreRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public PaymentStore execute(CreatePaymentStoreCommand command) {
        Company company = companyRepository.findById(command.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + command.companyId()));

        PaymentStore store = paymentStoreRepository.findByExternalReferenceAndProvider(command.externalReference(), command.provider())
                .orElseGet(() -> createDraftStore(command));

        if (store.hasProviderStore()) {
            return store;
        }

        PaymentStore createdStore = portFactory.store(command.provider()).createStore(store, toMerchantProfile(company));
        if (createdStore.providerStoreId() != null && !createdStore.providerStoreId().isBlank()) {
            paymentStoreRepository.updateProviderStoreId(store.id(), createdStore.providerStoreId());
            return store.withProviderStoreId(createdStore.providerStoreId());
        }

        return createdStore;
    }

    @Override
    public Optional<PaymentStore> findByExternalReference(PaymentProvider provider, String externalReference) {
        return paymentStoreRepository.findByExternalReferenceAndProvider(externalReference, provider);
    }

    @Override
    public Optional<PaymentStore> findCurrentTenant(PaymentProvider provider) {
        UUID tenantId = getCurrentTenantId();
        return companyRepository.findByTenantId(tenantId).stream()
                .map(company -> paymentStoreRepository.findByCompanyIdAndProvider(company.getId(), provider))
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public List<PaymentStoreView> execute(PaymentProvider provider) {
        return portFactory.store(provider).listStores();
    }

    private PaymentStore createDraftStore(CreatePaymentStoreCommand command) {
        paymentStoreRepository.save(new PaymentStore(
                null,
                command.companyId(),
                command.provider(),
                command.externalReference(),
                null
        ));

        return paymentStoreRepository.findByExternalReferenceAndProvider(command.externalReference(), command.provider())
                .orElseThrow(() -> new IllegalStateException("Store draft could not be reloaded after save"));
    }

    private MerchantProfile toMerchantProfile(Company company) {
        return new MerchantProfile(
                company.getName(),
                company.getStreetName(),
                company.getStreetNumber(),
                company.getCityName(),
                company.getStateName(),
                company.getLatitude(),
                company.getLongitude()
        );
    }

    private UUID getCurrentTenantId() {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new PaymentTenantContextException("Tenant context is required for this operation");
        }
        return tenantId;
    }
}
