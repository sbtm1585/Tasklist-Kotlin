package tasklist

enum class Priority(val color: String) {
    C("\u001B[101m \u001B[0m"),
    H("\u001B[103m \u001B[0m"),
    N("\u001B[102m \u001B[0m"),
    L("\u001B[104m \u001B[0m")
}

enum class DueTag(val color: String) {
    I("\u001B[102m \u001B[0m"),
    T("\u001B[103m \u001B[0m"),
    O("\u001B[101m \u001B[0m")
}