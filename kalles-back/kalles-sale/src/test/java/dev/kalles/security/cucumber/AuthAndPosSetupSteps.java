package dev.kalles.security.cucumber;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.enums.PermissionLevel;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.company.entity.Company;
import dev.kalles.company.entity.Tenant;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.company.repository.TenantRepository;
import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.security.repository.AccountRepository;
import dev.kalles.testsupport.DatabaseCleaner;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthAndPosSetupSteps {

    private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @LocalServerPort
    private int port;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CashRegisterRepository cashRegisterRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private Response response;
    private UUID companyId;
    private String currentAuthCookie;

    @Before("@security")
    public void setUpScenario() {
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        databaseCleaner.clean();

        response = null;
        companyId = null;
        currentAuthCookie = null;
    }

    @Dado("que o seed {string} carregou a tenant {string}")
    public void givenSeedLoadedTenant(String seedFile, String tenantId) {
        assertThat(seedFile).isEqualTo("R__seed_data.sql");
        tenantRepository.save(new Tenant(UUID.fromString(tenantId), "Conta de Teste Kalles"));
    }

    @Dado("existe a empresa {string}")
    public void givenCompanyExists(String companyName) {
        Company company = companyRepository.save(new Company(
                null,
                companyName,
                TENANT_ID,
                null,
                null,
                null,
                null,
                null,
                null
        ));
        companyId = company.getId();
    }

    @Dado("existe o caixa {string} para a empresa {string}")
    public void givenCashRegisterExists(String cashRegisterCode, String companyName) {
        assertThat(companyName).isEqualTo("Loja Matriz");
        cashRegisterRepository.save(new CashRegister(cashRegisterCode, "Caixa principal", companyId));
    }

    @Dado("existe a conta web administradora {string} com senha {string}")
    public void givenAdminAccountExists(String email, String password) {
        accountRepository.save(newAccount("Administrador", email, password, AccountRole.ADMIN));
    }

    @Dado("existe a conta web operadora {string} com senha {string} vinculada a empresa {string}")
    public void givenOperatorAccountExists(String email, String password, String companyName) {
        assertThat(companyName).isEqualTo("Loja Matriz");
        accountRepository.save(newAccount("Operador Caixa 01", email, password, AccountRole.OPERATOR));

        Operator operator = new Operator("Operador Caixa 01", "OP-CAIXA-01");
        operator.setCompanyId(companyId);
        operator.setPermissionLevel(PermissionLevel.BASIC);
        operatorRepository.save(operator);
    }

    @Dado("que o dispositivo nao possui o cookie {string}")
    public void givenDeviceHasNoCookie(String cookieName) {
        assertThat(cookieName).isEqualTo("kalles_pos_token");
    }

    @Quando("eu enviar o login com email {string} e senha {string}")
    public void whenISendLogin(String email, String password) {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .when()
                .post("/api/auth/login");
        currentAuthCookie = response.getCookie("kalles_auth_token");
    }

    @Entao("a resposta de login deve ter status HTTP {int}")
    public void thenLoginResponseHasStatus(int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a resposta deve definir o cookie de autenticacao {string}")
    public void thenResponseDefinesAuthCookie(String cookieName) {
        assertThat(cookieName).isEqualTo("kalles_auth_token");
        assertThat(currentAuthCookie).isNotBlank();
        assertThat(response.getHeaders().getValues(HttpHeaders.SET_COOKIE))
                .anyMatch(header -> header.contains("kalles_auth_token="));
    }

    @Entao("o login nao deve exigir pareamento de terminal para o perfil administrador")
    public void thenAdminLoginShouldNotRequirePairing() {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(currentAuthCookie).isNotBlank();
    }

    private Account newAccount(String name, String email, String rawPassword, AccountRole role) {
        Account account = new Account(TENANT_ID, name, email, passwordEncoder.encode(rawPassword), role);
        account.setCompanyId(companyId);
        account.setVerified(true);
        return account;
    }
}
