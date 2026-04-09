package dev.kalles.sale.security.steps;

import dev.kalles.sale.KallesSaleApplication;
import dev.kalles.sale.payment.support.AbstractStonePaymentContainerSupport;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KallesSaleApplication.class)
public class SecurityCucumberSpringConfiguration extends AbstractStonePaymentContainerSupport {
}
