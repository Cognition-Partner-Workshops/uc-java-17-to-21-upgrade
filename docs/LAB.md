# Lab: Java 17 → 21 with proof nothing changed

The repo holds two independent Spring Boot apps on **Java 17 / Spring Boot 3.2.12**:

| App | Path | Build | Parity |
|---|---|---|---|
| Conduit (blogging API, REST + GraphQL) | `conduit-api/` | Gradle | `make parity APP=conduit` (22 scenarios) |
| Loan service (legacy data-warehouse reads) | `loan-service/` | Maven | `make parity APP=loan` (13 scenarios) |

Goal: move each app to **Java 21 / Spring Boot 3.3+** with the build, tests, coverage gate and
behavioural parity all green, then adopt Java 21 features where they pay off.

Before you start, read `AGENTS.md`, `docs/BASELINE.md` and `parity/README.md`. JDK 17 and JDK 21
are both installed at `/usr/lib/jvm/java-17-openjdk-amd64` and `/usr/lib/jvm/java-21-openjdk-amd64`.

## Exercise 1 — upgrade one app

Copy-paste prompt (swap `conduit-api` / `make parity APP=conduit` for `loan-service` / `APP=loan`):

```text
Upgrade conduit-api in this repo from Java 17 / Spring Boot 3.2.12 to Java 21 and the latest
Spring Boot 3.3.x. Follow AGENTS.md and the java21-upgrade-parity skill.
First run the JDK 17 baseline and `make parity APP=conduit` and confirm both are green. Then probe
the build on JDK 21 without changes and list everything that breaks or warns. Fix each item at its
root cause. Done means, on JDK 21: `./gradlew build` green (tests, Spotless, JaCoCo gate unchanged),
test count not lower than docs/BASELINE.md, `make parity APP=conduit` with zero diffs, and no new
-Xlint deprecation warnings. Do not edit tests, golden files or parity transcripts, and do not lower
coverage gates. Open one PR with a table of what broke on JDK 21 and how you fixed each item.
```

## Exercise 2 — virtual threads and modern Java

Run after Exercise 1 is merged (or on top of its branch).

```text
In conduit-api (now on Java 21), adopt Java 21 features where they improve the code, keeping
behaviour identical.
1. Measure first: run `make perf APP=conduit` three times and record p50/p95/throughput.
2. Switch the blocking fan-out in ProfileSummaryService from the fixed ThreadPoolTaskExecutor to
   virtual threads, and enable spring.threads.virtual.enabled if appropriate. Re-run `make perf`
   three times and report before/after in a table.
3. Replace the instanceof chain over ProfileActivity with a sealed interface, records and a
   pattern-matching switch; replace list.get(list.size() - 1) with getLast(); replace APIs that are
   deprecated on 21.
`./gradlew build` and `make parity APP=conduit` must stay green with zero diffs. Do not edit tests,
transcripts or golden files. Open one PR with the latency table.
```

For the loan service use `ExposureService`, `ExposureComponent`/`ExposureCalculator` and
`make perf APP=loan`.

## Exercise 3 — fleet mode: both apps in parallel

```text
Start two Devin sessions in parallel, one per app in this repo, each upgrading its app from
Java 17 / Spring Boot 3.2.12 to Java 21 and the same Spring Boot 3.3.x patch (agree the patch
up front: use the latest 3.3.x). Session A: conduit-api, gate `./gradlew build` and
`make parity APP=conduit`. Session B: loan-service, gate `./mvnw verify` and `make parity APP=loan`.
Each session follows AGENTS.md and the java21-upgrade-parity skill, must not touch the other app's
directory, must not edit tests/transcripts/golden files or lower coverage gates, and opens its own
PR with a JDK 21 break/fix table. When both are done, summarise both PRs side by side: Spring Boot
version, test counts before/after, coverage, parity scenario counts, and anything left open.
```

## Checking your result

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
make baseline
make parity APP=conduit && make parity APP=loan
```
