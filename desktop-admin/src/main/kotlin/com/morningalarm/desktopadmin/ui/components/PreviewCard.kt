package com.morningalarm.desktopadmin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.morningalarm.desktopadmin.media.MediaPlaybackState
import com.morningalarm.desktopadmin.media.PlayableMedia
import com.morningalarm.desktopadmin.media.matches
import com.morningalarm.dto.ringtone.RingtoneListItemDto

@Composable
internal fun PreviewCard(
    item: RingtoneListItemDto,
    playbackState: MediaPlaybackState,
    playableMedia: PlayableMedia?,
    onTogglePlayback: (PlayableMedia) -> Unit,
) {
    val playbackStatus = playbackState.toCardPlaybackStatus(playableMedia)
    val isPlayingThisMedia = playableMedia != null && playbackState.matches(playableMedia) && playbackState !is MediaPlaybackState.Error

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF192230)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(item.description, color = Color(0xFFB4C0D4))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.isPremium) Badge("Premium", Color(0xFF334E8D))
                Badge("${item.durationSeconds}s", Color(0xFF2C3443))
                Badge("${item.likesCount} likes", Color(0xFF2C3443))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilledTonalButton(
                    onClick = { playableMedia?.let(onTogglePlayback) },
                    enabled = playableMedia != null,
                ) {
                    Text(if (isPlayingThisMedia) "Stop" else "Play")
                }
                if (playbackStatus != null) {
                    Text(
                        playbackStatus.message,
                        color = playbackStatus.color,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else if (playableMedia == null) {
                    Text(
                        "Upload or save audio to enable playback.",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text("Image: ${item.imageUrl.ifBlank { "No image selected yet." }}", color = Color(0xFF94A3B8))
            Text("Audio: ${item.audioUrl.ifBlank { "No audio selected yet." }}", color = Color(0xFF94A3B8))
        }
    }
}

@Composable
internal fun Badge(text: String, color: Color) {
    Box(
        modifier = Modifier.background(color, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

private data class PlaybackCardStatus(
    val message: String,
    val color: Color,
)

private fun MediaPlaybackState.toCardPlaybackStatus(playableMedia: PlayableMedia?): PlaybackCardStatus? {
    if (playableMedia == null || !matches(playableMedia)) return null
    return when (this) {
        MediaPlaybackState.Idle -> null
        is MediaPlaybackState.Error -> PlaybackCardStatus(
            message = message,
            color = Color(0xFFFCA5A5),
        )
        is MediaPlaybackState.Playing -> PlaybackCardStatus(
            message = "Playing inside admin app.",
            color = Color(0xFF86EFAC),
        )
        is MediaPlaybackState.Preparing -> PlaybackCardStatus(
            message = "Loading media...",
            color = Color(0xFFFCD34D),
        )
    }
}
