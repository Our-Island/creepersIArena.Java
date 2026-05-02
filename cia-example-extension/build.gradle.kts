plugins {
    `java-library`
}

base {
    archivesName.set("cia-example-extension")
}

dependencies {
    compileOnly(project(":cia-api"))
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
