package dev.kalles.security.support;

import dev.kalles.KallesSaleApplication;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.company.entity.Company;
import dev.kalles.company.entity.Tenant;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.company.repository.TenantRepository;
import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.security.repository.AccountRepository;
import dev.kalles.testsupport.DatabaseCleaner;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KallesSaleApplication.class)
public abstract class AbstractCompanyContextApiSupport extends AbstractSecurityApiContainerSupport {

    protected static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174210");
    protected static final UUID OTHER_TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174211");
    protected static final String TENANT_ADMIN_EMAIL = "tenant.admin@sistema.local";
    protected static final String BOUND_ADMIN_EMAIL = "bound.admin@sistema.local";
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
    protected AccountRepository accountRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected UUID companyAId;
    protected UUID companyBId;
    protected UUID foreignCompanyId;

    protected void resetScenarioData() {
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        databaseCleaner().clean();

        tenantRepository.save(new Tenant(TENANT_ID, "Tenant Contexto"));
        tenantRepository.save(new Tenant(OTHER_TENANT_ID, "Tenant Externo"));

        companyAId = companyRepository.save(new Company(
                null,
                "Loja A",
                TENANT_ID,
                null,
                null,
                null,
                null,
                null,
                null
        )).getId();

        companyBId = companyRepository.save(new Company(
                null,
                "Loja B",
                TENANT_ID,
                null,
                null,
                null,
                null,
                null,
                null
        )).getId();

        foreignCompanyId = companyRepository.save(new Company(
                null,
                "Loja Externa",
                OTHER_TENANT_ID,
                null,
                null,
                null,
                null,
                null,
                null
        )).getId();

        cashRegisterRepository.save(new CashRegister("CX-A1", "Caixa loja A", companyAId));
        cashRegisterRepository.save(new CashRegister("CX-B1", "Caixa loja B", companyBId));
        cashRegisterRepository.save(new CashRegister("CX-X1", "Caixa loja externa", foreignCompanyId));

        Account tenantAdmin = new Account(
                TENANT_ID,
                "Admin Tenant",
                TENANT_ADMIN_EMAIL,
                passwordEncoder.encode(DEFAULT_PASSWORD),
                AccountRole.ADMIN
        );
        tenantAdmin.setVerified(true);
        accountRepository.save(tenantAdmin);

        Account boundAdmin = new Account(
                TENANT_ID,
                "Admin Vinculado",
                BOUND_ADMIN_EMAIL,
                passwordEncoder.encode(DEFAULT_PASSWORD),
                AccountRole.ADMIN
        );
        boundAdmin.setCompanyId(companyAId);
        boundAdmin.setVerified(true);
        accountRepository.save(boundAdmin);
    }

    protected String loginAndExtractAuthCookie(String email) {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", email,
                        "password", DEFAULT_PASSWORD
                ))
                .when()
                .post("/api/auth/login");

        response.then().statusCode(200);
        return response.getCookie("kalles_auth_token");
    }
}
