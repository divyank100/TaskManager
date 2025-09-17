plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false   // 👈 add this
    alias(libs.plugins.hilt.android) apply false // 👈 add this
    alias(libs.plugins.ksp) apply false          // 👈 add this (for Room)
}
