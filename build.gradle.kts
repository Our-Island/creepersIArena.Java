plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.plugin.lombok) apply false
    alias(libs.plugins.run.paper) apply false
}

val javaVersionInt = libs.versions.java.get().toInt()

allprojects {
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
}

subprojects {
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            val jv = JavaVersion.toVersion(javaVersionInt)
            sourceCompatibility = jv
            targetCompatibility = jv

            if (JavaVersion.current() < jv) {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(javaVersionInt))
                }
            }
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(javaVersionInt)
        }

        tasks.withType<Test>().configureEach {
            failOnNoDiscoveredTests = false
        }
    }
}
