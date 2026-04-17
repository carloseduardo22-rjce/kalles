const { defineConfig } = require("cypress");

module.exports = defineConfig({
  video: false,
  screenshotOnRunFailure: true,
  viewportWidth: 1440,
  viewportHeight: 900,
  e2e: {
    baseUrl: "http://localhost:3000",
    specPattern: "cypress/e2e/**/*.cy.{ts,js}",
    supportFile: "cypress/support/e2e.ts",
  },
});
