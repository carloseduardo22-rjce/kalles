package dev.kalles.payment.application.service;

import dev.kalles.core.entity.Company;
import dev.kalles.core.repository.CompanyRepository;
import dev.kalles.payment.application.port.in.CreatePaymentStoreUseCase;
import dev.kalles.payment.application.port.in.GetPaymentStoreStatusUseCase;
import dev.kalles.payment.application.port.in.ListPaymentStoresUseCase;
import dev.kalles.payment.application.port.in.command.CreatePaymentStoreCommand;
import dev.kalles.payment.application.port.out.PaymentStoreRepository;
import dev.kalles.payment.domain.MerchantProfile;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStore;
import dev.kalles.payment.domain.PaymentStoreView;
import dev.kalles.payment.exception.PaymentTenantContextException;
import dev.kalles.security.context.TenantContextHolder;
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
        UUID tenantId = getCurrentTenantId();
        Company company = companyRepository.findByIdAndTenantId(command.companyId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + command.companyId()));

        PaymentStore store = paymentStoreRepository.findByCompanyIdAndProvider(company.getId(), command.provider())
                .map(existing -> validateExternalReference(existing, command.externalReference()))
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
        UUID tenantId = getCurrentTenantId();
        return companyRepository.findByTenantId(tenantId).stream()
                .map(company -> paymentStoreRepository.findByCompanyIdAndProvider(company.getId(), provider))
                .flatMap(Optional::stream)
                .filter(store -> externalReference.equals(store.externalReference()))
                .findFirst();
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

        return paymentStoreRepository.findByCompanyIdAndProvider(command.companyId(), command.provider())
                .orElseThrow(() -> new IllegalStateException("Store draft could not be reloaded after save"));
    }

    private PaymentStore validateExternalReference(PaymentStore store, String externalReference) {
        if (!store.externalReference().equals(externalReference)) {
            throw new IllegalArgumentException("External reference does not match the informed company");
        }
        return store;
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
