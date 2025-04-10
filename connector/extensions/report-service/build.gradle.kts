plugins {
    `java-library`

}

dependencies {
    implementation(libs.edc.auth.spi)
    implementation(libs.edc.web.spi)
    runtimeOnly(project(":extensions:keycloak-auth"))

    testImplementation(libs.edc.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito.core)
}
