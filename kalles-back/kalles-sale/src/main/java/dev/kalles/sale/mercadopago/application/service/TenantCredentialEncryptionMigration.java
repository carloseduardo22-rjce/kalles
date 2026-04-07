package dev.kalles.sale.mercadopago.application.service;

import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.MercadoPagoTenantConfigEntity;
import dev.kalles.sale.mercadopago.adapter.out.persistence.repository.SpringDataTenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class TenantCredentialEncryptionMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TenantCredentialEncryptionMigration.class);

    private final SpringDataTenantRepository tenantRepository;
    private final TenantCredentialCipherService cipherService;

    public TenantCredentialEncryptionMigration(
            SpringDataTenantRepository tenantRepository,
            TenantCredentialCipherService cipherService) {
        this.tenantRepository = tenantRepository;
        this.cipherService = cipherService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<MercadoPagoTenantConfigEntity> updatedTenants = new ArrayList<>();

        for (MercadoPagoTenantConfigEntity tenant : tenantRepository.findAll()) {
            String encryptedAccessToken = cipherService.encrypt(tenant.getMpAccessToken());
            String encryptedRefreshToken = cipherService.encrypt(tenant.getMpRefreshToken());
            String encryptedUserId = cipherService.encrypt(tenant.getMpUserId());

            boolean changed = !Objects.equals(encryptedAccessToken, tenant.getMpAccessToken())
                    || !Objects.equals(encryptedRefreshToken, tenant.getMpRefreshToken())
                    || !Objects.equals(encryptedUserId, tenant.getMpUserId());

            if (!changed) {
                continue;
            }

            tenant.setMpAccessToken(encryptedAccessToken);
            tenant.setMpRefreshToken(encryptedRefreshToken);
            tenant.setMpUserId(encryptedUserId);
            updatedTenants.add(tenant);
        }

        if (!updatedTenants.isEmpty()) {
            tenantRepository.saveAll(updatedTenants);
            log.info("Encrypted Mercado Pago tenant credentials for {} tenant(s).", updatedTenants.size());
        }
    }
}
