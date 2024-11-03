import org.gradle.internal.os.OperatingSystem
import org.gradle.jvm.tasks.Jar
import org.gradle.api.tasks.JavaExec

val lwjglVersion = "3.3.4"
val jomlVersion = "1.10.7"
val `joml-primitivesVersion` = "1.10.0"
val steamworks4jVersion = "1.9.0"
val `steamworks4j-serverVersion` = "1.9.0"

// LWJGL Natives for checking the os version
val lwjglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!
).let { (name, arch) ->
    when {
        arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } ->
            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
            else if (arch.startsWith("ppc"))
                "natives-linux-ppc64le"
            else if (arch.startsWith("riscv"))
                "natives-linux-riscv64"
            else
                "natives-linux"
        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) }     ->
            "natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"
        arrayOf("Windows").any { name.startsWith(it) }                ->
            "natives-windows"
        else                                                                            ->
            throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}

plugins {
    kotlin("jvm") version "2.0.0"
    id("java")
    id("application")
}

application {
    mainClass.set("tk.kt.LWKGLVulkanTutorial.Main") // Replace with your main class
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "tk.kt.LWKGLVulkanTutorial.MainKt" // Replace with your main class
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("${project.name}-${version}-${lwjglNatives}.jar")
}

group = "tk.kt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    dependencies {
        implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

        implementation("org.lwjgl", "lwjgl")
        implementation("org.lwjgl", "lwjgl-assimp")
        implementation("org.lwjgl", "lwjgl-bgfx")
        implementation("org.lwjgl", "lwjgl-glfw")
        implementation("org.lwjgl", "lwjgl-nanovg")
        implementation("org.lwjgl", "lwjgl-nuklear")
        implementation("org.lwjgl", "lwjgl-openal")
        implementation("org.lwjgl", "lwjgl-opengl")
        implementation("org.lwjgl", "lwjgl-par")
        implementation("org.lwjgl", "lwjgl-stb")
        implementation("org.lwjgl", "lwjgl-vulkan")
        runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
        runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives)
        runtimeOnly("org.lwjgl", "lwjgl-bgfx", classifier = lwjglNatives)
        runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
        runtimeOnly("org.lwjgl", "lwjgl-nanovg", classifier = lwjglNatives)
        runtimeOnly("org.lwjgl", "lwjgl-nuklear", classifier = lwjglNatives)
        runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
        runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
        runtimeOnly("org.lwjgl", "lwjgl-par", classifier = lwjglNatives)
        runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)
        if (lwjglNatives == "natives-macos" || lwjglNatives == "natives-macos-arm64") runtimeOnly("org.lwjgl", "lwjgl-vulkan", classifier = lwjglNatives)
        implementation("org.joml", "joml", jomlVersion)
        implementation("org.joml", "joml-primitives", `joml-primitivesVersion`)
    }

    implementation("org.tinylog:tinylog-api:2.7.0")
    implementation("org.tinylog:tinylog-api-kotlin:2.7.0")
    implementation("org.tinylog:tinylog-impl:2.7.0")

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java", "src/main/kotlin")
        }
    }
    test {
        java {
            srcDirs("src/test/java", "src/test/kotlin")
        }
    }
}



