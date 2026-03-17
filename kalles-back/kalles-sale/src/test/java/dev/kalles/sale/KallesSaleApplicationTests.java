package dev.kalles.sale;

import dev.kalles.sale.mercadopago.port.CaixaMpRepository;
import dev.kalles.sale.mercadopago.port.CompanyMpRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class KallesSaleApplicationTests {

    @MockitoBean
    private CaixaMpRepository caixaMpRepository;

    @MockitoBean
    private CompanyMpRepository companyMpRepository;

	@Test
	void contextLoads() {
	}

}
