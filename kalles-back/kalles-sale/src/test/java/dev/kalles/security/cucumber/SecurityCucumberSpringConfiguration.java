package dev.kalles.security.cucumber;

import dev.kalles.KallesSaleApplication;
import dev.kalles.payment.support.AbstractStonePaymentContainerSupport;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KallesSaleApplication.class)
public class SecurityCucumberSpringConfiguration extends AbstractStonePaymentContainerSupport {
}
