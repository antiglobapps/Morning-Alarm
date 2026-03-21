# Environments

Morning Alarm has three environments: **local**, **staging**, and **production**.
Each environment uses the same server binary but with different configuration.

---

## Local Development

Start with embedded H2 (no Docker required):

```bash
./gradlew :server:run
```

All defaults are built into `AppConfig.kt`. No environment variables needed.

| Variable | Value |
|---|---|
| `SERVER_DEV_MODE` | `true` (default) |
| `SERVER_HOST` | `0.0.0.0` |
| `SERVER_PORT` | `8080` |
| `SERVER_DB_URL` | `jdbc:h2:file:./server-data/dev-db;MODE=PostgreSQL;...` |
| `SERVER_JWT_SECRET` | `dev-only-jwt-secret-change-me` |
| `SERVER_ADMIN_ACCESS_SECRET` | `DEV_ADMIN_ACCESS_SECRET` from `DevAdminDefaults.kt` |

Start with local PostgreSQL via Docker Compose:

```bash
docker compose up --build
```

This uses the values in `docker-compose.yml` directly. No `.env` file required for local Docker.

---

## Staging

Staging mirrors production but uses a separate database and separate secrets.
Secrets must be set as environment variables or injected from a secret manager.

Minimum required variables:

```env
SERVER_DEV_MODE=false
SERVER_HOST=0.0.0.0
SERVER_PORT=8080
SERVER_PUBLIC_URL=https://staging.morningalarm.example.com

# Database
SERVER_DB_URL=jdbc:postgresql://<host>:5432/morning_alarm_staging
SERVER_DB_USER=morning_alarm
SERVER_DB_PASSWORD=<staging-db-password>

# JWT
SERVER_JWT_SECRET=<staging-jwt-secret>
SERVER_JWT_ISSUER=morning-alarm-server
SERVER_JWT_AUDIENCE=morning-alarm-app

# Admin access
SERVER_ADMIN_ACCESS_SECRET=<staging-admin-access-secret>
SERVER_ADMIN_BOOTSTRAP_SECRET=<staging-admin-bootstrap-secret>

# Media storage
SERVER_FIREBASE_BUCKET=morning-alarm-staging.appspot.com
SERVER_FIREBASE_CREDENTIALS=/secrets/firebase-staging.json
```

Admin accounts on staging are created via the bootstrap CLI after first deploy:

```bash
./gradlew :server:run \
  --args="--create-admin --email=admin@example.com --secret=<SERVER_ADMIN_BOOTSTRAP_SECRET>"
```

---

## Production

Same variable set as staging, with production-specific values.

```env
SERVER_DEV_MODE=false
SERVER_HOST=0.0.0.0
SERVER_PORT=8080
SERVER_PUBLIC_URL=https://api.morningalarm.app

# Database
SERVER_DB_URL=jdbc:postgresql://<prod-host>:5432/morning_alarm
SERVER_DB_USER=morning_alarm
SERVER_DB_PASSWORD=<prod-db-password>

# JWT — rotate separately, invalidates all active sessions
SERVER_JWT_SECRET=<prod-jwt-secret>
SERVER_JWT_ISSUER=morning-alarm-server
SERVER_JWT_AUDIENCE=morning-alarm-app

# Admin access
SERVER_ADMIN_ACCESS_SECRET=<prod-admin-access-secret>
SERVER_ADMIN_BOOTSTRAP_SECRET=<prod-admin-bootstrap-secret>

# Admin login brute-force (optional, defaults are safe)
SERVER_ADMIN_LOGIN_MAX_ATTEMPTS=5
SERVER_ADMIN_LOGIN_WINDOW_SECONDS=300

# Media storage
SERVER_FIREBASE_BUCKET=morning-alarm-prod.appspot.com
SERVER_FIREBASE_CREDENTIALS=/secrets/firebase-prod.json

# Media limits (optional)
SERVER_MEDIA_MAX_IMAGE_BYTES=5242880
SERVER_MEDIA_MAX_AUDIO_BYTES=52428800
```

Production secret values must **never** be committed to the repository.
See `docs/configuration/secrets.md` for the full catalog and ownership rules.

---

## Environment Differences Summary

| Feature | Local | Staging | Production |
|---|---|---|---|
| DB | H2 embedded or local Postgres | Postgres (separate instance) | Postgres (managed) |
| Dev admin auto-created | Yes (empty DB) | No | No |
| JWT secret | hardcoded dev value | env var | env var, rotated on breach |
| Media storage | local filesystem | Firebase (staging bucket) | Firebase (prod bucket) |
| Admin access secret | `DevAdminDefaults` fallback | env var | env var, rotated on breach |
| `SERVER_DEV_MODE` | `true` | `false` | `false` |
