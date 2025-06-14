package de.jensklingenberg.transform

import de.jensklingenberg.DebugLogger
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.name.FqName

// All the functions of these are readonly
val wellKnownReadonlyClasses = setOf(
    "kotlin.String",
    "kotlin.collections.List",
    "kotlin.collections.Set",
    "kotlin.collections.Map",
    "kotlin.collections.Collection",
    "kotlin.sequences.Sequence",
    "kotlin.text.Regex",
    "kotlin.text.MatchResult",
)

val wellKnownPureClasses = setOf(
    "kotlin.Int",
    "kotlin.Long",
    "kotlin.Float",
    "kotlin.Double",
    "kotlin.Boolean",
    "kotlin.Char",
    "kotlin.String",

    "kotlin.ranges.IntRange",
    "kotlin.ranges.LongRange",
    "kotlin.ranges.CharRange",
    "kotlin.ranges.FloatRange",
    "kotlin.ranges.DoubleRange",
)

val x = listOf(1 .. 2)

fun classMatches(function: IrFunction, wellKnownClasses: Set<String>): Boolean {
    val parentClassIdentifier = function.parentClassOrNull
        ?.let { it.packageFqName?.asString() + "." + it.name.asString() } ?: return false
    return parentClassIdentifier in wellKnownClasses
}

fun isMarkedAsPure(function: IrFunction): Boolean {
    // Marked by @Contract(pure = true)
    val pure = function.getAnnotationArgumentValue<Boolean>(FqName("org.jetbrains.annotations.Contract"), "pure")
    if (pure == true) return true

    if (classMatches(function, wellKnownPureClasses)) return true

    // Simple values like int + int -> plus(int, int), are marked thus
    val constEvaluation = function.getAnnotation(FqName("kotlin.internal.IntrinsicConstEvaluation"))
    if (constEvaluation != null) return true

    return false
}


fun isReadonly(function: IrFunction): Boolean {
    // Marked by @Contract(pure = true)
    val contractValue = function.getAnnotationArgumentValue<String>(FqName("org.jetbrains.annotations.Contract"), "value")
    if (contractValue == "readonly") return true
    
    if (classMatches(function, wellKnownReadonlyClasses)) return true

    return false
}


private fun userDisplayFileLocation(function: IrFunction, expression: IrElement): String {
    val location = function.fileEntry.getLineAndColumnNumbers(expression.startOffset)
    // Location we get is 0-indexed but we need the 1-indexed line and column for click-to-get-to-location
    val fileLocation = "file://${function.fileEntry.name}:${location.line + 1}:${location.column + 1}"
    return fileLocation
}

enum class FunctionColoring{
    None,
    Readonly,
    Pure
}

/** Warns every time a var is set a value, or an unpure function is called.
 * Vars that are created within the function are OK to set */
