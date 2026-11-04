# perf

`latency.py` measures the two blocking fan-out endpoints:

| App | Endpoint | Fan-out |
|---|---|---|
| conduit | `GET /profiles/{username}/summary` | 3 blocking reads (articles, followers, favourites) |
| loan | `GET /api/borrowers/{id}/exposure` | borrower + loans, then one payments read per loan |

Both run their reads on a fixed `ThreadPoolTaskExecutor` (pool size 4) and add a simulated
50 ms remote-call delay per read, so throughput is capped by the pool, not the CPU.

```bash
make perf APP=conduit            # starts the app, runs 200 requests at concurrency 50, stops it
make perf APP=loan
python3 perf/latency.py --app conduit --target http://localhost:8080 --requests 400 --concurrency 100
```

Record the before/after numbers from the same machine. Parity (`make parity`) must stay green
whatever the executor change is.
