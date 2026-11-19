plugins {
    id("com.android.library")
}

val gemstoneRoot = rootProject.projectDir.resolve("../core/gemstone")
val coreRoot = gemstoneRoot.parentFile
val gemstoneSrc = gemstoneRoot.resolve("android/gemstone/src")
val jniLibsDir = layout.buildDirectory.dir("jniLibs").get().asFile
val generatedKotlinDir = gemstoneSrc.resolve("main/java")
val isRelease = System.getenv("BUILD_MODE") == "release"
val cargoBuildFlag = if (isRelease) "--release" else ""
val gemstoneHostLibrary = rootProject.extra["gemstoneHostLibrary"] as File
val defaultCargoNdkAbis = when {
    System.getenv("UNIT_TESTS") == "true" -> "x86_64"
    isRelease -> "arm64-v8a,armeabi-v7a"
    else -> "arm64-v8a"
}
val cargoNdkTargets = (System.getenv("GEMSTONE_ANDROID_ABIS") ?: defaultCargoNdkAbis)
    .split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .joinToString(" ") { "-t $it" }

android {
    namespace = "com.gemwallet.gemstone"
    ndkVersion = libs.versions.androidNdk.get()

    defaultConfig {
        consumerProguardFiles(gemstoneRoot.resolve("android/gemstone/consumer-rules.pro"))
    }

    sourceSets {
        getByName("main") {
            kotlin {
                directories.add(generatedKotlinDir.absolutePath)
            }
            jniLibs {
                directories.add(jniLibsDir.absolutePath)
            }
            manifest.srcFile(gemstoneSrc.resolve("main/AndroidManifest.xml"))
        }
    }
}

tasks.withType<Exec>().configureEach {
    environment("CARGO_TARGET_DIR", coreRoot.resolve("target").absolutePath)
    val userHome = System.getProperty("user.home")
    val cargoBin = File(userHome, ".cargo/bin").absolutePath
    val currentPath = System.getenv("PATH") ?: ""
    environment("HOME", userHome)
    environment("USERPROFILE", userHome)
    if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
        val gitBin = File(userHome, "AppData/Local/hermes/git/bin").absolutePath
        val gitUsrBin = File(userHome, "AppData/Local/hermes/git/usr/bin").absolutePath
        val gitCmd = File(userHome, "AppData/Local/hermes/git/cmd").absolutePath
        environment("PATH", "$cargoBin;$gitBin;$gitUsrBin;$gitCmd;$currentPath")
    } else {
        environment("PATH", "$cargoBin:$currentPath")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xwarning-level=UNUSED_EXPRESSION:disabled")
}

val isWindows = org.gradle.internal.os.OperatingSystem.current().isWindows

fun shellCommandLine(cmd: String): List<String> {
    return if (isWindows) {
        listOf("cmd", "/c", cmd)
    } else {
        listOf("/bin/sh", "-l", "-c", cmd)
    }
}

val buildGemstoneHost = tasks.register<Exec>("buildGemstoneHost") {
    description = "Build the host Gemstone library used by JVM tests"
    workingDir = coreRoot
    outputs.file(gemstoneHostLibrary)
    outputs.upToDateWhen { false }
    commandLine(shellCommandLine("cargo rustc --package gemstone --lib --crate-type cdylib"))
}

val bindgenKotlin = tasks.register<Exec>("bindgenKotlin") {
    description = "Generate Kotlin bindings from gemstone via uniffi"
    workingDir = gemstoneRoot
    dependsOn(buildGemstoneHost)
    inputs.file(gemstoneHostLibrary)
    inputs.dir(coreRoot.resolve("bin/uniffi-bindgen"))
    inputs.files(
        coreRoot.resolve("Cargo.toml"),
        coreRoot.resolve("Cargo.lock"),
        coreRoot.resolve(".cargo/config.toml"),
        coreRoot.parentFile.resolve("rust-toolchain.toml"),
        gemstoneRoot.resolve("uniffi.toml"),
        gemstoneRoot.resolve("justfile"),
    )
    outputs.dir(generatedKotlinDir.resolve("uniffi"))
    commandLine(shellCommandLine("just bindgen-kotlin"))
}

val buildCargoNdk = tasks.register<Exec>("buildCargoNdk") {
    description = "Build gemstone native libraries using cargo-ndk"
    workingDir = gemstoneRoot
    val outputDir = jniLibsDir
    outputs.dir(outputDir)
    outputs.upToDateWhen { false }
    doFirst { outputDir.deleteRecursively() }
    environment("CARGO_PROFILE_RELEASE_STRIP", "debuginfo")
    commandLine(shellCommandLine("cargo ndk $cargoNdkTargets -o ${jniLibsDir.absolutePath} rustc --lib --crate-type cdylib $cargoBuildFlag"))
}

tasks.configureEach {
    if (name.startsWith("lint") || name.startsWith("updateLintBaseline")) {
        enabled = false
    }
    if (name.matches(Regex("(compile|extract|source|javaDoc).*(Debug|Release).*"))) {
        dependsOn(bindgenKotlin)
    }
    if (name.matches(Regex("merge.*(Debug|Release).*JniLib.*"))) {
        dependsOn(buildCargoNdk)
    }
}

dependencies {
    api("net.java.dev.jna:jna:5.19.1@aar")
    implementation(libs.kotlinx.coroutines.android)
}
