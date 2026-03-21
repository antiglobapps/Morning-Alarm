# Secrets Registry

This document is the human-readable registry for project secret and credential identifiers.

Canonical machine-readable source:
- `config/secrets.catalog.toml`

This registry stores identifiers, ownership, and loading policy.
It does not store production values.

## Rules

- Production secret values must come from environment variables or an external secret manager.
- Dev-only defaults may live in code only when the catalog explicitly marks them as `code-default`.
- Each module keeps its own loader; this registry is naming and ownership metadata, not a runtime container.
- Non-secret environment variables such as host, port, public URLs, and media limits stay in module docs and are intentionally not duplicated here.
- When a new secret or credential identifier appears, update this file, `config/secrets.catalog.toml`, and the nearest module `AGENTS.md` in the same change set.

## Shared Dev Credentials

These entries are committed to the repository on purpose and are valid only for local development.

| Catalog ID | Owner | Used by | Load style | Purpose |
| --- | --- | --- | --- | --- |
| `DEV_ADMIN_EMAIL` | `shared` | `server`, `desktop-admin` | code default | Default local admin login identifier for an empty dev auth database. |
| `DEV_ADMIN_PASSWORD` | `shared` | `server`, `desktop-admin` | code default | Stable dev-only password for the default local admin account. |
| `DEV_ADMIN_ACCESS_SECRET` | `shared` | `server`, `desktop-admin` | code default | Dev-only fallback secret for admin login and protected admin routes. |

Current implementation source:
- `shared/src/commonMain/kotlin/com/morningalarm/api/auth/DevAdminDefaults.kt`

## Server Runtime Secrets

These entries belong to the `server` module and are loaded from the environment in production.

| Catalog ID | Used by | Dev policy | Prod policy | Purpose |
| --- | --- | --- | --- | --- |
| `SERVER_DB_PASSWORD` | `server` | optional | required from env or secret manager | Database password for backend runtime connections. |
| `SERVER_JWT_SECRET` | `server` | dev-only fallback exists | required from env or secret manager | JWT signing secret for bearer tokens. |
| `SERVER_ADMIN_BOOTSTRAP_SECRET` | `server` | optional | required for CLI admin bootstrap flows | Secret for `--create-admin` and `--promote-admin` server commands. |
| `SERVER_ADMIN_ACCESS_SECRET` | `server`, `desktop-admin` | falls back to `DEV_ADMIN_ACCESS_SECRET` | required from env or secret manager | Extra admin access secret for admin login and protected admin APIs. |

Current loader:
- `server/src/main/kotlin/com/morningalarm/server/bootstrap/AppConfig.kt`

## Desktop Admin Runtime Inputs

`desktop-admin` does not own repository-level production secrets.
It consumes:
- shared dev credentials from `DevAdminDefaults` for localhost login autofill
- operator-provided admin credentials for non-dev servers
- the same server-defined admin access secret that protects admin APIs

The desktop client must not invent local secret identifiers outside the central catalog.

## Local Docker Infrastructure

| Catalog ID | Used by | Scope | Purpose |
| --- | --- | --- | --- |
| `POSTGRES_PASSWORD` | `postgres`, `server` | local Docker only | Password for the bundled PostgreSQL container. |

Current loader:
- `docker-compose.yml`

## Design Tooling

| Catalog ID | Used by | Load style | Purpose |
| --- | --- | --- | --- |
| `FIGMA_TOKEN` | `design-app` script tooling | manual CLI argument | Personal Figma token for `design-app/scripts/create-figma-variables.mjs`. |

Note:
- The current script reads the token as a CLI argument, not as an environment variable.
- The catalog still reserves `FIGMA_TOKEN` as the canonical identifier for operator documentation and future loader unification.

## Modules Without Catalog Entries Right Now

- `android`
- `ios`
- `app-shared`
- `web`

These modules currently do not define repository-level secret identifiers.
