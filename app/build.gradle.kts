plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id ("androidx.navigation.safeargs.kotlin")
    id ("kotlin-kapt")
}

android {
    namespace = "com.afitech.absensi"
    compileSdk {
        version = release(36)
    }
    signingConfigs {
        create("release") {
            storeFile = file("keystore/E-Absensi.jks")
            storePassword = "qwerty"
            keyAlias = "afitech"
            keyPassword = "qwerty"
        }
    }
    defaultConfig {
        applicationId = "com.afitech.absensi"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 🔥 DEBUG pakai nama biasa
        manifestPlaceholders["appLabel"] = "E-Absensi"
    }

    buildTypes {

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            manifestPlaceholders["appLabel"] = "E-Absensi (Debug)"
        }
    }
    applicationVariants.all {
        outputs.all {

            val appName = "E-Absensi"
            val versionName = defaultConfig.versionName
            val buildTypeName = buildType.name

            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl)
                .outputFileName = "${appName}_v${versionName}_${buildTypeName}.apk"
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
        viewBinding = true
    }

    dependencies {
        // Core
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)

        // Navigation
        val navVersion = "2.7.7"
        implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
        implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

        // Firebase (🔥 BOM)
        implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
        implementation("com.google.firebase:firebase-firestore-ktx")
        implementation("com.google.firebase:firebase-auth-ktx")
        implementation("com.google.firebase:firebase-storage-ktx")

        // Test
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)

//        maps
//        implementation("com.google.android.gms:play-services-maps:18.2.0")
        implementation("com.google.android.libraries.places:places:3.3.0")
        implementation("com.google.android.gms:play-services-location:21.0.1")

        implementation("androidx.exifinterface:exifinterface:1.3.7")
        implementation("com.google.android.gms:play-services-auth:21.0.0")
        implementation("com.airbnb.android:lottie:6.7.1")
        implementation ("com.github.bumptech.glide:glide:4.16.0")
        kapt ("com.github.bumptech.glide:compiler:4.16.0")
        implementation("com.google.android.gms:play-services-ads:23.1.0")
    }
}