plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.spi.identity.did)
    implementation(libs.edc.fc.spi.crawler)
    runtimeOnly(libs.edc.fc.core)
    runtimeOnly(libs.edc.fc.api)

    testImplementation(libs.edc.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
}
