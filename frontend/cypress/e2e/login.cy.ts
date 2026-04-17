describe("Login", () => {
  function stubCsrf() {
    cy.intercept("GET", "**/api/auth/csrf", {
      statusCode: 200,
      body: { token: "csrf-token" },
      headers: {
        "set-cookie": "XSRF-TOKEN=csrf-token; Path=/",
      },
    }).as("getCsrf");
  }

  it("redireciona admin para /caixas", () => {
    stubCsrf();
    cy.intercept("POST", "**/api/auth/login", {
      statusCode: 200,
      body: {},
    }).as("login");
    cy.intercept("GET", "**/api/auth/me", {
      statusCode: 200,
      body: {
        role: "ADMIN",
        tenantId: "123e4567-e89b-12d3-a456-426614174000",
        companyId: "123e4567-e89b-12d3-a456-426614174001",
      },
    }).as("getMe");
    cy.intercept("GET", "**/api/companies", {
      statusCode: 200,
      body: [
        {
          id: "123e4567-e89b-12d3-a456-426614174001",
          name: "Loja Matriz",
        },
      ],
    }).as("getCompanies");
    cy.intercept("GET", "**/api/cash-registers", {
      statusCode: 200,
      body: [],
    }).as("getRegisters");

    cy.visit("/login");

    cy.get("[data-testid='login-email']").type("admin@sistema.local");
    cy.get("[data-testid='login-password']").type("123456");
    cy.get("[data-testid='login-submit']").click();

    cy.wait("@login").its("request.body").should("deep.include", {
      email: "admin@sistema.local",
      password: "123456",
    });
    cy.url().should("include", "/caixas");
  });

  it("redireciona operador para /pdv quando existe sessao ativa no caixa", () => {
    stubCsrf();
    cy.intercept("POST", "**/api/auth/login", {
      statusCode: 200,
      body: {},
    }).as("login");
    cy.intercept("GET", "**/api/auth/me", {
      statusCode: 200,
      body: {
        role: "OPERATOR",
        tenantId: "123e4567-e89b-12d3-a456-426614174000",
        companyId: "123e4567-e89b-12d3-a456-426614174001",
      },
    }).as("getMe");
    cy.intercept("GET", "**/api/companies", {
      statusCode: 200,
      body: [
        {
          id: "123e4567-e89b-12d3-a456-426614174001",
          name: "Loja Matriz",
        },
      ],
    }).as("getCompanies");
    cy.intercept("GET", "**/api/cash-registers", {
      statusCode: 200,
      body: [
        {
          cashRegisterId: "123e4567-e89b-12d3-a456-426614174010",
          code: "CAIXA-01",
          description: "Caixa principal",
          active: true,
          hasActiveSession: true,
          activeSessionId: "123e4567-e89b-12d3-a456-426614174011",
          activeOperatorName: "Operador Caixa 01",
          initialAmount: 100,
          openedAt: "2026-04-15T09:00:00Z",
          paymentIntegrationConfigured: true,
          activeSessionCashOnlyOperation: false,
        },
      ],
    }).as("getRegisters");
    cy.intercept("GET", "**/api/cash-registers/operators", {
      statusCode: 200,
      body: [
        {
          id: "123e4567-e89b-12d3-a456-426614174012",
          name: "Operador Caixa 01",
          code: "OP001",
          permissionLevel: null,
        },
      ],
    }).as("getOperators");
    cy.intercept("POST", "**/api/sales", {
      statusCode: 200,
      body: {
        id: "123e4567-e89b-12d3-a456-426614174013",
        sessionToken: "123e4567-e89b-12d3-a456-426614174011",
        state: "OPEN",
        items: [],
        payments: [],
        subtotal: 0,
        total: 0,
        amountDue: 0,
        clientId: null,
        fidelityDiscountApplied: 0,
      },
    }).as("createSale");

    cy.visit("/login");

    cy.get("[data-testid='login-email']").type("operador@sistema.local");
    cy.get("[data-testid='login-password']").type("123456");
    cy.get("[data-testid='login-submit']").click();

    cy.url().should("include", "/pdv");
  });
});
