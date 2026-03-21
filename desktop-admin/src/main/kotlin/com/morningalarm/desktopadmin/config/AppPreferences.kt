package com.morningalarm.desktopadmin.config

import java.util.prefs.Preferences

class AppPreferences {
    private val preferences = Preferences.userRoot().node("com.morningalarm.desktopadmin")

    var baseUrl: String
        get() = preferences.get(BASE_URL_KEY, DEFAULT_BASE_URL)
        set(value) = preferences.put(BASE_URL_KEY, value)

    private companion object {
        const val BASE_URL_KEY = "base_url"
        const val DEFAULT_BASE_URL = "http://localhost:8080"
    }
}
