package chess.domain

import Board
import Direction
import Pawn
import Piece
import chess.domain.board_components.*
import getAllMoves


enum class MoveType{
    REGULAR,
    CAPTURE,
    CASTLE,
    PROMOTION,
    ENPASSANT,
    CHECK,
    CHECKMATE,
    ILLEGAL;
}

private const val UP = -1
private const val DOWN = 1
private const val LEFT = -1
private const val RIGHT = 1


private const val POSITION_FROM_LETTER = 1
private const val POSITION_FROM_NUMBER = 2
private const val POSITION_TO_LETTER = 3
private const val POSITION_TO_NUMBER = 4


/**
 * @param startSquare       the square where the piece is placed
 * @param endSquare         the square where the piece is moved to.
 * Represents the piece movement.
 */
data class PieceMove(val startSquare: Square, val endSquare: Square)

/**
 * receives the move input and transforms to a data class of coordinates
 * @param String  input to make move ex: Pe2e4
 * @return  the Movement as a data class [PieceMove]
 **/
fun String.formatToPieceMove(): PieceMove{

    val startSquare = Square((this[POSITION_FROM_LETTER]).toColumn(), (this[POSITION_FROM_NUMBER]).toRow())

    val endSquare = Square((this[POSITION_TO_LETTER]).toColumn(), (this[POSITION_TO_NUMBER]).toRow())

    return PieceMove(startSquare,endSquare)
}

fun PieceMove.formatToString(board : Board) =
    board.getPiece(startSquare).toString() +
            startSquare.toString()+
            endSquare.toString()



/**
 * This function returns the moves for the pieces: KNIGHT, PAWN, KING,
 * @param possibleDirections     List of directions to verify
 * @param pos                    the position of the piece
 * @return the list of possible moves for the piece given
 */
fun getMovesByAddingDirection(possibleDirections : List<Direction> ,pos: Square,board : Board): List<PieceMove> {

    val startingPiece = board.getPiece(pos) ?: return emptyList()
    return possibleDirections.mapNotNull {
        val newPos = pos.addDirection(it)
        if (newPos != null) {
            val piece = board.getPiece(newPos)
            if (piece == null || piece.player != startingPiece.player)
                PieceMove(pos, newPos)
            else null
            }
        else null
        }

}


/**
 * This function returns the moves for the pieces: ROOK, BISHOP, QUEEN,
 * @param possibleDirections     List of directions to verify
 * @param pos                    the position of the piece
 * @param board                  the board where the piece is
 * @return the list of possible moves for the piece given
 */
fun getMoves( board: Board, pos: Square,possibleDirections : List<Direction> ): List<PieceMove> {
    var moves = listOf<PieceMove>()
    val piece = board.getPiece(pos) ?: throw IllegalArgumentException("No piece at position $pos")
    val color = piece.player
    possibleDirections.forEach {
        var newPos : Square? = pos.addDirection(it)
        while(newPos != null ){
            val pieceAtEndSquare = board.getPiece(newPos)
            if (pieceAtEndSquare == null){
                moves = moves + PieceMove(pos, newPos)
            }
            if(pieceAtEndSquare != null && pieceAtEndSquare.player != color){
                moves = moves + PieceMove(pos, newPos)
                break
            }
            if(pieceAtEndSquare != null && pieceAtEndSquare.player == color){
                break
            }

            newPos = newPos.addDirection(it)
        }
    }
    return moves
}

/**
 * @param board                  the board where the piece is
 * @param pieceInfo              the [PieceMove] on the move to verify
 * @return the type of the movement for the piece given
 */
fun Piece.canNormalPieceMove(board: Board, pieceInfo: PieceMove): MoveType {
    val pieceAtEndSquare = board.getPiece(pieceInfo.endSquare)
    if(isCheckMate(board,player)) return MoveType.CHECKMATE
    return when(getPossibleMoves(board, pieceInfo.startSquare).contains(pieceInfo)){
        isKingInCheck(board,pieceInfo) -> MoveType.CHECK
        false -> MoveType.ILLEGAL
        pieceAtEndSquare == null -> MoveType.REGULAR
        pieceAtEndSquare != null && pieceAtEndSquare.player != this.player -> MoveType.CAPTURE
        else -> MoveType.ILLEGAL
    }
}

