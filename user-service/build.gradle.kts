plugins {
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
}
val springCloudVersion by extra("2025.1.0")

dependencies {
    implementation(project(":common"))
    implementation(project(":auth-spring-boot-starter"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation(libs.mapstruct)

    compileOnly("org.projectlombok:lombok")

    runtimeOnly("org.postgresql:postgresql")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-security-test")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("com.redis:testcontainers-redis")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.awaitility:awaitility")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

sonar {
    properties {
        property("sonar.sources", "src/main/java")
        property("sonar.tests", "src/test/java")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "build/reports/jacoco/test/jacocoTestReport.xml"
        )
    }
}


tasks.jacocoTestReport {
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "com/innowise/userservice/model/**",
                    "com.innowise.userservice.UserServiceApplication*"
                )
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = BigDecimal.valueOf(0.8)
            }
        }
    }
}