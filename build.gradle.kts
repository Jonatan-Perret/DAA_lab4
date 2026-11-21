// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // KSP (Kotlin Symbol Processing) : nécessaire pour la génération de code de Room
    alias(libs.plugins.ksp) apply false
}
