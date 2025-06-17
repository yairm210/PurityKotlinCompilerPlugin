plugins {
    id("org.jetbrains.kotlin.multiplatform") version libs.versions.kotlin
//    id("de.jensklingenberg.gradle-plugin") version "1.0.3" // Use the version published to Maven Local
    id("compiler.gradleplugin.test") version "1.0.3" // Use the version published to Maven Local
}
//apply(plugin = "compiler.gradleplugin.test")


configure<de.jensklingenberg.gradle.TestCompilerExtension> {
    enabled = true
}


kotlin {
    jvm()
    jvmToolchain(8) // test plugin compatibility to older jvm
//    linuxX64("linux")
//    js()
    sourceSets {
        val commonMain by getting {}

//        val jsMain by getting {
//
//            dependencies {
//
//            }
//        }

        val jvmMain by getting {


            dependencies {

            }
        }
//        val linuxMain by getting {
//
//        }

    }
}

