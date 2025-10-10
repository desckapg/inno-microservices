version = "0.0.1-SNAPSHOT"

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

sonar {
    properties {
        property("sonar.sources", "src/main/java")
    }
}
