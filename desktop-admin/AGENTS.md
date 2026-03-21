# Desktop Admin Module

This document describes the purpose and maintenance rules for the internal `desktop-admin` module.

## Purpose

`desktop-admin` is the internal desktop administration tool for Morning Alarm.

It is intended for:
- authenticated admin access to the backend
- content management flows such as ringtone administration
- previewing admin-managed content before it reaches end users

## Current Status

The module now contains a Compose Desktop MVP for internal ringtone administration.

Implemented parts:
- application entry point at `src/main/kotlin/com/morningalarm/desktopadmin/Main.kt`
- persistent base URL preference in `config/AppPreferences.kt`
- centralized admin API client in `data/AdminApiClient.kt`
- login screen with admin email/password/admin secret
- in-memory admin session handling with logout and session-expiration fallback
- ringtone management workspace with:
  - list/search
  - create/edit form
  - image/audio upload
  - active/premium toggles
  - delete action
  - preview of selected ringtone card
  - preview of current client-visible ringtone list

Main package structure:
- `config` — local desktop preferences and runtime configuration
- `data` — HTTP client, session-expiration handling, API error mapping
- `ui` — Compose Desktop screens and admin workspace

## Rules

- Reuse shared DTOs and route contracts from the `shared` module whenever desktop-admin talks to the server.
- Do not treat the desktop client as a security boundary. Real access control must always be enforced by the server.
- Never store production secrets in the repository or hardcode privileged credentials in the client.
- Keep bearer token and admin secret only in runtime memory unless a dedicated secure storage decision is documented and implemented.
- React to `401` by dropping the current session and returning the operator to the login screen.
- Any new screen, package, or admin workflow added here must be documented in this file.
