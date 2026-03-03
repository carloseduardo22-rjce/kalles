package dev.kalles.sale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "dev.kalles")
@EnableJpaAuditing
public class KallesSaleApplication {

	public static void main(String[] args) {
		SpringApplication.run(KallesSaleApplication.class, args);
	}

}
