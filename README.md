# uc-java-17-to-21-upgrade

Two Spring Boot services on a common **Java 17 / Spring Boot 3.2.12** baseline. The workshop upgrades both to **Java 21 / Spring Boot 3.3+** and proves nothing changed, using the existing tests plus a behavioural parity check.

| App | Path | Build | What it is | Imported from |
|---|---|---|---|---|
| Conduit API | [`conduit-api/`](conduit-api/) | Gradle 8.5 wrapper | RealWorld blogging backend: REST + GraphQL (Netflix DGS), MyBatis, SQLite, Flyway, JJWT, Spotless, JaCoCo 80% gate | `Cognition-Partner-Workshops/uc-spring-boot-upgrade-microservice-extraction` @ `cb773cff4dea93232d3965827cb8ef5c26c83cae` |
| Loan service | [`loan-service/`](loan-service/) | Maven 3.9.6 wrapper | Loan servicing API over a legacy-style H2 schema: Spring Web, Spring Data JPA | `Cognition-Partner-Workshops/uc-data-source-migration-jdbc-normalization` @ `51dad16f87ee4527b77f5e74f1674613952bc708` |

Both source `main` branches were imported with full history. Conduit was on Java 11 / Spring Boot 2.6.3 in its source repo and has been brought to the Java 17 / Boot 3.2 baseline here; see [`docs/MERGE_NOTES.md`](docs/MERGE_NOTES.md) for what was removed, changed and why.

## Versions

| | Baseline (this branch) | Workshop target |
|---|---|---|
| Java | 17 | 21 |
| Spring Boot | 3.2.12 (both apps) | 3.3.x or later (both apps) |

## Build and test

Requires JDK 17 on `PATH` / `JAVA_HOME`.

```bash
make baseline            # both apps

cd conduit-api && ./gradlew build     # tests + JaCoCo gate (>= 80%) + spotlessCheck
cd loan-service && ./mvnw verify
```

Run locally:

```bash
cd conduit-api && ./gradlew bootRun     # http://localhost:8080  (e.g. curl localhost:8080/tags)
cd loan-service && ./mvnw spring-boot:run   # http://localhost:8080  (e.g. curl localhost:8080/api/loans)
```

Both default to port 8080; pass `--server.port=...` to run them side by side.

## Repository layout

```
conduit-api/     Gradle app (package io.spring)
loan-service/    Maven app (package com.workshop.loanservice)
docs/            Merge notes and workshop docs
AGENTS.md        Conventions and guardrails for coding agents
```

## License

Each app keeps its original MIT license (`conduit-api/LICENSE`, `loan-service/LICENSE`). Root files are MIT; see [`LICENSE`](LICENSE) and [`NOTICE`](NOTICE).
