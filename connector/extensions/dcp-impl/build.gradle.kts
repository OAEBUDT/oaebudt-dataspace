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
    implementation(libs.edc.dcp.core)
    implementation(libs.edc.spi.identity.trust)
    implementation(libs.edc.spi.transform)
    implementation(libs.edc.spi.catalog)
    implementation(libs.edc.spi.identity.did)
    implementation(libs.edc.lib.jws2020)
    implementation(libs.edc.lib.transform)
}
