plugins {
    `java-library`
    id("application")
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
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}