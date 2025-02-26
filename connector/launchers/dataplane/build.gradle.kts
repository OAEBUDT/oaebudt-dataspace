plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

group = "org.oaebudt.edc"

repositories {
    mavenCentral()
}

dependencies {
    runtimeOnly(libs.edc.bom.dataplane)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xml")
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}