# Shared Module

This document describes shared API contracts and data models used by both clients and server.

## Purpose

The `shared` module contains cross-platform serializable contracts:
- route constants
- header constants
- request/response DTOs
- standardized error payloads

Base packages:
- `com.morningalarm.api.*`
- `com.morningalarm.dto.*`

Shared header constants:
- `ApiHeaders.REQUEST_ID` — request tracing header shared by clients and server
- `ApiHeaders.ADMIN_SECRET` — admin access header required by desktop-admin and protected admin APIs

Contract split:
- public app/client contracts stay under `com.morningalarm.api.*` and `com.morningalarm.dto.*`
- admin-only contracts for desktop administration live under `com.morningalarm.api.admin.*` and `com.morningalarm.dto.admin.*`

## Auth Contracts

Authentication contracts live under:
- `com.morningalarm.api.auth`
- `com.morningalarm.dto.auth`

Implemented auth endpoints and models:
- `POST /api/v1/auth/social` — social authorization by provider and opaque social token
- `POST /api/v1/auth/email/register` — register user by email/password
- `POST /api/v1/auth/email/login` — login by email/password
- `POST /api/v1/auth/admin/login` — admin login with additional admin access secret verification
- `POST /api/v1/auth/password/reset/request` — request password reset by email
- `POST /api/v1/auth/password/reset/confirm` — confirm password reset with reset token
- `POST /api/v1/auth/token/refresh` — exchange refresh token for a new bearer token pair

Key DTOs:
- `AuthSessionDto` — issued auth session with user id, role, bearer token, refresh token, expiry, and `isNewUser`
- `SocialAuthRequestDto` / `SocialAuthResponseDto`
- `EmailRegisterRequestDto` / `EmailRegisterResponseDto`
- `EmailLoginRequestDto` / `EmailLoginResponseDto`
- `AdminLoginRequestDto` / `AdminLoginResponseDto`
- `PasswordResetRequestDto` / `PasswordResetRequestResponseDto`
- `PasswordResetConfirmRequestDto` / `PasswordResetConfirmResponseDto`
- `RefreshTokenRequestDto` / `RefreshTokenResponseDto`
- `UserRoleDto` — business role of the authenticated user (`ADMIN` or `USER`)
- `ApiError` — standardized error response for all server modules

## Ringtone Contracts

Ringtone contracts live under:
- `com.morningalarm.api.ringtone`
- `com.morningalarm.dto.ringtone`

Implemented ringtone endpoints and models:
- `GET /api/v1/ringtones` — list ringtones with `likesCount` and `isLikedByUser`
- `GET /api/v1/ringtones/{ringtoneId}` — get one ringtone with user-specific like state
- `POST /api/v1/ringtones/{ringtoneId}/like-toggle` — toggle current user's like for a ringtone

Key DTOs:
- `RingtoneListItemDto` — ringtone card/detail data with media URLs, premium flag, like state, and likes count
- `RingtoneListResponseDto`
- `RingtoneDetailResponseDto`
- `ToggleRingtoneLikeResponseDto`

## Admin Ringtone Contracts

Admin ringtone contracts live under:
- `com.morningalarm.api.admin.ringtone`
- `com.morningalarm.dto.admin.ringtone`

Implemented admin endpoints and contracts:
- `GET /api/v1/admin/ringtones` — list all ringtones for admin management
- `GET /api/v1/admin/ringtones/{ringtoneId}` — get full ringtone detail for admin
- `POST /api/v1/admin/ringtones` — create ringtone with full admin metadata
- `PUT /api/v1/admin/ringtones/{ringtoneId}` — update ringtone with full admin metadata
- `DELETE /api/v1/admin/ringtones/{ringtoneId}` — delete ringtone
- `GET /api/v1/admin/ringtones/{ringtoneId}/preview` — get preview data for a user-facing ringtone card
- `GET /api/v1/admin/ringtones/client-preview` — get the ringtone list as it should look to users
- `POST /api/v1/admin/ringtones/{ringtoneId}/active-toggle` — toggle active status
- `POST /api/v1/admin/ringtones/{ringtoneId}/premium-toggle` — toggle premium status

Key DTOs:
- `AdminRingtoneListItemDto` — admin list row with media URLs, activation, premium state, and likes count
- `AdminRingtoneDetailDto` — full admin ringtone detail with embedded user-card preview
- `CreateAdminRingtoneRequestDto` / `CreateAdminRingtoneResponseDto`
- `UpdateAdminRingtoneRequestDto` / `UpdateAdminRingtoneResponseDto`
- `DeleteAdminRingtoneResponseDto`
- `AdminRingtonePreviewResponseDto`
- `AdminRingtoneClientListPreviewResponseDto`
- `ToggleRingtoneActiveResponseDto`
- `ToggleRingtonePremiumResponseDto`

Admin ringtone model fields:
- `title`
- `description`
- `imageUrl`
- `audioUrl`
- `durationSeconds`
- `isActive`
- `isPremium`
- `likesCount`
- `createdAtEpochSeconds`
- `updatedAtEpochSeconds`

## Admin Upload Contracts

Upload contracts live under:
- `com.morningalarm.api.admin.upload`
- `com.morningalarm.dto.upload`

Implemented upload endpoints and contracts:
- `POST /api/v1/admin/uploads/image` — upload image media for admin content management
- `POST /api/v1/admin/uploads/audio` — upload audio media for admin content management

Key DTOs:
- `MediaKindDto` — uploaded media type (`IMAGE`, `AUDIO`)
- `UploadedMediaDto` — absolute media URL and file metadata
- `UploadImageResponseDto`
- `UploadAudioResponseDto`

## Maintenance Rule

When a new shared DTO, route, header, or contract model is added, update this file with:
- model or contract name
- package
- short purpose description
