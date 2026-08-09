package dev.kalles.fiscal.adapter.out.persistence;

import dev.kalles.fiscal.adapter.out.persistence.entity.FiscalIssuerAddressEntity;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalIssuerAddressRepository;
import dev.kalles.fiscal.application.port.out.FiscalIssuerAddressRepository;
import dev.kalles.fiscal.domain.FiscalIssuerAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FiscalIssuerAddressPersistenceAdapter implements FiscalIssuerAddressRepository {

    private final SpringDataFiscalIssuerAddressRepository repository;

    @Override
    public Optional<FiscalIssuerAddress> findByCompany(UUID tenantId, UUID companyId) {
        return repository.findByTenantIdAndCompanyId(tenantId, companyId)
                .map(FiscalIssuerAddressEntity::toDomain);
    }

    @Override
    public FiscalIssuerAddress save(FiscalIssuerAddress address) {
        FiscalIssuerAddressEntity entity = repository.findByTenantIdAndCompanyId(address.tenantId(), address.companyId())
                .orElseGet(FiscalIssuerAddressEntity::new);
        entity.setTenantId(address.tenantId());
        entity.setCompanyId(address.companyId());
        entity.setZipCode(address.zipCode());
        entity.setStateCode(address.stateCode());
        entity.setStateIbgeCode(address.stateIbgeCode());
        entity.setCityName(address.cityName());
        entity.setCityIbgeCode(address.cityIbgeCode());
        entity.setDistrict(address.district());
        entity.setStreet(address.street());
        entity.setNumber(address.number());
        entity.setComplement(address.complement());
        entity.setCountryName(address.countryName());
        entity.setCountryCode(address.countryCode());
        return repository.save(entity).toDomain();
    }
}
