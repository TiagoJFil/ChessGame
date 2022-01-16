package chess.domain

import Board
import Direction
import King
import Piece
import chess.domain.board_components.*
import counterCheckMoves
import getAllBoardMovesFrom
import getKingPossibleMoves
import piecesCountFrom


//TODO : add a movetype for promotion and capture or change into sealed class and the promotion to have values
enum class MoveType{
    REGULAR,
    CAPTURE,
    CASTLE,
    PROMOTION,
    ENPASSANT,
    CHECK,
    CHECKMATE,
    ILLEGAL,
    STALEMATE;
}


private const val POSITION_FROM_LETTER = 1
private const val POSITION_FROM_NUMBER = 2
private const val POSITION_TO_LETTER = 3
private const val POSITION_TO_NUMBER = 4
private const val NO_PIECE_INPUT = 4

/**
 * @param startSquare       the square where the piece is placed
 * @param endSquare         the square where the piece wants to move to.
 * Represents the piece movement.
 */
data class PieceMove(val startSquare: Square, val endSquare: Square)

/**
 * Represents a move in the game
 * @property move    the move
 * Only formatted moves are allowed ex: Pe2e4, pb1xc3, Kb6c7=Q , Pa6xb7=Q , etc.
 */
data class Move(val move: String){
    init {
        require(move.isFormatted())
    }
    override fun toString() = move
}

/**
 * Checks if a string is formatted correctly to be allowed as a moved
 * @return  a [Boolean] value indicating whether the string is formatted correctly (true) or not (false)
 */
private fun String.isFormatted(): Boolean {
    val filtered = Regex("([RNBQKPrnbqkp])([abcdefgh])([12345678])x?([abcdefgh])([12345678])=?([NBQR])?(.ep)?")
    return filtered.matches(this)
}

fun String.toMove(board: Board): Move? {
    val filterForNoPieceName = Regex("([abcdefgh])([12345678])([abcdefgh])([12345678])")

    if(this.length == NO_PIECE_INPUT && filterForNoPieceName.matches(this)) {
        val piece = board.getPiece(this.substring(0, 2).toSquare())
        if(piece != null) {
            val filteredInput = piece.toString().uppercase() + this
            return Move(filteredInput)
        }
        else return null
    }

    return Move(this)
}


/**
 * Receives a move input as a [String] and transforms it into a [PieceMove]
 **/
fun String.formatToPieceMove(): PieceMove{
    val filterForPieceMove = Regex("([RNBQKPrnbqkp])([abcdefgh])([12345678])([abcdefgh])([12345678])")
    if(!filterForPieceMove.matches(this)) throw IllegalArgumentException("The String is not formatted correctly")
    val startSquare = Square((this[POSITION_FROM_LETTER]).toColumn(), (this[POSITION_FROM_NUMBER]).toRow())
    val endSquare = Square((this[POSITION_TO_LETTER]).toColumn(), (this[POSITION_TO_NUMBER]).toRow())

    return PieceMove(startSquare,endSquare)
}

/**
 * Receives a move input as a [PieceMove] and transforms it into a [String]
 */
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
 * @param verifyForCheck        a [Boolean] value indicating whether the moves should be verified for check or not
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
                moves = moves + (PieceMove(pos, newPos))
            }
            if(pieceAtEndSquare != null && pieceAtEndSquare.player != color){
                moves = moves + (PieceMove(pos, newPos))
                break
            }
            if(pieceAtEndSquare != null && pieceAtEndSquare.player == color){
                break
            }

            newPos = newPos.addDirection(it)
        }
    }
    if(verifyForCheck) moves = moves.filter {  it.endSquare in board.counterCheckMoves(!board.player)  && !isMyKingInCheckPostMove(board,it) }

    return moves
}

/**
 * @param board                  the board to verify the move on
 * @param pieceInfo              the [PieceMove] on the move to verify
 * @return the [MoveType] of move the piece wants to make
 */
fun Piece.canNormalPieceMove(board: Board, pieceInfo: PieceMove): MoveType {
    val pieceAtEndSquare = board.getPiece(pieceInfo.endSquare)

    return when(getPossibleMoves(board, pieceInfo.startSquare, isKingInCheck(board,board.player)).contains(pieceInfo)){
        false -> MoveType.ILLEGAL
        isStalemateAfterMove(board,pieceInfo) -> MoveType.STALEMATE
        isCheckMateAfterMove(board,pieceInfo) -> MoveType.CHECKMATE
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
fun getMoveType(moveString: Move, board: Board): MoveType {
    val piece  = board.getPiece(moveString.move.substring(1..2).toSquare()) ?: return MoveType.ILLEGAL

    val pieceInfo = moveString.move.formatToPieceMove()

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

    return piece.getPossibleMoves(board, this, isKingInCheck(board,player))
}

/**
 * Checks whether the move receives as a [String] is a valid promotable move.
 * @param move           The move to be checked.
 * @return a [Boolean]   value indicating whether the move is promotable or not.
 */
fun Board.isTheMovementPromotable(move: String): Boolean {
    val filteredInput = move.toMove(this) ?: return false
    return getMoveType(filteredInput, this) == MoveType.PROMOTION
}


/**
 * Verifies if the opponent king is in checkMate
 * @param board game board
 * @param color the color of the player
 * @return true if the king is in checkMate false otherwise
 */



fun isCheckMateAfterMove(board: Board, pieceInfo: PieceMove): Boolean {
    val tempBoard = board.makeMove(pieceInfo.formatToString(board))
    val kingCantMove = tempBoard.getKingPossibleMoves(!board.player,true).isEmpty()
    val playermoves = tempBoard.getAllBoardMovesFrom(!board.player,true)
    return kingCantMove &&  isKingInCheck(tempBoard,!board.player) && playermoves.isEmpty()
}

fun isOpponentKingInCheckAfterMove(board: Board, pieceInfo: PieceMove): Boolean {
    val tempBoard = board.makeMove(pieceInfo.formatToString(board))
    val opponentKing = tempBoard.getKingSquare(!board.player)
    val playerMoves = tempBoard.getAllBoardMovesFrom(board.player, false).map { it.endSquare }
    return opponentKing in playerMoves
}

fun isMyKingInCheckPostMove(board: Board, pieceInfo: PieceMove): Boolean{
    val endPiece = board.getPiece(pieceInfo.endSquare)
    if (endPiece != null && endPiece is King && endPiece.player == board.player) return true
    val tempBoard = board.makeMove(pieceInfo.formatToString(board))

    return isKingInCheck(tempBoard,board.player)
}

fun isStalemateAfterMove(board: Board, pieceInfo: PieceMove): Boolean{
    val tempBoard = board.makeMove(pieceInfo.formatToString(board))
    return tempBoard.piecesCountFrom(!board.player) == 1 && !isKingInCheck(tempBoard,!board.player) && tempBoard.getKingPossibleMoves(!board.player,true).isEmpty()
}

fun isKingInCheck(board: Board,player: Player) = board.getKingSquare(player) in board.getAllBoardMovesFrom(!player,false).map { it.endSquare }


