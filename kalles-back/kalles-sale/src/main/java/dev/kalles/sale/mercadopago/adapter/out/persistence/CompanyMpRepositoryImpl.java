package dev.kalles.sale.mercadopago.adapter.out.persistence;

import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.MercadoPagoCompanyEntity;
import dev.kalles.sale.mercadopago.adapter.out.persistence.repository.SpringDataMercadoPagoCompanyRepository;
import dev.kalles.sale.mercadopago.domain.Company;
import dev.kalles.sale.mercadopago.port.CompanyMpRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CompanyMpRepositoryImpl implements CompanyMpRepository {

    private final SpringDataMercadoPagoCompanyRepository repository;

    public CompanyMpRepositoryImpl(SpringDataMercadoPagoCompanyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Company> findById(UUID companyId) {
        return repository.findById(companyId).map(this::toDomain);
    }

    @Override
    public Optional<Company> findByExternalId(String externalId) {
        return repository.findByExternalId(externalId).map(this::toDomain);
    }

    private Company toDomain(MercadoPagoCompanyEntity entity) {
        return new Company(
                entity.getId(),
                entity.getExternalId(),
                entity.getName(),
                entity.getStreetName(),
                entity.getStreetNumber(),
                entity.getCityName(),
                entity.getStateName(),
                entity.getLatitude() != null ? entity.getLatitude() : 0.0,
                entity.getLongitude() != null ? entity.getLongitude() : 0.0,
                entity.getMpStoreId(),
                entity.getTenantId());
    }

    @Override
    public void save(Company company) {
        MercadoPagoCompanyEntity entity = company.id() != null ? 
                repository.findById(company.id()).orElse(new MercadoPagoCompanyEntity()) : 
                new MercadoPagoCompanyEntity();

        entity.setId(company.id());
        entity.setExternalId(company.externalId());
        entity.setName(company.name());
        entity.setStreetName(company.streetName());
        entity.setStreetNumber(company.streetNumber());
        entity.setCityName(company.cityName());
        entity.setStateName(company.stateName());
        entity.setLatitude(company.latitude());
        entity.setLongitude(company.longitude());
        entity.setMpStoreId(company.mpStoreId());
        entity.setTenantId(company.tenantId());
        
        repository.save(entity);
    }

    @Override
    public void saveStoreId(UUID companyId, Long storeId) {
        repository.findById(companyId).ifPresent(entity -> {
            entity.setMpStoreId(storeId);
            repository.save(entity);
        });
    }
}
