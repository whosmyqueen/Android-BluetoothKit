plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

}