package com.morningalarm.desktopadmin.media

import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class JavaFxMediaPlaybackEngine : MediaPlaybackEngine {
    private var currentPlayer: MediaPlayer? = null

    init {
        JavaFxRuntime.run {}
    }

    override fun play(
        media: PlayableMedia,
        onPlaying: () -> Unit,
        onCompleted: () -> Unit,
        onError: (message: String) -> Unit,
    ) {
        val normalizedUrl = media.url.toMediaSourceUri()
        JavaFxRuntime.run {
            disposeCurrentPlayer()

            val mediaSource = Media(normalizedUrl)
            val nextPlayer = MediaPlayer(mediaSource)
            currentPlayer = nextPlayer

            mediaSource.errorProperty().addListener { _, _, error ->
                if (currentPlayer === nextPlayer && error != null) {
                    disposeCurrentPlayer()
                    onError(error.message ?: "Media source cannot be decoded.")
                }
            }
            nextPlayer.setOnReady {
                if (currentPlayer === nextPlayer) {
                    nextPlayer.play()
                }
            }
            nextPlayer.setOnPlaying {
                if (currentPlayer === nextPlayer) {
                    onPlaying()
                }
            }
            nextPlayer.setOnEndOfMedia {
                if (currentPlayer === nextPlayer) {
                    disposeCurrentPlayer()
                    onCompleted()
                }
            }
            nextPlayer.setOnError {
                if (currentPlayer === nextPlayer) {
                    val message = nextPlayer.error?.message
                        ?: mediaSource.error?.message
                        ?: "Media playback failed."
                    disposeCurrentPlayer()
                    onError(message)
                }
            }
        }
    }

    override fun stop() {
        JavaFxRuntime.run {
            disposeCurrentPlayer()
        }
    }

    override fun close() {
        stop()
    }

    private fun disposeCurrentPlayer() {
        val player = currentPlayer ?: return
        currentPlayer = null
        runCatching { player.stop() }
        runCatching { player.dispose() }
    }
}

private fun String.toMediaSourceUri(): String {
    val candidate = trim()
    require(candidate.isNotBlank()) { "Media URL is empty." }

    val uri = runCatching { URI(candidate) }
        .getOrElse { throw IllegalArgumentException("Media URL is invalid.") }

    require(uri.isAbsolute) { "Media URL must be absolute." }
    val scheme = uri.scheme?.lowercase()
    require(scheme in SUPPORTED_URI_SCHEMES) {
        "Unsupported media URL scheme: ${scheme ?: "unknown"}."
    }

    return uri.toASCIIString()
}

private object JavaFxRuntime {
    @Volatile
    private var isInitialized: Boolean = false

    fun run(action: () -> Unit) {
        ensureInitialized()
        if (Platform.isFxApplicationThread()) {
            action()
        } else {
            Platform.runLater(action)
        }
    }

    private fun ensureInitialized() {
        if (isInitialized) return

        synchronized(this) {
            if (isInitialized) return

            val latch = CountDownLatch(1)
            try {
                Platform.startup {
                    Platform.setImplicitExit(false)
                    latch.countDown()
                }
            } catch (_: IllegalStateException) {
                Platform.setImplicitExit(false)
                latch.countDown()
            }

            check(latch.await(10, TimeUnit.SECONDS)) {
                "JavaFX runtime startup timed out."
            }
            isInitialized = true
        }
    }
}

private val SUPPORTED_URI_SCHEMES = setOf("file", "http", "https")
