plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    runtimeOnly(project(":extensions:superuser-seed"))

    implementation(libs.edc.ih.lib.credentialquery)

    runtimeOnly(libs.edc.bom.identityhub)
    runtimeOnly(libs.edc.vault.hashicorp)
    runtimeOnly(libs.edc.bom.identityhub.sql)

//    testImplementation(libs.edc.lib.crypto)
//    testImplementation(libs.edc.lib.keys)
//    testImplementation(libs.mockito.core)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xml")
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}
