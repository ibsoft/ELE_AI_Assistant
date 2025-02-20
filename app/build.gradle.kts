plugins {
    id("com.android.application") version "8.8.0"
    id("org.jetbrains.kotlin.android") version "1.9.10"
    id("com.google.devtools.ksp") version "1.9.10-1.0.13"
}

android {
    namespace = "com.ibsoft.ele"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ibsoft.ele"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true // Enable Jetpack Compose
        dataBinding = true
    }

    // Specify the Compose compiler extension version.
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // Standard AndroidX and Material libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")

    // Jetpack Compose dependencies using the BOM for consistency:
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // Optional: Compose support for Activities
    implementation("androidx.activity:activity-compose:1.7.2")

    // Retrofit & Gson for network calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Room for SQLite persistence
    implementation("androidx.room:room-runtime:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")

    // Coroutines for asynchronous tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Material 3 for Jetpack Compose (optional)
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material3:material3-window-size-class:1.1.2")


    // OkHttp library
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    // OkHttp Logging Interceptor for logging HTTP requests/responses
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")

    implementation("com.github.bumptech.glide:glide:4.15.1")
    ksp("androidx.room:room-compiler:2.5.2")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.android.billingclient:billing:6.0.1")



}

// (Optional) Ensure the Kotlin compilation target is set to 17
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}


