package dev.kalles.payment.steps;

import dev.kalles.KallesSaleApplication;
import dev.kalles.payment.support.AbstractStonePaymentContainerSupport;
import dev.kalles.payment.support.StonePaymentTestConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KallesSaleApplication.class)
@Import(StonePaymentTestConfiguration.class)
public class StonePaymentCucumberSpringConfiguration extends AbstractStonePaymentContainerSupport {
}
