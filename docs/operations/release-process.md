# Release Process

Morning Alarm has three independent deliverables with separate release flows.

---

## 1. Server (`server`)

### Build

```bash
./gradlew :server:installDist
# Output: server/build/install/server/
```

Docker image:

```bash
docker build -t morning-alarm-server:latest .
```

### Deploy

The server is deployed as a Docker container. The recommended flow:

1. Build and tag the image with the commit SHA:
   ```bash
   docker build -t ghcr.io/<org>/morning-alarm-server:$COMMIT_SHA .
   docker push ghcr.io/<org>/morning-alarm-server:$COMMIT_SHA
   ```
2. On the target host, pull and restart:
   ```bash
   docker pull ghcr.io/<org>/morning-alarm-server:$COMMIT_SHA
   docker compose up -d --no-deps server
   ```
3. Verify health:
   ```bash
   curl https://api.morningalarm.app/health/ready
   ```

### Schema changes

Until migration tooling is introduced (see `docs/operations/migration-strategy.md`), the server
bootstraps its schema on startup. No manual DB step is required for additive changes.
Breaking schema changes require a coordinated deploy — see the migration strategy doc.

### Rollback

Re-deploy the previous image tag. The H2/Postgres schema is backwards-compatible for additive changes.

### Compatibility check

Before deploy, verify that the `shared` module contracts are compatible with the running clients:
- Server and desktop-admin must use the same version of `shared` DTOs.
- Check `shared/AGENTS.md` for the current contract version.

---

## 2. Web (`web`)

The `web` module is an [Astro](https://astro.build) static site.

### Build

```bash
cd web
npm ci
npm run build
# Output: web/dist/
```

### Deploy

The `dist/` directory is a static site and can be hosted anywhere:

**Netlify** (recommended):
- Connect the repo to Netlify.
- Set build command: `cd web && npm ci && npm run build`
- Set publish directory: `web/dist`
- Set environment variables in Netlify dashboard if needed.

**Manual / S3-compatible**:
```bash
aws s3 sync web/dist/ s3://morning-alarm-web --delete
aws cloudfront create-invalidation --distribution-id $CF_ID --paths "/*"
```

### Release trigger

`web` has no runtime dependency on `server`. It can be released independently at any time.
Changes to App Store links, screenshots, FAQ content, or legal pages do not require a server deploy.

---

## 3. Desktop Admin (`desktop-admin`)

The desktop admin is a Compose Desktop application distributed as a native binary.

### Build native distribution

```bash
# macOS (.dmg)
./gradlew :desktop-admin:packageDmg

# Windows (.msi)
./gradlew :desktop-admin:packageMsi

# Linux (.deb)
./gradlew :desktop-admin:packageDeb

# Current OS (useful for CI)
./gradlew :desktop-admin:packageDistributionForCurrentOS
```

Output: `desktop-admin/build/compose/binaries/`

### Distribution

`desktop-admin` is an **internal tool** — it is not published to app stores.
Distribution options:
- Share the binary directly with trusted operators via a private channel.
- Attach the binary to a GitHub Release as an artifact.
- Host in a private S3 bucket with access control.

### Version

Package version is set in `desktop-admin/build.gradle.kts`:

```kotlin
nativeDistributions {
    packageVersion = "0.1.0"
}
```

Bump this before each distribution.

### Compatibility with server

`desktop-admin` communicates with `server` using shared DTOs from the `shared` module.
Both must be built from the same version of `shared` to avoid deserialization errors.
Always release `server` before distributing a new `desktop-admin` binary that uses new API contracts.

---

## Contract Compatibility Check

The `shared` module contains all DTOs and route constants used by both server and clients.

Rule: **server is released first, clients second.**

Before releasing `desktop-admin`:
1. Confirm that `server` on the target environment is already running the matching version.
2. Check that no `@Serializable` DTO was removed or had a required field added without a default.

Before releasing `server`:
1. Confirm that all new fields in response DTOs have defaults (backwards-compatible for existing clients).
2. Run `./gradlew :server:validateOpenApi` to confirm the schema is consistent.
