plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("io.realm.kotlin") version "1.16.0"
    id("maven-publish")
}

android {
    namespace = "info.hermiths.lbesdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    libraryVariants.all { variant ->
        variant.outputs.all { output ->
            if (output is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                output.outputFileName = "LbeIm-release.aar"
            }
            true
        }
    }
}

dependencies {
    api("androidx.core:core-ktx:1.13.1")
    api("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    api("androidx.activity:activity-compose:1.9.3")
    api(platform("androidx.compose:compose-bom:2024.10.00"))
    api("androidx.compose.ui:ui")
    api("androidx.compose.ui:ui-graphics")
    api("androidx.compose.ui:ui-tooling-preview")
    api("androidx.compose.material3:material3")
    api("androidx.documentfile:documentfile:1.0.1")
    api("androidx.navigation:navigation-compose:2.8.3")
    api("junit:junit:4.13.2")
    api("androidx.test.ext:junit:1.2.1")
    api("androidx.test.espresso:espresso-core:3.6.1")
    api("androidx.compose.ui:ui-test-junit4")
    api("androidx.compose.ui:ui-tooling")
    api("androidx.compose.ui:ui-test-manifest")

    // ViewModel
    api("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    // LiveData
    api("androidx.compose.runtime:runtime-livedata:1.7.4")

    // Scarlet
    api("com.tinder.scarlet:scarlet:0.1.12")
    api("com.tinder.scarlet:message-adapter-protobuf:0.1.12")
    api("com.tinder.scarlet:websocket-okhttp:0.1.12")
    api("com.tinder.scarlet:stream-adapter-rxjava2:0.1.12")

    //RX
    api("io.reactivex.rxjava2:rxjava:2.2.21")
    api("io.reactivex.rxjava2:rxandroid:2.1.1")
    api("io.reactivex.rxjava2:rxkotlin:2.4.0")

    // Gson
    api("com.google.code.gson:gson:2.10.1")

    // Protobuf
    api("com.google.protobuf:protobuf-kotlin:4.29.0-RC1")
    api("com.google.protobuf:protobuf-java:3.22.3")
    // implementation ("com.google.protobuf:protobuf-lite:3.0.1")
    // implementation("io.grpc:grpc-stub:1.57.0")
    // implementation("io.grpc:grpc-protobuf:1.57.0")

    //  Coil
    api("io.coil-kt.coil3:coil-compose:3.0.4")
    api("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
    api("io.coil-kt.coil3:coil-gif:3.0.4")

    // Glide
//    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")

    // Retrofit
    api("com.squareup.retrofit2:retrofit:2.11.0")
    api("com.squareup.retrofit2:converter-gson:2.9.0")
//    api("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")

    // Exoplayer
    api("androidx.media3:media3-exoplayer:1.4.1")
    api("androidx.media3:media3-ui:1.4.1")

    // Realm
    api("io.realm.kotlin:library-base:1.16.0")

    // Permission
    api("com.google.accompanist:accompanist-permissions:0.36.0")
}

afterEvaluate {
    publishing {
        publications {
            create("release", MavenPublication::class.java) {
                from(components["release"]) // 发布 release 组件
                groupId = "com.github.haishuangsu" // 你的 JitPack 组名
                artifactId = "LbeIMSdk"           // 库的名称
                version = "1.0.2"                 // 库的版本号
            }
        }

        repositories {
            maven {
                url = uri("https://jitpack.io")
            }
        }
    }
}