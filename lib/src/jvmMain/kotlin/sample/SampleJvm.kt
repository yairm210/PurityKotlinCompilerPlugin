package sample

import org.jetbrains.annotations.Contract

actual class Sample {
    actual fun checkMe() = 42
}

actual object Platform {
    actual val name: String = "JVM"
}

fun main() {
    /**
     * The compiler plugin will replace this with create<MyTest>(_MyTestProvider)
     */
    val myTest = create<MyTest>()
    myTest.print()
    
    var external = 3

    @Contract(pure = true)
    fun returnExternal() = external

    @Contract(pure = true)
    fun returnExternal2(): Int {
        return external
    }

    @Contract(pure = true)
    fun give(a: Int): Int {
        return a
    }
    
    fun untrustable(a: Int, b: Int): Int {
        external = 4
        return a
    }
    
    fun justSideEffect(){
        external = 4
    }
    
    @Contract(pure = true)
    fun add(a: Int, b: Int): Int {
        external = 4
        untrustable(5, 6)
        justSideEffect()
        return a + give(b) + untrustable(5,6)
    }
    println(add(1,2))
}