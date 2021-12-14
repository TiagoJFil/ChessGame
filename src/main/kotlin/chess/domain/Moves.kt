package chess.domain

import Board
import Colors
import Direction
import Piece
import PieceMove
import PieceType
import chess.domain.board_components.*


import kotlin.math.abs

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
private const val PAWN_PROMOTION_ROW_WHITE = 7
private const val PAWN_PROMOTION_ROW_BLACK = 0


/**
 * Class to define de orientation of the movement
 */
private enum class Orientation{
    DIAGONAL,
    HORIZONTAL,
    VERTICAL
}


interface VerifyMoves {
    fun CanMoveTo(pieceInfo: PieceMove, board: Board): MoveType?
    operator fun invoke(pieceInfo: PieceMove, board: Board) = CanMoveTo(pieceInfo,board)
}


/**
 * @param possibleDirections     List of directions to verify
 * @param pos                    the position of the piece
 * @param board                  the board where the piece is
 * @return the list of possible moves for the piece given
 */
fun getMoves( board: Board, pos: Square,possibleDirections : List<Direction> ): List<PieceMove> {
    var moves = listOf<PieceMove>()
    possibleDirections.forEach {
        var newPos : Square? = pos.addDirection(it)
        while(newPos != null ){
            val pieceAtEndSquare = board.getPieceAt(newPos)
            if (pieceAtEndSquare == null){
                moves += PieceMove(pos, newPos)
            }
            if(pieceAtEndSquare != null && pieceAtEndSquare.player.color != board.getPlayerColor()){
                moves += PieceMove(pos, newPos)
                break
            }
            if(pieceAtEndSquare != null && pieceAtEndSquare.player.color == board.getPlayerColor()){
                break
            }

            newPos = newPos.addDirection(it)
        }
    }
    return moves
}

fun Piece.canNormalPieceMove(board: Board, pieceInfo: PieceMove): MoveType {
    val pieceAtEndSquare = board.getPieceAt(pieceInfo.endSquare)
    return when( getPossibleMoves(board, pieceInfo.startSquare).contains(pieceInfo) ){
        false -> MoveType.ILLEGAL
      pieceAtEndSquare == null -> MoveType.REGULAR
      pieceAtEndSquare != null && pieceAtEndSquare.player.color == !board.getPlayerColor() -> MoveType.CAPTURE
     else -> MoveType.ILLEGAL
}
}
/**
 * @param moveString string like Pa2a3
 * @param board local game board
 * checks if the move is possible
 */
fun canPieceMoveTo(moveString:String,board: Board): MoveType {
    //if the starting square is empty (the piece is empty)
    val piece  = board.getPieceAt(moveString.substring(1..2).toSquare()) ?: return MoveType.ILLEGAL

    val pieceInfo = PieceMove(moveString.substring(1..2).toSquare(), moveString.substring(3..4).toSquare())

    return piece.canMove(board, pieceInfo)
}



/**
 * @param endPos        The position where the piece will be moved to.
 * @param board         The board to perform the move on.
 * @returns a full string with the Piece, the start position and the end position or null if there is no move possible.
 */
fun traceBackPawn(endPos:String, board: Board):String?{
    /*
    val column = endPos[0]
    val row = endPos[1]
    var pieceCapturingRight : Piece? = null
    var pieceCapturingLeft : Piece? = null
    var pieceRowBehind : Piece? = null
    var pieceTwoRowsBehind : Piece? = null

    val direction : Int = if(board.getPlayerColor() == Colors.WHITE) DOWN else UP

    if((column + 1).isAColumn() && (row - direction).isARow())  pieceCapturingRight = board.getPieceAt(
        Square(
            findColumn(
                column + 1
            ), findRow(row - direction)
        )
    )
    if((column - 1).isAColumn() && (row - direction).isARow())  pieceCapturingLeft =  board.getPieceAt(
        Square(
            findColumn(
                column - 1
            ), findRow(row - direction)
        )
    )
    if((row - direction).isARow() && column.isAColumn()) pieceRowBehind = board.getPieceAt(
        Square(
            findColumn(column),
            findRow(row - direction)
        )
    )
    if((row -(direction + direction)).isARow() && column.isAColumn()) pieceTwoRowsBehind = board.getPieceAt(
        Square(
            findColumn(column),
            findRow(row - (direction + direction))
        )
    )

    var tracedPawn : String? = "P"


    val endPosPiece = board.getPieceAt(Square(findColumn(endPos[0]), findRow(endPos[1])))

    tracedPawn = if(endPosPiece == null) {
        if(pieceRowBehind!= null && pieceRowBehind.type == PieceType.PAWN) tracedPawn + column + (row - direction)
        else if(pieceTwoRowsBehind!= null && pieceTwoRowsBehind.type == PieceType.PAWN && !pieceTwoRowsBehind.hasMoved()) tracedPawn + column + (row - (direction*2))
        else null
    }
    else{
        if(pieceCapturingRight!= null && pieceCapturingRight.type == PieceType.PAWN) tracedPawn + (column+1) + (row - direction)
        else if(pieceCapturingLeft != null && pieceCapturingLeft.type == PieceType.PAWN) tracedPawn + (column-1) + (row - direction)
        else null
    }

    return if(tracedPawn == null) tracedPawn else tracedPawn + endPos
*/
    return null
}


