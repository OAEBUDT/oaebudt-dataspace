plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.spi.identity.did)
    implementation(libs.edc.fc.spi.crawler)

    testImplementation(libs.edc.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
}
