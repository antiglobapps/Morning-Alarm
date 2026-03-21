package com.morningalarm.desktopadmin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.morningalarm.api.auth.DevAdminDefaults
import com.morningalarm.desktopadmin.config.AppPreferences
import com.morningalarm.desktopadmin.config.ConnectionMode

@Composable
internal fun LoginScreen(
    preferences: AppPreferences,
    errorMessage: String?,
    inProgress: Boolean,
    onLogin: (baseUrl: String, email: String, password: String, adminSecret: String) -> Unit,
) {
    var connectionMode by rememberSaveable { mutableStateOf(preferences.connectionMode) }
    var customBaseUrl by rememberSaveable { mutableStateOf(preferences.customBaseUrl) }
    var email by rememberSaveable { mutableStateOf(DevAdminDefaults.EMAIL) }
    var password by rememberSaveable { mutableStateOf(DevAdminDefaults.PASSWORD) }
    var adminSecret by rememberSaveable { mutableStateOf(DevAdminDefaults.ACCESS_SECRET) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F141D)),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.width(440.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF17202F)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Morning Alarm Admin", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Выберите режим подключения к серверу.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB4C0D4),
                )
                Text(
                    "Dev mode pre-fills shared local admin credentials.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                )

                ConnectionModeSelector(
                    selected = connectionMode,
                    onSelect = { connectionMode = it },
                )

                if (connectionMode == ConnectionMode.CUSTOM) {
                    OutlinedTextField(
                        value = customBaseUrl,
                        onValueChange = { customBaseUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Server Base URL") },
                        placeholder = { Text("https://example.com") },
                        singleLine = true,
                    )
                } else {
                    Text(
                        "Сервер: ${AppPreferences.DEV_BASE_URL}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7C8CFF),
                    )
                }

                HorizontalDivider(color = Color(0xFF2A3448))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = adminSecret,
                    onValueChange = { adminSecret = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Admin Secret") },
                    singleLine = true,
                )

                if (!errorMessage.isNullOrBlank()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        preferences.connectionMode = connectionMode
                        val baseUrl = when (connectionMode) {
                            ConnectionMode.DEV -> AppPreferences.DEV_BASE_URL
                            ConnectionMode.CUSTOM -> {
                                preferences.customBaseUrl = customBaseUrl
                                customBaseUrl.trim()
                            }
                        }
                        onLogin(baseUrl, email.trim(), password, adminSecret.trim())
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !inProgress,
                ) {
                    if (inProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Log In")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionModeSelector(
    selected: ConnectionMode,
    onSelect: (ConnectionMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ConnectionMode.entries.forEach { mode ->
            val isSelected = mode == selected
            val label = when (mode) {
                ConnectionMode.DEV -> "Dev (localhost)"
                ConnectionMode.CUSTOM -> "Custom server"
            }
            Button(
                onClick = { onSelect(mode) },
                modifier = Modifier.weight(1f).height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(0xFFFFB86B) else Color(0xFF2A3448),
                    contentColor = if (isSelected) Color(0xFF10151F) else Color(0xFFB4C0D4),
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}
