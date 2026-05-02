import org.gradle.api.tasks.SourceSetContainer

plugins {
    java
    alias(libs.plugins.run.paper)
}

base {
    archivesName.set(rootProject.name)
}

dependencies {
    implementation(project(":cia-core"))

    compileOnly(libs.paper.api)
    compileOnly(libs.jspecify)
}

val bundledModulePaths = listOf(
    ":cia-api",
    ":cia-core"
)

tasks.jar {
    for (modulePath in bundledModulePaths) {
        val module = project(modulePath)
        dependsOn(module.tasks.named("classes"))

        val sourceSets = module.extensions.getByType<SourceSetContainer>()
        from(sourceSets.named("main").map { it.output })
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version
    )

    inputs.properties(props)
    filteringCharset = "UTF-8"

    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

tasks {
    runServer {
        minecraftVersion(libs.versions.minecraft.get())
    }
}
