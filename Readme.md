

To publish the compiler plugin:
- Update version in `compiler-plugin/build.gradle.kts` 
- Update version in `java/de/jensklingenberg/gradle/HelloWorldGradleSubplugin.kt`
- `./gradlew :compiler-plugin:publishToMavenLocal`

To publish the gradle plugin:
- Update version in `gradle-plugin/build.gradle.kts`
- `./gradlew :gradle-plugin:build`


## Usage

* Inside the project folder run `./gradlew clean build` 

The plugin is only active when the build cache is changed. This is why you need to run "clean" before building, when you want to see the log output again.

### 👷 Project Structure
*  <kbd>lib</kbd> - A Kotlin Multiplatform project which applies a gradle plugin(compiler.plugin.helloworld) which triggers the compiler plugin.
*  <kbd>compiler-plugin</kbd> - This module contains the Kotlin Compiler Plugin
*  <kbd>gradle-plugin</kbd> - This module contains the gradle plugin which trigger the compiler plugin

## Useful resources
[The Road to the New Kotlin Compiler](https://www.youtube.com/watch?v=iTdJJq_LyoY)

[https://github.com/bnorm/kotlin-ir-plugin-template](https://github.com/bnorm/kotlin-ir-plugin-template)

[Writing Your Second Kotlin Compiler Plugin, Part 1 — Project Setup](https://blog.bnorm.dev/writing-your-second-compiler-plugin-part-1)

[Experimenting with the Kotlin Compiler by Jossi Wolf, Snapp Mobile EN](https://www.youtube.com/watch?v=Y6gEA-nS2uQ)

[Crash course on the Kotlin compiler | 1. Frontend: Parsing phase](https://medium.com/google-developer-experts/crash-course-on-the-kotlin-compiler-1-frontend-parsing-phase-9898490d922b)

## ✍️ Feedback

Feel free to send feedback on [Twitter](https://twitter.com/jklingenberg_) or [file an issue](https://github.com/foso/KotlinCompilerPluginExample/issues/new). Feature requests are always welcome.


### Find this project useful ? :heart:
* Support it by clicking the :star: button on the upper right of this page. :v:

## 📜 License

-------

This project is licensed under Apache License, Version 2.0

    Copyright 2019 Jens Klingenberg

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


## Acknowledgments

Projects that helped me understand how to setup the project:
* [Foso/KotlinCompilerPluginExample](https://github.com/Foso/KotlinCompilerPluginExample)
