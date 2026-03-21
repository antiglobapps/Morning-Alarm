package com.morningalarm.desktopadmin.media

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DesktopMediaPlaybackControllerTest {
    private val morningTone = PlayableMedia(
        url = "https://cdn.example.com/ringtones/morning.mp3",
        title = "Morning",
    )
    private val rainTone = PlayableMedia(
        url = "https://cdn.example.com/ringtones/rain.mp3",
        title = "Rain",
    )

    @Test
    fun `toggle starts playback and publishes playing state`() {
        val engine = FakeMediaPlaybackEngine()
        val controller = DesktopMediaPlaybackController(engine)

        controller.toggle(morningTone)

        assertEquals(MediaPlaybackState.Preparing(morningTone), controller.state.value)
        engine.emitPlaying()

        assertEquals(MediaPlaybackState.Playing(morningTone), controller.state.value)
    }

    @Test
    fun `toggle on the same media stops current playback`() {
        val engine = FakeMediaPlaybackEngine()
        val controller = DesktopMediaPlaybackController(engine)

        controller.toggle(morningTone)
        engine.emitPlaying()

        controller.toggle(morningTone)

        assertEquals(MediaPlaybackState.Idle, controller.state.value)
        assertTrue(engine.stopCalls > 0)
    }

    @Test
    fun `engine errors are exposed in playback state`() {
        val engine = FakeMediaPlaybackEngine()
        val controller = DesktopMediaPlaybackController(engine)

        controller.toggle(morningTone)
        engine.emitError("Unsupported codec")

        val errorState = assertIs<MediaPlaybackState.Error>(controller.state.value)
        assertEquals(morningTone, errorState.media)
        assertEquals("Unsupported codec", errorState.message)
    }

    @Test
    fun `stale callbacks from previous media are ignored after switching urls`() {
        val engine = FakeMediaPlaybackEngine()
        val controller = DesktopMediaPlaybackController(engine)

        controller.toggle(morningTone)
        val previousPlayback = engine.lastPlayback ?: error("Expected initial playback")

        controller.toggle(rainTone)

        previousPlayback.onPlaying()
        assertEquals(MediaPlaybackState.Preparing(rainTone), controller.state.value)

        engine.emitPlaying()
        assertEquals(MediaPlaybackState.Playing(rainTone), controller.state.value)
    }
}

private class FakeMediaPlaybackEngine : MediaPlaybackEngine {
    var lastPlayback: PlaybackCallbacks? = null
        private set
    var stopCalls: Int = 0
        private set

    override fun play(
        media: PlayableMedia,
        onPlaying: () -> Unit,
        onCompleted: () -> Unit,
        onError: (message: String) -> Unit,
    ) {
        lastPlayback = PlaybackCallbacks(
            media = media,
            onPlaying = onPlaying,
            onCompleted = onCompleted,
            onError = onError,
        )
    }

    override fun stop() {
        stopCalls += 1
    }

    override fun close() = Unit

    fun emitPlaying() {
        lastPlayback?.onPlaying?.invoke() ?: error("No playback has been started")
    }

    fun emitError(message: String) {
        lastPlayback?.onError?.invoke(message) ?: error("No playback has been started")
    }
}

private data class PlaybackCallbacks(
    val media: PlayableMedia,
    val onPlaying: () -> Unit,
    val onCompleted: () -> Unit,
    val onError: (message: String) -> Unit,
)
