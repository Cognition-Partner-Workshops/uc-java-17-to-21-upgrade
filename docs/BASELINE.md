# Baseline: Java 17 / Spring Boot 3.2.12

Recorded on the workshop VM with `make baseline` (Conduit `./gradlew build`, loan service
`./mvnw verify`) and the parity harness. Use these numbers as the "before" for any upgrade.

## Toolchain

| Tool | Version |
|---|---|
| JDK (baseline) | OpenJDK 17.0.20.1 |
| JDK (probe / target) | OpenJDK 21.0.12.1 |
| Gradle (Conduit wrapper) | 8.5 |
| Maven (loan-service wrapper) | 3.9.6 |
| Python (parity, perf) | 3.10+ with `requests`, `PyYAML` (`parity/requirements.txt`) |

## Per-app baseline (JDK 17)

| | Conduit (`conduit-api/`) | Loan service (`loan-service/`) |
|---|---|---|
| Command | `./gradlew build` | `./mvnw verify` |
| Result | BUILD SUCCESSFUL | BUILD SUCCESS |
| Test classes | 44 | 5 |
| Tests executed | 219 (0 failures, 0 skipped) | 39 (19 test methods, parameterized; 0 failures) |
| Instruction coverage (JaCoCo) | 98.6% | 80.8% |
| Coverage gate | 0.80 (generated DGS types excluded) | 0.80 |
| Formatting | `spotlessCheck` clean (google-java-format) | n/a |
| `-Xlint:all` warnings, main | 31 (24 rawtypes, 6 serial, 1 unchecked) | 0 |
| `-Xlint:all` warnings, test | 7 (unchecked) | 0 |
| Parity scenarios | 22 (`make parity APP=conduit`) | 13 (`make parity APP=loan`) |

Warning counts come from `./gradlew clean compileJava compileTestJava -Pxlint` and
`./mvnw -Pxlint clean test-compile`.

## Dependency versions

| | Conduit | Loan service |
|---|---|---|
| Spring Boot | 3.2.12 (`io.spring.dependency-management` 1.1.7) | 3.2.12 (starter parent) |
| Web / data | Spring MVC, HATEOAS, Security 6, MyBatis Spring Boot 3.0.3, Flyway 9.22.3, SQLite JDBC 3.45.3.0 | Spring MVC, Spring Data JPA, Hibernate 6.4.10.Final, H2 2.2.224 |
| GraphQL | Netflix DGS 8.7.1 (codegen 6.x) | n/a |
| Other | JJWT 0.12.6, Joda-Time 2.12.7, Lombok 1.18.26 | none |
| Test | JUnit 5.10.5, REST Assured, Spring Security Test, MyBatis test 3.0.3 | JUnit 5.10.5, Mockito 5.7.0, JSONAssert, Spring Boot Test |
| Build plugins | Spotless 6.25.0, JaCoCo 0.8.12 | maven-compiler 3.11.0, Surefire 3.1.2, JaCoCo 0.8.12 |

## JDK 21 probe (no code changes)

Each app was built on JDK 21 exactly as committed (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`,
Conduit `./gradlew clean build --warning-mode all`, loan `./mvnw -B clean verify`). Nothing was
fixed. CI repeats this probe as a report-only job.

### Conduit

Breaks:

1. `:compileJava` fails:
   `java.lang.NoSuchFieldError: Class com.sun.tools.javac.tree.JCTree$JCImport does not have member field 'com.sun.tools.javac.tree.JCTree qualid'`.
   The Lombok annotation processor version pinned in `build.gradle` does not support JDK 21's javac.

Warnings (seen once the compile is unblocked, with `-Xlint:all` on JDK 21):

1. `[deprecation] Locale(String,String) in Locale has been deprecated` — `application/profile/ActivityDescriber.java`
2. `[deprecation] getId() in Thread has been deprecated` — `application/profile/ProfileSummaryService.java`
3. `[deprecation] URL(String) in URL has been deprecated` — `application/profile/ProfileSummaryService.java`
4. `[this-escape] possible 'this' escape before subclass is fully initialized` — `JacksonCustomizations.java`, `application/CursorPageParameter.java` (new lint category in JDK 21)
5. Test JVM: `OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended`
6. Gradle 8.5 deprecations, also present on JDK 17: `Project.getConvention()`, `org.gradle.api.plugins.Convention`, `JavaPluginConvention` (removed in Gradle 9).

### Loan service

Breaks:

1. Tests cannot start: Surefire's forked JVM exits with `Unrecognized VM option 'UseBiasedLocking'`
   (`Error: Could not create the Java Virtual Machine.`). The flag comes from the
   `legacy.jvm.flags` property applied to the Surefire `argLine`; biased locking was removed after JDK 17.

Warnings:

1. `WARNING: A Java agent has been loaded dynamically (byte-buddy-agent-1.14.19.jar)` /
   `Dynamic loading of agents will be disallowed by default in a future release` — Mockito's inline
   mock maker on JDK 21 (JEP 451).
2. With `--release 21` and `-Xlint:all` (`./mvnw -Pxlint -Djava.version=21 test-compile`), in
   `service/ExposureService.java`:
   - `[deprecation] Locale(java.lang.String,java.lang.String) in java.util.Locale has been deprecated`
   - `[deprecation] getId() in java.lang.Thread has been deprecated`
   - `[deprecation] URL(java.lang.String) in java.net.URL has been deprecated`

   With the current `--release 17` these do not show, because the Java 17 API signatures are not deprecated.

## Fan-out latency (JDK 17, fixed pool of 4, 50 ms simulated I/O per read)

`make perf APP=<app>` — 200 requests, concurrency 50, same VM:

| App | Endpoint | p50 | p95 | max | Throughput |
|---|---|---|---|---|---|
| Conduit | `GET /profiles/{username}/summary` | 1867 ms | 1915 ms | 1916 ms | 26.4 req/s |
| Loan service | `GET /api/borrowers/{id}/exposure` | 1909 ms | 1943 ms | 1957 ms | 25.9 req/s |

Each request makes several 50 ms blocking reads, but the pool runs only 4 at a time, so requests queue.
