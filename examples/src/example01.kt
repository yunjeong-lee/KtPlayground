fun boolConjunct(a: Boolean, b: Boolean): Int {
    when {
        a && b -> { return 0 }
        a && !b -> { return 0 }
        !a && b -> { return 1 }
        !a && !b -> { return 0 }
    }
    return -1
}
