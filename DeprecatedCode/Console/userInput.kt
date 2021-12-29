import chess.Chess

/**
 * Reads a line from the console and parses it to obtain the corresponding command.
 * @return a pair bearing the command text and its parameter
 */
fun readCommand(chess: Chess): Pair<String, String?> {
    val player = chess.board.getPlayerColor()
    val gamename = chess.currentGameId

    if (gamename != null) {
        print("${gamename.id}:$player> ")
    }else print("> ")
    val input = readln()
    val command = input.substringBefore(delimiter = ' ')
    val argument = input.substringAfter(delimiter = ' ', missingDelimiterValue = "").trim()
    return Pair(command.trim().toLowerCase(), if (argument.isNotBlank()) argument else null)
}

/**
 * Let's use this while we don't get to Kotlin v1.6
 */
fun readln() = readLine()!!

