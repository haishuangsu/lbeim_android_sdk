plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("io.realm.kotlin") version "1.16.0"
//    id("com.kezong.fat-aar")
}

android {
    namespace = "info.hermiths.lbesdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
//            )
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    libraryVariants.all { variant ->
        variant.outputs.all { output ->
            if (output is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
//                val outputFile = output.outputFile
//                outputFile.writeToFile()
                // 修改 AAR 输出文件名
                output.outputFileName = "LbeIm-release.aar"
            }
            true
        }
    }
}


dependencies {
    api("androidx.core:core-ktx:1.13.1") {
        isTransitive = true
    }
    api("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6") {
        isTransitive = true
    }
    api("androidx.activity:activity-compose:1.9.3") {
        isTransitive = true
    }
    api(platform("androidx.compose:compose-bom:2024.10.00"))
    api("androidx.compose.ui:ui") {
        isTransitive = true
    }
    api("androidx.compose.ui:ui-graphics") {
        isTransitive = true
    }
    api("androidx.compose.ui:ui-tooling-preview") {
        isTransitive = true
    }
    api("androidx.compose.material3:material3") {
        isTransitive = true
    }
    api("androidx.documentfile:documentfile:1.0.1") {
        isTransitive = true
    }
    api("androidx.navigation:navigation-compose:2.8.3") {
        isTransitive = true
    }
    api("junit:junit:4.13.2") {
        isTransitive = true
    }
    api("androidx.test.ext:junit:1.2.1") {
        isTransitive = true
    }
    api("androidx.test.espresso:espresso-core:3.6.1") {
        isTransitive = true
    }
    api("androidx.compose.ui:ui-test-junit4") {
        isTransitive = true
    }
    api("androidx.compose.ui:ui-tooling") {
        isTransitive = true
    }
    api("androidx.compose.ui:ui-test-manifest") {
        isTransitive = true
    }

    // ViewModel
    api("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6") {
        isTransitive = true
    }

    // LiveData
    api("androidx.compose.runtime:runtime-livedata:1.7.4") {
        isTransitive = true
    }

    // Scarlet
    api("com.tinder.scarlet:scarlet:0.1.12") {
        isTransitive = true
    }
    api("com.tinder.scarlet:message-adapter-protobuf:0.1.12") {
        isTransitive = true
    }
    api("com.tinder.scarlet:websocket-okhttp:0.1.12") {
        isTransitive = true
    }
    api("com.tinder.scarlet:stream-adapter-rxjava2:0.1.12") {
        isTransitive = true
    }

    //RX
    api("io.reactivex.rxjava2:rxjava:2.2.21") {
        isTransitive = true
    }
    api("io.reactivex.rxjava2:rxandroid:2.1.1") {
        isTransitive = true
    }
    api("io.reactivex.rxjava2:rxkotlin:2.4.0") {
        isTransitive = true
    }

    // Gson
    api("com.google.code.gson:gson:2.10.1") {
        isTransitive = true
    }

    // Protobuf
    api("com.google.protobuf:protobuf-kotlin:4.29.0-RC1") {
        isTransitive = true
    }
    api("com.google.protobuf:protobuf-java:3.22.3") {
        isTransitive = true
    }
    // implementation ("com.google.protobuf:protobuf-lite:3.0.1")
    // implementation("io.grpc:grpc-stub:1.57.0")
    // implementation("io.grpc:grpc-protobuf:1.57.0")

    //  Coil
    api("io.coil-kt.coil3:coil-compose:3.0.4") {
        isTransitive = true
    }
    api("io.coil-kt.coil3:coil-network-okhttp:3.0.4") {
        isTransitive = true
    }
    api("io.coil-kt.coil3:coil-gif:3.0.4") {
        isTransitive = true
    }

    // Glide
//    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")

    // Retrofit
    api("com.squareup.retrofit2:retrofit:2.11.0") {
        isTransitive = true
    }
    api("com.squareup.retrofit2:converter-gson:2.9.0") {
        isTransitive = true
    }
//    api("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")

    // Exoplayer
    api("androidx.media3:media3-exoplayer:1.4.1") {
        isTransitive = true
    }
    api("androidx.media3:media3-ui:1.4.1") {
        isTransitive = true
    }

    // Realm
    api("io.realm.kotlin:library-base:1.16.0") {
        isTransitive = true
    }

    api("com.google.accompanist:accompanist-permissions:0.36.0") {
        isTransitive = true
    }
}