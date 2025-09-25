plugins {
    id("org.springframework.boot") version "4.0.0-M3"
    id("io.spring.dependency-management") version "1.1.7"
}

version = "0.0.1-SNAPSHOT"
description = "user-service"

val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation(libs.mapstruct)

    mockitoAgent(libs.mockito) { isTransitive = false }

    compileOnly("org.projectlombok:lombok")

    runtimeOnly("org.postgresql:postgresql")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(libs.mockito)
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.redis:testcontainers-redis")
    testImplementation("org.testcontainers:junit-jupiter")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    //jvmArgs.add("-javaagent:${mockitoAgent.asPath}")
}
