plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
}

dependencies {
    runtimeOnly(libs.edc.fc.core)
    runtimeOnly(libs.edc.fc.ext.api)
    implementation(libs.edc.fc.spi.crawler)

    testImplementation(libs.edc.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xml")
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}
