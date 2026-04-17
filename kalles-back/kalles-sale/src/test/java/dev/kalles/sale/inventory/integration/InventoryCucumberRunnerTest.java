package dev.kalles.sale.inventory.integration;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/inventory/blindagem_multi_tenant_estoque.feature")
@ConfigurationParameter(
        key = GLUE_PROPERTY_NAME,
        value = "dev.kalles.sale.inventory.steps"
)
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-reports/inventory-report.html"
)
class InventoryCucumberRunnerTest {
}
