package chess.UI.Compose

import Board
import androidx.compose.runtime.MutableState
import chess.GameName
import chess.Storage.ChessRepository
import chess.Storage.Move
import chess.domain.Player
import chess.domain.board_components.Square
import chess.domain.board_components.toSquare
import chess.domain.commands.getPlayedMoves
import chess.domain.getPiecePossibleMovesFrom


fun Iterable<Move>.getMovesAsString(gameId : GameName, database: ChessRepository): String {


    if (this.count() == 0) return ""
    var res = ""
    var plays = 0
    var noOfPlays = 1
    this.forEach {
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

fun Iterable<Move>.toAString(): String {

    if (this.count() == 0) return ""
    var res = ""
    var plays = 0
    var noOfPlays = 1
    this.forEach {
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
    //TODO see if this is removed, kinda useless
    if(showPossibleMoves) selected.value = emptyList()
}

fun getPossibleMovesIfOptionEnabled(showPossibleMoves: Boolean, selected: MutableState<List<Square>>, board: Board,currentPlayer : Player, move: String) {
    if(showPossibleMoves) {
        val moves = move.toSquare().getPiecePossibleMovesFrom(board,currentPlayer)
        if (moves.isNotEmpty()) {
            val possibleMoves = moves.map { it.endSquare }
            selected.value = possibleMoves
        }
    }else listOf<Square>()

}