class CheckFunctionColoringVisitor(
    private val function: IrFunction,
    private val declaredFunctionColoring: FunctionColoring,
    private val messageCollector: MessageCollector,
    private val pureFunctionNames: Set<String>,
    ) : IrElementVisitor<Unit, Unit> { // Returns whether this is an acceptable X function
    var isReadonly = true
    var isPure = true
    
    fun actualFunctionColoring(): FunctionColoring {
        return when {
            isPure -> FunctionColoring.Pure
            isReadonly -> FunctionColoring.Readonly
            else -> FunctionColoring.None
        }
    }

    // Iterate over IR tree and warn on each var set where the var is not created within this function
    @OptIn(org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI::class)
    override fun visitSetValue(expression: IrSetValue, data: Unit) {
        // Not sure if we can assume owner is set at this point :think:
        val varValueDeclaration: IrValueDeclaration = expression.symbol.owner
        
        // If the variable is created in this function that's ok
        if (varValueDeclaration is IrVariable && varValueDeclaration.isVar && varValueDeclaration.parent != function) {
            val fileLocation = userDisplayFileLocation(function, expression)
            isReadonly = false
            isPure = false

            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                "$fileLocation Function \"${function.name}\" is marked as $declaredFunctionColoring but sets variable \"${varValueDeclaration.name}\""
            )
        }
        super.visitSetValue(expression, data)
    }

    override fun visitGetValue(expression: IrGetValue, data: Unit) {
        val varValueDeclaration: IrValueDeclaration = expression.symbol.owner
        
        // If the variable is created in this function that's ok
        if (varValueDeclaration is IrVariable && varValueDeclaration.isVar && varValueDeclaration.parent != function) {
            val fileLocation = userDisplayFileLocation(function, expression)
            isPure = false

            if (declaredFunctionColoring == FunctionColoring.Pure) {
                messageCollector.report(
                    CompilerMessageSeverity.WARNING,
                    "$fileLocation Function \"${function.name}\" is marked as $declaredFunctionColoring but gets variable \"${varValueDeclaration.name}\""
                )
            }
        }
        super.visitGetValue(expression, data)
    }
    
    @OptIn(org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI::class)
    override fun visitCall(expression: IrCall, data: Unit) {
        // Only accept calls to functions marked as pure or readonly
        val calledFunctionColoring =  when {
            isMarkedAsPure(function) -> FunctionColoring.Pure
            isReadonly(function) -> FunctionColoring.Readonly
            else -> FunctionColoring.None
        }
        
        
        
        if (calledFunctionColoring < FunctionColoring.Pure) isPure = false
        if (calledFunctionColoring < FunctionColoring.Readonly) isReadonly = false
        
        if (declaredFunctionColoring < calledFunctionColoring) {
            val fileLocation = userDisplayFileLocation(function, expression)
            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                "$fileLocation Function \"${function.name}\" is marked as $declaredFunctionColoring " +
                        "but calls non-$calledFunctionColoring function \"${expression.symbol.owner.name}\""
            )
        }
        
        super.visitCall(expression, data) 
    }

    override fun visitElement(element: IrElement, data: Unit) {
        element.acceptChildren(this, data)
    }
}

internal class PurityElementTransformer(
    private val pluginContext: IrPluginContext,
    private val debugLogger: DebugLogger,
    private val pureFunctionNames: List<String>
) : IrElementTransformerVoidWithContext() {
    
    // These are created behind the scenes for every class, don't warn for them
    val autogeneratedFunctions = setOf(
        "<init>",
        "equals",
        "hashCode",
        "toString"
    )
    fun isAutogeneratedFunction(functionName: String): Boolean {
        return autogeneratedFunctions.contains(functionName) 
                || functionName.startsWith("<get-") // Getters for properties
    }
    
    
    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        
        // Skip interface/abstract functions that are not implemented
        if (declaration.body == null) {
            return super.visitSimpleFunction(declaration)
        }
        
        val functionDeclaredColoring = when {
            isMarkedAsPure(declaration) -> FunctionColoring.Pure
            isReadonly(declaration) -> FunctionColoring.Readonly
            else -> FunctionColoring.None
        }
        val messageCollector = if (functionDeclaredColoring == FunctionColoring.None) MessageCollector.NONE 
        else debugLogger.messageCollector
        
        val visitor = CheckFunctionColoringVisitor(declaration, functionDeclaredColoring, messageCollector, pureFunctionNames.toSet())
        declaration.accept(visitor, Unit)
        
        val actualColoring = visitor.actualFunctionColoring()
        if (functionDeclaredColoring != actualColoring){
            
            // Don't warn for unmarked autogenerated functions - they are not under the user's control
            if (functionDeclaredColoring < actualColoring && isAutogeneratedFunction(declaration.name.asString())) {
                return super.visitSimpleFunction(declaration)
            }
            
            val fileLocation = userDisplayFileLocation(declaration, declaration)
            
            // if equal, no message; If less that declared, we already warn for each individual violation
            if (functionDeclaredColoring < actualColoring) {
                val message = when (actualColoring) {
                    FunctionColoring.Pure -> "Function \"${declaration.name}\" can be marked with @Contract(pure = true) to indicate it is pure"
                    FunctionColoring.Readonly -> "Function \"${declaration.name}\" can be marked with @Contract(\"readonly\") to indicate it is readonly"
                    else -> throw Exception("Unexpected function coloring: $actualColoring")
                }

                debugLogger.messageCollector.report(CompilerMessageSeverity.WARNING, "$fileLocation $message")
            }
        }
        
        return super.visitSimpleFunction(declaration)
    }

}