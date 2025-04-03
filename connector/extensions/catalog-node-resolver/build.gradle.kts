plugins {
    `java-library`
    id("application")
    id("jacoco")
}

tasks.test {
    useJUnitPlatform() // Ensures JUnit 5 is used
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    runtimeOnly(libs.edc.fc.core)
    runtimeOnly(libs.edc.fc.ext.api)
    implementation(libs.edc.fc.spi.crawler)
    implementation(libs.edc.spi.identity.did)

    testImplementation(libs.edc.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}
