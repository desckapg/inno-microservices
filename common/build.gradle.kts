plugins {
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot") version "4.0.0-M3"
}

dependencies {
    implementation("jakarta.validation:jakarta.validation-api")

    compileOnly("org.projectlombok:lombok")

    annotationProcessor("org.projectlombok:lombok")
}

tasks.getByName("bootJar").enabled = false
