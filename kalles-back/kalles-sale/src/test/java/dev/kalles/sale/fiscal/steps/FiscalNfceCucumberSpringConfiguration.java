package dev.kalles.sale.fiscal.steps;

import dev.kalles.sale.KallesSaleApplication;
import dev.kalles.sale.cashregister.support.AbstractCashRegisterApiSupport;
import dev.kalles.sale.fiscal.support.FiscalTestConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KallesSaleApplication.class)
@Import(FiscalTestConfiguration.class)
public class FiscalNfceCucumberSpringConfiguration extends AbstractCashRegisterApiSupport {
}
