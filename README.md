# Morning-Alarm

Morning Alarm Apps

## AI Agent Setup

This repository contains shared configuration for both `Claude Code` and `Codex CLI`.

### Codex CLI

- Project instructions live in `AGENTS.md`
- Repository defaults live in `.codex/config.toml`
- Shared team skills live in `.agents/skills/`
- Personal overrides should stay in `~/.codex/config.toml`

Recommended usage:

```bash
codex --profile implement
codex --profile review
codex --profile deep
```

### Claude Code

- Claude-specific project instructions live in `CLAUDE.md`
- Claude-specific settings live in `.claude/`

## Server Docker

Local Docker setup for the backend is included.

Run:

```bash
docker compose up --build
```

Available endpoints after startup:

- `http://localhost:8080/swagger`
- `http://localhost:8080/openapi`
- `http://localhost:8080/openapi.yaml`

Services:
- `postgres` — PostgreSQL 16 on `localhost:5432`
- `server` — Ktor backend on `localhost:8080`

## Dev Admin Defaults

In local dev mode with an empty server auth database, the server auto-creates a default admin user.
The desktop admin login form pre-fills the same shared credentials.

- `Email`: `admin@example.com`
- `Password`: `admin12345`
- `Admin Secret`: `admin-dev-secret`

## Secret Registry

Canonical secret and credential identifiers are documented in:

- `docs/configuration/secrets.md`
- `config/secrets.catalog.toml`

The registry stores identifiers, ownership, and dev/prod loading rules.
Production values must not be committed to the repository.

## Gradle

The repository now includes a Gradle wrapper. Use:

```bash
./gradlew tasks
./gradlew :desktop-admin:build
```

The desktop admin module is configured with a Java 21 toolchain, so local builds require a JDK 21 installation.
Dependency resolution uses both `google()` and `mavenCentral()` because Compose Desktop pulls AndroidX artifacts during desktop builds.

## CI

GitHub Actions CI is defined in `.github/workflows/ci.yml`.

It now uses selective execution based on changed module paths.

Rules:

- every `pull_request` runs path detection first
- PRs targeting `release/*` always run the full suite, regardless of changed files
- pushes to `main` and `release/*` also run the full suite
- `server` changes trigger `:shared:check`, `:server:check`, OpenAPI diff validation, and dependent client checks
- `desktop-admin` checks start an isolated local dev server, wait for `/health/ready`, seed test data through the real admin API, and then run `:desktop-admin:check`
- `web` changes run `npm ci`, `npm run test --if-present`, and `npm run build`
- `design-app` changes run `npm ci`, `npm run test --if-present`, and `npm run build`

Reusable helper scripts for isolated client environments live in `scripts/ci/`.

The repository does not currently contain `android/` or `ios/` application modules. The selective workflow is already structured so future mobile client jobs can follow the same pattern: one isolated local dev server per client job, seeded with client-owned test data.
