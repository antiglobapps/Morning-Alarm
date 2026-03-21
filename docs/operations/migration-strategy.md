# Database Migration Strategy

## Current State

The server bootstraps its database schema on startup using hand-written DDL in schema classes:
- `AuthDatabaseSchema`
- `UserDatabaseSchema`
- `RingtoneDatabaseSchema`

Each class runs `CREATE TABLE IF NOT EXISTS` on startup. This is safe for new deployments
and for additive changes (new tables, new nullable columns with defaults), but it cannot
handle destructive changes (column renames, type changes, constraint additions on existing data).

**Rule:** Do not add Flyway/Liquibase migration files until an explicit release-preparation command is given.

---

## Chosen Tooling: Flyway

When migration tooling is introduced, the project will use **[Flyway](https://flywaydb.org)**.

Reasons:
- SQL-based migrations — no DSL to learn, easy to review
- Works with both H2 (dev) and PostgreSQL (prod) out of the box
- Simple Gradle integration (`org.flywaydb:flyway-core`)
- Widely adopted, well-documented

---

## Migration Plan (when the time comes)

### Step 1 — Add Flyway dependency

```kotlin
// server/build.gradle.kts
implementation("org.flywaydb:flyway-core:10.x.x")
runtimeOnly("org.flywaydb:flyway-database-postgresql:10.x.x")
```

### Step 2 — Create baseline migration

Generate a single baseline SQL file that captures the current schema
(everything that the existing `*DatabaseSchema` classes create):

```
server/src/main/resources/db/migration/V1__baseline_schema.sql
```

This file represents the schema as of the migration cutover point.

### Step 3 — Remove bootstrap DDL

Remove `CREATE TABLE IF NOT EXISTS` calls from `AuthDatabaseSchema`, `UserDatabaseSchema`,
and `RingtoneDatabaseSchema`. Replace with Flyway initialization in `createModuleDependencies`.

### Step 4 — Configure Flyway in `Modules.kt`

```kotlin
Flyway.configure()
    .dataSource(dataSource)
    .locations("classpath:db/migration")
    .load()
    .migrate()
```

### Step 5 — All future schema changes as versioned migrations

Every schema change after cutover must be a new migration file:

```
V2__add_ringtone_tags.sql
V3__add_user_preferences.sql
```

---

## Naming Convention for Migration Files

```
V{version}__{description}.sql
```

- Version: integer, monotonically increasing (`V1`, `V2`, …)
- Description: snake_case summary of the change
- Examples:
  - `V1__baseline_schema.sql`
  - `V2__add_password_reset_token_index.sql`
  - `V3__add_ringtone_sort_order.sql`

---

## Dev vs Prod Behaviour

| Scenario | Behaviour |
|---|---|
| Fresh local H2 database | Flyway runs all migrations from V1 |
| Existing prod database at cutover | Flyway baseline marks V1 as already applied |
| New migration added | Flyway applies it on next server startup |
| Migration checksum mismatch | Flyway throws — never edit applied migrations |

---

## What Not To Do

- Do not edit an already-applied migration file — Flyway will detect the checksum mismatch and refuse to start.
- Do not delete migration files from the codebase.
- Do not run raw `ALTER TABLE` on prod without a corresponding migration file.
