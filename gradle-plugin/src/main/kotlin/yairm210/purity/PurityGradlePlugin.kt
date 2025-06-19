package yairm210.purity

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

open class TestCompilerExtension {
    var enabled: Boolean = true
}

@Suppress("unused")
class PurityGradlePlugin : KotlinCompilerPluginSupportPlugin {

    companion object {
        const val COMPILER_PLUGIN_GROUP_NAME = "il.yairm210.purity"
        const val ARTIFACT_NAME = "compiler-plugin"
        const val VERSION_NUMBER = "0.0.5"
    }

    private var gradleExtension : TestCompilerExtension = TestCompilerExtension()
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        gradleExtension = kotlinCompilation.target.project.extensions.findByType(TestCompilerExtension::class.java) ?: TestCompilerExtension()

        return kotlinCompilation.target.project.provider {
            val options = mutableListOf(SubpluginOption("enabled", gradleExtension.enabled.toString()))
            options
        }
    }

    override fun apply(target: Project) {
        target.extensions.create(
            "helloWorld",
            TestCompilerExtension::class.java
        )
        super.apply(target)
    }

    override fun getCompilerPluginId(): String = "helloWorldPlugin"

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return true
    }

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = COMPILER_PLUGIN_GROUP_NAME,
        artifactId = ARTIFACT_NAME,
        version = VERSION_NUMBER // remember to bump this version before any release!
    )

}
