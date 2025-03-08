plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.edc.auth.spi)
    implementation(libs.edc.web.spi)
    implementation(libs.edc.spi.token)
    implementation(libs.edc.data.plane.public.api)


    implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")
    implementation("com.nimbusds:oauth2-oidc-sdk:11.3")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xml")
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}