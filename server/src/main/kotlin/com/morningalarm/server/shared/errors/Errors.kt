package com.morningalarm.server.shared.errors

open class AppException(
    val code: String,
    override val message: String,
    val details: String? = null,
) : RuntimeException(message)

class ValidationException(
    message: String,
    details: String? = null,
) : AppException("validation_error", message, details)

class NotFoundException(
    message: String,
    details: String? = null,
) : AppException("not_found", message, details)

class ConflictException(
    message: String,
    details: String? = null,
) : AppException("conflict", message, details)

class UnauthorizedException(
    message: String,
    details: String? = null,
) : AppException("unauthorized", message, details)

class ForbiddenException(
    message: String,
    details: String? = null,
) : AppException("forbidden", message, details)

class TooManyRequestsException(
    message: String,
    details: String? = null,
) : AppException("too_many_requests", message, details)
