package chess

import Board
import chess.Storage.ChessRepository
import chess.domain.Player


/**
 * the chess.chess.main class of the chess game which is responsible for jointing the classes
 * @property board                 the board containing the chess pieces
 * @property database              the database to use
 * @property currentGameId         the GameId of the current game, can be null if no game is running
 * @property localPlayer         the current Player on this machine
 */
data class Chess(val board: Board, val database: ChessRepository, val currentGameId: GameName?, val localPlayer: Player)

/**
 * Represents a GameId with an identifier.
 * @param id        the identifier of the game
 * Blank characters are not allowed.
 */
data class GameName(val id: String){
    init {
        require(isAValidGameName(id))
    }
    override fun toString(): String {
        return id
    }
}
private fun isAValidGameName(id: String) = id.isNotEmpty() && id.all { !it.isWhitespace() }

/**
 * Converts a [String] into a [GameName] if it is a valid game name.
 * Else, returns null.
 */
fun String.toGameNameOrNull() = if(isAValidGameName(this)) GameName(this) else null



