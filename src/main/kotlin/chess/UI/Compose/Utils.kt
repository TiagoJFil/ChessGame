package chess.UI.Compose

import androidx.compose.runtime.MutableState
import chess.Chess
import chess.GameName
import chess.Storage.ChessDataBase
import chess.domain.board_components.Square
import chess.domain.board_components.toSquare
import chess.domain.commands.getMovesAction
import chess.getPiecePossibleMovesFrom


fun getMovesAsString(gameId : GameName, database: ChessDataBase): String {
    val moves = getMovesAction(gameId,database)

    if (moves.count() == 0) return ""
    var res = ""
    var plays = 0
    var noOfPlays = 1
    moves.forEach {
        if (plays % 2 == 1) {
            noOfPlays++
            res+= "- ${it.move}\n"

        } else {
            res+= "$noOfPlays. ${it.move} "
        }
        plays++
    }
    return res

}

fun clearPossibleMovesIfOptionEnabled(showPossibleMoves: Boolean, selected: MutableState<List<Square>>){
    if(showPossibleMoves) selected.value = emptyList()
}

fun getPossibleMovesIfOptionEnabled(showPossibleMoves: Boolean, selected: MutableState<List<Square>>, chess: Chess, move: String){
    if(showPossibleMoves) {
        val moves = chess.getPiecePossibleMovesFrom(move.toSquare())
        if (moves.isNotEmpty()) {
            val possibleMoves = moves.map { it.endSquare }
            selected.value = possibleMoves
        }
    }
}