rootProject.name = "Connector"

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
include("tests")
