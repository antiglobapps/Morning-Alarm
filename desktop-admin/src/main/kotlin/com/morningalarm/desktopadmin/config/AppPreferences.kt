package com.morningalarm.desktopadmin.config

import java.util.prefs.Preferences

enum class ConnectionMode { DEV, CUSTOM }

class AppPreferences {
    private val preferences = Preferences.userRoot().node("com.morningalarm.desktopadmin")

    var connectionMode: ConnectionMode
        get() = runCatching { ConnectionMode.valueOf(preferences.get(CONNECTION_MODE_KEY, ConnectionMode.DEV.name)) }
            .getOrDefault(ConnectionMode.DEV)
        set(value) = preferences.put(CONNECTION_MODE_KEY, value.name)

    var customBaseUrl: String
        get() = preferences.get(CUSTOM_BASE_URL_KEY, "")
        set(value) = preferences.put(CUSTOM_BASE_URL_KEY, value)

    val resolvedBaseUrl: String
        get() = when (connectionMode) {
            ConnectionMode.DEV -> DEV_BASE_URL
            ConnectionMode.CUSTOM -> customBaseUrl.ifBlank { DEV_BASE_URL }
        }

    companion object {
        const val CONNECTION_MODE_KEY = "connection_mode"
        const val CUSTOM_BASE_URL_KEY = "custom_base_url"
        const val DEV_BASE_URL = "http://localhost:8080"
    }
}
