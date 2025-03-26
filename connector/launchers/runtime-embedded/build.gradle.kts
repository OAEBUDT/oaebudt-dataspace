plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
}

dependencies {
    runtimeOnly(project(":extensions:dcp-impl"))

    runtimeOnly(project(":launchers:controlplane")) {
        // this will remove the RemoteDataPlaneSelectorService
        exclude(group = "org.eclipse.edc", "data-plane-selector-client")
    }
    runtimeOnly(project(":launchers:dataplane")) {
        // this will remove the RemoteDataPlaneSelectorService
        exclude(group = "org.eclipse.edc", "data-plane-selector-client")
    }
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
