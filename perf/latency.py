"""Latency probe for the two fan-out endpoints (the virtual-threads showcase).

  python3 perf/latency.py --app conduit --target http://localhost:8080 [--requests 200 --concurrency 50]
  python3 perf/latency.py --app loan    --target http://localhost:8081

Fires N GET requests with C concurrent clients and prints p50/p95/p99/max and throughput. Run it
before and after a change on the same machine and compare. The endpoints simulate a blocking remote
call per read (conduit.profile-summary.simulated-latency-ms / loan.exposure.simulated-latency-ms),
so results are dominated by how many blocking calls the executor can run at once.
"""
import argparse
import statistics
import time
from concurrent.futures import ThreadPoolExecutor

import requests

PATHS = {
    "conduit": ["/profiles/johndoe/summary", "/profiles/janedoe/summary", "/profiles/bobsmith/summary"],
    "loan": ["/api/borrowers/B-1000%d/exposure" % i for i in range(1, 6)],
}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--app", required=True, choices=sorted(PATHS))
    parser.add_argument("--target", required=True)
    parser.add_argument("--requests", type=int, default=200)
    parser.add_argument("--concurrency", type=int, default=50)
    args = parser.parse_args()

    paths = PATHS[args.app]
    session = requests.Session()
    adapter = requests.adapters.HTTPAdapter(pool_maxsize=args.concurrency)
    session.mount("http://", adapter)
    session.get(args.target.rstrip("/") + paths[0], timeout=60).raise_for_status()

    def one(i):
        start = time.perf_counter()
        resp = session.get(args.target.rstrip("/") + paths[i % len(paths)], timeout=120)
        resp.raise_for_status()
        return (time.perf_counter() - start) * 1000

    started = time.perf_counter()
    with ThreadPoolExecutor(max_workers=args.concurrency) as pool:
        latencies = sorted(pool.map(one, range(args.requests)))
    elapsed = time.perf_counter() - started

    def pct(p):
        return latencies[min(len(latencies) - 1, int(round(p / 100.0 * len(latencies))) - 1)]

    print("app=%s requests=%d concurrency=%d" % (args.app, args.requests, args.concurrency))
    print("p50=%.0fms p95=%.0fms p99=%.0fms max=%.0fms mean=%.0fms throughput=%.1f req/s" % (
        pct(50), pct(95), pct(99), latencies[-1], statistics.mean(latencies), args.requests / elapsed))


if __name__ == "__main__":
    main()
