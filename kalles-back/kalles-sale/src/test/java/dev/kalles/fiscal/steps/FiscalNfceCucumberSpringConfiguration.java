package dev.kalles.fiscal.steps;

import dev.kalles.KallesSaleApplication;
import dev.kalles.cashregister.support.AbstractCashRegisterApiSupport;
import dev.kalles.fiscal.support.FiscalTestConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KallesSaleApplication.class)
@Import(FiscalTestConfiguration.class)
public class FiscalNfceCucumberSpringConfiguration extends AbstractCashRegisterApiSupport {
}
