plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.rhezarijaya.storiesone"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.rhezarijaya.storiesone"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "STORY_API_BASE_URL", "\"https://story-api.dicoding.dev/v1/\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // testoptions diberikan karena terdapat error saat testing paging dengan error sebagai berikut
    // java.lang.RuntimeException: Method isLoggable in android.util.Log not mocked
    // solusi yang diberikan pada stack trace nya: https://developer.android.com/r/studio-ui/build/not-mocked
    testOptions {
        animationsDisabled = true
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.9.0")

    // activity
    implementation("androidx.activity:activity-ktx:1.7.2")

    // camera (for exif)
    implementation("androidx.camera:camera-camera2:1.2.3")

    // datastore preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Idling Resource
    implementation("androidx.test.espresso:espresso-idling-resource:3.5.1")

    // livedata + viewmodel
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // paging
    implementation("androidx.room:room-paging:2.6.0-beta01")
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")

    // room
    implementation("androidx.room:room-runtime:2.6.0-beta01")
    implementation("androidx.room:room-ktx:2.6.0-beta01")
    kapt("androidx.room:room-compiler:2.6.0-beta01")

    // google maps
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // retrofit
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // circleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")

    // MockWebServer
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
    androidTestImplementation("com.squareup.okhttp3:okhttp-tls:4.9.3")

    // Intents
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
}