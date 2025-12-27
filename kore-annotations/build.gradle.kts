plugins {
    id("java-library")
    id("maven-publish")
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

publishing {
    publications {
        register<MavenPublication>("release") {
            version = "1.0.13"
        }
    }
}