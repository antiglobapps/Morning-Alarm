plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinxSerialization)
    application
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.server.logback)
    implementation(libs.swagger.parser)
    implementation(libs.hikari)
    implementation(libs.jwt)
    runtimeOnly(libs.postgresql)
    implementation(projects.shared)

    testImplementation(kotlin("test"))
    testImplementation(libs.h2)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.ktor.serialization.kotlinx.json)
}

application {
    mainClass.set("com.morningalarm.server.ServerKt")
}

kotlin {
    jvmToolchain(17)
}

tasks.register<JavaExec>("validateOpenApi") {
    group = "verification"
    description = "Validates the server OpenAPI schema"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.morningalarm.server.tools.OpenApiValidatorKt")
    args(layout.projectDirectory.file("src/main/resources/openapi/documentation.yaml").asFile.absolutePath)
}

tasks.named("check") {
    dependsOn("validateOpenApi")
}
