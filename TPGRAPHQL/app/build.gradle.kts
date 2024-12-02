plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.apollographql.apollo3") version "3.8.2"

}

android {
    namespace = "ma.ensa.tpgraphql"
    compileSdk = 34


    defaultConfig {
        applicationId = "ma.ensa.tpgraphql"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    implementation ("com.apollographql.apollo3:apollo-runtime:3.8.2")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


apollo {
    service("bank") {
        packageName.set("ma.ensa.tpgraphql")
        schemaFile.set(file("src/main/graphql/schema.graphqls"))
        generateKotlinModels.set(true)

    }
}