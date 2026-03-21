package com.morningalarm.api.auth

object AuthRoutes {
    const val BASE = "/api/v1/auth"
    const val SOCIAL = "$BASE/social"
    const val EMAIL_REGISTER = "$BASE/email/register"
    const val EMAIL_LOGIN = "$BASE/email/login"
    const val PASSWORD_RESET_REQUEST = "$BASE/password/reset/request"
    const val PASSWORD_RESET_CONFIRM = "$BASE/password/reset/confirm"
    const val TOKEN_REFRESH = "$BASE/token/refresh"
    const val ADMIN_LOGIN = "$BASE/admin/login"
}
