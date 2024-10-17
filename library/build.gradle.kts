plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}
group = "com.github.whosmyqueen"
android.buildFeatures.buildConfig = true
android.buildFeatures.aidl = true
android {
    namespace = "com.inuker.bluetooth.library"
    compileSdk = 34

    defaultConfig {
        minSdk = 25
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

}