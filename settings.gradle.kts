
includeBuild("gradle-plugin")
includeBuild("compiler-plugin")

include(":lib")

// Allow plugins from local - required for lib to get the gradle plugin from our maven local
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}

