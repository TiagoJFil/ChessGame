package chess.domain

import Board
import Direction
import Pawn
import Piece
import PieceMove
import chess.domain.board_components.*


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
 * @param possibleDirections     List of directions to verify
 * @param pos                    the position of the piece
 * @param board                  the board where the piece is
 * @return the list of possible moves for the piece given
 */
fun getMoves( board: Board, pos: Square,possibleDirections : List<Direction> ): List<PieceMove> {
    var moves = listOf<PieceMove>()
    val piece = board.getPieceAt(pos) ?: throw IllegalArgumentException("No piece at position $pos")
    val color = piece.player.color
    possibleDirections.forEach {
        var newPos : Square? = pos.addDirection(it)
        while(newPos != null ){
            val pieceAtEndSquare = board.getPieceAt(newPos)
            if (pieceAtEndSquare == null){
                moves += PieceMove(pos, newPos)
            }
            if(pieceAtEndSquare != null && pieceAtEndSquare.player.color != color){
                moves += PieceMove(pos, newPos)
                break
            }
            if(pieceAtEndSquare != null && pieceAtEndSquare.player.color == color){
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
      pieceAtEndSquare != null && pieceAtEndSquare.player.color != this.player.color -> MoveType.CAPTURE
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

    return null
}




     fun CanMoveTo(pieceInfo: PieceMove, board: Board): MoveType {/*
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



