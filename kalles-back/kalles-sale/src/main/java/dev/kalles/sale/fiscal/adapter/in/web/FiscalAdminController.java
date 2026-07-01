package dev.kalles.sale.fiscal.adapter.in.web;

import dev.kalles.sale.fiscal.adapter.in.web.dto.FiscalCertificateResponse;
import dev.kalles.sale.fiscal.adapter.in.web.dto.FiscalConfigurationResponse;
import dev.kalles.sale.fiscal.adapter.in.web.dto.FiscalIssuerAddressResponse;
import dev.kalles.sale.fiscal.adapter.in.web.dto.FiscalIssuerProfileResponse;
import dev.kalles.sale.fiscal.adapter.in.web.dto.FiscalProductClassificationResponse;
import dev.kalles.sale.fiscal.adapter.in.web.dto.FiscalReadinessResponse;
import dev.kalles.sale.fiscal.adapter.in.web.dto.RegisterFiscalCertificateRequest;
import dev.kalles.sale.fiscal.adapter.in.web.dto.SaveFiscalConfigurationRequest;
import dev.kalles.sale.fiscal.adapter.in.web.dto.SaveFiscalIssuerAddressRequest;
import dev.kalles.sale.fiscal.adapter.in.web.dto.SaveFiscalIssuerProfileRequest;
import dev.kalles.sale.fiscal.adapter.in.web.dto.SaveFiscalPreparationRequest;
import dev.kalles.sale.fiscal.adapter.in.web.dto.SaveFiscalProductClassificationRequest;
import dev.kalles.sale.fiscal.application.port.in.FiscalAdminUseCase;
import dev.kalles.sale.fiscal.application.port.in.RegisterFiscalCertificateCommand;
import dev.kalles.sale.fiscal.application.port.in.SaveFiscalConfigurationCommand;
import dev.kalles.sale.fiscal.application.port.in.SaveFiscalIssuerAddressCommand;
import dev.kalles.sale.fiscal.application.port.in.SaveFiscalIssuerProfileCommand;
import dev.kalles.sale.fiscal.application.port.in.SaveFiscalPreparationCommand;
import dev.kalles.sale.fiscal.application.port.in.SaveFiscalProductClassificationCommand;
import dev.kalles.sale.security.context.CompanyContextHolder;
import dev.kalles.sale.security.context.TenantContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fiscal")
@RequiredArgsConstructor
public class FiscalAdminController {

    private final FiscalAdminUseCase fiscalAdminUseCase;

    @PostMapping("/issuer-profile")
    @ResponseStatus(HttpStatus.CREATED)
    public FiscalIssuerProfileResponse saveIssuerProfile(@Valid @RequestBody SaveFiscalIssuerProfileRequest request) {
        return FiscalIssuerProfileResponse.from(fiscalAdminUseCase.saveIssuerProfile(new SaveFiscalIssuerProfileCommand(
                TenantContextHolder.getTenantId(),
                CompanyContextHolder.getCompanyId(),
                request.cnpj(),
                request.legalName(),
                request.tradeName(),
                request.stateRegistration(),
                request.taxRegime(),
                request.cnae()
        )));
    }

    @PostMapping("/issuer-address")
    @ResponseStatus(HttpStatus.CREATED)
    public FiscalIssuerAddressResponse saveIssuerAddress(@Valid @RequestBody SaveFiscalIssuerAddressRequest request) {
        return FiscalIssuerAddressResponse.from(fiscalAdminUseCase.saveIssuerAddress(new SaveFiscalIssuerAddressCommand(
                TenantContextHolder.getTenantId(),
                CompanyContextHolder.getCompanyId(),
                request.zipCode(),
                request.stateCode(),
                request.stateIbgeCode(),
                request.cityName(),
                request.cityIbgeCode(),
                request.district(),
                request.street(),
                request.number(),
                request.complement(),
                request.countryName(),
                request.countryCode()
        )));
    }

    @GetMapping("/readiness")
    public FiscalReadinessResponse getReadiness() {
        return FiscalReadinessResponse.from(fiscalAdminUseCase.getReadiness(
                TenantContextHolder.getTenantId(),
                CompanyContextHolder.getCompanyId()
        ));
    }

    @PostMapping("/preparation")
    @ResponseStatus(HttpStatus.CREATED)
    public FiscalReadinessResponse savePreparation(@Valid @RequestBody SaveFiscalPreparationRequest request) {
        return FiscalReadinessResponse.from(fiscalAdminUseCase.savePreparation(new SaveFiscalPreparationCommand(
                TenantContextHolder.getTenantId(),
                CompanyContextHolder.getCompanyId(),
                request.cnpj(),
                request.legalName(),
                request.tradeName(),
                request.stateRegistration(),
                request.taxRegime(),
                request.cnae(),
                request.zipCode(),
                request.stateCode(),
                request.stateIbgeCode(),
                request.cityName(),
                request.cityIbgeCode(),
                request.district(),
                request.street(),
                request.number(),
                request.complement(),
                request.countryName(),
                request.countryCode(),
                request.model(),
                request.environment(),
                request.cscId(),
                request.cscToken(),
                request.series(),
                request.nextNumber(),
                request.certificateBase64(),
                request.certificatePassword(),
                request.certificateExpiresAt()
        )));
    }

    @PostMapping("/configurations")
    @ResponseStatus(HttpStatus.CREATED)
    public FiscalConfigurationResponse saveConfiguration(@Valid @RequestBody SaveFiscalConfigurationRequest request) {
        return FiscalConfigurationResponse.from(fiscalAdminUseCase.saveConfiguration(new SaveFiscalConfigurationCommand(
                TenantContextHolder.getTenantId(),
                CompanyContextHolder.getCompanyId(),
                request.model(),
                request.environment(),
                request.stateCode(),
                request.cscId(),
                request.cscToken(),
                request.series(),
                request.nextNumber()
        )));
    }

    @PostMapping("/certificates")
    @ResponseStatus(HttpStatus.CREATED)
    public FiscalCertificateResponse registerCertificate(@Valid @RequestBody RegisterFiscalCertificateRequest request) {
        return FiscalCertificateResponse.from(fiscalAdminUseCase.registerCertificate(new RegisterFiscalCertificateCommand(
                TenantContextHolder.getTenantId(),
                CompanyContextHolder.getCompanyId(),
                request.certificateBase64(),
                request.password(),
                request.expiresAt()
        )));
    }

    @PostMapping("/product-classifications")
    @ResponseStatus(HttpStatus.CREATED)
    public FiscalProductClassificationResponse saveProductClassification(@Valid @RequestBody SaveFiscalProductClassificationRequest request) {
        return FiscalProductClassificationResponse.from(fiscalAdminUseCase.saveProductClassification(new SaveFiscalProductClassificationCommand(
                TenantContextHolder.getTenantId(),
                CompanyContextHolder.getCompanyId(),
                request.productId(),
                request.ncm(),
                request.cest(),
                request.cfop(),
                request.cfopSale(),
                request.origin(),
                request.csosn(),
                request.cst(),
                request.unit(),
                request.gtin()
        )));
    }
}
