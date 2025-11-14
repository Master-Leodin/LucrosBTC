
plugins {
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

android {
    namespace = "btcemais.lucrosbtc"
    compileSdk = 35

    defaultConfig {
        applicationId = "btcemais.lucrosbtc"
        minSdk = 26
        targetSdk = 34
        versionCode = 5
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
    // **ATUALIZAR VERSÃO JAVA PARA 17** (Recomendado para Kotlin 1.9+)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Usar aliases do catálogo para padronização e evitar erros de versão
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Room
    implementation(libs.androidx.room.runtime)
    // Usar alias para kapt
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.androidx.lifecycle.runtime.ktx) // Adicionado para consistência

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Activity/Fragment
    implementation(libs.androidx.activity.ktx) // Corrigido para activity-ktx
    implementation(libs.androidx.fragment.ktx) // Adicionado para consistência

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp.logging.interceptor )

    // RecyclerView
    implementation(libs.androidx.recyclerview)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("androidx.core:core-ktx:1.13.1")
}
