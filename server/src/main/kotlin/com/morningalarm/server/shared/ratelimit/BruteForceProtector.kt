package com.morningalarm.server.shared.ratelimit

import com.morningalarm.server.shared.errors.TooManyRequestsException
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks failed login attempts per key (typically email) and blocks further attempts
 * once the threshold is exceeded within the sliding time window.
 *
 * Thread-safe. State is in-memory only — resets on server restart.
 */
class BruteForceProtector(
    val maxAttempts: Int = 5,
    val windowSeconds: Long = 300L,
    private val clock: () -> Long = { System.currentTimeMillis() / 1000L },
) {
    // Maps key -> list of failure epoch-second timestamps within the current window
    private val failureTimes = ConcurrentHashMap<String, List<Long>>()

    /**
     * Throws [TooManyRequestsException] when the key has exceeded [maxAttempts]
     * failures within the last [windowSeconds] seconds.
     */
    fun checkBlocked(key: String) {
        val now = clock()
        val recent = recentFailures(key, now)
        if (recent.size >= maxAttempts) {
            throw TooManyRequestsException(
                "Too many failed attempts. Try again in ${windowSeconds / 60} minutes.",
            )
        }
    }

    /** Records a failed attempt for the given key. */
    fun recordFailure(key: String) {
        val now = clock()
        failureTimes.merge(key, listOf(now)) { existing, new ->
            // Keep only failures inside the window to prevent unbounded growth
            existing.filter { it > now - windowSeconds } + new
        }
    }

    /** Clears the failure history for the given key after a successful login. */
    fun recordSuccess(key: String) {
        failureTimes.remove(key)
    }

    private fun recentFailures(key: String, now: Long): List<Long> =
        failureTimes[key]?.filter { it > now - windowSeconds } ?: emptyList()
}
