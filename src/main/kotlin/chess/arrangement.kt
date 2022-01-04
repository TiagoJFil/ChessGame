package chess

import Board
import androidx.compose.ui.window.ApplicationScope
import chess.Storage.ChessDataBase
import chess.domain.PieceMove
import chess.domain.Player
import chess.domain.board_components.Square
import chess.domain.filterPiecesMoves
import com.mongodb.client.MongoClient


/**
 * the chess.chess.main class of the chess game which is responsible for jointing the classes
 * @property board                 the board containing the chess pieces
 * @property dataBase              the database to use
 * @property currentGameId         the GameId of the current game, can be null if no game is running
 * @property currentPlayer         the current Player on this machine
 */
data class Chess(val board: Board, val dataBase: ChessDataBase, val currentGameId:GameName?, val currentPlayer: Player)

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
 * Gets the possible moves for the piece at the given square
 * @param square the square where the piece is located
 * @return a list of possible moves for the piece at the given square or an empty list if there is no piece at the given square or the piece is not movable
 */
fun Chess.getPiecePossibleMovesFrom(square: Square): List<PieceMove> {
    val piece = this.board.getPiece(square)
    if(piece != null && piece.player != this.currentPlayer)
        return emptyList()
    return filterPiecesMoves(this.board,piece?.getPossibleMoves(this.board, square), piece)  ?: emptyList()
}

/**
* Exits the game and closes the MongoDb driver
*/
fun ApplicationScope.exit(driver: MongoClient){
    driver.close()
    exitApplication()
}