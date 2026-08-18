package dev.kalles.client.support;

import dev.kalles.client.entity.Client;
import dev.kalles.client.repository.ClientRepository;
import dev.kalles.security.support.AbstractCompanyContextApiSupport;
import dev.kalles.testsupport.CsrfTestClient;
import dev.kalles.testsupport.CsrfTestClient.CsrfContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public abstract class AbstractClientApiSupport extends AbstractCompanyContextApiSupport {

    @Autowired
    protected ClientRepository clientRepository;

    protected void resetClientScenario() {
        resetScenarioData();
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    protected AuthContext authenticateTenantAdminWithCsrf() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);
        CsrfContext csrf = CsrfTestClient.fetch(port);
        return new AuthContext(authCookie, csrf.csrfCookie(), csrf.csrfToken());
    }

    protected RequestSpecification givenAuthenticated(AuthContext authContext, UUID companyId) {
        return given()
                .cookie("kalles_auth_token", authContext.authCookie())
                .cookie("XSRF-TOKEN", authContext.csrfCookie())
                .header("X-XSRF-TOKEN", authContext.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .contentType(ContentType.JSON);
    }

    protected Client seedClient(UUID companyId, String name, String cpf) {
        Client client = new Client();
        client.setCompanyId(companyId);
        client.setName(name);
        client.setCpf(cpf);
        client.setBirthDate(LocalDate.of(1990, 5, 20));
        client.setGender('F');
        client.setCodeCountry("55");
        client.setCellphone("11999999999");
        return clientRepository.save(client);
    }

    protected record AuthContext(String authCookie, String csrfCookie, String csrfToken) {
    }

}
