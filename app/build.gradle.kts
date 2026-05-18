plugins {
    alias(libs.plugins.android.application)
//    for room db
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
//    for firebase
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")

    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

android {
    namespace = "com.example.stockapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.stockapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    // Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Jetpack Compose BOM (Bill of Materials) and Core Layout Toolkits
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Material Design 3 (Teaches modern styled components like Card, Scaffold, Text)
    implementation(libs.androidx.material3)

    // Integration with Activities & ViewModels
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Reactive State Bridge: Converts your Room LiveData seamlessly into reactive Compose States
    implementation(libs.androidx.runtime.livedata)

    // Room db
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // ViewModel + LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Retrofit, HTTP client for Alpha Vantage API
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // MPAndroidChart for price history chart
    implementation(libs.mpandroidchart)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.material)

//    recycler view
    implementation(libs.androidx.recyclerview)

//    WorkManager
    implementation(libs.androidx.work.runtime.ktx)

//    HttpLoggingInterceptor for seeing the full API response in the Logcat
    implementation(libs.logging.interceptor)

//    testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    testImplementation(kotlin("test"))
}