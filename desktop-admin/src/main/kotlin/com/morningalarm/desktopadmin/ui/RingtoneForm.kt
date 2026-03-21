package com.morningalarm.desktopadmin.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
internal fun RingtoneForm(
    draft: RingtoneDraft,
    onDraftChange: (RingtoneDraft) -> Unit,
    onUploadImage: () -> Unit,
    onUploadAudio: () -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?,
    onToggleActive: (() -> Unit)?,
    onTogglePremium: (() -> Unit)?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = draft.title,
            onValueChange = { onDraftChange(draft.copy(title = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Title") },
        )
        OutlinedTextField(
            value = draft.description,
            onValueChange = { onDraftChange(draft.copy(description = it)) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            label = { Text("Description") },
        )
        MediaAttachmentField(
            title = "Image",
            attachedFileName = draft.imageUrl.toAttachedFileName(),
            emptyText = "No image attached yet.",
            uploadedText = "Choose a local file and the admin app will upload it to the server storage automatically.",
            actionLabel = if (draft.imageUrl.isBlank()) "Attach Image" else "Replace Image",
            buttonColor = Color(0xFF3C7FD6),
            onAttach = onUploadImage,
        )
        MediaAttachmentField(
            title = "Audio",
            attachedFileName = draft.audioUrl.toAttachedFileName(),
            emptyText = "No audio attached yet.",
            uploadedText = "Choose a local audio file and the server will store it, then return the URL for this ringtone.",
            actionLabel = if (draft.audioUrl.isBlank()) "Attach Audio" else "Replace Audio",
            buttonColor = Color(0xFF3F9A53),
            onAttach = onUploadAudio,
        )
        OutlinedTextField(
            value = draft.durationSeconds,
            onValueChange = { onDraftChange(draft.copy(durationSeconds = it.filter(Char::isDigit))) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Duration Seconds") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Text(
            "WAV and MP3 uploads auto-fill duration. You can still edit it before saving.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = draft.isActive,
                    onCheckedChange = { onDraftChange(draft.copy(isActive = it)) },
                )
                Text("Active")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = draft.isPremium,
                    onCheckedChange = { onDraftChange(draft.copy(isPremium = it)) },
                )
                Text("Premium")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSave) {
                Text(if (draft.id == null) "Create" else "Save")
            }
            if (onDelete != null) {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
            if (onToggleActive != null) {
                TextButton(onClick = onToggleActive) {
                    Text(if (draft.isActive) "Deactivate" else "Activate")
                }
            }
            if (onTogglePremium != null) {
                TextButton(onClick = onTogglePremium) {
                    Text(if (draft.isPremium) "Unset Premium" else "Set Premium")
                }
            }
        }
    }
}

@Composable
private fun MediaAttachmentField(
    title: String,
    attachedFileName: String?,
    emptyText: String,
    uploadedText: String,
    actionLabel: String,
    buttonColor: Color,
    onAttach: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF192230)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (attachedFileName == null) {
                Text(emptyText, color = Color(0xFFB4C0D4))
                Text(uploadedText, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
            } else {
                Text("Attached file: $attachedFileName", color = Color(0xFFB4C0D4), fontWeight = FontWeight.Medium)
                Text("Already uploaded to server storage. Select another file to replace it.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
            }
            Button(
                onClick = onAttach,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, buttonColor.copy(alpha = 0.75f)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White,
                ),
            ) {
                Text(actionLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun String.toAttachedFileName(): String? {
    val value = trim()
    if (value.isBlank()) return null
    val sanitized = value.substringBefore('?').substringBefore('#')
    return sanitized.substringAfterLast('/').ifBlank { sanitized }
}
