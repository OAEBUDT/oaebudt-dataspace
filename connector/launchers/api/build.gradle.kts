plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.edc.data.plane.spi)
    implementation(libs.edc.web.spi)
    implementation(libs.edc.core.spi)
    implementation(libs.edc.auth.spi)
    implementation(libs.edc.lib.http)
    implementation(libs.edc.spi.token)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xml")
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}