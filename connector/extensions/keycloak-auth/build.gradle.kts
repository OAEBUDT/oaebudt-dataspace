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
    implementation(libs.edc.auth.spi)
    implementation(libs.edc.web.spi)
    implementation(libs.nimbus.jose.jwt)
    implementation(libs.nimbus.oauth2.oidc)

    testImplementation(libs.edc.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito.core)
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}
