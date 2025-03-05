import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin

plugins {
    `java-library`
    id("com.bmuschko.docker-remote-api") version "9.4.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.sonarqube") version "6.0.1.5171"
}

buildscript {
    dependencies {
        val edcGradlePluginsVersion: String by project
        classpath("org.eclipse.edc.edc-build:org.eclipse.edc.edc-build.gradle.plugin:${edcGradlePluginsVersion}")
    }
}

val edcGradlePluginsVersion: String by project

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    testing {
        suites {
            val test by getting(JvmTestSuite::class) {
                useJUnitJupiter()
            }
        }
    }

    // needed for E2E tests
    tasks.register("printClasspath") {
        dependsOn(tasks.compileJava)
        doLast {
            println(sourceSets["main"].runtimeClasspath.asPath)
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "OAEBUDT_oaebudt-dataspace")
        property("sonar.organization", "oaebudt")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
