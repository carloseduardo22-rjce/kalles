package dev.kalles.security.exception;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemResponseWriterTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withBean(ProblemResponseWriter.class);

    @Test
    void shouldWriteTheProblemWithTheCodeAtTheTopLevel() {
        contextRunner.run(context -> {
            MockHttpServletResponse response = new MockHttpServletResponse();

            context.getBean(ProblemResponseWriter.class).write(
                    response,
                    HttpStatus.BAD_REQUEST,
                    "COMPANY_CONTEXT_REQUIRED",
                    "Filial obrigatoria",
                    "Informe o header X-Company-ID."
            );

            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.getContentType()).startsWith("application/problem+json");
            assertThat(response.getContentAsString()).isEqualTo(
                    "{\"detail\":\"Informe o header X-Company-ID.\",\"status\":400,"
                            + "\"title\":\"Filial obrigatoria\",\"code\":\"COMPANY_CONTEXT_REQUIRED\"}");
        });
    }

    @Test
    void shouldWriteThePathWhenItIsGiven() {
        contextRunner.run(context -> {
            MockHttpServletResponse response = new MockHttpServletResponse();

            context.getBean(ProblemResponseWriter.class).write(
                    response,
                    HttpStatus.FORBIDDEN,
                    "CSRF_INVALID",
                    "Token CSRF invalido",
                    "O token nao confere.",
                    "/api/clients"
            );

            assertThat(response.getStatus()).isEqualTo(403);
            assertThat(response.getContentAsString()).isEqualTo(
                    "{\"detail\":\"O token nao confere.\",\"status\":403,"
                            + "\"title\":\"Token CSRF invalido\",\"code\":\"CSRF_INVALID\",\"path\":\"/api/clients\"}");
        });
    }

    @Test
    void shouldEscapeTheQuotesTheHandWrittenJsonUsedToEscape() {
        contextRunner.run(context -> {
            MockHttpServletResponse response = new MockHttpServletResponse();

            context.getBean(ProblemResponseWriter.class).write(
                    response,
                    HttpStatus.BAD_REQUEST,
                    "COMPANY_CONTEXT_INVALID",
                    "Filial \"invalida\"",
                    "Quebra\nde linha"
            );

            assertThat(response.getContentAsString())
                    .contains("\\\"invalida\\\"")
                    .contains("Quebra\\nde linha");
        });
    }
}
