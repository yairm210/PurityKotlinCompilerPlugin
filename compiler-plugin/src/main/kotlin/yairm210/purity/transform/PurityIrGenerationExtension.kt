package yairm210.purity.transform

import yairm210.purity.DebugLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class PurityIrGenerationExtension(private val debugLogger: DebugLogger,
                                           private val pureFunctionNames:List<String>) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(PurityElementTransformer(pluginContext, debugLogger, pureFunctionNames), null)
    }
}