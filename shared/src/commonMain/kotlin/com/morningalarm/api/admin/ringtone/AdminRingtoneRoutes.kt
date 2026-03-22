package com.morningalarm.api.admin.ringtone

object AdminRingtoneRoutes {
    const val BASE = "/api/v1/admin/ringtones"

    const val LIST = BASE
    const val CREATE = BASE
    const val CLIENT_LIST_PREVIEW = "$BASE/client-preview"

    const val DETAIL = "$BASE/{ringtoneId}"
    const val UPDATE = "$BASE/{ringtoneId}"
    const val DELETE = "$BASE/{ringtoneId}"
    const val PREVIEW = "$BASE/{ringtoneId}/preview"
    const val SET_VISIBILITY = "$BASE/{ringtoneId}/visibility"
    const val TOGGLE_PREMIUM = "$BASE/{ringtoneId}/premium-toggle"
}
