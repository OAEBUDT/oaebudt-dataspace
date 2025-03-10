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

include(":launchers:controlplane")
include(":launchers:dataplane")
include(":launchers:runtime-embedded")
include(":launchers:api")
include("tests")

