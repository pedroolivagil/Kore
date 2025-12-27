plugins {
    id("java-library")
    id("maven-publish")
}
java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.pedroolivagil"
            artifactId = "kore-annotations"
            version = "1.0.12"
        }
    }
}