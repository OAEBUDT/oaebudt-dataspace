plugins {
    `java-library`
    id("application")
    id("jacoco")
}

tasks.test {
    useJUnitPlatform()
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
    implementation(libs.edc.ih.spi.credentials)
    implementation(libs.edc.ih.spi)

    testImplementation(libs.edc.junit)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito.core)
}
