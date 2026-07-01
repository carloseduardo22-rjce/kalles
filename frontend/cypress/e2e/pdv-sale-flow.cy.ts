describe("Fluxo principal do PDV", () => {
  const sessionToken = "123e4567-e89b-12d3-a456-426614174201";
  const operatorId = "123e4567-e89b-12d3-a456-426614174202";
  const companyId = "123e4567-e89b-12d3-a456-426614174203";

  function stubSaleFlow() {
    cy.intercept("GET", "**/api/auth/me", {
      statusCode: 200,
      body: {
        role: "OPERATOR",
        tenantId: "123e4567-e89b-12d3-a456-426614174000",
        companyId,
      },
    }).as("getMe");

    cy.intercept("GET", "**/api/companies", {
      statusCode: 200,
      body: [
        {
          id: companyId,
          name: "Loja Matriz",
        },
      ],
    }).as("getCompanies");

    cy.intercept("GET", "**/api/auth/csrf", {
      statusCode: 200,
      body: { token: "csrf-token" },
      headers: {
        "set-cookie": "XSRF-TOKEN=csrf-token; Path=/",
      },
    }).as("getCsrf");

    cy.intercept("GET", "**/api/cash-registers", {
      statusCode: 200,
      body: [
        {
          cashRegisterId: "123e4567-e89b-12d3-a456-426614174210",
          code: "CAIXA-01",
          description: "Caixa principal",
          active: true,
          hasActiveSession: true,
          activeSessionId: sessionToken,
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
          id: operatorId,
          name: "Operador Caixa 01",
          code: "OP001",
          permissionLevel: "SUPERVISOR",
        },
      ],
    }).as("getOperators");

    cy.intercept("GET", `**/api/sales/${sessionToken}`, {
      statusCode: 404,
      body: {
        detail: "Nenhuma venda em andamento ou pendente de conclusao para esta sessao",
      },
    }).as("getCurrentSale");

    cy.intercept("POST", `**/api/sales/${sessionToken}`, {
      statusCode: 201,
      body: {
        id: "123e4567-e89b-12d3-a456-426614174220",
        sessionToken,
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

    cy.intercept("POST", `**/api/sales/${sessionToken}/items`, {
      statusCode: 200,
      body: {
        id: "123e4567-e89b-12d3-a456-426614174220",
        sessionToken,
        state: "OPEN",
        items: [
          {
            id: "123e4567-e89b-12d3-a456-426614174221",
            productName: "Produto PDV",
            productInternalCode: "SKU-001",
            unitPrice: 30,
            quantity: 1,
            discount: 0,
            subtotal: 30,
          },
        ],
        payments: [],
        subtotal: 30,
        total: 30,
        amountDue: 30,
        clientId: null,
        fidelityDiscountApplied: 0,
      },
    }).as("addItem");

    cy.intercept("PATCH", `**/api/sales/${sessionToken}/items/discount`, {
      statusCode: 204,
      body: "",
    }).as("applyDiscount");

    cy.intercept("POST", `**/api/sales/${sessionToken}/payments`, {
      statusCode: 200,
      body: {
        id: "123e4567-e89b-12d3-a456-426614174220",
        sessionToken,
        state: "PAID",
        items: [
          {
            id: "123e4567-e89b-12d3-a456-426614174221",
            productName: "Produto PDV",
            productInternalCode: "SKU-001",
            unitPrice: 30,
            quantity: 1,
            discount: 5,
            subtotal: 25,
          },
        ],
        payments: [
          {
            id: "123e4567-e89b-12d3-a456-426614174222",
            method: "CASH",
            amount: 30,
            changeAmount: 5,
            confirmed: true,
            transactionId: "cash-1",
            createdAt: "2026-04-15T09:05:00Z",
          },
        ],
        subtotal: 25,
        total: 25,
        amountDue: 0,
        clientId: null,
        fidelityDiscountApplied: 0,
      },
    }).as("addPayment");

    cy.intercept("POST", `**/api/sales/${sessionToken}/complete`, {
      statusCode: 204,
      body: "",
    }).as("completeSale");
  }

  it("permite adicionar item, aplicar desconto, receber em dinheiro e concluir a venda", () => {
    stubSaleFlow();
    cy.setCookie("kalles_auth_token", "test-auth-token");

    cy.visit("/pdv", {
      onBeforeLoad(win) {
        win.localStorage.setItem(
          "kalles:onboarding:pdv-terminal:v1:completed",
          "true",
        );
        win.sessionStorage.setItem(
          "kalles:active-session",
          JSON.stringify({
            sessionId: sessionToken,
            operatorId,
            cashRegisterCode: "CAIXA-01",
            operatorName: "Operador Caixa 01",
            initialAmount: 100,
            openedAt: "2026-04-15T09:00:00Z",
            cashOnlyOperation: false,
          }),
        );
        win.sessionStorage.setItem("@kalles:activeCompanyId", companyId);
      },
    });

    cy.wait("@getCompanies");
    cy.wait("@createSale");

    cy.get("[data-testid='product-search-input']").type("SKU-001");
    cy.get("[data-testid='product-search-submit']").click();
    cy.get("[data-testid='item-quantity-dialog']").should("be.visible");
    cy.get("[data-testid='item-quantity-input']").clear().type("1");
    cy.get("[data-testid='item-quantity-confirm']").click();

    cy.wait("@addItem");
    cy.get("[data-testid='sale-item-SKU-001']").should("be.visible");
    cy.get("[data-testid='sale-item-discount-SKU-001']").click();

    cy.get("[data-testid='discount-amount-input']").type("5");
    cy.get("[data-testid='discount-confirm']").click();
    cy.wait("@applyDiscount");

    cy.contains("R$ 25,00").should("be.visible");

    cy.get("[data-testid='payment-amount-input']")
      .should("have.value", "0,00")
      .clear()
      .type("3000")
      .should("have.value", "30,00");
    cy.get("[data-testid='payment-submit-cash']").click();

    cy.wait("@addPayment");
    cy.get("[data-testid='complete-sale']").click();
    cy.wait("@completeSale");

    cy.get("[data-testid='sale-finished-banner']").should("be.visible");
    cy.get("[data-testid='new-sale']").should("be.visible");
  });
});
