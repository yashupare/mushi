import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.zipline.gradle.plugin)
    alias(libs.plugins.spotubeGradle)
}

kotlin {
    applyDefaultHierarchyTemplate()

    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":plugin_interfaces"))

            api(libs.zipline.core)
            api(libs.kotlinx.coroutines.core)
            api(libs.semver)
        }

        jsMain.dependencies {}
    }
}

zipline {
    mainFunction.set("org.spotube.js_plugin_example.main")
}

plugins.withType<YarnPlugin> {
    the<YarnRootExtension>().yarnLockAutoReplace = true
}

//fun registerPackageTask(flavor: String, compileTaskName: String) {
//    val capitalizedFlavor = flavor.replaceFirstChar { it.uppercase() }
//
//    tasks.register<Zip>("package${capitalizedFlavor}Plugin") {
//        group = "distribution"
//        description = "Packages the $flavor Zipline executable and plugin.json into a smplug."
//
//        // 1. Depend on the Zipline compilation task
//        val compileTask = tasks.named(compileTaskName)
//        dependsOn(compileTask)
//
//        // 2. Set the output location and name
//        archiveFileName.set("plugin-$flavor.smplug")
//        destinationDirectory.set(layout.buildDirectory.dir("distributions"))
//
//        // 3. Include the Zipline outputs
//        // We use a provider/closure to ensure the directory exists when the task runs
//        from(layout.buildDirectory.dir("zipline/$capitalizedFlavor")) {
//            // This preserves subdirectories if Zipline generated any
//            include("**/*")
//        }
//
//        // 4. Include plugin.json
//        // Use layout.projectDirectory.file() to reach the root JSON
//        from(layout.projectDirectory.file("plugin.json"))
//
//        // Set inputs for incremental build support
//        inputs.file(layout.projectDirectory.file("plugin.json"))
//    }
//}
//
//registerPackageTask("development", "compileDevelopmentExecutableKotlinJsZipline")
//registerPackageTask("production", "compileProductionExecutableKotlinJsZipline")