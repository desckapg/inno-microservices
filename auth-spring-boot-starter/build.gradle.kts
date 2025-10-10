plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.7"

}

version = "0.0.1-SNAPSHOT"
description = "auth-spring-boot-starter"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.0-M3")
    }
}

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter")
    compileOnly("org.springframework.boot:spring-boot-starter-security")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.projectlombok:lombok")

    implementation(libs.java.jwt)

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Отключаем задачу bootJar если она есть
tasks.findByName("bootJar")?.enabled = false

// Включаем обычный jar
tasks.jar {
    enabled = true
    archiveClassifier.set("") // Убираем classifier
}