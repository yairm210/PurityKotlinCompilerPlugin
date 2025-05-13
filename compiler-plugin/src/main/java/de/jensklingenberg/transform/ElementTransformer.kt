package de.jensklingenberg.transform

import de.jensklingenberg.DebugLogger
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.wasm.ir2wasm.LocationType
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import java.util.StringJoiner

internal class ElementTransformer(
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger
) : IrElementTransformerVoidWithContext() {

    //    override fun visitValueParameterNew(declaration: IrValueParameter): IrStatement {
//        declaration.transform(CreateFuncTransformer(pluginContext,debugLogger), null)
//        return super.visitValueParameterNew(declaration)
//    }
//
//    override fun visitPropertyNew(declaration: IrProperty): IrStatement {
//        declaration.transform(CreateFuncTransformer(pluginContext, debugLogger), null)
//        return super.visitPropertyNew(declaration)
//    }
//
//    override fun visitCall(expression: IrCall): IrExpression {
//        expression.transform(CreateFuncTransformer(pluginContext, debugLogger), null)
//        return super.visitCall(expression)
//    }
//
//    override fun visitVariable(declaration: IrVariable): IrStatement {
//        declaration.transform(CreateFuncTransformer(pluginContext, debugLogger), null)
//        return super.visitVariable(declaration)
//    }
//
//
    override fun visitFunctionExpression(expression: IrFunctionExpression): IrExpression {
        expression.transform(CreateFuncTransformer(pluginContext, debugLogger), null)
        return super.visitFunctionExpression(expression)
    }


    fun isMarkedAsPure(function: IrFunction): Boolean {
        // Marked by @Contract(pure = true)
        val pure = function.getAnnotationArgumentValue<Boolean>(FqName("org.jetbrains.annotations.Contract"), "pure")
        if (pure == true) return true

        // Simple values like int + int -> plus(int, int), are marked thus
        val constEvaluation = function.getAnnotation(FqName("kotlin.internal.IntrinsicConstEvaluation"))
        if (constEvaluation != null) return true

        return false
    }

    fun checkExpressionPurity(declaration: IrFunction, expression: IrStatement, depth: Int = 0): Boolean {

        val start = LocationType.START.getLineAndColumnNumberFor(expression, declaration.fileEntry)
        val end = LocationType.END.getLineAndColumnNumberFor(expression, declaration.fileEntry)
        val fileLocation = "file://${declaration.fileEntry.name}:${start.line}:${start.column}"

        // Cannot get report locations to work :/
//                        location = CompilerMessageLocation.create(
//                            expression.symbol.owner.fileEntry.name, start.line, start.column, null
//                        )

        val prefix = if (depth == 0) "" else {
            val sb = StringBuilder()
            for (i in 1..depth) sb.append('-')
            sb.append("> ")
            sb.toString()
        }
        debugLogger.messageCollector.report(
            org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.WARNING,
            prefix + "Function \"${declaration.name}\": ${expression::class.simpleName} visited"
        )

        if (expression is IrExpression && expression.isConstantLike) return true

        fun warn(message: String) {
            debugLogger.messageCollector.report(
                org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.WARNING,
                "$fileLocation Function ${declaration.name} is marked as pure, but $message"
            )
        }

        return when (expression) {
            // When you call a function that returns a value, 
            // but you do not use that value
            is IrTypeOperatorCallImpl -> checkExpressionPurity(declaration, expression.argument, depth + 1)
            is IrCall -> { // We call another function directly, e.g. `x()`
                val isPure = isMarkedAsPure(expression.symbol.owner)
                        && expression.valueArguments.all { checkExpressionPurity(declaration, it!!) }

                if (!isPure) warn("calls non-pure function ${expression.symbol.owner.name}")

                isPure
            }
            is IrReturn -> checkExpressionPurity(declaration, expression.value, depth + 1) // We care about the returned value itself
            is IrSetValueImpl -> { // e.g. `x = 4`
                val callee = expression.symbol.owner
                warn("sets value of \"${callee.name}\"")

                false
            }
            is IrGetValueImpl -> { // e.g. "return a", the "a" part
                val isVar = expression.symbol.owner.let { it is IrVariable && it.isVar }
                if (isVar){
                    warn("reads from variable \"${expression.symbol.owner.name}\"")
                }
                // Reading from vars is disallowed - they can change
                return !isVar
            }

            else -> {
                warn("expression of type ${expression::class.simpleName}")
                debugLogger.messageCollector.report(
                    org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.WARNING,
                    "$fileLocation Unhandled expression class "+expression::class.simpleName,
                )
                false
            }
        }
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {

        if (!isMarkedAsPure(declaration)) return super.visitFunctionNew(declaration)

//        debugLogger.log("Function ${declaration.name} is pure")
        debugLogger.messageCollector.report(
            org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.WARNING,
            "Function ${declaration.name} is pure"
        )

        for (expression in declaration.body?.statements ?: emptyList()) {
            checkExpressionPurity(declaration, expression)
        }

        return super.visitFunctionNew(declaration)
    }

}