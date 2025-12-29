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
    implementation(libs.legacy.support.v4)
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

publishing {
    publications {
        register<MavenPublication>("release") {
            artifactId = "kore"
            version = "1.0.24"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}