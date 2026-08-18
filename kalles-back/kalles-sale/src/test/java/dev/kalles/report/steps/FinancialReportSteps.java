package dev.kalles.report.steps;

import dev.kalles.testsupport.LocalHttpTestClient;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FinancialReportSteps extends FinancialReportCucumberSpringConfiguration {

    private AuthContext authContext;
    private Response response;

    @Before
    public void beforeScenario() {
        resetFinancialReportScenario();
        authContext = null;
        response = null;
    }

    @Dado("um admin autenticado para reports")
    public void givenAuthenticatedTenantAdmin() {
        authContext = authenticateTenantAdminWithCsrf();
    }

    @Dado("dados financeiros cadastrados em filiais diferentes")
    public void givenFinancialDataAcrossCompanies() {
        var productA = seedProduct(TENANT_ID, "Arroz", "ARZ-001");
        var locationA = seedLocation(companyAId, "Deposito A", "A-01");
        seedStockEntry(companyAId, productA, locationA, 10, "20.00", LocalDateTime.of(2026, 4, 10, 10, 0));
        seedCompletedSale(companyAId, "CX-A1-BDD", "op-a1-bdd", "500.00", LocalDateTime.of(2026, 4, 10, 9, 0));

        var productB = seedProduct(TENANT_ID, "Feijao", "FEJ-001");
        var locationB = seedLocation(companyBId, "Deposito B", "B-01");
        seedStockEntry(companyBId, productB, locationB, 5, "30.00", LocalDateTime.of(2026, 4, 10, 10, 0));
        seedCompletedSale(companyBId, "CX-B1-BDD", "op-b1-bdd", "900.00", LocalDateTime.of(2026, 4, 10, 9, 0));
    }

    @Quando("ele consultar o relatorio financeiro da filial ativa")
    public void whenFetchingFinancialReportForActiveCompany() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/reports/profit-vs-supplier-expenses?startDate=2026-04-01&endDate=2026-04-30",
                Map.of(
                        "Cookie", "kalles_auth_token=" + authContext.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );
    }

    @Quando("ele consultar o relatorio financeiro sem informar a filial ativa")
    public void whenFetchingFinancialReportWithoutCompanyHeader() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/reports/profit-vs-supplier-expenses?startDate=2026-04-01&endDate=2026-04-30",
                Map.of("Cookie", "kalles_auth_token=" + authContext.authCookie())
        );
    }

    @Entao("a resposta de report deve ter status HTTP {int}")
    public void thenReportResponseShouldHaveStatus(int statusCode) {
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("o relatorio deve retornar apenas os valores da filial ativa")
    public void thenReportShouldReturnOnlyActiveCompanyValues() {
        assertThat(response.jsonPath().getFloat("totalSales")).isEqualTo(500.00f);
        assertThat(response.jsonPath().getFloat("totalSupplierExpenses")).isEqualTo(200.00f);
        assertThat(response.jsonPath().getList("purchasedProducts")).hasSize(1);
        assertThat(response.jsonPath().getString("purchasedProducts[0].productInternalCode")).isEqualTo("ARZ-001");
    }

    @Entao("a resposta deve informar que a filial ativa e obrigatoria para reports")
    public void thenReportShouldInformCompanyIsRequired() {
        assertThat(response.jsonPath().getString("code")).isEqualTo("COMPANY_CONTEXT_REQUIRED");
    }
}
