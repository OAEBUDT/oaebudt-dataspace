plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.lib.sql)
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.database.postgres)

    testImplementation(libs.edc.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.testcontainers.junit)
}
