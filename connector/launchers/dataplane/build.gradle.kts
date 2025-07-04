plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

dependencies {
    runtimeOnly(libs.edc.bom.dataplane)
    runtimeOnly(libs.edc.dataplane.v2)
    runtimeOnly(libs.edc.bom.dataplane.sql)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xml")
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}
