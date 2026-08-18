package dev.kalles.cashregister.support;

import dev.kalles.KallesSaleApplication;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.enums.PermissionLevel;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.company.entity.Company;
import dev.kalles.company.entity.Tenant;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.company.repository.TenantRepository;
import dev.kalles.payment.adapter.out.mercadopago.persistence.entity.MercadoPagoPointEntity;
import dev.kalles.payment.adapter.out.mercadopago.persistence.repository.MercadoPagoPointJpaRepository;
import dev.kalles.sale.repository.SaleAuditEventRepository;
import dev.kalles.security.entity.Account;
import dev.kalles.security.entity.PosDeviceSession;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.security.repository.AccountRepository;
import dev.kalles.security.repository.PosDeviceSessionRepository;
import dev.kalles.security.support.AbstractSecurityApiContainerSupport;
import dev.kalles.testsupport.CsrfTestClient;
import dev.kalles.testsupport.CsrfTestClient.CsrfContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KallesSaleApplication.class)
public abstract class AbstractCashRegisterApiSupport extends AbstractSecurityApiContainerSupport {

    protected static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174111");
    protected static final String CASH_REGISTER_CODE = "CAIXA-01";
    protected static final String OPERATOR_CODE = "OP001";
    protected static final String OPERATOR_EMAIL = "operador.caixa01@sistema.local";
    protected static final String DEFAULT_PASSWORD = "123456";

    @LocalServerPort
    protected int port;

    @Autowired
    protected TenantRepository tenantRepository;

    @Autowired
    protected CompanyRepository companyRepository;

    @Autowired
    protected CashRegisterRepository cashRegisterRepository;

    @Autowired
    protected CashRegisterSessionRepository cashRegisterSessionRepository;

    @Autowired
    protected OperatorRepository operatorRepository;

    @Autowired
    protected SaleAuditEventRepository saleAuditEventRepository;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected PosDeviceSessionRepository posDeviceSessionRepository;

    @Autowired
    protected MercadoPagoPointJpaRepository mercadoPagoPointRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected UUID companyId;
    protected UUID cashRegisterId;

    protected void resetScenarioData() {
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        databaseCleaner().clean();

        tenantRepository.save(new Tenant(TENANT_ID, "Tenant teste caixa"));
        companyId = companyRepository.save(new Company(
                null,
                "Loja teste caixa",
                TENANT_ID,
                null,
                null,
                null,
                null,
                null,
                null
        )).getId();

        CashRegister cashRegister = cashRegisterRepository.save(
                new CashRegister(CASH_REGISTER_CODE, "Caixa principal", companyId)
        );
        cashRegisterId = cashRegister.getId();

        Operator operator = new Operator("Operador Caixa 01", OPERATOR_CODE);
        operator.setCompanyId(companyId);
        operatorRepository.save(operator);

        Account account = new Account(TENANT_ID, "Operador Caixa 01", OPERATOR_EMAIL,
                passwordEncoder.encode(DEFAULT_PASSWORD), AccountRole.OPERATOR);
        account.setCompanyId(companyId);
        account.setVerified(true);
        accountRepository.save(account);
    }

    protected void configurePaymentIntegration(boolean configured) {
        mercadoPagoPointRepository.deleteAll();
        if (configured) {
            MercadoPagoPointEntity point = new MercadoPagoPointEntity();
            point.setExternalReference("MP-" + CASH_REGISTER_CODE);
            point.setCashRegisterId(cashRegisterId);
            point.setProviderPointId(987654321L);
            mercadoPagoPointRepository.save(point);
        }
    }

    protected AuthContext authenticateOperator() {
        String pairingToken = seedPairingToken();

        Response loginResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_pos_token", pairingToken)
                .body(Map.of(
                        "email", OPERATOR_EMAIL,
                        "password", DEFAULT_PASSWORD
                ))
                .when()
                .post("/api/auth/login");

        loginResponse.then().statusCode(200);

        String authCookie = loginResponse.getCookie("kalles_auth_token");

        CsrfContext csrfContext = CsrfTestClient.fetch(port);

        return new AuthContext(
                authCookie,
                csrfContext.csrfCookie(),
                csrfContext.csrfToken()
        );
    }

    protected String seedPairingToken() {
        String token = "pairing-token-" + cashRegisterId;
        PosDeviceSession session = new PosDeviceSession();
        session.setCompanyId(companyId);
        session.setPosId(cashRegisterId);
        session.setToken(token);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusDays(30));
        session.setActive(true);
        posDeviceSessionRepository.save(session);
        return token;
    }

    protected UUID seedCompany(String name) {
        return companyRepository.save(new Company(
                null,
                name,
                TENANT_ID,
                null,
                null,
                null,
                null,
                null,
                null
        )).getId();
    }

    protected UUID seedCashRegister(UUID targetCompanyId, String code, String description) {
        return cashRegisterRepository.save(new CashRegister(code, description, targetCompanyId)).getId();
    }

    protected void seedOperator(UUID targetCompanyId, String name, String code, PermissionLevel permissionLevel) {
        Operator operator = new Operator(name, code);
        operator.setCompanyId(targetCompanyId);
        operator.setPermissionLevel(permissionLevel);
        operatorRepository.save(operator);
    }

    protected UUID seedSession(UUID targetCompanyId, String registerCode, String operatorCode, BigDecimal initialAmount) {
        CashRegister cashRegister = cashRegisterRepository.findByCodeAndCompanyId(registerCode, targetCompanyId)
                .orElseThrow();
        Operator operator = operatorRepository.findByCodeAndCompanyId(operatorCode, targetCompanyId)
                .orElseThrow();
        return cashRegisterSessionRepository.save(
                CashRegisterSession.open(cashRegister, operator, initialAmount)
        ).getId();
    }

    protected record AuthContext(String authCookie, String csrfCookie, String csrfToken) {
    }

}
