plugins {
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
}

val springCloudVersion by extra("2025.1.0")

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
    implementation(project(":common"))
    implementation(project(":auth-spring-boot-starter"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation(libs.java.jwt)
    implementation(libs.bcrypt)
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    implementation(libs.spring.boot.starter.aop)
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation(libs.opentelemetry.logback.appender)

    compileOnly("org.projectlombok:lombok")
    compileOnly(libs.mapstruct)

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    runtimeOnly("org.postgresql:postgresql")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor(libs.mapstruct.processor)

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito:mockito-core")
    }
    testImplementation(libs.mockito)
    mockitoAgent(libs.mockito) { isTransitive = false }
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation(libs.system.stubs.jupiter)
    testImplementation(libs.fixture.monkey.starter)
    testImplementation(libs.fixture.monkey.datafaker)
    testImplementation(libs.datafaker)
    testImplementation(libs.wiremock.standalone)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testCompileOnly("org.projectlombok:lombok")

    testAnnotationProcessor("org.projectlombok:lombok")
    testAnnotationProcessor(libs.mapstruct.processor)
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
                    "com/innowise/authservice/model/**",
                    "com.innowise.authservice.UserServiceApplication.class"
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

tasks.withType<Test> {
    jvmArgs = listOf(
        "-javaagent:${mockitoAgent.asPath}",
        "--add-opens=java.base/java.time=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "-XX:+EnableDynamicAgentLoading"
    )
}
