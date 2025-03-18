rootProject.name = "connector"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

include("extensions:keycloak-auth")
include(":launchers:controlplane")
include(":launchers:dataplane")
include(":launchers:runtime-embedded")
include("tests")
