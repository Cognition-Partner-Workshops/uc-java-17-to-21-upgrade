# Merge notes

How this repository was assembled from two source repos, and every change made on the way to the Java 17 / Spring Boot 3.2.12 baseline. Nothing in either source repository was modified.

## Sources

| Directory | Source repo | Branch | Commit |
|---|---|---|---|
| `conduit-api/` | `Cognition-Partner-Workshops/uc-spring-boot-upgrade-microservice-extraction` | `main` | `cb773cff4dea93232d3965827cb8ef5c26c83cae` |
| `loan-service/` | `Cognition-Partner-Workshops/uc-data-source-migration-jdbc-normalization` | `main` | `51dad16f87ee4527b77f5e74f1674613952bc708` |

Each `main` was rewritten into its subdirectory and merged with `--allow-unrelated-histories`, so `git log -- conduit-api` and `git log -- loan-service` show the original commits (the loan-service source has a single commit). No other branches were imported. Both MIT license files are kept as-is; see `NOTICE`.

## Removed from Conduit

| Path / item | Why |
|---|---|
| `conduit-api/frontend/` (Next.js client) | Out of scope: backend-only Java demo. |
| `conduit-api/.windsurf/` | Editor-specific agent config from the source repo. |
| `conduit-api/src/test/java/io/spring/selenium/` (2 Selenium/TestNG E2E tests, page objects, utilities) | Required the removed frontend and a browser; not part of the JUnit suite. |
| `conduit-api/src/test/resources/selenium/` (TestNG suites, config) | Same as above. |
| `build.gradle`: `selenium-java`, `webdrivermanager`, `testng`, `extentreports`, `httpclient5` test deps; `seleniumTest` task; `exclude 'io/spring/selenium/**'` in `test` | Only used by the Selenium suite. |
| `.gitignore`: "Frontend (Node.js)" block | Frontend removed. |
| `README.md`: "Frontend (Next.js)" section | Frontend removed. |
| `conduit-api/AGENTS.md` | Described the source repo's microservice-extraction exercise (pinned Spring Boot 2.6.3, allowed skipping the JaCoCo gate). Superseded by the root `AGENTS.md`, which keeps its code conventions. |

## Conduit: Java 11 / Boot 2.6.3 to Java 17 / Boot 3.2.12

The source repo has several open, unmerged Java 17 upgrade PRs. They were each built locally on JDK 17:

| PR | Boot | Gradle | DGS | JJWT | Result on JDK 17 |
|---|---|---|---|---|---|
| #76 | 3.2.0 | 8.5 | 7.6 | 0.12.5 | Build fails: Gradle task-dependency validation errors in `spotlessJava` |
| #254 | 3.2.5 | 8.7 | 8.7.1 | 0.12.5 | Tests pass, JaCoCo 25.5% (gate 80%) |
| #270 | 3.2.5 | 8.6 | 8.5.3 | 0.11.5 | Tests pass, JaCoCo 33.1% |
| #275 | 3.2.5 | 8.7 | 8.5.8 | 0.11.5 | Tests pass, JaCoCo 46.8% |
| #311 | 3.2.12 | 8.6 | 8.7.1 | 0.12.6 | Tests pass, JaCoCo 32.0% |
| **#319** | **3.2.12** | **8.5** | **8.7.1** | **0.12.6** | **Tests pass, JaCoCo 32.0% (52.0% without DGS codegen)** |
| #320 | 3.2.12 | 8.7 | 8.7.1 | 0.11.2 | Older JJWT API |
| #323 | 3.2.12 | 8.14.3 | 8.7.1 | 0.12.6 | Tests pass, JaCoCo 31.4%; larger diff |
| #324 | 3.2.12 | 8.5 | 8.2.5 | — | 296 files changed, mostly unrelated/generated |

No PR passes the existing 80% gate unchanged: the Boot 3 build counts DGS-generated classes, and the suite was thin in places.

**Reused: #319** (`devin/1791470430-java17-spring-boot-3.2`, head `914819af`). It is on the latest 3.2.x patch (3.2.12), uses current JJWT (0.12.x builder/parser API) and DGS 8.7.1 on Spring GraphQL, and confines itself to the upgrade: build file, wrapper, `javax.*` to `jakarta.*`, Spring Security 6 `SecurityFilterChain` config, the graphql-java `handleException` API, and JWT code. Its PR docs (`UPGRADE_NOTES.md`, README/AGENTS edits) were not carried over.

Applied on top:

1. **Coverage tests from source PR #293** (commits `b3c89376`, `3a2c5423`): unit and integration tests for the low-coverage modules, GraphQL `AuthenticationException` mapping in `GraphQLCustomizeExceptionHandler`, and a `lombok.config` that marks Lombok output as generated.
2. **JaCoCo excludes only DGS codegen** (`io/spring/graphql/types/**`, `io/spring/graphql/DgsConstants*`). The 0.80 minimum is unchanged.
3. Ported those tests to the Boot 3 APIs: `javax.validation` to `jakarta.validation`, `onException(...)` to `handleException(...).join()`; then `spotlessApply`.
4. `sourceCompatibility = '17'` instead of a Gradle toolchain block, so the build runs on whatever JDK `JAVA_HOME` points to. A toolchain would pin compilation to 17 and hide what happens on JDK 21.
5. `README.md` and the seed article text in `V2__seed_data.sql` now say Java 17 instead of Java 11. `.java-version` is `17` (from #319).

Result on JDK 17: `./gradlew build` green, 208 tests, 0 failures, 98.7% instruction coverage, `spotlessCheck` clean, no non-JDK `javax.*` imports.

## Loan service

| Change | Why |
|---|---|
| Spring Boot parent 3.2.3 to 3.2.12 | Same 3.2.x patch as Conduit. |
| `<relativeTo/>` to `<relativePath/>` in `pom.xml` | Typo in source; Maven warned on every build. |
| Added `mvnw`, `mvnw.cmd` (Maven Wrapper 3.3.2, `only-script`) | Source had `.mvn/wrapper/maven-wrapper.properties` but no scripts. Pins Maven 3.9.6. |

The source README lists `GET /api/payments/loan/{loanId}`, but no such route exists: payments are served at `GET /api/loans/{loanId}/payments`. The code was not changed; parity and docs use the real route.

`docs/MIGRATION_TASKS.md` and `data/` belong to the source repo's data-migration workshop and are kept for reference only.
