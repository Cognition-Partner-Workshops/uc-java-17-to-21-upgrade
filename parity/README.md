# Behavioural parity harness

Replays a fixed list of HTTP scenarios against a running app, normalizes each response and
compares it with the transcript recorded on the Java 17 / Spring Boot 3.2 baseline. Any
difference fails the run.

```bash
pip install -r parity/requirements.txt
make parity APP=conduit                      # build if needed, start on a free port with a fresh DB, compare, stop
make parity APP=loan
make parity APP=conduit TARGET=http://localhost:8080   # compare against an app you started (must be fresh)
```

`make parity` exits non-zero on any diff, missing transcript or extra scenario.

## What is recorded

`parity/<app>/transcripts/NN-<scenario>.json` holds, per request: method, path, whether a token was
sent, request body, and the normalized response (status, Content-Type without charset, body).

- **conduit** (22 scenarios): register, 422 validation envelope, login (good and bad password),
  current user, 401 envelope, update user, create/get/update/delete article, favourite, comments,
  follow, feed paging, article listing filters, tags, 404 envelope, profile summary, and four GraphQL
  operations (createArticle, article/articles query, me/profile query, error results).
- **loan** (13 scenarios): all five endpoint groups (loans, loan detail, borrowers, borrower detail,
  loan payments) plus borrower exposure, not-found cases, malformed IDs, and an unknown route.

Seed data: conduit uses its Flyway seed (`V2__seed_data.sql`) plus users and articles the scenarios
create through the API; it must start with an empty database file (the runner uses a temp file).
The loan service loads `schema-legacy.sql` / `data-legacy.sql` into in-memory H2 on every start.

## Normalization

Implemented in `normalize.py`, applied before recording and before comparing:

| Volatile value | Rule |
|---|---|
| JWTs (`eyJ…​.…​.…​`) | replaced with `<JWT:n>`, numbered by first appearance in the scenario |
| UUIDs (article, comment IDs) | replaced with `<UUID:n>`, numbered by first appearance in the scenario, also in request paths |
| ISO-8601 timestamps (`createdAt`, `updatedAt`, error `timestamp`) | replaced with `<TIMESTAMP>`; plain dates such as `2025-12-01` are kept |
| `tagList` / `tags` arrays | sorted (SQLite returns tags in no fixed order) |
| JSON object keys | sorted; array order is otherwise kept because order is behaviour |
| Tomcat HTML error pages | reduced to the page `<title>` |

Numbers keep their exact JSON text (e.g. `2100.00` stays `2100.00`), so a change in BigDecimal scale
is a parity failure.

## Rules

- Do not edit transcripts to make parity pass. A diff is either a regression to fix in the app or a
  deliberate, reviewed behaviour change.
- Re-record (`parity/run.sh <app> --record`) only on the baseline, never as part of an upgrade.
