plugins {
    `java-library`
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
    plugins.apply("application")
    plugins.apply("jacoco")

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy(tasks.named("jacocoTestReport"))
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn(tasks.named("test"))
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
    }

    testing {
        suites {
            val test by getting(JvmTestSuite::class) {
                useJUnitJupiter()
            }
        }
    }

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