private class CanPawnMoveTo(): VerifyMoves {

    override fun CanMoveTo(pieceInfo: PieceMove, board: Board): MoveType {/*
        val piece = board.getPieceAt(pieceInfo.startSquare) ?: throw IllegalArgumentException("ERROR: Check failed..")
        val direction : Int = if(piece.belongsToWhitePlayer()) UP else DOWN
        val border = if(piece.belongsToWhitePlayer()) PAWN_PROMOTION_ROW_WHITE else PAWN_PROMOTION_ROW_BLACK

        val pieceAtEndPos = board.getPieceAt(pieceInfo.endSquare)

        val moveCondition = pieceInfo.endSquare.column.value() == pieceInfo.startSquare.column.value() &&
                pieceInfo.endSquare.row.value() == pieceInfo.startSquare.row.value() + direction

        if (moveCondition && pieceAtEndPos == null && pieceInfo.endSquare.row.value() == border) return MoveType.PROMOTION
        if (moveCondition && pieceAtEndPos == null) return MoveType.REGULAR

        if (pieceAtEndPos != null) {
            val canCapture = abs(pieceInfo.endSquare.column.value() - pieceInfo.startSquare.column.value()) == 1 &&
                    pieceInfo.endSquare.row.value() == pieceInfo.startSquare.row.value() + direction &&
                    pieceAtEndPos.player.color != piece.player.color
            //----------------
            if(canCapture && pieceInfo.endSquare.row.value() == border) return MoveType.PROMOTION
            if (canCapture) {
                return MoveType.CAPTURE
            }


        }

        //condition for moving 2 tiles
        if (!piece.hasMoved()) {
            if (pieceInfo.endSquare.column.value() == pieceInfo.startSquare.column.value() &&
                pieceInfo.endSquare.row.value() == pieceInfo.startSquare.row.value() + 2*direction && pieceAtEndPos == null &&
                isPathClear(pieceInfo, board, Orientation.VERTICAL)
            ) return MoveType.REGULAR

        }
        return MoveType.ILLEGAL
 */
        return MoveType.ILLEGAL
    }

}

private class CanKingMoveTo(): VerifyMoves {
    override fun CanMoveTo(pieceInfo: PieceMove, board: Board): MoveType {
        val pieceAtEndPos = board.getPieceAt(pieceInfo.endSquare)

        val possibleMoves = (abs(pieceInfo.startSquare.row.value() - pieceInfo.endSquare.row.value()) == 1 ||
                abs(pieceInfo.startSquare.column.value() - pieceInfo.endSquare.column.value()) == 1)

        if(possibleMoves && pieceAtEndPos == null) return MoveType.REGULAR

        if(pieceAtEndPos != null) {
            if (possibleMoves && pieceAtEndPos.player.color == !board.getPlayerColor()) return MoveType.CAPTURE
        }


        //falta a condiçao de que quando o king nao tem mais possibleMoves e alguem lhe deixa o rei em check entao é checkmate
        if(canCastle(pieceInfo,board)) return MoveType.CASTLE

        return MoveType.ILLEGAL
    }
    private fun canCastle(pieceInfo: PieceMove, board: Board): Boolean {
        /*
        val piece = board.getPieceAt(pieceInfo.startSquare) ?: return false
        val rookAtRightPos : Piece?
        val rookAtLeftPos : Piece?


        if (piece.belongsToWhitePlayer()){
            rookAtRightPos = board.getPieceAt(Square(Column.H, Row.One))
            rookAtLeftPos = board.getPieceAt(Square(Column.A, Row.One))
        }else{
            rookAtRightPos = board.getPieceAt(Square(Column.H, Row.Eight))
            rookAtLeftPos = board.getPieceAt(Square(Column.A, Row.Eight))
        }
        if(!piece.hasMoved() && isPathClear(pieceInfo,board, Orientation.HORIZONTAL)) {

            if(abs(pieceInfo.startSquare.column.value() - pieceInfo.endSquare.column.value()) == 2 &&
                (( rookAtRightPos != null && rookAtRightPos.type == PieceType.ROOK && !rookAtRightPos.hasMoved() )
                ||
                ( rookAtLeftPos != null && rookAtLeftPos.type == PieceType.ROOK && !rookAtLeftPos.hasMoved() ))
            ) return true

        }
        return false
        */
        return false
    }
}




