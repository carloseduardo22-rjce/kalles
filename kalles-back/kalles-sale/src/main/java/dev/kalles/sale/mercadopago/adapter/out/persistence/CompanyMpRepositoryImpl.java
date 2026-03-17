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
        return repository.findById(companyId).map(entity -> new Company(
                entity.getId(),
                entity.getName(),
                entity.getStreetName(),
                entity.getStreetNumber(),
                entity.getCityName(),
                entity.getStateName(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getMpStoreId()));
    }

    @Override
    public void save(Company company) {
        MercadoPagoCompanyEntity entity = repository.findById(company.id())
                .orElse(new MercadoPagoCompanyEntity(company.id(), company.name(), company.streetName(),
                        company.streetNumber(), company.cityName(), company.stateName(), company.latitude(),
                        company.longitude(), company.mpStoreId()));

        entity.setId(company.id());
        entity.setName(company.name());
        entity.setStreetName(company.streetName());
        entity.setStreetNumber(company.streetNumber());
        entity.setCityName(company.cityName());
        entity.setStateName(company.stateName());
        entity.setLatitude(company.latitude());
        entity.setLongitude(company.longitude());
        entity.setMpStoreId(company.mpStoreId());

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
