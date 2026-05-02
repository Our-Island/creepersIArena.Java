plugins {
    `java-library`
}

base {
    archivesName.set("cia-example-extension")
}

dependencies {
    compileOnly(project(":cia-api"))
    annotationProcessor(project(":cia-api"))
    compileOnly(libs.paper.api)
    compileOnly(libs.jspecify)
}

tasks.jar {
    archiveExtension.set("cia.jar")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Acia.extension.version=${project.version}")
}
