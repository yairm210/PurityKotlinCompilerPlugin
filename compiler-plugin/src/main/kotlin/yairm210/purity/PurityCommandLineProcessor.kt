package yairm210.purity

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@AutoService(CommandLineProcessor::class) // don't forget!
class PurityCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "helloWorldPlugin"

    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = "enabled", valueDescription = "<true|false>",
            description = "whether to enable the plugin or not"
        ),
        CliOption(
            optionName = "pure_function_names", valueDescription = "<fully qualified function names delimited by commas>",
            description = "A list of fully qualified function names that are acceptable as pure functions",
            required = false
        )
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) = when (option.optionName) {
        "enabled" -> configuration.put(KEY_ENABLED, value.toBoolean())
        "pure_function_names" -> configuration.put(KEY_PURE_FUNCTION_NAMES, value.split(",").map { it.trim() })
        else -> throw IllegalArgumentException("Unknown option: ${option.optionName}")
    }
}

val KEY_ENABLED = CompilerConfigurationKey<Boolean>("whether the plugin is enabled")
val KEY_PURE_FUNCTION_NAMES = CompilerConfigurationKey<List<String>>("A list of fully qualified function names that are acceptable")
