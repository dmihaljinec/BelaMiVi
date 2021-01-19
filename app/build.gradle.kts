import com.android.build.gradle.api.ApplicationVariant

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdkVersion(AppConfig.compileSdk)
    buildToolsVersion(AppConfig.buildTools)

    defaultConfig {
        applicationId(AppConfig.applicationId)
        minSdkVersion(AppConfig.minSdk)
        targetSdkVersion(AppConfig.targetSdk)
        versionCode(AppConfig.versionCode)
        versionName(AppConfig.versionName)
        testInstrumentationRunner(AppConfig.testInstrumentationRunner)
    }

    applicationVariants.all(object : Action<ApplicationVariant> {
        override fun execute(variant: ApplicationVariant) {
            variant.resValue(
                "string",
                "versionName",
                "${variant.versionName}.${variant.versionCode}"
            )
            variant.resValue(
                "string",
                "gitHashShort",
                "git hash: ${"git rev-parse --short HEAD".runCommand(workingDir = rootDir)}"
            )
        }
    })

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions("mode")
    productFlavors {
        create("dev") {
            applicationIdSuffix = ".dev"
            dimension = "mode"
        }
        create("prod") {
            dimension = "mode"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        dataBinding = true
    }
}

configurations.all {
    // androidx.test includes junit 4.12 so this will force that entire project uses same junit version
    resolutionStrategy.force("junit:junit:${Versions.junit}")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(AppDependencies.appLibs)
    kapt(AppDependencies.appAnnotationProcessors)
    testImplementation(AppDependencies.testLibs)
    androidTestImplementation(AppDependencies.androidTestLibs)
}

