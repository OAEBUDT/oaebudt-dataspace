plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.web.spi)
    implementation(libs.mongodb.driver.sync)
    implementation(libs.jersey.multipart)
    implementation(libs.edc.controlplane.spi)
    implementation(libs.edc.fc.spi.core)
    implementation(libs.edc.spi.dataplane.http)
    implementation(project(":spi:core-spi"))

    testImplementation(libs.edc.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito.core)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.parsson)

}
