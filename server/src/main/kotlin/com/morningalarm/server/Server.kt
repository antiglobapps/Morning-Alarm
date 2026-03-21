package com.morningalarm.server

import com.morningalarm.server.bootstrap.AppConfigLoader
import com.morningalarm.server.bootstrap.applicationModule
import com.morningalarm.server.bootstrap.createModuleDependencies
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    val config = AppConfigLoader.fromEnv()

    // ── CLI bootstrap commands (server-side only, never exposed via HTTP) ────
    if (args.isNotEmpty()) {
        handleBootstrapCommand(args, config)
        return
    }

    embeddedServer(
        factory = Netty,
        host = config.host,
        port = config.port,
        module = {
            applicationModule(config = config)
        },
    ).start(wait = true)
}

private fun handleBootstrapCommand(args: Array<String>, config: com.morningalarm.server.bootstrap.AppConfig) {
    val command = args[0]
    when (command) {
        "--create-admin" -> {
            val email = findArgValue(args, "--email")
                ?: error("Usage: --create-admin --email=<email> --secret=<bootstrap_secret> [--display-name=<display_name>]")

            val secret = findArgValue(args, "--secret")
                ?: error("Usage: --create-admin --email=<email> --secret=<bootstrap_secret> [--display-name=<display_name>]")
            val displayName = findArgValue(args, "--display-name")

            val requiredSecret = config.adminBootstrapSecret
            if (requiredSecret.isNullOrBlank()) {
                error("SERVER_ADMIN_BOOTSTRAP_SECRET environment variable must be set to use bootstrap commands")
            }
            if (secret != requiredSecret) {
                error("Invalid bootstrap secret")
            }

            val dependencies = createModuleDependencies(config)
            try {
                val result = dependencies.authService.createAdmin(email, displayName)
                println("SUCCESS: Admin user '${result.email}' (id=${result.userId}) has been created")
                println("TEMPORARY_PASSWORD: ${result.temporaryPassword}")
                println("ACTION_REQUIRED: Rotate the temporary password after the first admin login")
            } catch (e: Exception) {
                error("FAILED: ${e.message}")
            } finally {
                (dependencies.dataSource as? java.io.Closeable)?.close()
            }
        }
        "--promote-admin" -> {
            val email = findArgValue(args, "--email")
                ?: error("Usage: --promote-admin --email=<email> --secret=<bootstrap_secret>")

            val secret = findArgValue(args, "--secret")
                ?: error("Usage: --promote-admin --email=<email> --secret=<bootstrap_secret>")

            val requiredSecret = config.adminBootstrapSecret
            if (requiredSecret.isNullOrBlank()) {
                error("SERVER_ADMIN_BOOTSTRAP_SECRET environment variable must be set to use bootstrap commands")
            }
            if (secret != requiredSecret) {
                error("Invalid bootstrap secret")
            }

            val dependencies = createModuleDependencies(config)
            try {
                val userId = dependencies.authService.promoteToAdmin(email)
                println("SUCCESS: User '$email' (id=$userId) has been promoted to ADMIN role")
            } catch (e: Exception) {
                error("FAILED: ${e.message}")
            } finally {
                (dependencies.dataSource as? java.io.Closeable)?.close()
            }
        }
        else -> {
            error(
                "Unknown command: $command\n" +
                    "Available commands: --create-admin --email=<email> --secret=<secret> [--display-name=<display_name>], " +
                    "--promote-admin --email=<email> --secret=<secret>",
            )
        }
    }
}

private fun findArgValue(args: Array<String>, prefix: String): String? {
    // Support both --key=value and --key value
    for (i in args.indices) {
        if (args[i].startsWith("$prefix=")) {
            return args[i].substringAfter("=")
        }
        if (args[i] == prefix && i + 1 < args.size) {
            return args[i + 1]
        }
    }
    return null
}
