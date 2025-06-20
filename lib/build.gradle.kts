import yairm210.purity.TestCompilerExtension

plugins {
    id("org.jetbrains.kotlin.multiplatform") version libs.versions.kotlin
    id("io.github.yairm210.purity-plugin") version "1.0.3" // Use the version published to Maven Local
}


configure<TestCompilerExtension> {
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

