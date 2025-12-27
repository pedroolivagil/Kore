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
            artifactId = "kore-annotations"
            version = "1.0.15"

            from(components["java"])
        }
    }
}