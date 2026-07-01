/// <reference types="cypress" />

describe("Selecao global de loja e pagamentos visiveis", () => {
  const companyAId = "123e4567-e89b-12d3-a456-426614174301";
  const companyBId = "123e4567-e89b-12d3-a456-426614174302";

  beforeEach(() => {
    cy.intercept("GET", "**/api/auth/me", {
      statusCode: 200,
      body: {
        role: "ADMIN",
        tenantId: "123e4567-e89b-12d3-a456-426614174300",
        companyId: null,
      },
    });

    cy.intercept("GET", "**/api/companies", {
      statusCode: 200,
      body: [
        { id: companyAId, name: "Loja Matriz" },
        { id: companyBId, name: "Filial Centro" },
      ],
    }).as("getCompanies");

    cy.intercept("GET", "**/api/cash-registers", {
      statusCode: 200,
      body: [
        {
          cashRegisterId: "123e4567-e89b-12d3-a456-426614174303",
          code: "CX-01",
          description: "Caixa principal",
          active: true,
          hasActiveSession: false,
          activeSessionId: null,
          activeOperatorName: null,
          initialAmount: null,
          openedAt: null,
          paymentIntegrationConfigured: true,
          activeSessionCashOnlyOperation: null,
        },
      ],
    });

    cy.intercept("GET", "**/api/payment-stores?provider=MERCADO_PAGO", {
      statusCode: 200,
      body: [],
    });

    cy.intercept("GET", "**/api/payment-points?provider=MERCADO_PAGO", {
      statusCode: 200,
      body: [],
    });

    cy.setCookie("kalles_auth_token", "test-auth-token");
  });

  it("mantem seletor de loja no shell e exibe apenas Mercado Pago nas telas de pagamento", () => {
    cy.visit("/admin/pagamentos", {
      onBeforeLoad(win) {
        win.localStorage.setItem(
          "kalles:onboarding:payments:v1:completed",
          "true",
        );
        win.sessionStorage.setItem("@kalles:activeCompanyId", companyAId);
      },
    });

    cy.wait("@getCompanies");
    cy.contains("Loja ativa").should("be.visible");
    cy.contains("Loja Matriz").should("be.visible");
    cy.contains("Mercado Pago").should("be.visible");
    cy.contains("Stone").should("not.exist");
  });

  it("permite trocar a loja ativa fora da tela de lojas", () => {
    cy.visit("/admin/produtos", {
      onBeforeLoad(win) {
        win.localStorage.setItem(
          "kalles:onboarding:products:v1:completed",
          "true",
        );
        win.sessionStorage.setItem("@kalles:activeCompanyId", companyAId);
      },
    });

    cy.wait("@getCompanies");
    cy.contains("Loja ativa").should("be.visible");
    cy.contains("Loja Matriz").should("be.visible");

    cy.get("[data-onboarding='company-switcher']").click();
    cy.contains("Filial Centro").click();

    cy.window().then((win) => {
      expect(win.sessionStorage.getItem("@kalles:activeCompanyId")).to.eq(
        companyBId,
      );
    });
  });
});
