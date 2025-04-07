plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

dependencies {
    runtimeOnly(project(":extensions:superuser-seed"))
    runtimeOnly(project(":extensions:credential-from-file"))

    runtimeOnly(libs.edc.bom.identityhub)
    runtimeOnly(libs.edc.vault.hashicorp)
    runtimeOnly(libs.edc.bom.identityhub.sql)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xml")
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}
