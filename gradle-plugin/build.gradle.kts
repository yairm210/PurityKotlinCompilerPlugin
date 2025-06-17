import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version("2.0.0")
    kotlin("kapt") version("2.0.0")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.1.0"
    `maven-publish`
}
apply(plugin = "kotlin-kapt") // todo not sure if required, test without

group = "de.jensklingenberg"
version = "1.0.4"

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
    plugins {

        create("simplePlugin") {
            id = "compiler.gradleplugin.test" // users will do `apply plugin: "compiler.plugin.helloworld"`
            implementationClass = "de.jensklingenberg.gradle.HelloWorldGradleSubPlugin" // entry-point class
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