/**
 * Determines the type of the movement for the piece given
 * @param moveString string like Pa2a3
 * @param board local game board
 * @return the type of the movement for the piece given
 */
fun canPieceMoveTo(moveString:String,board: Board): MoveType {
    val piece  = board.getPiece(moveString.substring(1..2).toSquare()) ?: return MoveType.ILLEGAL

    val pieceInfo = PieceMove(moveString.substring(1..2).toSquare(), moveString.substring(3..4).toSquare())

    return piece.canMove(board, pieceInfo)
}


/**
 * @param endPos        The position where the piece will be moved to.
 * @param board         The board to perform the move on.
 * @returns a full string with the Piece, the start position and the end position or null if there is no move possible.
 */
fun traceBackPawn(endPos:String, board: Board):String?{

    val column = endPos[0]
    val row = endPos[1]
    var pieceCapturingRight : Piece? = null
    var pieceCapturingLeft : Piece? = null
    var pieceRowBehind : Piece? = null
    var pieceTwoRowsBehind : Piece? = null

    val direction : Int = if(board.getPlayerColor().isWhite()) DOWN else UP

    if((column + 1).isAColumn() && (row - direction).isARow())  pieceCapturingRight = board.getPiece(
        Square((column + 1).toColumn(), (row - direction).toRow()
        )
    )
    if((column - 1).isAColumn() && (row - direction).isARow())  pieceCapturingLeft =  board.getPiece(
        Square((column - 1).toColumn(), (row - direction).toRow()
        )
    )
    if((row - direction).isARow() && column.isAColumn()) pieceRowBehind = board.getPiece(
        Square((column).toColumn(), (row - direction).toRow()
        )
    )
    if((row -(direction + direction)).isARow() && column.isAColumn()) pieceTwoRowsBehind = board.getPiece(
        Square((column).toColumn(), (row - (direction + direction)).toRow())
    )

    var tracedPawn : String? = "P"


    val endPosPiece = board.getPiece(Square((endPos[0]).toColumn(), (endPos[1]).toRow()))

    tracedPawn = if(endPosPiece == null) {
        if(pieceRowBehind!= null && pieceRowBehind   is Pawn) tracedPawn + column + (row - direction)
        else if(pieceTwoRowsBehind!= null && pieceTwoRowsBehind is Pawn && !pieceTwoRowsBehind.hasMoved()) tracedPawn + column + (row - (direction*2))
        else null
    }
    else{
        if(pieceCapturingRight!= null && pieceCapturingRight is Pawn) tracedPawn + (column+1) + (row - direction)
        else if(pieceCapturingLeft != null && pieceCapturingLeft is Pawn) tracedPawn + (column-1) + (row - direction)
        else null
    }

    return if(tracedPawn == null) tracedPawn else tracedPawn + endPos

}

/**
 * Verifies if the opponent king is in checkMate
 * @param board game board
 * @param color the color of the player
 * @return true if the king is in checkMate false otherwise
 */
fun isCheckMate(board: Board, player: Player): Boolean {
    val king = board.getKingPiece(player)
    val possibleMoves = king.getPossibleMoves(board, board.getKingSquare(player)).map { it.endSquare }
    val list = board.getAllMoves(!player).filter { it.endSquare in possibleMoves }
    if (list.size >= 3) return true
    return false
}

fun isKingInCheck(board: Board,pieceInfo: PieceMove): Boolean {
    val tempBoard = board.makeMove(pieceInfo.formatToString(board))
    val kingSquare = tempBoard.getKingSquare(board.player)
    val listOfEndSquares = tempBoard.getAllMoves(tempBoard.player).map { it.endSquare }
    return kingSquare in listOfEndSquares
}













