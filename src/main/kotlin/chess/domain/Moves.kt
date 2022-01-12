package chess.domain

import Board
import Direction
import King
import Piece
import chess.domain.board_components.*
import counterCheckMoves
import playerMoves

//TODO : add a movetype for promotion and capture or change into sealed class and the promotion to have values
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


private const val POSITION_FROM_LETTER = 1
private const val POSITION_FROM_NUMBER = 2
private const val POSITION_TO_LETTER = 3
private const val POSITION_TO_NUMBER = 4


/**
 * @param startSquare       the square where the piece is placed
 * @param endSquare         the square where the piece wants to move to.
 * Represents the piece movement.
 */
data class PieceMove(val startSquare: Square, val endSquare: Square)

/**
 * Receives a move input as a [String] and transforms it into a [PieceMove]
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
 * This function returns the moves for the pieces: KNIGHT, KING.
 * @param possibleDirections     List of [Direction] to verify
 * @param pos                    the [Square] of the piece
 * @param board                  the [Board] to verify the moves on
 * @return the list of possible [PieceMove] for the piece given
 */
fun getMovesByAddingDirection(possibleDirections : List<Direction> , pos : Square, board : Board): List<PieceMove> {
    val startingPiece = board.getPiece(pos) ?: return emptyList()
    val moves = possibleDirections.mapNotNull {
        val newPos = pos.addDirection(it)
        if (newPos != null) {
            val piece = board.getPiece(newPos)
            if (piece == null || piece.player != startingPiece.player)
                PieceMove(pos, newPos)
            else null
        }
        else null
    }


    return moves
}


/**
 * This function returns the moves for the pieces: ROOK, BISHOP, QUEEN,
 * @param possibleDirections     List of [Direction] to verify
 * @param pos                    the [Square] of the piece
 * @param board                  the [Board] to verify the moves on
 * @return the list of possible moves for the piece given
 */
fun getMoves(possibleDirections : List<Direction>, pos : Square, board : Board , verifyForCheck : Boolean): List<PieceMove> {
    var moves = listOf<PieceMove>()
    val piece = board.getPiece(pos) ?: throw IllegalArgumentException("No piece at position $pos")
    val color = piece.player
    possibleDirections.forEach {
        var newPos : Square? = pos.addDirection(it)
        while(newPos != null ){
            val pieceAtEndSquare = board.getPiece(newPos)
            if (pieceAtEndSquare == null){
                moves += (PieceMove(pos, newPos))
            }
            if(pieceAtEndSquare != null && pieceAtEndSquare.player != color){
                moves += (PieceMove(pos, newPos))
                break
            }
            if(pieceAtEndSquare != null && pieceAtEndSquare.player == color){
                break
            }

            newPos = newPos.addDirection(it)
        }
    }
    if(verifyForCheck) moves = moves.filter {  it.endSquare in board.counterCheckMoves(!board.player)  && !isKingInCheckPostMove(board,it) }

    return moves
}

/**
 * @param board                  the board to verify the move on
 * @param pieceInfo              the [PieceMove] on the move to verify
 * @return the [MoveType] of move the piece wants to make
 */
fun Piece.canNormalPieceMove(board: Board, pieceInfo: PieceMove): MoveType {
    val pieceAtEndSquare = board.getPiece(pieceInfo.endSquare)
    return when(getPossibleMoves(board, pieceInfo.startSquare, isKingInCheck(board)).contains(pieceInfo)){
        false -> MoveType.ILLEGAL
        isCheckMate(board) -> MoveType.CHECKMATE
        isOpponentKingInCheckAfterMove(board,pieceInfo) -> MoveType.CHECK
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
fun getMoveType(moveString:String, board: Board): MoveType {
    val piece  = board.getPiece(moveString.substring(1..2).toSquare()) ?: return MoveType.ILLEGAL

    val pieceInfo = PieceMove(moveString.substring(1..2).toSquare(), moveString.substring(3..4).toSquare())

    return piece.canMove(board, pieceInfo)
}


/**
 * Gets the possible moves for the piece at the given square
 * @param square the square where the piece is located
 * @return a list of possible moves for the piece at the given square or an empty list if there is no piece at the given square or the piece is not movable
 */
fun Square.getPiecePossibleMovesFrom(board: Board,player: Player): List<PieceMove> {
    val piece = board.getPiece(this) ?: return emptyList()
     if(piece is King) return piece.getPossibleMoves(board,this,true)
    if(piece.player != player)
        return emptyList()

    return piece.getPossibleMoves(board, this, isKingInCheck(board))
}



/**
 * Verifies if the opponent king is in checkMate
 * @param board game board
 * @param color the color of the player
 * @return true if the king is in checkMate false otherwise
 */
fun isCheckMate(board: Board): Boolean {
/*
    val king = board.getKingPiece(board.player)
    val kingSquare = board.getKingSquare(board.player)
    val opponentMoves = board.playerMoves(!board.player,false)
    if(isKingInCheck(board)){
        val possibleMoves = king.getPossibleMoves(board, kingSquare,true).map { it.endSquare }
        val filteredOpponentMoves = opponentMoves.filter { it in possibleMoves }
        val playerMoves = board.playerMoves(board.player,true)
        if (filteredOpponentMoves.size >= possibleMoves.size
            && cannotDefendKing(possibleMoves, playerMoves)
        ) return true

    }

 */
    return false
}

fun isOpponentKingInCheckAfterMove(board: Board, pieceInfo: PieceMove): Boolean {
    val tempBoard = board.makeMove(pieceInfo.formatToString(board))
    val opponentKing = tempBoard.getKingSquare(!board.player)
    val playerMoves = tempBoard.playerMoves(board.player,false)
    return opponentKing in playerMoves
}

fun isKingInCheckPostMove(board: Board,pieceInfo: PieceMove): Boolean{
    val tempBoard = board.makeMove(pieceInfo.formatToString(board))
    val king = tempBoard.getKingSquare(board.player)
    val opponentMoves = tempBoard.playerMoves(tempBoard.player,false)
    return king in opponentMoves
}



fun isKingInCheck(board: Board) = board.getKingSquare(board.player) in board.playerMoves(!board.player,false)





/*
fun filterCheckMoves(board: Board, moves: List<PieceMove>?, piece: Piece?): List<PieceMove>? {
    if(isKingInCheck(board) && moves != null && piece !is King)
        return moves.filter { it.endSquare in board.playerMoves(!board.player) && !isKingInCheckPostMove(board,it) }
    if(isKingInCheck(board) && moves != null && piece is King) return moves.filter { it.endSquare !in board.playerMoves(!board.player)  && !isKingInCheckPostMove(board,it)}
    return moves
}
*/

fun canDefendKing(possibleKingMoves : List<Square>, playerMoves: List<Square>): Boolean {
    if(possibleKingMoves.size == 1 && possibleKingMoves.first() in playerMoves) return false
    return true
}

fun cannotDefendKing(possibleKingMoves : List<Square>, playerMoves: List<Square>) =
    !canDefendKing(possibleKingMoves, playerMoves)
