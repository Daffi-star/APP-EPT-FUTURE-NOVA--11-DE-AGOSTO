import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(FileInputStream(file))
}

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.dafi.ruwayspace"
    compileSdk = 37 // 👈 Corregido aquí (número directo)

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.dafi.ruwayspace"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            // ...
            // Pon tu clave real aquí entre comillas dobles para probar:
        buildConfigField("String", "GEMINI_API_KEY", "\"AQ.Ab8RN6If_Jd17vijMa43QZMYnNUthL-oMuM6fBoXcyO-5DMqkw\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false // 👈 Corregido aquí (propiedad estándar)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation(platform("com.google.firebase:firebase-bom:34.17.0"))
    val roomVersion = "2.7.0"

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
}