package com.morningalarm.server

import com.morningalarm.api.auth.DevAdminDefaults
import com.morningalarm.server.bootstrap.AppConfig
import com.morningalarm.server.bootstrap.AppConfigLoader
import com.morningalarm.server.bootstrap.ModuleDependencies
import com.morningalarm.server.bootstrap.applicationModule
import com.morningalarm.server.bootstrap.createModuleDependencies
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.Closeable

fun main(args: Array<String>) {
    val config = AppConfigLoader.fromEnv()

    // ── CLI bootstrap commands (server-side only, never exposed via HTTP) ────
    if (args.isNotEmpty()) {
        handleBootstrapCommand(args, config)
        return
    }

    if (config.devMode) {
        println("DEV MODE: using embedded H2 database (${config.databaseUrl})")
    }

    val dependencies = createModuleDependencies(config)
    createDevAdminIfNeeded(config, dependencies)

    embeddedServer(
        factory = Netty,
        host = config.host,
        port = config.port,
        module = {
            applicationModule(config = config, dependencies = dependencies)
        },
    ).start(wait = true)
}

private fun handleBootstrapCommand(args: Array<String>, config: AppConfig) {
    val command = args[0]
    val secret = findArgValue(args, "--secret")
    val email = findArgValue(args, "--email")

    when (command) {
        "--create-admin" -> {
            requireNotNull(email) { "Usage: --create-admin --email=<email> --secret=<bootstrap_secret> [--display-name=<display_name>]" }
            validateBootstrapSecret(config, secret)
            val displayName = findArgValue(args, "--display-name")

            withDependencies(config) { dependencies ->
                val result = dependencies.adminBootstrapService.createAdmin(email, displayName)
                println("SUCCESS: Admin user '${result.email}' (id=${result.userId}) has been created")
                println("TEMPORARY_PASSWORD: ${result.temporaryPassword}")
                println("ACTION_REQUIRED: Rotate the temporary password after the first admin login")
            }
        }
        "--promote-admin" -> {
            requireNotNull(email) { "Usage: --promote-admin --email=<email> --secret=<bootstrap_secret>" }
            validateBootstrapSecret(config, secret)

            withDependencies(config) { dependencies ->
                val userId = dependencies.adminBootstrapService.promoteToAdmin(email)
                println("SUCCESS: User '$email' (id=$userId) has been promoted to ADMIN role")
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

private fun validateBootstrapSecret(config: AppConfig, secret: String?) {
    val requiredSecret = config.adminBootstrapSecret
    if (requiredSecret.isNullOrBlank()) {
        error("SERVER_ADMIN_BOOTSTRAP_SECRET environment variable must be set to use bootstrap commands")
    }
    if (secret != requiredSecret) {
        error("Invalid bootstrap secret")
    }
}

private fun withDependencies(config: AppConfig, action: (ModuleDependencies) -> Unit) {
    val dependencies = createModuleDependencies(config)
    try {
        action(dependencies)
    } catch (e: Exception) {
        error("FAILED: ${e.message}")
    } finally {
        (dependencies.dataSource as? Closeable)?.close()
    }
}

private fun findArgValue(args: Array<String>, prefix: String): String? {
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

private fun createDevAdminIfNeeded(config: AppConfig, dependencies: ModuleDependencies) {
    if (!config.devMode) {
        return
    }

    val result = dependencies.adminBootstrapService.createDevAdminIfDatabaseEmpty(
        email = DevAdminDefaults.EMAIL,
        password = DevAdminDefaults.PASSWORD,
        displayName = DevAdminDefaults.DISPLAY_NAME,
    ) ?: return

    println(
        "DEV MODE: created default admin '${result.email}' (id=${result.userId}) " +
            "with password '${result.password}' and admin secret '${config.adminAccessSecret}'",
    )
}
