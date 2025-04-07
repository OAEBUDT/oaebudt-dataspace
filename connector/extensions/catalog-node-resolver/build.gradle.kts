plugins {
    `java-library`
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
