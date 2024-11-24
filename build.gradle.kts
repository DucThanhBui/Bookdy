plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.parcelize")
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = (property("android.compileSdk") as String).toInt()
    defaultConfig {
        minSdk = (property("android.minSdk") as String).toInt()
        targetSdk = (property("android.targetSdk") as String).toInt()

        applicationId = "com.example.bookdy"

        versionName = "1.0"
        versionCode = 1

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
        }
    }
    packaging {
        resources.excludes.add("META-INF/*")
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
            res.srcDirs("src/main/res")
            assets.srcDirs("src/main/assets")
        }
    }
    namespace = "com.example.bookdy"
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.kotlin.stdlib)

    val readium_version = "3.0.0"
    implementation (libs.readium.shared)
    implementation (libs.readium.streamer)
    implementation (libs.readium.navigator)
    implementation (libs.readium.adapter.pdfium)
    implementation(libs.readium.adapter.exoplayer)

    implementation(libs.readium.navigator.media.audio)
    implementation(libs.readium.navigator.media.tts)
//    implementation ("org.readium.kotlin-toolkit:readium-opds:$readium_version")
//    implementation ("org.readium.kotlin-toolkit:readium-lcp:$readium_version")
//    implementation(project(":readium:readium-shared"))
//    implementation(project(":readium:readium-streamer"))
//    implementation(project(":readium:readium-navigator"))
//    implementation(project(":readium:navigators:media:readium-navigator-media-tts"))
    // Only required if you want to support audiobooks using ExoPlayer.
    // Only required if you want to support PDF files using PDFium.
//    implementation(project(":readium:adapters:pdfium"))
    // Retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    //pref datastore
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.cardview)

    implementation(libs.bundles.compose)
//    debugImplementation(libs.androidx.compose.ui)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.paging)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.webkit)
    implementation(libs.google.material)
    implementation(libs.timber)
    implementation(libs.picasso)
    implementation(libs.joda.time)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.jsoup)

    implementation(libs.bundles.media3)

    // Room database
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
    implementation(libs.logging.interceptor)



    // Gson
    implementation (libs.gson)
    implementation(kotlin("script-runtime"))
}
