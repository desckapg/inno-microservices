plugins {
    id("org.springframework.boot") version "4.0.0-M3"
    id("io.spring.dependency-management") version "1.1.7"
}

version = "0.0.1-SNAPSHOT"
description = "auth-service"

val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(libs.java.jwt)
    implementation(libs.bcrypt)

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
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation(libs.system.stubs.jupiter)
    testImplementation(libs.fixture.monkey.starter)
    testImplementation(libs.fixture.monkey.datafaker)
    testImplementation(libs.datafaker)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testCompileOnly("org.projectlombok:lombok")

    testAnnotationProcessor("org.projectlombok:lombok")
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
