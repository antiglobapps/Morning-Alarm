package com.morningalarm.api.ringtone

object RingtoneRoutes {
    const val BASE = "/api/v1/ringtones"
    const val LIST = BASE
    const val DETAIL = "$BASE/{ringtoneId}"
    const val TOGGLE_LIKE = "$BASE/{ringtoneId}/like-toggle"
}
