package chess.domain.Pieces

import Board
import Direction
import Piece
import PieceMove
import chess.Storage.Move
import chess.domain.MoveType
import chess.domain.Player
import chess.domain.board_components.Square
import kotlin.math.abs


/**
 * @param player    the player that owns this piece
 * Represents a knight piece
 */
class Knight (override val player: Player) : Piece {

    private val possibleDirections = listOf(
        Pair(1, 2),
        Pair(2, 1),
        Pair(2, -1),
        Pair(1, -2),
        Pair(-1, -2),
        Pair(-2, -1),
        Pair(-2, 1),
        Pair(-1, 2)
    )

    /**
     * @return the piece as a string with the correspondent color
     */
    override fun toString(): String {
        return if (player.isWhite()) "n" else "N"
    }

    /**
     * @return the list of possible directions for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> = possibleDirections.mapNotNull {
        val newPos = pos.addDirection(it)
        if (newPos != null) PieceMove(pos, newPos)
        else null
    }


    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        TODO()

        val pieceAtEndPos = board.getPieceAt(pieceInfo.endSquare)
        val canMove =
            ((abs(pieceInfo.startSquare.row.value() - pieceInfo.endSquare.row.value()) == 2 && abs(pieceInfo.startSquare.column.value() - pieceInfo.endSquare.column.value()) == 1)
                    ||
                    (abs(pieceInfo.startSquare.row.value() - pieceInfo.endSquare.row.value()) == 1 && abs(pieceInfo.startSquare.column.value() - pieceInfo.endSquare.column.value()) == 2))

        if(pieceAtEndPos == null && canMove) return MoveType.REGULAR
        if(pieceAtEndPos != null) {
            val capturesOpponentPiece = canMove && pieceAtEndPos.player.color == !board.getPlayerColor()
            if (capturesOpponentPiece) return MoveType.CAPTURE
        }
        return MoveType.ILLEGAL
    }



}