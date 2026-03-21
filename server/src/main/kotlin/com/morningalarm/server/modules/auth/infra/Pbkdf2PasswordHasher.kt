package com.morningalarm.server.modules.auth.infra

import com.morningalarm.server.modules.auth.application.ports.PasswordHasher
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class Pbkdf2PasswordHasher : PasswordHasher {
    override fun hash(password: String): String {
        val salt = ByteArray(SALT_SIZE).also(secureRandom::nextBytes)
        val hash = derive(password, salt)
        return "${Base64.getEncoder().encodeToString(salt)}:${Base64.getEncoder().encodeToString(hash)}"
    }

    override fun matches(password: String, hash: String): Boolean {
        val parts = hash.split(':')
        if (parts.size != 2) {
            return false
        }
        val salt = Base64.getDecoder().decode(parts[0])
        val expected = Base64.getDecoder().decode(parts[1])
        val actual = derive(password, salt)
        return expected.contentEquals(actual)
    }

    private fun derive(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
    }

    private companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val ITERATIONS = 65_536
        const val KEY_LENGTH_BITS = 256
        const val SALT_SIZE = 16
        val secureRandom = SecureRandom()
    }
}
