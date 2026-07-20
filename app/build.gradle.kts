plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

import java.util.Properties
import java.io.File

fun getSecret(key: String): String {
    val envVal = System.getenv(key)
    if (!envVal.isNullOrEmpty()) {
        return envVal
    }
    val envFile = project.rootProject.file(".env")
    if (envFile.exists()) {
        val props = Properties()
        envFile.inputStream().use { props.load(it) }
        val propVal = props.getProperty(key)
        if (!propVal.isNullOrEmpty()) {
            return propVal
        }
    }
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        val props = Properties()
        localPropertiesFile.inputStream().use { props.load(it) }
        val propVal = props.getProperty(key)
        if (!propVal.isNullOrEmpty()) {
            return propVal
        }
    }
    return ""
}

android {
    namespace = "com.example"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aistudio.bluefoxledger.pzxwml"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        val dbUrl = getSecret("DATABASE_URL")
        buildConfigField("String", "DATABASE_URL", "\"$dbUrl\"")

        val githubToken = getSecret("GITHUB_TOKEN")
        buildConfigField("String", "GITHUB_TOKEN", "\"$githubToken\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // PostgreSQL JDBC driver bundles these — they conflict with Android's resource merger
            excludes += "META-INF/MANIFEST.MF"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/*.kotlin_module"
            // Exclude duplicate service files from JDBC driver
            pickFirsts += "META-INF/services/java.sql.Driver"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // PostgreSQL JDBC Driver for Direct Hosted DB Sync
    // 42.7.x is the latest stable with better Android compatibility
    implementation("org.postgresql:postgresql:42.7.3") {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.slf4j", module = "slf4j-simple")
        // Exclude checker-qual — not needed on Android and causes build issues
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    
    // Coil
    implementation(libs.coil.compose)
    
    testImplementation("junit:junit:4.13.2")
}

// Ensure BuildConfig is regenerated whenever the .env file is updated
tasks.configureEach {
    if (name.contains("generateBuildConfig", ignoreCase = true)) {
        val envFile = project.rootProject.file(".env")
        if (envFile.exists()) {
            inputs.file(envFile)
        }
    }
}
