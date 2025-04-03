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
    implementation(libs.edc.ih.spi.credentials)

//    testImplementation(libs.edc.junit)
//    testImplementation(libs.junit.jupiter)
//    testImplementation(libs.assertj)
//    testImplementation(libs.mockito.core)
}