private class CanRookMoveTo(): VerifyMoves {
    override fun CanMoveTo(pieceInfo: PieceMove, board: Board): MoveType {
        val pieceDefaultOrientation = listOf(Orientation.VERTICAL, Orientation.HORIZONTAL)

        val orientationFromInput = getOrientation(pieceInfo.startSquare,pieceInfo.endSquare) ?: return MoveType.ILLEGAL
        if(!pieceDefaultOrientation.contains(orientationFromInput)) return MoveType.ILLEGAL //in case the piece doesn't allow the orientation of the move

        val moveIsPossible = isMovePossible(pieceInfo,orientationFromInput)
        val pieceAtEndPos = board.getPieceAt(pieceInfo.endSquare)


        if(moveIsPossible && pieceAtEndPos == null && isPathClear(pieceInfo,board,orientationFromInput)) return MoveType.REGULAR

        if(pieceAtEndPos != null) {
            val capturesOpponentPiece = moveIsPossible && pieceAtEndPos.player.color == !board.getPlayerColor() && isPathClear(pieceInfo, board, orientationFromInput)
            if (capturesOpponentPiece) return MoveType.CAPTURE
        }
        return MoveType.ILLEGAL
    }
}



/**
 * This function checks if the path is clear.
 * @param pieceInfo the piece info
 * @param board  the board to be checked on
 * @param orientation the orientation of the move
 * @return a [Boolean] value indicating if the path is clear(true), false otherwise
 */
private fun isPathClear(pieceInfo: PieceMove, board: Board, orientation: Orientation):Boolean{
    val yDirection = if(pieceInfo.startSquare.row.value() < pieceInfo.endSquare.row.value()) DOWN else UP //move up in the array, keep in mind that the row EIGHT == index 0
    val xDirection = if(pieceInfo.startSquare.column.value() < pieceInfo.endSquare.column.value()) RIGHT else LEFT
    var currentPos = pieceInfo.startSquare
    val finalPos = pieceInfo.endSquare
    var whileLoop : Square
    currentPos = when(orientation){
        Orientation.DIAGONAL -> {
            Square(
                Column.values()[currentPos.column.value() + xDirection],
                Row.values()[currentPos.row.value() + yDirection]
            )
        }

        Orientation.HORIZONTAL -> {
            Square(Column.values()[currentPos.column.value() + xDirection], Row.values()[currentPos.row.value()])

        }
        Orientation.VERTICAL -> {
            Square(Column.values()[currentPos.column.value()], Row.values()[currentPos.row.value() + yDirection])

        }
    }
    while(currentPos != finalPos){

        if(board.getPieceAt(currentPos) != null){
            return false
        }

        whileLoop = when(orientation){
            Orientation.DIAGONAL -> {
                Square(
                    Column.values()[currentPos.column.value() + xDirection],
                    Row.values()[currentPos.row.value() + yDirection]
                )
            }

            Orientation.HORIZONTAL -> {
                Square(Column.values()[currentPos.column.value() + xDirection], Row.values()[currentPos.row.value()])
            }

            Orientation.VERTICAL -> {
                Square(Column.values()[currentPos.column.value()], Row.values()[currentPos.row.value() + yDirection])
            }

        }
        currentPos = whileLoop
    }
    return true
}

/**
 * @param startPos - the position of the piece
 * @param endPos   - the position to move the piece to
 * @return the [Orientation] of the move or null if the move is not valid
 */
private fun getOrientation(startPos: Square, endPos: Square): Orientation?{
    if(abs(startPos.column.value() - endPos.column.value()) == abs(startPos.row.value() - endPos.row.value())){
        return Orientation.DIAGONAL
    }
    if(abs(startPos.column.value() - endPos.column.value()) == 0){
        return Orientation.VERTICAL
    }
    if(abs(startPos.row.value() - endPos.row.value()) == 0){
        return Orientation.HORIZONTAL
    }
    return null
}


private fun isMovePossible(pieceInfo: PieceMove, orientation: Orientation): Boolean {
    return (orientation == Orientation.VERTICAL &&
            (abs(pieceInfo.startSquare.row.value() - pieceInfo.endSquare.row.value()) <= 7 &&
                    abs(pieceInfo.startSquare.column.value() - pieceInfo.endSquare.column.value()) == 0))
            ||
            (orientation == Orientation.HORIZONTAL &&
                    (abs(pieceInfo.startSquare.row.value() - pieceInfo.endSquare.row.value()) == 0 &&
                            abs(pieceInfo.startSquare.column.value() - pieceInfo.endSquare.column.value()) <= 7))
            ||
            (orientation == Orientation.DIAGONAL &&
                    (abs(pieceInfo.startSquare.row.value() - pieceInfo.endSquare.row.value() ) ==
                            abs(pieceInfo.startSquare.column.value() - pieceInfo.endSquare.column.value())))
}

//Não testa se o cavalo faz check
/*
fun getCheck(board: Board): Boolean {
    TODO()
    val kingSquare = board.getKingSquare(board.getPlayerColor())
    val lastMove = board.getLastMove().substring(3..4).toSquare()
    if(kingSquare != null) {
        val pieceMove = PieceMove(lastMove, kingSquare)
        if(isPathClear(pieceMove,
                board,
                getOrientation(lastMove,kingSquare)!!)
        ) return true
    }

    return false

}
*/





