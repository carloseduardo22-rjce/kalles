package dev.kalles.fiscal.application.port.in;

import dev.kalles.fiscal.domain.FiscalCertificate;
import dev.kalles.fiscal.domain.FiscalConfiguration;
import dev.kalles.fiscal.domain.FiscalIssuerAddress;
import dev.kalles.fiscal.domain.FiscalIssuerProfile;
import dev.kalles.fiscal.domain.FiscalProductClassification;
import dev.kalles.fiscal.domain.FiscalReadiness;

import java.util.UUID;

public interface FiscalAdminUseCase {
    FiscalIssuerProfile saveIssuerProfile(SaveFiscalIssuerProfileCommand command);

    FiscalIssuerAddress saveIssuerAddress(SaveFiscalIssuerAddressCommand command);

    FiscalReadiness savePreparation(SaveFiscalPreparationCommand command);

    FiscalConfiguration saveConfiguration(SaveFiscalConfigurationCommand command);

    FiscalCertificate registerCertificate(RegisterFiscalCertificateCommand command);

    FiscalProductClassification saveProductClassification(SaveFiscalProductClassificationCommand command);

    FiscalReadiness getReadiness(UUID tenantId, UUID companyId);
}
