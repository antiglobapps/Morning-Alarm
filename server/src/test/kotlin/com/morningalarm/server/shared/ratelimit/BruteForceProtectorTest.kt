package com.morningalarm.server.shared.ratelimit

import com.morningalarm.server.shared.errors.TooManyRequestsException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BruteForceProtectorTest {
    @Test
    fun `does not block before limit is reached`() {
        val protector = BruteForceProtector(maxAttempts = 3, windowSeconds = 60)
        protector.recordFailure("user@example.com")
        protector.recordFailure("user@example.com")
        // 2 failures out of 3 — should not block
        protector.checkBlocked("user@example.com")
    }

    @Test
    fun `blocks exactly at the limit`() {
        val protector = BruteForceProtector(maxAttempts = 3, windowSeconds = 60)
        protector.recordFailure("user@example.com")
        protector.recordFailure("user@example.com")
        protector.recordFailure("user@example.com")
        assertFailsWith<TooManyRequestsException> {
            protector.checkBlocked("user@example.com")
        }
    }

    @Test
    fun `success clears failure history`() {
        val protector = BruteForceProtector(maxAttempts = 3, windowSeconds = 60)
        protector.recordFailure("user@example.com")
        protector.recordFailure("user@example.com")
        protector.recordFailure("user@example.com")
        protector.recordSuccess("user@example.com")
        // Should not throw after success clears the history
        protector.checkBlocked("user@example.com")
    }

    @Test
    fun `failures outside the window do not count`() {
        var fakeTime = 1000L
        val protector = BruteForceProtector(maxAttempts = 2, windowSeconds = 60, clock = { fakeTime })

        protector.recordFailure("user@example.com")
        protector.recordFailure("user@example.com")
        // Both failures are inside window at this point — blocked
        assertFailsWith<TooManyRequestsException> {
            protector.checkBlocked("user@example.com")
        }

        // Advance time past the window
        fakeTime = 1000L + 61L
        // Failures are now outside the window — should not block
        protector.checkBlocked("user@example.com")
    }

    @Test
    fun `keys are tracked independently`() {
        val protector = BruteForceProtector(maxAttempts = 1, windowSeconds = 60)
        protector.recordFailure("alice@example.com")

        // alice is blocked
        assertFailsWith<TooManyRequestsException> {
            protector.checkBlocked("alice@example.com")
        }
        // bob is not blocked
        protector.checkBlocked("bob@example.com")
    }

    @Test
    fun `unknown key is not blocked`() {
        val protector = BruteForceProtector(maxAttempts = 3, windowSeconds = 60)
        // No failures recorded for this key
        protector.checkBlocked("fresh@example.com")
    }
}
