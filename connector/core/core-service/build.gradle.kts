plugins {
    `java-library`
}

dependencies {
    implementation(project(":spi:core-spi"))
    implementation(libs.edc.core.spi)
}
