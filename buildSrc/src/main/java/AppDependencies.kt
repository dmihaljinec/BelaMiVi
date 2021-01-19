import org.gradle.api.artifacts.dsl.DependencyHandler
@Suppress("unused")
object AppDependencies {
    // Kotlin std lib
    private const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    // Coroutines
    private const val kotlinCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    private const val kotlinCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    // ViewModel and LiveData
    private const val lifecycleExtensions = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}"
    private const val lifecycleLiveDataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    private const val lifecycleViewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    // Android UI and appcompat
    private const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    private const val constraintLayout = "com.android.support.constraint:constraint-layout:${Versions.constraintLayout}"
    private const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    private const val fragmentKtx = "androidx.fragment:fragment-ktx:${Versions.fragment}"
    private const val preferenceKtx = "androidx.preference:preference-ktx:${Versions.preference}"
    // Navigation
    private const val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    private const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
    // Room
    private const val roomKtx = "androidx.room:room-ktx:${Versions.room}"
    private const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    // Hilt (Dagger dependency injection)
    private const val hiltAndroid = "com.google.dagger:hilt-android:${Versions.hiltDagger}"
    private const val hiltDaggerCompiler = "com.google.dagger:hilt-compiler:${Versions.hiltDagger}"
    private const val hiltLifecycleViewModel = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.hilt}"
    private const val hiltWork = "androidx.hilt:hilt-work:${Versions.hilt}"
    private const val hiltCompiler = "androidx.hilt:hilt-compiler:${Versions.hilt}"
    // Work
    private const val workRuntimeKtx = "androidx.work:work-runtime-ktx:${Versions.work}"
    // Stateless4j state machine
    private const val stateless4j = "com.github.stateless4j:stateless4j:${Versions.stateless4j}"
    // ProcessPhoenix to restart app after restoring database
    private const val processPhoenix = "com.jakewharton:process-phoenix:${Versions.processPhoenix}"
    // Unit test
    private const val junit = "junit:junit:${Versions.junit}"
    private const val kotlinxCoroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    private const val mockk = "io.mockk:mockk:${Versions.mockk}"
    // Android Instrumented test
    private const val testCore = "androidx.test:core:${Versions.androidTest}"
    private const val testCoreKtx = "androidx.test:core-ktx:${Versions.androidTest}"
    private const val testRunner = "androidx.test:runner:${Versions.androidTest}"
    // Leak canary
    private const val leakCanaryAndroid = "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}"

    val appLibs = arrayListOf<String>().apply {
        add(appcompat)
        add(constraintLayout)
        add(fragmentKtx)
        add(hiltAndroid)
        add(hiltLifecycleViewModel)
        add(hiltWork)
        add(kotlinCoroutinesAndroid)
        add(kotlinCoroutinesCore)
        add(kotlinStdLib)
        add(lifecycleExtensions)
        add(lifecycleLiveDataKtx)
        add(lifecycleViewModelKtx)
        add(navigationFragmentKtx)
        add(navigationUiKtx)
        add(preferenceKtx)
        add(processPhoenix)
        add(recyclerView)
        add(roomKtx)
        add(stateless4j)
        add(workRuntimeKtx)
    }

    val appAnnotationProcessors = arrayListOf<String>().apply {
        add(hiltCompiler)
        add(hiltDaggerCompiler)
        add(roomCompiler)
    }
    
    val testLibs = arrayListOf<String>().apply {
        add(junit)
        add(kotlinxCoroutinesTest)
        add(mockk)
    }
    
    val androidTestLibs = arrayListOf<String>().apply {
        add(testCore)
        add(testCoreKtx)
        add(testRunner)
    }
}

//util functions for adding the different type dependencies from build.gradle file
fun DependencyHandler.kapt(list: List<String>) {
    list.forEach { dependency ->
        add("kapt", dependency)
    }
}

fun DependencyHandler.implementation(list: List<String>) {
    list.forEach { dependency ->
        add("implementation", dependency)
    }
}

fun DependencyHandler.androidTestImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("androidTestImplementation", dependency)
    }
}

fun DependencyHandler.testImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("testImplementation", dependency)
    }
}
