package com.morningalarm.server.tools

import io.swagger.parser.OpenAPIParser
import io.swagger.v3.parser.core.models.ParseOptions
import java.io.File

fun main(args: Array<String>) {
    require(args.size == 1) {
        "Usage: OpenApiValidatorKt <path-to-openapi-yaml>"
    }

    val schemaFile = File(args[0])
    require(schemaFile.exists()) {
        "OpenAPI schema file was not found: ${schemaFile.absolutePath}"
    }

    val parseOptions = ParseOptions().apply {
        isResolve = true
        isResolveFully = true
        isFlatten = false
    }

    val result = OpenAPIParser().readLocation(schemaFile.toURI().toString(), emptyList(), parseOptions)
    val specification = result.openAPI
    require(specification != null) {
        val messages = result.messages.joinToString(separator = "\n")
        "OpenAPI schema could not be parsed:\n$messages"
    }

    require(result.messages.isEmpty()) {
        "OpenAPI schema validation returned messages:\n${result.messages.joinToString(separator = "\n")}"
    }

    require(!specification.paths.isNullOrEmpty()) {
        "OpenAPI schema must define at least one path"
    }

    println("OpenAPI schema is valid: ${schemaFile.absolutePath}")
}
