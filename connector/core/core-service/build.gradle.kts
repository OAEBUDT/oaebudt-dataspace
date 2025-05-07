plugins {
    `java-library`
}

dependencies {
    implementation(project(":spi:core-spi"))
    implementation(libs.edc.core.spi)
    implementation(libs.mongodb.driver.sync)

    testImplementation(libs.edc.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.testcontainers.junit)
}
