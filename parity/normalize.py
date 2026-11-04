"""Normalization rules applied to every recorded response before it is stored or compared.

Rules (also documented in parity/README.md):
  1. JWTs (three base64url segments starting with "eyJ") -> "<JWT:n>", n numbered by first appearance.
  2. UUIDs -> "<UUID:n>", numbered by first appearance within the scenario run.
  3. ISO-8601 timestamps (date + time) -> "<TIMESTAMP>". Plain dates (YYYY-MM-DD) are kept.
  4. JSON object keys are sorted; array order is kept (order is behaviour).
  5. Only status code, Content-Type (without charset) and the body are kept from the response.
  6. Arrays that are sets in the API ("tagList", and "tags" from GET /tags) are sorted, because
     SQLite returns them in no guaranteed order.
  7. Request paths go through rules 1-3 too (e.g. comment IDs in DELETE URLs).
  8. Non-JSON bodies are kept as text with rules 1-3 applied; Tomcat HTML error pages are reduced
     to their <title>.
"""
import json
import re

JWT_RE = re.compile(r"eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+")
UUID_RE = re.compile(r"\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\b")
TS_RE = re.compile(r"\b\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:?\d{2})?")
TITLE_RE = re.compile(r"<title>(.*?)</title>", re.S | re.I)


SET_KEYS = {"tagList", "tags"}


class Normalizer:
    def __init__(self):
        self.jwts = {}
        self.uuids = {}

    def _label(self, table, prefix, value):
        if value not in table:
            table[value] = "<%s:%d>" % (prefix, len(table) + 1)
        return table[value]

    def text(self, value):
        value = JWT_RE.sub(lambda m: self._label(self.jwts, "JWT", m.group(0)), value)
        value = UUID_RE.sub(lambda m: self._label(self.uuids, "UUID", m.group(0).lower()), value)
        return TS_RE.sub("<TIMESTAMP>", value)

    def value(self, value):
        if isinstance(value, str):
            return self.text(value)
        if isinstance(value, list):
            return [self.value(v) for v in value]
        if isinstance(value, dict):
            out = {}
            for k in sorted(value):
                v = self.value(value[k])
                if k in SET_KEYS and isinstance(v, list) and all(isinstance(i, str) for i in v):
                    v = sorted(v)
                out[k] = v
            return out
        return value

    def response(self, status, content_type, body_text):
        content_type = (content_type or "").split(";")[0].strip()
        body = None
        if body_text:
            try:
                body = self.value(json.loads(body_text))
            except ValueError:
                title = TITLE_RE.search(body_text)
                body = self.text(title.group(1).strip() if title else body_text.strip())
        return {"status": status, "contentType": content_type, "body": body}
