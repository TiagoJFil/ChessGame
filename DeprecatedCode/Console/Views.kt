import chess.Chess
import chess.Storage.Move

/**
 * A command view is merely a function that renders the command execution result.
 */
typealias View = (input: Any?) -> Unit

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
 * Displays a board and a message
 * The message can be null
 */
fun printBoardAndMessage(input: Any?){
    val boardAndMessage = input as Pair<*, *>
    val chess = boardAndMessage.first as Chess
    val message = boardAndMessage.second as String?


    boardTemplate(chess.board)
    if(message != null)
        println(message)
}
/**
 * Displays a message on the console.
 */
fun printMessage(input: Any?){
    val message = input as String
    println(message)
}
/**
 * Displays the Moves already made.
 */
fun printMoves(input : Any?){
    val moves = input as Iterable<Move>
    var plays = 0
    var noOfPlays = 1
    moves.forEach {

        if (plays % 2 == 1) {
            noOfPlays++
            println(it.move)

        } else {
            print("$noOfPlays. ")
            print("${it.move} ")
        }
        plays++
    }
    println()
}

/**
 * @param line     the line to be displayed
 * Displays a line at the console.
 */
fun displayView(line: String){
    println(line)
}
