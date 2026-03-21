plugins {
    base
}

tasks.register<Exec>("npmInstall") {
    workingDir = projectDir
    commandLine("npm", "ci")
    onlyIf { file("package.json").exists() }
}

tasks.register<Exec>("npmBuild") {
    dependsOn("npmInstall")
    workingDir = projectDir
    commandLine("npm", "run", "build")
    onlyIf { file("package.json").exists() }
}

tasks.named("build") {
    dependsOn("npmBuild")
}
