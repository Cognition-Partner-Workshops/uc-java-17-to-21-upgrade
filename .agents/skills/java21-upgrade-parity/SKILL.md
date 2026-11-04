---
name: java21-upgrade-parity
description: Upgrade one app in this repo (conduit-api or loan-service) from Java 17 / Spring Boot 3.2 to Java 21 / Spring Boot 3.3+ and prove behaviour is unchanged with tests, coverage gates and the HTTP parity harness. Use for any Java 21, Spring Boot 3.3/3.4, virtual threads, or modern-Java-language task in this repo.
---

# Java 21 upgrade with behavioural parity

## 1. Baseline first (JDK 17)

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
make conduit-build          # or: make loan-verify
make parity APP=conduit     # or: APP=loan — must be green before you change anything
```

Read `docs/BASELINE.md` for the recorded test counts, coverage and the JDK 21 probe results.

## 2. Probe on JDK 21 before editing

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
cd conduit-api && ./gradlew --no-daemon clean build --warning-mode all     # or: cd loan-service && ./mvnw -B clean verify
```

Write down every failure and warning. Do not assume the first error is the only one; fix one, re-run.

## 3. Upgrade, in this order

1. Java level: Gradle `sourceCompatibility`/`targetCompatibility` (or a toolchain) and `.java-version`
   for Conduit; `<java.version>` for the loan service. Both apps must end on the same Java release.
2. Build plugins and annotation processors that inspect javac or bytecode (Lombok, JaCoCo, Gradle
   itself, Surefire JVM flags). Check versions pinned explicitly in the build files, not just the BOM.
3. Spring Boot to the same 3.3.x (or newer 3.x) patch in both apps, then libraries that track it
   (DGS, MyBatis starter, JJWT). Read the Boot 3.3 release notes for removed properties.
4. Deprecations surfaced by `-Xlint:all` on 21 (`-Pxlint` for Gradle, `-Pxlint` for Maven). Replace,
   do not suppress.

## 4. Gates — all must be green on JDK 21

- `./gradlew build` (tests, Spotless, JaCoCo ≥ 0.80) / `./mvnw verify` (tests, JaCoCo ≥ 0.80).
- Same or higher test count than `docs/BASELINE.md`.
- `make parity APP=<app>` with zero diffs.
- No `javax.*` imports except JDK packages.

## 5. Modern Java (only after the upgrade is green)

- Virtual threads: the fan-out services (`ProfileSummaryService`, `ExposureService`) use a fixed
  `ThreadPoolTaskExecutor`. Compare `make perf APP=<app>` before and after.
- Records / sealed interfaces / pattern-matching `switch` for the small type hierarchies
  (`ProfileActivity`, `ExposureComponent`) and their `instanceof` chains.
- `SequencedCollection.getLast()` for `list.get(list.size() - 1)`.
- Keep JSON output identical: Lombok `@Data` getters and records serialize differently
  (`getX()` vs `x()` accessors are fine for Jackson, but check field names and ordering in parity).

## Never

- Edit `parity/*/transcripts/`, `loan-service/src/test/resources/golden/` or existing tests to get green.
- Lower or skip coverage gates, or use `-x test`, `-DskipTests`, `@Disabled`.
- Re-record parity transcripts as part of an upgrade.
