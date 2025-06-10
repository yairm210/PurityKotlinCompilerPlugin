package de.jensklingenberg.transform

import de.jensklingenberg.DebugLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class PurityIrGenerationExtension(private val debugLogger: DebugLogger,
                                           private val pureFunctionNames:List<String>) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(PurityElementTransformer(pluginContext, debugLogger, pureFunctionNames), null)
    }
}