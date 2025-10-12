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
            property("sonar.projectKey", "desckapg_inno-microservices_${project.name}")
        }
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
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
            html.required.set(true)
            xml.required.set(true)
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "desckapg_inno-microservices")
        property("sonar.organization", "desckapg")
    }
}

tasks.sonar {
    dependsOn(":user-service:build")
    dependsOn(":auth-service:build")
}

tasks.withType<Wrapper> {
    gradleVersion = "9.1.0"
}