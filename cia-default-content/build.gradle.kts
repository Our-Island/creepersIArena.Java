plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.lombok)
}

base {
    archivesName.set("cia-default-content")
}

dependencies {
    compileOnly(project(":cia-api"))

    // The default content currently reuses internal runtime helpers while the public API is still stabilizing.
    // Third-party extensions should prefer `cia-api` only.
    compileOnly(project(":cia-core"))
    annotationProcessor(project(":cia-api"))

    compileOnly(libs.paper.api)

    compileOnly(libs.jspecify)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    testImplementation(project(":cia-api"))
    testImplementation(project(":cia-core"))
    testImplementation(testFixtures(project(":cia-api")))
    testImplementation(libs.paper.api)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.jar {
    archiveExtension.set("cia.jar")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Acia.extension.version=${project.version}")
}


val javaVersionInt = libs.versions.java.get().toInt()

kotlin {
    jvmToolchain(javaVersionInt)
}

kotlinLombok {
    lombokConfigurationFile(rootProject.file("lombok.config"))
}
