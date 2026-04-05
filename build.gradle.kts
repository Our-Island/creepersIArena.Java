plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.lombok)
    alias(libs.plugins.run.paper)
}

val javaVersionInt = libs.versions.java.get().toInt()

group = property("group") as String
version = property("version") as String
description = property("description") as String

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    // Compile only, use CiaPaperLoader to download when used
//    compileOnly(libs.kotlin.reflect)
//    compileOnly(libs.kotlinx.coroutines.core)

    compileOnly(libs.paper.api)

    compileOnly(libs.luckperms.api)

    compileOnly(libs.jspecify)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

tasks {
    runServer {
        minecraftVersion(libs.versions.minecraft.get())
    }
}

tasks.test {
    failOnNoDiscoveredTests = false
}

java {
    val jv = JavaVersion.toVersion(javaVersionInt)
    sourceCompatibility = jv
    targetCompatibility = jv

    if (JavaVersion.current() < jv) {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersionInt))
        }
    }
}

kotlin {
    jvmToolchain(javaVersionInt)
}

kotlinLombok {
    lombokConfigurationFile(file("lombok.config"))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersionInt)
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
