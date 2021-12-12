package chess

import Board
import chess.domain.commands.Commands
import chess.domain.commands.ExitCommand
import chess.domain.commands.JoinCommand
import chess.domain.commands.MovesCommand
import chess.domain.commands.OpenCommand
import chess.domain.commands.PlayCommand
import chess.domain.commands.RefreshCommand
import View
import chess.Storage.ChessDataBase
import chess.domain.Player
import printBoardAndMessage
import printMessage
import printMoves

/**
 * the chess.chess.main class of the chess game which is responsible for jointing the classes
 * @property board                 the board containing the chess pieces
 * @property dataBase              the database to use
 * @property currentGameId         the GameId of the current game, can be null if no game is running
 * @property currentPlayer         the current Player on this machine
 */
data class Chess(var board: Board, val dataBase: ChessDataBase, var currentGameId:GameName?, var currentPlayer: Player)

/**
 * Represents a GameId with an identifier.
 * @param id        the identifier of the game
 * Blank characters are not allowed.
 */
data class GameName(val id:String){
    init {
        require(isAValidGameName())
    }
    private fun isAValidGameName() = this.id.isNotEmpty() && this.id.all { !it.isWhitespace() }
}


/**
 * @property command               the command to be executed
 * @property view                  the view associated with the command
 * Represents a command and a view to be executed.
 */
data class CommandHandler(
    val command: Commands,
    val display: View
)

/**
 * Gets the container bearing the associations between user entered strings and the corresponding command handlers.
 * @param billboard the [Billboard] to be used by all commands
 * @param author    the [Author] instance to be used when posting messages
 * @return the container with the command handler mappings
 */
fun buildCommandHandler(chess: Chess): Map<String, CommandHandler> {
    return mapOf(
        "open" to CommandHandler(OpenCommand(chess),::printBoardAndMessage),
        "refresh" to CommandHandler(RefreshCommand(chess),::printBoardAndMessage),
        "exit" to CommandHandler(ExitCommand(), ::printMessage),
        "join" to CommandHandler(JoinCommand(chess),::printBoardAndMessage),
        "moves" to CommandHandler(MovesCommand(chess),::printMoves),
        "play" to CommandHandler(PlayCommand(chess),::printBoardAndMessage)
    )
}


