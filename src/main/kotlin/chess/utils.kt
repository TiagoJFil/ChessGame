/**
 * Transforms a string with the board as a flat string into a board as a formatted string
 */
fun String.toBoard(): String {
    var string = ""
    val stringSize = this.length
    var i = 0
    while (i < stringSize) {
        string += this[i] + " "
        i++
    }
    return string
}


