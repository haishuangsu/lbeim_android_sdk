import com.android.build.gradle.internal.scope.ProjectInfo.Companion.getBaseName
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.realm.kotlin")
//    id("kotlin-kapt")
//    id("com.google.dagger.hilt.android")
//    id ("com.google.protobuf") version "0.9.4"
}

android {
    namespace = "info.hermiths.chatapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "info.hermiths.chatapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        val properties = Properties()
        properties.load(rootProject.file("local.properties").reader())
        buildConfigField("String", "websocketApiKey", "\"${properties["WEB_SOCKET_API_KEY"]}\"")
        buildConfigField("String", "lbeSign", "\"${properties["LBE_SIGN"]}\"")
    }

    signingConfigs {
        create("release") {
            keyAlias = "hermit"
            keyPassword = "gavin@95"
            storeFile = file("../hermit.jks")
            storePassword = "gavin@95"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Allow references to generated code
//kapt {
//    correctErrorTypes = true
//}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.documentfile:documentfile:1.0.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    // LiveData
    implementation("androidx.compose.runtime:runtime-livedata:1.7.4")

    // Scarlet
    implementation("com.tinder.scarlet:scarlet:0.1.12")
    implementation("com.tinder.scarlet:message-adapter-protobuf:0.1.12")
    implementation("com.tinder.scarlet:websocket-okhttp:0.1.12")
    implementation("com.tinder.scarlet:stream-adapter-rxjava2:0.1.12")

    //RX
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Protobuf
    implementation("com.google.protobuf:protobuf-kotlin:4.29.0-RC1")
    implementation("com.google.protobuf:protobuf-java:3.22.3")
    // implementation ("com.google.protobuf:protobuf-lite:3.0.1")
    // implementation("io.grpc:grpc-stub:1.57.0")
    // implementation("io.grpc:grpc-protobuf:1.57.0")

    //  Coil
    implementation("io.coil-kt.coil3:coil-compose:3.0.0-rc01")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0-rc01")
    implementation("io.coil-kt.coil3:coil-gif:3.0.2")
    implementation("io.coil-kt.coil3:coil-svg:3.0.2")

    // Glide
    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    api("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")

    // Exoplayer
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    // Realm
    implementation("io.realm.kotlin:library-base:1.16.0")

    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    // Hilt
    //  implementation("com.google.dagger:hilt-android:2.52")
    //  kapt("com.google.dagger:hilt-android-compiler:2.44")
}
