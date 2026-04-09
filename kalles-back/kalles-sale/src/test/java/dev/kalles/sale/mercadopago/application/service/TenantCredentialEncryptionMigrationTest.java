package dev.kalles.sale.mercadopago.application.service;

import dev.kalles.sale.support.LegacyMercadoPagoReferenceTest;
import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.MercadoPagoTenantConfigEntity; 
import dev.kalles.sale.mercadopago.adapter.out.persistence.repository.SpringDataTenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@DisplayName("TenantCredentialEncryptionMigration")
@LegacyMercadoPagoReferenceTest
class TenantCredentialEncryptionMigrationTest {

    @Test
    @DisplayName("Encrypts legacy tenant credentials persisted in plain text")  
    void encryptsLegacyTenantCredentialsPersistedInPlainText() {
        SpringDataTenantRepository tenantRepository = mock(SpringDataTenantRepository.class);
        TenantCredentialCipherService cipherService = spy(
                new TenantCredentialCipherService(new dev.kalles.sale.note.adapter.out.crypto.AesCryptoAdapter(), "test-secret"));

        MercadoPagoTenantConfigEntity tenant = new MercadoPagoTenantConfigEntity(
                UUID.randomUUID(),
                "plain-access-token",
                "plain-refresh-token",
                "plain-user-id");

        when(tenantRepository.findAll()).thenReturn(List.of(tenant));

        TenantCredentialEncryptionMigration migration =
                new TenantCredentialEncryptionMigration(tenantRepository, cipherService);

        migration.run(new DefaultApplicationArguments(new String[0]));

        verify(tenantRepository).saveAll(anyList());
        assertNotEquals("plain-access-token", tenant.getMpAccessToken());
        assertNotEquals("plain-refresh-token", tenant.getMpRefreshToken());
        assertNotEquals("plain-user-id", tenant.getMpUserId());
    }
}
