
includeBuild("gradle-plugin"){
//    dependencySubstitution {
//        substitute(module("de.jensklingenberg:gradle-plugin:1.0.0")).using(project(":"))
//    }
}
includeBuild("compiler-plugin")

include(":lib")

// Allow plugins from local - required for lib to get the gradle plugin from our maven local
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}

