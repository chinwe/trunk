// kotlin basic syntax

fun maxOf(a: Int, b: Int): Int {
    if (a > b) {
        return a
    } else {
        return b
    }
}

fun parseInt(str: String): Int? {
    return str.toIntOrNull()
}

fun main(args: Array<String>) {

    println("max of 0 and 42 is ${maxOf(0, 42)}")
}