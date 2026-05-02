import org.gradle.jvm.tasks.Jar

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

val defaultContentExtensionJar = project(":cia-default-content").tasks.named<Jar>("jar")
val bundledExtensionsDir = layout.buildDirectory.dir("generated/bundled-extensions")

val copyBundledExtensions by tasks.registering(Copy::class) {
    dependsOn(defaultContentExtensionJar)

    from(defaultContentExtensionJar.flatMap { it.archiveFile }) {
        rename { "cia-default-content.cia.jar" }
    }

    into(bundledExtensionsDir.map {
        it.dir("META-INF/cia/bundled-extensions")
    })
}

tasks.jar {
    dependsOn(copyBundledExtensions)

    for (modulePath in bundledModulePaths) {
        val module = project(modulePath)
        dependsOn(module.tasks.named("classes"))

        val sourceSets = module.extensions.getByType<SourceSetContainer>()
        from(sourceSets.named("main").map { it.output })
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.processResources {
    dependsOn(copyBundledExtensions)

    from(bundledExtensionsDir)

    val props = mapOf(
        "version" to project.version,
        "minecraft" to libs.versions.minecraft.get()
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
