plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("org.jetbrains.kotlin.plugin.lombok") version "2.3.10"
}

group = "top.ourisland"
version = property("cia-version") as String

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

val javaVersion = (property("java-version") as String).toInt()
val kotlinVersion = property("kotlin-version") as String

val minecraftVersion = property("minecraft-version") as String
val paperApiVersion = property("paper-api") as String

val lombokVersion = property("lombok-version") as String

dependencies {
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")

    // Compile only, use CiaPaperLoader to download when used
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
//    compileOnly("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
//    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    compileOnly("io.papermc.paper:paper-api:${paperApiVersion}")
    compileOnly("net.luckperms:api:5.5")

    testCompileOnly("org.projectlombok:lombok:${lombokVersion}")
    testAnnotationProcessor("org.projectlombok:lombok:${lombokVersion}")
}

tasks {
    runServer {
        minecraftVersion(minecraftVersion)
    }
}

tasks.test {
    failOnNoDiscoveredTests = false
}

java {
    val jv = JavaVersion.toVersion(javaVersion)
    sourceCompatibility = jv
    targetCompatibility = jv

    if (JavaVersion.current() < jv) {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    }
}

kotlin {
    jvmToolchain(javaVersion)
}

kotlinLombok {
    lombokConfigurationFile(file("lombok.config"))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    if (javaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(javaVersion)
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
