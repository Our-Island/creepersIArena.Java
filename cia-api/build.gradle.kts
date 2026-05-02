plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.lombok)
}

base {
    archivesName.set("cia-api")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.jspecify)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

val javaVersionInt = libs.versions.java.get().toInt()

kotlin {
    jvmToolchain(javaVersionInt)
}

kotlinLombok {
    lombokConfigurationFile(rootProject.file("lombok.config"))
}
