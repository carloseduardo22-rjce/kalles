describe("Gerenciamento de caixas", () => {
  function stubAdminContext() {
    cy.intercept("GET", "**/api/auth/me", {
      statusCode: 200,
      body: {
        role: "ADMIN",
        tenantId: "123e4567-e89b-12d3-a456-426614174000",
        companyId: "123e4567-e89b-12d3-a456-426614174001",
      },
    }).as("getMe");
  }

  function stubCsrf() {
    cy.intercept("GET", "**/api/auth/csrf", {
      statusCode: 200,
      body: { token: "csrf-token" },
      headers: {
        "set-cookie": ["XSRF-TOKEN=csrf-token; Path=/"],
      },
    }).as("getCsrf");
  }

  it("permite abrir o caixa em modo somente dinheiro a partir da tela de caixas", () => {
    let registerLoads = 0;

    stubAdminContext();
    stubCsrf();

    cy.intercept("GET", "**/api/cash-registers", (req) => {
      registerLoads += 1;
      req.reply({
        statusCode: 200,
        body:
          registerLoads === 1
            ? [
                {
                  cashRegisterId: "123e4567-e89b-12d3-a456-426614174020",
                  code: "CAIXA-01",
                  description: "Caixa principal",
                  active: true,
                  hasActiveSession: false,
                  activeSessionId: null,
                  activeOperatorName: null,
                  initialAmount: null,
                  openedAt: null,
                  paymentIntegrationConfigured: false,
                  activeSessionCashOnlyOperation: null,
                },
              ]
            : [
                {
                  cashRegisterId: "123e4567-e89b-12d3-a456-426614174020",
                  code: "CAIXA-01",
                  description: "Caixa principal",
                  active: true,
                  hasActiveSession: true,
                  activeSessionId: "123e4567-e89b-12d3-a456-426614174021",
                  activeOperatorName: "Operador Caixa 01",
                  initialAmount: 50,
                  openedAt: "2026-04-15T09:00:00Z",
                  paymentIntegrationConfigured: false,
                  activeSessionCashOnlyOperation: true,
                },
              ],
      });
    }).as("getRegisters");

    cy.intercept("GET", "**/api/cash-registers/operators", {
      statusCode: 200,
      body: [
        {
          id: "123e4567-e89b-12d3-a456-426614174022",
          name: "Operador Caixa 01",
          code: "OP001",
          permissionLevel: null,
        },
      ],
    }).as("getOperators");

    cy.intercept("POST", "**/api/cash-register-sessions/open", (req) => {
      expect(req.body).to.deep.include({
        cashRegisterCode: "CAIXA-01",
        operatorCode: "OP001",
        allowCashOnlyOperation: true,
      });

      req.reply({
        statusCode: 201,
        body: {
          sessionId: "123e4567-e89b-12d3-a456-426614174021",
          operatorId: "123e4567-e89b-12d3-a456-426614174022",
          cashRegisterCode: "CAIXA-01",
          operatorName: "Operador Caixa 01",
          initialAmount: 50,
          openedAt: "2026-04-15T09:00:00Z",
          status: "OPEN",
          cashOnlyOperation: true,
        },
      });
    }).as("openSession");

    cy.visit("/caixas");

    cy.get("[data-testid='open-session-CAIXA-01']").click();
    cy.get("[data-testid='cash-only-open-warning']").should("be.visible");
    cy.get("#operator-select").click();
    cy.contains("[role='option']", "Operador Caixa 01").click();
    cy.get("#initial-amount").type("50");
    cy.get("[data-testid='open-session-confirm']").click();

    cy.wait("@openSession");
    cy.contains("Sessão em modo somente dinheiro").should("be.visible");
  });
});
