plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ch.heigvd.iict.daa.labo4"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "ch.heigvd.iict.daa.labo4"
        minSdk = 23
        targetSdk = 37
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
    buildFeatures {
        // génère des classes de binding typées pour chaque layout (remplace findViewById)
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Fragments (by activityViewModels()) et RecyclerView
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)

    // Architecture MVVM : ViewModel et LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Base de données Room 3 (+ support des LiveData), code généré via KSP
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.livedata)
    ksp(libs.androidx.room.compiler)

    // DataStore : persistance du choix de tri (question 6.1)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}