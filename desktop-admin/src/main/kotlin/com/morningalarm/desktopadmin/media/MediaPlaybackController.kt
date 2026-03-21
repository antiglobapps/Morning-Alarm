package com.morningalarm.desktopadmin.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class MediaPlaybackKind {
    AUDIO,
    VIDEO,
}

internal data class PlayableMedia(
    val url: String,
    val title: String,
    val kind: MediaPlaybackKind = MediaPlaybackKind.AUDIO,
)

internal sealed interface MediaPlaybackState {
    data object Idle : MediaPlaybackState

    data class Preparing(
        val media: PlayableMedia,
    ) : MediaPlaybackState

    data class Playing(
        val media: PlayableMedia,
    ) : MediaPlaybackState

    data class Error(
        val media: PlayableMedia,
        val message: String,
    ) : MediaPlaybackState
}

internal interface MediaPlaybackController : AutoCloseable {
    val state: StateFlow<MediaPlaybackState>

    fun toggle(media: PlayableMedia)

    fun stop()
}

internal interface MediaPlaybackEngine : AutoCloseable {
    fun play(
        media: PlayableMedia,
        onPlaying: () -> Unit,
        onCompleted: () -> Unit,
        onError: (message: String) -> Unit,
    )

    fun stop()
}

internal class DesktopMediaPlaybackController(
    private val engine: MediaPlaybackEngine = JavaFxMediaPlaybackEngine(),
) : MediaPlaybackController {
    private val mutableState = MutableStateFlow<MediaPlaybackState>(MediaPlaybackState.Idle)
    private var playbackToken: Long = 0

    override val state: StateFlow<MediaPlaybackState> = mutableState.asStateFlow()

    override fun toggle(media: PlayableMedia) {
        if (mutableState.value.isActive(media)) {
            stop()
            return
        }

        val nextToken = ++playbackToken
        mutableState.value = MediaPlaybackState.Preparing(media)

        runCatching {
            engine.play(
                media = media,
                onPlaying = {
                    if (nextToken == playbackToken) {
                        mutableState.value = MediaPlaybackState.Playing(media)
                    }
                },
                onCompleted = {
                    if (nextToken == playbackToken) {
                        mutableState.value = MediaPlaybackState.Idle
                    }
                },
                onError = { message ->
                    if (nextToken == playbackToken) {
                        mutableState.value = MediaPlaybackState.Error(
                            media = media,
                            message = message,
                        )
                    }
                },
            )
        }.onFailure { error ->
            if (nextToken == playbackToken) {
                mutableState.value = MediaPlaybackState.Error(
                    media = media,
                    message = error.message ?: "Failed to start media playback.",
                )
            }
        }
    }

    override fun stop() {
        playbackToken += 1
        runCatching { engine.stop() }
        mutableState.value = MediaPlaybackState.Idle
    }

    override fun close() {
        stop()
        engine.close()
    }
}

internal fun String.toPlayableMediaOrNull(
    title: String,
    kind: MediaPlaybackKind = MediaPlaybackKind.AUDIO,
): PlayableMedia? {
    val normalizedUrl = trim()
    if (normalizedUrl.isBlank()) return null
    return PlayableMedia(
        url = normalizedUrl,
        title = title.ifBlank { "Untitled media" },
        kind = kind,
    )
}

internal fun MediaPlaybackState.matches(media: PlayableMedia): Boolean = when (this) {
    MediaPlaybackState.Idle -> false
    is MediaPlaybackState.Error -> this.media.url == media.url
    is MediaPlaybackState.Playing -> this.media.url == media.url
    is MediaPlaybackState.Preparing -> this.media.url == media.url
}

private fun MediaPlaybackState.isActive(media: PlayableMedia): Boolean = when (this) {
    MediaPlaybackState.Idle -> false
    is MediaPlaybackState.Error -> false
    is MediaPlaybackState.Playing -> this.media.url == media.url
    is MediaPlaybackState.Preparing -> this.media.url == media.url
}
