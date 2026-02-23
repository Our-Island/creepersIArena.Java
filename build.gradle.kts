plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("org.jetbrains.kotlin.plugin.lombok") version "2.3.10"
}

group = "top.ourisland"
version = "0.0.1"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    // Compile only, use CiaPaperLoader to download when used
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.10")
//    compileOnly("org.jetbrains.kotlin:kotlin-reflect:2.3.10")
//    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.5")

    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
}

tasks {
    runServer {
        minecraftVersion("1.21.11")
    }
}

tasks.test {
    failOnNoDiscoveredTests = false
}

val targetJavaVersion = 21

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    if (JavaVersion.current() < javaVersion) {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
        }
    }
}

kotlin {
    jvmToolchain(targetJavaVersion)
}

kotlinLombok {
    lombokConfigurationFile(file("lombok.config"))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
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
