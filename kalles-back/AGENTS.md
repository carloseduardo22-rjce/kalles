# Project Guidelines

## Methodology

**TDD is mandatory.** Every new feature or behavior change must follow this order:

1. Write the test(s) first — they must fail before any production code exists.
2. Write the minimum production code needed to make the tests pass.
3. Refactor if needed, keeping all tests green.

Never create or modify production code without a corresponding test written first.

## Code Style

- **No comments in code.** Do not add inline comments, block comments, or Javadoc/docstrings to any code you write or modify. Code must be self-explanatory through meaningful names.
- Do not add explanatory comments like `// save to database` or `// validate input`.
- Do not add docstrings or Javadoc unless explicitly requested.

## Architecture

- Spring Boot 4.0.1 / Java 25 / PostgreSQL
- Entities and services live in `dev.kalles.sale.core.*`
- Controllers live in `dev.kalles.api.*`
- DTOs are Java records; Bean Validation annotations belong on DTOs, not entities
- Schema managed exclusively by Flyway (`ddl-auto: none`)
- Use `ProblemDetail` for error responses
- Use `@RequiredArgsConstructor` + `private final` for dependency injection

## Build and Test

```
./mvnw test
./mvnw test -pl kalles-sale
```

Tests use H2 in-memory with Flyway disabled (`spring.flyway.enabled=false`).
