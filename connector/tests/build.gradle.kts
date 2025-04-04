plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    testImplementation(libs.edc.boot.lib)
    testImplementation(libs.edc.junit)

    testImplementation(libs.assertj)
    testImplementation(libs.awaitility)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockserver.netty)
    testImplementation(libs.rest.assured)
    testImplementation(libs.json.schema.validator)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.keycloak)
    testImplementation(libs.testcontainers.nginx)
    testImplementation(libs.testcontainers.hashicorp.vault)
    testImplementation(libs.testcontainers.postgresql)

    testImplementation(testFixtures(libs.edc.sql.test.fixtures))
    testImplementation(testFixtures(libs.edc.ih.test.fixtures))
    testImplementation(testFixtures(libs.edc.management.api.test.fixtures))
}
