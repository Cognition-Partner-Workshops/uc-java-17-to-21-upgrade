# AGENTS.md

Two independent Spring Boot apps in one repo. Baseline is **Java 17 / Spring Boot 3.2.12**; the workshop target is **Java 21 / Spring Boot 3.3+**. Each app has its own build tool and must build on its own. Do not merge the builds or share code between the apps.

## Build, test, verify

| | Conduit (`conduit-api/`) | Loan service (`loan-service/`) |
|---|---|---|
| Build + test + gates | `./gradlew build` | `./mvnw verify` |
| Tests only | `./gradlew test` | `./mvnw test` |
| Format | `./gradlew spotlessApply` (check: `spotlessCheck`) | n/a |
| Coverage report | `build/reports/jacoco/test/html/index.html` | n/a |
| Run | `./gradlew bootRun` | `./mvnw spring-boot:run` |

`make baseline` runs both builds from the repo root. The active JDK is whatever `JAVA_HOME` points to.

## Guardrails

- **Do not edit transcripts, golden files, or tests to make parity or a build pass.** If behaviour changed, fix the code, or stop and explain why the change is intended.
- **Do not lower coverage gates** (Conduit JaCoCo minimum is 0.80) and do not skip them with `-x jacocoTestCoverageVerification`, `-DskipTests`, `@Disabled`, or exclusions for non-generated code.
- Keep both apps on the **same** Spring Boot version and the same Java release.
- Keep the build tools as they are: Gradle wrapper for Conduit, Maven wrapper for the loan service. Bump wrapper versions rather than replacing them.
- Use `jakarta.*` APIs. The only `javax.*` imports allowed are JDK packages (for example `javax.crypto`).
- No new runtime dependencies unless the task needs them; say why in the PR.

## Conventions (from Conduit, applied to both apps)

- **Package layout.** Conduit: `io.spring.api` (REST controllers, security, exception mapping), `io.spring.graphql` (DGS data fetchers/mutations), `io.spring.application` (query and command services, DTOs), `io.spring.core` (domain model and repository interfaces), `io.spring.infrastructure` (MyBatis mappers and repository implementations, JWT service). Loan service: `controller`, `service`, `dto`, `entity`, `repository` under `com.workshop.loanservice`. Put new code in the matching layer.
- **Constructor injection** with `private final` fields. Conduit uses Lombok `@AllArgsConstructor`; the loan service uses explicit constructors. A few older Conduit classes still use `@Autowired`; do not copy that into new code.
- **Persistence.** Conduit uses MyBatis XML mappers in `src/main/resources/mapper/` and Flyway migrations in `src/main/resources/db/migration/` (`V<n>__name.sql`). The loan service reads the legacy H2 schema in `schema-legacy.sql` / `data-legacy.sql`; do not change that data.
- **Formatting.** Conduit Java code is formatted by Spotless (google-java-format). Run `./gradlew spotlessApply` before committing; CI runs `spotlessCheck`. Match the surrounding style in the loan service.
- **API shape.** Keep URLs, status codes and JSON envelopes unchanged. Conduit validation errors are 422 with an `{"errors": {"field": ["message"]}}` envelope (see `io.spring.api.exception`); the loan service returns plain DTO JSON.
- Use camelCase for variable names.
