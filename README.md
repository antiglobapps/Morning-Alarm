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
