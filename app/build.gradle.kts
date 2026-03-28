import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("jacoco")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

val apiKey: String = localProperties.getProperty("API_KEY")
    ?: throw GradleException("API_KEY NOT FOUND in local.properties")
android {
    namespace = "com.group5.gue"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.group5.gue"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "API_KEY", "\"$apiKey\"")
        manifestPlaceholders["API_KEY"] = apiKey

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
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.robolectric:robolectric:4.12.2")
    testImplementation("androidx.test:core:1.6.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(platform(libs.supabase.bom))
    implementation(libs.auth.kt)
    implementation(libs.postgrest.kt)
    implementation(libs.storage.kt)


    implementation(libs.ktor.client.okhttp)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("com.google.android.gms:play-services-maps:20.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.work.runtime)
    implementation(libs.glide)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.preference)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

jacoco {
    toolVersion = "0.8.14"
}

// Create a custom JaCoCo report task
tasks.register<JacocoReport>("jacocoTestReport") {

    // Run BOTH test types before generating report
    dependsOn("testDebugUnitTest", "connectedDebugAndroidTest")

    //Configure output formats
    reports {
        xml.required.set(true) // XML -> required for SonarQube
        html.required.set(true) //HTML -> visual coverage report
    }

    // Exclude auto-generated or irrelevant files from coverage
    val fileFilter = listOf(
        "**/R.class", // Android resources classes
        "**/R$*.class", // Nested resources classes
        "**/BuildConfig.*", // Auto-generated BuildConfig
        "**/Manifest*.*", // Manifest-generated files
        "**/*Test*.*", // Exclude test classes
        "android/**/*.*" //Android framework classes
    )

    //Kotlin compiled classes for debug build
    val kotlinClasses = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        exclude(fileFilter)
    }

    //Java compiled classes for debug build
    val javaClasses = fileTree(layout.buildDirectory.dir("intermediates/javac/debug")) {
        exclude(fileFilter)
    }

    // Compiled Kotlin + Java class directories
    classDirectories.setFrom(files(kotlinClasses, javaClasses))

    // Tell JacoCo where your actual source code lives
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))

    //Point to the JaCoCo execution data generated by unit tests and instrumented tests
    executionData.setFrom(fileTree(layout.buildDirectory) {

        include(
            // Unit test coverage
            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",

            // Instrumented test coverage
            "outputs/code_coverage/debugAndroidTest/connected/**/*.ec"
        )
    }
    )
}