import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version("2.0.0")
    kotlin("kapt") version("2.0.0")
//    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.1"
//    `maven-publish`
}
apply(plugin = "kotlin-kapt") // todo not sure if required, test without

group = "io.github.yairm210"
version = "0.0.5"

// Not sure if required - there's no Java :think:
java.targetCompatibility = JavaVersion.VERSION_1_8

// Make KAPT stubs Java 8 compatible
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}



allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://maven.google.com")
        maven("https://plugins.gradle.org/m2/")
        google()
    }
}
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.0.0")
}

gradlePlugin {
    website = "https://github.com/yairm210/purity"
    vcsUrl = "https://github.com/yairm210/purity.git"
    
    plugins {
        create("simplePlugin") {
            id = "io.github.yairm210.purity-plugin" // users will do `apply plugin: "compiler.plugin.helloworld"`
            displayName = "Kotlin Purity Plugin"
            description = "A Kotlin compiler plugin that allows you to mark functions as pure and readonly, enabling optimizations and better code analysis."
            implementationClass = "yairm210.purity.PurityGradlePlugin" // entry-point class
            tags = listOf("kotlin", "compiler-plugin", "purity", "pure-functions", "readonly")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../local-plugin-repository")
        }
    }
}

tasks.register("sourcesJar", Jar::class) {
    group = "build"
    description = "Assembles Kotlin sources"

    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
    dependsOn(tasks.classes)
}

tasks.build {
    dependsOn("publishToMavenLocal")
}