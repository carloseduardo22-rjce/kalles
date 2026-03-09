package dev.kalles.sale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "dev.kalles")
@EnableJpaAuditing
@EntityScan(basePackages = "dev.kalles")
@EnableJpaRepositories(basePackages = "dev.kalles")
public class KallesSaleApplication {

	public static void main(String[] args) {
		SpringApplication.run(KallesSaleApplication.class, args);
	}

}
