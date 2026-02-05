plugins {
    alias(libs.plugins.android.application)
    id("maven-publish")
}

android {
    namespace = "com.olivadevelop.kore"
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments.put("kore.version", "$version")
                arguments.put("kore.projectDir", project.projectDir.path)
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.material.calendarview)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.lottie)
    implementation(libs.preference)
    implementation(libs.work.runtime)
    implementation(libs.picasso)
    implementation(libs.exp4j)
    implementation(libs.activity)
    implementation(libs.colorpickerview)
    implementation(libs.recyclerview)
//    implementation(libs.legacy.support.v4)
    implementation(project(":kore-annotations"))
    annotationProcessor(project(":kore-processor"))
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.room.runtime)
    implementation(libs.commons.lang3)
    annotationProcessor(libs.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.flexbox)
    implementation(libs.gson)
    implementation(libs.play.services.ads)
    implementation(libs.billing)
    implementation(libs.guava)
    implementation(libs.mpandroidchart)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add(
        "-Akore.version=$version"
    )
    options.compilerArgs.add(
        "-Akore.projectDir="+project.projectDir.absolutePath
    )
}

publishing {
    publications {
        register<MavenPublication>("release") {
            artifactId = "kore"
            version = rootProject.version.toString()

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}