rootProject.name = "connector"

pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {

    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        mavenLocal()
    }
}

include(":extensions:keycloak-auth")
include(":extensions:superuser-seed")
include(":extensions:dcp-impl")

include(":launchers:controlplane")
include(":launchers:dataplane")
include(":launchers:runtime-embedded")
include(":launchers:identity-hub")
include(":launchers:issuerservice")

include("tests")
