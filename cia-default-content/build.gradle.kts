plugins {
    `java-library`
}

base {
    archivesName.set("cia-default-content")
}

dependencies {
    compileOnly(project(":cia-api"))

    // The default content currently reuses internal runtime helpers while the public API is still stabilizing.
    // Third-party extensions should prefer `cia-api` only.
    compileOnly(project(":cia-core"))

    compileOnly(libs.paper.api)
    compileOnly(libs.jspecify)
}

tasks.jar {
    archiveExtension.set("cia.jar")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version
    )

    inputs.properties(props)
    filteringCharset = "UTF-8"

    filesMatching("cia-extension.yml") {
        expand(props)
    }
}
