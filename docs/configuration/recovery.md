# Admin Recovery Procedures

This document describes the procedures for recovering admin access and responding to security incidents.

---

## 1. Password Reset (Admin Account)

If an admin loses their password, the operator with server access can reset it:

```bash
# Request a password reset email (if email gateway is configured)
# The token is logged by InMemoryAuthEmailGateway in dev mode

# On the server host — create a fresh admin account with a new temporary password
./gradlew :server:run --args="--create-admin --email=admin@example.com --secret=<SERVER_ADMIN_BOOTSTRAP_SECRET>"
```

After reset, the admin must change the temporary password immediately via the admin login flow.

**Important:** `confirmPasswordReset` automatically revokes all existing refresh tokens for that user.
Any active desktop-admin sessions will require a fresh login.

---

## 2. Promote an Existing User to Admin

If admin access must be granted to an existing user (e.g., after a team change):

```bash
./gradlew :server:run --args="--promote-admin --email=<email> --secret=<SERVER_ADMIN_BOOTSTRAP_SECRET>"
```

This operation is logged as `ADMIN_PROMOTED` in the audit log.

---

## 3. Admin Access Secret Rotation

`SERVER_ADMIN_ACCESS_SECRET` is used as a second factor for admin login and all protected admin API calls.

Rotation procedure:
1. Generate a new secret (e.g., `openssl rand -hex 32`).
2. Update the secret in the server environment / secret manager.
3. Restart the server to pick up the new value.
4. Update `SERVER_ADMIN_ACCESS_SECRET` in the desktop-admin configuration.
5. All existing admin sessions remain valid (bearer tokens are not affected),
   but future admin API calls will require the new header value.

---

## 4. JWT Secret Rotation

Rotating `SERVER_JWT_SECRET` immediately invalidates all active bearer tokens.

Rotation procedure:
1. Generate a new secret.
2. Update `SERVER_JWT_SECRET` in the environment.
3. Restart the server.
4. All clients must log in again to receive new tokens.

---

## 5. Full Admin Lockout Recovery

If no admin account exists or all admin accounts are inaccessible:

1. SSH into the server host.
2. Run the bootstrap command with the bootstrap secret:
   ```bash
   SERVER_ADMIN_BOOTSTRAP_SECRET=<secret> ./gradlew :server:run \
     --args="--create-admin --email=recovery@example.com"
   ```
3. The temporary password is printed once to stdout — save it immediately.
4. Log in via the desktop-admin with the temporary password and rotate it.

> The `SERVER_ADMIN_BOOTSTRAP_SECRET` must be known to the operator.
> It is stored separately from `SERVER_ADMIN_ACCESS_SECRET` (see `docs/configuration/secrets.md`).

---

## 6. Incident Response — Suspected Compromise

If an admin credential is believed to be compromised:

1. **Rotate `SERVER_ADMIN_ACCESS_SECRET`** (see §3 above) — blocks all current admin API calls using the old secret.
2. **Rotate `SERVER_JWT_SECRET`** (see §4 above) — invalidates all active bearer tokens.
3. **Reset the compromised account password** (see §1 above) — revokes all refresh tokens.
4. Review the `audit` log for recent `ADMIN_LOGIN_SUCCESS`, `RINGTONE_*`, and `MEDIA_UPLOADED` events.
5. Check for `ADMIN_LOGIN_FAILURE` spikes that may indicate a brute-force attempt.

---

## 7. Secrets Not Stored in Repo

Production secret values are never committed to the repository. See `docs/configuration/secrets.md` for the full catalog.

Dev-only defaults (committed intentionally):
- `DEV_ADMIN_EMAIL`, `DEV_ADMIN_PASSWORD`, `DEV_ADMIN_ACCESS_SECRET` — valid only for local development databases.
- These values are defined in `shared/src/commonMain/kotlin/com/morningalarm/api/auth/DevAdminDefaults.kt`.
