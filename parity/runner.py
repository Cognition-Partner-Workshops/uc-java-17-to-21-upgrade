"""Replays an app's scenarios against a running instance and records or compares transcripts.

  python3 parity/runner.py --app conduit --target http://localhost:8080          # compare
  python3 parity/runner.py --app conduit --target http://localhost:8080 --record # re-record

Each scenario writes one file: parity/<app>/transcripts/<NN>-<name>.json. Compare mode exits 1 on
any difference (and on missing or extra transcripts).
"""
import argparse
import difflib
import importlib
import json
import os
import sys

import requests

from normalize import Normalizer

HERE = os.path.dirname(os.path.abspath(__file__))


class Client:
    def __init__(self, target, normalizer):
        self.target = target.rstrip("/")
        self.normalizer = normalizer
        self.session = requests.Session()
        self.exchanges = []

    def call(self, method, path, body=None, token=None, raw_path=False):
        headers = {"Accept": "application/json"}
        if token:
            headers["Authorization"] = "Token " + token
        url = self.target + path
        req = requests.Request(method, url, json=body, headers=headers)
        prepared = self.session.prepare_request(req)
        if raw_path:
            prepared.url = url
        resp = self.session.send(prepared, timeout=30, allow_redirects=False)
        try:
            parsed = resp.json()
        except ValueError:
            parsed = None
        self.exchanges.append({
            "request": {
                "method": method,
                "path": self.normalizer.text(path),
                "auth": bool(token),
                "body": self.normalizer.value(body) if body is not None else None,
            },
            "response": self.normalizer.response(
                resp.status_code, resp.headers.get("Content-Type"), resp.text),
        })
        return resp.status_code, parsed


def run(app, target):
    scenarios = importlib.import_module("scenarios_%s" % app).SCENARIOS
    state = {}
    results = []
    for index, (name, fn) in enumerate(scenarios, start=1):
        client = Client(target, Normalizer())
        fn(client, state)
        results.append(("%02d-%s.json" % (index, name),
                        json.dumps({"scenario": name, "exchanges": client.exchanges},
                                   indent=2, ensure_ascii=False, sort_keys=False) + "\n"))
    return results


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--app", required=True, choices=["conduit", "loan"])
    parser.add_argument("--target", required=True)
    parser.add_argument("--record", action="store_true")
    args = parser.parse_args()

    sys.path.insert(0, HERE)
    out_dir = os.path.join(HERE, args.app, "transcripts")
    os.makedirs(out_dir, exist_ok=True)
    results = run(args.app, args.target)

    if args.record:
        for stale in os.listdir(out_dir):
            if stale.endswith(".json"):
                os.remove(os.path.join(out_dir, stale))
        for filename, content in results:
            with open(os.path.join(out_dir, filename), "w", encoding="utf-8") as f:
                f.write(content)
        print("recorded %d %s scenarios in %s" % (len(results), args.app, out_dir))
        return 0

    failures = 0
    expected_files = sorted(f for f in os.listdir(out_dir) if f.endswith(".json"))
    actual_files = [f for f, _ in results]
    for missing in sorted(set(expected_files) - set(actual_files)):
        print("MISSING scenario (transcript exists, scenario did not run): %s" % missing)
        failures += 1
    for filename, content in results:
        path = os.path.join(out_dir, filename)
        if not os.path.exists(path):
            print("NEW scenario without transcript: %s" % filename)
            failures += 1
            continue
        with open(path, encoding="utf-8") as f:
            expected = f.read()
        if expected != content:
            failures += 1
            print("DIFF %s" % filename)
            sys.stdout.writelines(difflib.unified_diff(
                expected.splitlines(True), content.splitlines(True),
                fromfile="recorded/" + filename, tofile="target/" + filename))
        else:
            print("ok   %s" % filename)
    print("%s parity: %d scenarios, %d failing" % (args.app, len(results), failures))
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
