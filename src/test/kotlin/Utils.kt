/**
 * @param board     the board to be displayed
 * renders the board as a View.
 */
fun boardTemplate(board: Board) {
    val string = board.toString()
    println("    a b c d e f g h ")
    println("   -----------------  ")
    var i = 0
    var k = 8
    while (i < 8 && k > 0) {
        println("$k | ${
            string.toBoard().chunked(16)[i]
        }|")
        i++
        k--
    }
    println("   -----------------  ")
}
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