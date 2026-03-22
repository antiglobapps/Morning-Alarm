plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

val osName = System.getProperty("os.name").lowercase()
val osArch = System.getProperty("os.arch").lowercase()
val javafxPlatform = when {
    "mac" in osName && (osArch == "aarch64" || osArch == "arm64") -> "mac-aarch64"
    "mac" in osName -> "mac"
    "win" in osName -> "win"
    osArch == "aarch64" || osArch == "arm64" -> "linux-aarch64"
    else -> "linux"
}
val javafxVersion = libs.versions.javafx.get()

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.runtime)
    implementation(compose.ui)
    implementation("org.openjfx:javafx-base:$javafxVersion:$javafxPlatform")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$javafxPlatform")
    implementation("org.openjfx:javafx-media:$javafxVersion:$javafxPlatform")
    implementation("org.openjfx:javafx-swing:$javafxVersion:$javafxPlatform")
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.server.logback)
    implementation(libs.orbit.core)
    implementation(libs.orbit.compose)
    implementation(libs.orbit.viewmodel)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(projects.shared)

    testImplementation(kotlin("test"))
    testImplementation(libs.orbit.test)
    testImplementation(libs.kotlinx.coroutines.test)
}

kotlin {
    jvmToolchain(21)
}

compose.desktop {
    application {
        mainClass = "com.morningalarm.desktopadmin.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
            )

            packageName = "MorningAlarmDesktopAdmin"
            packageVersion = "1.0.0"
            description = "Morning Alarm — internal admin tool"
            vendor = "Morning Alarm"

            macOS {
                bundleID = "com.morningalarm.desktopadmin"
                packageName = "Morning Alarm Admin"
            }
            windows {
                menuGroup = "Morning Alarm"
                upgradeUuid = "3e8b2f1a-4c6d-4e9f-a1b2-c3d4e5f6a7b8"
            }
            linux {
                packageName = "morning-alarm-admin"
            }
        }
    }
}
