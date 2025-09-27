plugins {
    java
    jacoco
    id("org.sonarqube") version "6.3.1.5724"
}

allprojects {
    group = "com.innowise"
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "org.sonarqube")

    sonar {
        properties {
            property("sonar.sources", "src/main/java")
            property("sonar.tests", "src/test/java")
            property(
                "sonar.coverage.jacoco.xmlReportPaths",
                "build/reports/jacoco/test/jacocoTestReport.xml"
            )
            property("sonar.projectKey", "desckapg_inno-microservices_${project.name}")
        }
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
    }

    tasks.jacocoTestReport {
        reports {
            xml.required.set(true)
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "desckapg_inno-microservices")
        property("sonar.organization", "desckapg")
        property("sonar.scm.disabled", "true")

    }
}

tasks.sonar {
    dependsOn(":user-service:build")
}

tasks.withType<Wrapper> {
    gradleVersion = "8.14.3"
}