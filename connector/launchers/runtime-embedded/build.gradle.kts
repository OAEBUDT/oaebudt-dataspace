plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

dependencies {
    runtimeOnly(project(":extensions:keycloak-auth"))
    runtimeOnly(project(":extensions:catalog-node-resolver"))
    runtimeOnly(project(":extensions:dcp-impl"))

    runtimeOnly(project(":launchers:controlplane")) {
        exclude(group = "org.eclipse.edc", "data-plane-selector-client")
    }
    runtimeOnly(project(":launchers:dataplane")) {
        exclude(group = "org.eclipse.edc", "data-plane-selector-client")
    }

    runtimeOnly(libs.edc.fc.core)
    runtimeOnly(libs.edc.fc.api)

    runtimeOnly(libs.edc.vault.hashicorp)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xml")
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}
