package dev.kalles.fidelity.integration;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/fidelity/gestao_politica_fidelidade_multi_filial.feature")
@ConfigurationParameter(
        key = GLUE_PROPERTY_NAME,
        value = "dev.kalles.fidelity.steps"
)
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-reports/fidelity-policy-report.html"
)
class FidelityPolicyCucumberRunnerTest {
}
