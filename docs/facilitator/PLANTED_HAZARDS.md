# Planted hazards (facilitators only)

Do not share with participants. Nothing in `AGENTS.md`, `docs/LAB.md` or the skill points here.

## Why anything was planted

Probing the Stage 1 baseline on JDK 21 with no changes, **both apps built and passed on JDK 21**
(only Gradle deprecation notices, a CDS warning and Mockito's dynamic-agent warning). A Java 21
upgrade demo needs at least one real break per app, so one genuine JDK 21 build break was planted in
each app, plus code that gives the modernization steps something real to do.

## Build breaks (one per app)

| App | Where | What | Symptom on JDK 21 | Real fix |
|---|---|---|---|---|
| Conduit | `conduit-api/build.gradle` | Lombok pinned to `1.18.26` for `compileOnly` and `annotationProcessor` (Boot 3.2.12 would give 1.18.36) | `:compileJava` fails: `NoSuchFieldError: ... JCTree$JCImport does not have member field 'com.sun.tools.javac.tree.JCTree qualid'` | Drop the pin (use the Boot-managed version) or pin ≥ 1.18.30 |
| Loan service | `loan-service/pom.xml` | `legacy.jvm.flags` = `-XX:+UseBiasedLocking -Xss512k` appended to the Surefire `argLine` | Surefire fork fails: `Unrecognized VM option 'UseBiasedLocking'`, 0 tests run | Remove `-XX:+UseBiasedLocking` (biased locking was removed after JDK 17); keep `-Xss512k` and `@{argLine}` so JaCoCo still runs |

Both pass on JDK 17 (Lombok 1.18.26 supports 17; JDK 17 only prints a deprecation warning for the flag).

Watch for: participants deleting the whole `argLine` (breaks JaCoCo coverage → gate fails),
or lowering the gate instead.

## Showcase code (Java 21 modernization targets)

| Target | Conduit | Loan service |
|---|---|---|
| Blocking fan-out on a fixed `ThreadPoolTaskExecutor` (pool 4) with simulated 50 ms I/O → virtual threads | `application/profile/ProfileSummaryService` behind `GET /profiles/{username}/summary` | `service/ExposureService` behind `GET /api/borrowers/{id}/exposure` |
| `if/else` + `instanceof` chain over a small hierarchy → sealed interface + records + pattern-matching `switch` | `ProfileActivity` (`AuthoredArticle`, `FollowerTally`, `FavoriteTally`) in `ActivityDescriber.describe` | `ExposureComponent` (`OutstandingPrincipal`, `EscrowHolding`, `PaymentRecord`, `DelinquencyNotice`) in `ExposureCalculator.summarize` |
| `list.get(list.size() - 1)` → `SequencedCollection.getLast()` | `ProfileSummaryService` (latest article) | `ExposureCalculator` (last payment date) |
| `new Locale(String, String)` (deprecated in 19) → `Locale.of` | `ActivityDescriber` | `ExposureService` |
| `new URL(String)` (deprecated in 20) → `URI.create(...).toURL()` | `ProfileSummaryService.profileUrl` | `ExposureService.selfLink` |
| `Thread.getId()` (deprecated in 19) → `threadId()` | `ProfileSummaryService` debug log | `ExposureService` debug log |

Parity traps that the harness catches:

- `ActivityDescriber` output strings and `outstandingBalanceDisplay` (`£178,234.12`) are in the
  transcripts; a careless `Locale` change (e.g. `Locale.UK` vs `Locale.ENGLISH`) shows as a diff.
- Converting `AuthoredArticle` or the exposure DTO to records must keep JSON property names and the
  BigDecimal scale (`2100.00`); the golden snapshots and transcripts compare the exact JSON text.
- `ProfileSummaryService` uses `@Value`-injected settings; a virtual-thread executor must still be
  shut down (`DisposableBean.destroy`) or tests leak threads.

## Expected demo numbers

Baseline `make perf` (JDK 17, pool 4): ~1.9 s p50 and ~26 req/s for both apps at concurrency 50.
With virtual threads the per-request latency should drop towards the sum of the sequential 50 ms
reads (roughly 100–150 ms) and throughput should rise by an order of magnitude. Measure on the day;
do not quote these as promises.
