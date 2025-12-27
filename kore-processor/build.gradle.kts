plugins {
    id("java-library")
    id("maven-publish")
}
java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
dependencies {
    implementation(project(":kore-annotations"))
    implementation("com.google.auto.service:auto-service:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.pedroolivagil"
            artifactId = "kore-processor"
            version = "1.0.12"
        }
    }
}