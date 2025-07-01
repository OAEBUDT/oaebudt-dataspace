plugins {
    `java-library`
}

dependencies {
    implementation(project(":spi:core-spi"))
    implementation(libs.edc.core.spi)
    implementation(libs.edc.lib.sql)

    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.database.postgres)

    testImplementation(libs.edc.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.postgres)
    testImplementation(testFixtures(libs.edc.sql.test.fixtures))
}
