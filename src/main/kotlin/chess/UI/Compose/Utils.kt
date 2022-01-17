package chess.UI.Compose

import Board
import androidx.compose.runtime.MutableState
import chess.domain.Player
import chess.domain.board_components.Square
import chess.domain.board_components.toSquare
import chess.domain.getPiecePossibleMovesFrom





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