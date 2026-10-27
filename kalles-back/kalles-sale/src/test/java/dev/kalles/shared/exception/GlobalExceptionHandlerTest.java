package dev.kalles.shared.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldMapConflictToHttp409WithStableCode() {
        ProblemDetail problem = handler.handleDomain(new SampleConflict());

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getDetail()).isEqualTo("conflito de teste");
        assertThat(problem.getProperties()).containsEntry("code", "SAMPLE_CONFLICT");
    }

    @Test
    void shouldMapBusinessRuleViolationToHttp422WithStableCode() {
        ProblemDetail problem = handler.handleDomain(new SampleRuleViolation());

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(problem.getDetail()).isEqualTo("regra de teste");
        assertThat(problem.getProperties()).containsEntry("code", "SAMPLE_RULE");
    }

    @Test
    void shouldMapIllegalStateToHttp500WithoutExposingItsMessage() {
        ProblemDetail problem = handler.handleUnexpected(new IllegalStateException("detalhe interno"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getDetail()).doesNotContain("detalhe interno");
    }

    @Test
    void shouldExposeTheCodeOfNotFoundAndForbiddenProblems() {
        ProblemDetail notFound = handler.handleNotFound(new NotFoundException("SAMPLE_MISSING", "faltou"));
        ProblemDetail forbidden = handler.handleForbidden(new ForbiddenOperationException("SAMPLE_DENIED", "negado"));

        assertThat(notFound.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(notFound.getProperties()).containsEntry("code", "SAMPLE_MISSING");
        assertThat(forbidden.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(forbidden.getProperties()).containsEntry("code", "SAMPLE_DENIED");
    }

    @Test
    void shouldOmitTheCodeWhenTheProblemHasNone() {
        ProblemDetail problem = handler.handleForbidden(new ForbiddenOperationException("negado"));

        assertThat(problem.getProperties()).isNull();
    }

    private static final class SampleConflict extends ConflictException {
        SampleConflict() {
            super("SAMPLE_CONFLICT", "conflito de teste");
        }
    }

    private static final class SampleRuleViolation extends BusinessRuleViolationException {
        SampleRuleViolation() {
            super("SAMPLE_RULE", "regra de teste");
        }
    }
}
