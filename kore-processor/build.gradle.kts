plugins {
    id("java-library")
    id("maven-publish")
}
java {
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
            artifactId = "kore-processor"
            version = "1.0.14"

            from(components["java"])
        }
    }
}