package chess

import Board
import androidx.compose.ui.window.ApplicationScope
import chess.Storage.ChessDataBase
import chess.domain.PieceMove
import chess.domain.Player
import chess.domain.board_components.Square
import chess.domain.filterCheckMoves
import com.mongodb.client.MongoClient


/**
 * the chess.chess.main class of the chess game which is responsible for jointing the classes
 * @property board                 the board containing the chess pieces
 * @property dataBase              the database to use
 * @property currentGameId         the GameId of the current game, can be null if no game is running
 * @property currentPlayer         the current Player on this machine
 */
data class Chess(val board: Board, val dataBase: ChessDataBase, val currentGameId: GameName?, val currentPlayer: Player)

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



