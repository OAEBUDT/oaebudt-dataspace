plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.ih.spi.credentials)
    implementation(libs.edc.ih.spi)

    testImplementation(libs.edc.junit)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito.core)
}
