# Monitoring Baseline

This document describes the minimum monitoring setup for the Morning Alarm server.
All listed capabilities are already implemented — this is a guide to wiring them into infrastructure.

---

## 1. Health Probes

Two endpoints are available out of the box:

| Endpoint | Purpose | Expected response |
|---|---|---|
| `GET /health/live` | Liveness — server process is running | `200 OK` / body `OK` |
| `GET /health/ready` | Readiness — server is ready to serve traffic | `200 OK` / body `OK` |

**Usage:**
- Container orchestrators (Docker, Kubernetes) should use `/health/live` for restart decisions.
- Load balancers should use `/health/ready` to gate traffic.
- In `docker-compose.yml`, the healthcheck is already configured against `/health/live`.

---

## 2. Request Tracing

Every request receives a unique `X-Request-Id` header (UUID).

- If the client sends `X-Request-Id`, the server echoes it back.
- If not, the server generates one.
- The header is returned in every response, including error responses.
- All `ApiError` response bodies include `requestId` for correlation.

**To trace a specific request:** search server logs by the request ID value.

---

## 3. Audit Log

Key admin and recovery actions are written to a dedicated SLF4J logger named `audit`.

Logged events (see `server/AGENTS.md` §14 for the full list):
- `ADMIN_CREATED`, `ADMIN_PROMOTED`
- `ADMIN_LOGIN_SUCCESS`, `ADMIN_LOGIN_FAILURE`
- `PASSWORD_RESET_REQUESTED`, `PASSWORD_RESET_CONFIRMED`, `SESSIONS_REVOKED`
- `RINGTONE_CREATED/UPDATED/DELETED/ACTIVE_TOGGLED/PREMIUM_TOGGLED`
- `MEDIA_UPLOADED`

**Log format example:**
```
[audit] WARN  ADMIN_LOGIN_FAILURE email=admin@example.com reason=Invalid admin secret
[audit] INFO  RINGTONE_CREATED ringtoneId=rng_abc123 title="Gentle Rain" adminId=usr_xyz
```

To separate audit logs from application logs in Logback, add a dedicated appender for the `audit` logger
in `server/src/main/resources/logback.xml`.

---

## 4. Alert Baselines

The following conditions should trigger alerts in any production monitoring system:

| Condition | Suggested threshold | Severity |
|---|---|---|
| `ADMIN_LOGIN_FAILURE` events | > 3 in 5 min for the same email | Warning |
| HTTP 429 responses on `/api/v1/auth/admin/login` | Any | Warning |
| HTTP 5xx responses | > 1% of requests over 5 min | Critical |
| `/health/ready` returning non-200 | Any | Critical |
| DB connection pool exhaustion | Pool usage > 90% | Warning |

These are implemented in the monitoring system of your choice (Grafana, Datadog, CloudWatch, etc.)
using the server logs and health endpoint as inputs.

---

## 5. Structured Logging Recommendations

The server currently uses Logback with default formatting. For production:

1. Switch to JSON log format (add `logstash-logback-encoder` or similar).
2. Include `X-Request-Id` in every log line via MDC — the `CallId` plugin already propagates it.
3. Ship logs to a centralized store (e.g., Loki, CloudWatch Logs, Papertrail).

These are infrastructure decisions deferred until a deployment target is chosen.

---

## 6. OpenAPI / Swagger

The server exposes its API documentation at:

| Path | Description |
|---|---|
| `/swagger` | Interactive Swagger UI |
| `/openapi` | OpenAPI HTML rendering |
| `/openapi.yaml` | Raw OpenAPI schema (machine-readable) |

These endpoints are always active — no auth required. Do not expose them on public prod without rate limiting or IP restriction if the API is internal-only.
