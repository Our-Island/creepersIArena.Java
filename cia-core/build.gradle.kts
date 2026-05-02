plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.lombok)
}

base {
    archivesName.set("cia-core")
}

dependencies {
    api(project(":cia-api"))
    compileOnly(libs.paper.api)
    compileOnly(libs.luckperms.api)

    compileOnly(libs.jspecify)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    testImplementation(libs.paper.api)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}


val javaVersionInt = libs.versions.java.get().toInt()

kotlin {
    jvmToolchain(javaVersionInt)
}

kotlinLombok {
    lombokConfigurationFile(rootProject.file("lombok.config"))
}
