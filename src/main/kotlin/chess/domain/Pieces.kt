package chess.domain

import Board
import chess.domain.board_components.Square
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.toSquare

const val LEFT = -1
const val RIGHT = 1
const val UP = -1
const val DOWN = 1


private const val PAWN_PROMOTION_ROW_WHITE = 7
private const val PAWN_PROMOTION_ROW_BLACK = 0
private const val G_COLUMN_NUMBER = 6

/**
 * Represents a direction a piece can add to its position.
 * @param first the x direction AKA [Column]
 * @param second the y direction AKA [Row]
 */

typealias Direction = Pair<Int, Int>



/**
 * Determines the type of the movement for the piece given
 * @param moveString string like Pa2a3
 * @param board local game board
 * @return the type of the movement for the piece given
 * Detects already if the player is trying to move a piece that is not his and if there is a piece at the start
 */
fun getMoveType(moveString: Move, board: Board): MoveType {
    val pieceAtStart  = board.getPiece(moveString.move.substring(1..2).toSquare()) ?: return MoveType.ILLEGAL
    val pieceAtEnd = board.getPiece(moveString.move.substring(3..4).toSquare())

    if(pieceAtStart.player != board.player) return MoveType.ILLEGAL

    val myKing = board.getKingPiece(board.player)
    val oponKing = board.getKingPiece(!board.player)
    if(pieceAtEnd == myKing || pieceAtEnd == oponKing) return MoveType.ILLEGAL

    val pieceInfo = moveString.move.formatToPieceMove()
    return pieceAtStart.canMove(board, pieceInfo)
}




/**
 * Represents a chess piece.
 */
sealed interface Piece {
    val player: Player

    /**
     * @return the piece as a string with the correspondent color
     */
    override fun toString(): String

    /**
     * Gets all the posible [PieceMove]s for the given piece
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @param verifyForCheck a [Boolean] indicating wheter the possible moves should take into account the fact that the king is on check(to be able to protect him)
     * @return the list of possible directions for the piece
     */
    fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean): List<PieceMove>


    /**
     * @return a [Boolean] value if the piece belongs to the white player(true) or false if it belongs to the black player
     */
    fun belongsToWhitePlayer(): Boolean {
        return player.isWhite()
    }

    /**
     * Creates a new object of the same type with the same properties
     */
    fun copy() : Piece {
        return when(this) {
            is Pawn -> {
                (this as Pawn).copy()
            }
            is Rook -> {
                (this as Rook).copy()
            }
            is King -> {
                (this as King).copy()
            }
            is Knight -> Knight(player)
            is Bishop -> Bishop(player)
            is Queen -> Queen(player)
        }
    }

    /**
     * Gets the [MoveType] of the movement received
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    fun canMove(board: Board, pieceInfo: PieceMove): MoveType = canNormalPieceMove(board, pieceInfo)

}

/**
 * @property player    the player that owns this piece
 * Represents a pawn piece
 */
class Pawn (override val player: Player) : Piece {

    private var moveCount: Int = 0
    private var moved: Boolean = false
    /**
     * @return a [Boolean] value indicating whether the piece has moved or not
     */
    private fun hasMoved(): Boolean = this.moved

    /**
     * Sets a piece as moved
     */
    fun setAsMoved() {
        this.moved = true
    }

    /**
     * Increases the pawn [moveCount]
     * Needed for a correct en passant
     */
    fun increaseMoveCounter(){
        if(this.moved) this.moveCount++
    }

    /**
     * The possible offset this piece can move to
     */
    private val possibleDirections  : List<Direction> = if(player.isWhite()) listOf(Direction(0, UP))
    else listOf(Direction(0, DOWN))

    /**
     * @return the piece as a string with the correspondent color
     */
    override fun toString(): String {
        return if (player.isWhite()) "P" else "p"
    }


    /**
     * Gets the piece possible moves
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @param verifyForCheck a [Boolean] indicating wheter the possible moves should take into account the fact that the king is on check(to be able to protect him)
     * @return the list of possible [PieceMove] for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean): List<PieceMove> {
        var moves = listOf<PieceMove>()
        val colorDirection = if (belongsToWhitePlayer())  UP else DOWN
        if(!hasMoved() && board.getPiece(pos.addDirectionNotNull(Direction(0, 1 * colorDirection))) == null && board.getPiece(pos.addDirectionNotNull(
                Direction(0, 2 * colorDirection)
            )) == null){
            moves = moves + (PieceMove(pos, pos.addDirectionNotNull(Direction(0, 2 * colorDirection))))
        }
        val squareToDiagonalRight = pos.addDirection(Direction(RIGHT,colorDirection))
        val squareToDiagonalLeft = pos.addDirection(Direction(LEFT,colorDirection))

        if(squareToDiagonalLeft != null){
            val piece = board.getPiece(squareToDiagonalLeft)
            if(piece != null && piece.player != this.player){
                moves = moves + (PieceMove(pos, squareToDiagonalLeft))
            }
            if(canEnPassant(board,PieceMove(pos, squareToDiagonalLeft))){
                moves = moves + (PieceMove(pos, squareToDiagonalLeft))
            }
        }

        if(squareToDiagonalRight != null ){
            val piece = board.getPiece(squareToDiagonalRight)
            if(piece != null && piece.player != this.player){
                moves = moves + (PieceMove(pos, squareToDiagonalRight))
            }
            if(canEnPassant(board,PieceMove(pos,squareToDiagonalRight)))
                moves = moves + (PieceMove(pos, squareToDiagonalRight))
        }

        if(pos.addDirection(possibleDirections[0]) != null && board.getPiece(pos.addDirectionNotNull(possibleDirections[0])) == null){
            moves = moves + (PieceMove(pos, pos.addDirectionNotNull(possibleDirections[0])))
        }

        if(verifyForCheck) moves = moves.filter { !isKingInCheckPostMove(board,it,board.player) }

        return moves
    }


    /**
     * Gets the [MoveType] of the movement received
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        val pieceAtEndSquare = board.getPiece(pieceInfo.endSquare)

        return when(getPossibleMoves(board, pieceInfo.startSquare,isKingInCheck(board,board.player) ||  isKingInCheckPostMove(board,pieceInfo,board.player) ).contains(pieceInfo)){
            false -> MoveType.ILLEGAL
            isCheckMateAfterMove(board,pieceInfo) -> MoveType.CHECKMATE
            isKingInCheckPostMove(board,pieceInfo,!board.player) -> MoveType.CHECK
            isStalemateAfterMove(board,pieceInfo) -> MoveType.STALEMATE
            canPromote(pieceInfo) -> MoveType.PROMOTION
            pieceAtEndSquare == null && canEnPassant(board,pieceInfo) -> MoveType.ENPASSANT
            pieceAtEndSquare == null -> MoveType.REGULAR
            pieceAtEndSquare != null && pieceAtEndSquare.player != this.player -> MoveType.CAPTURE

            else -> MoveType.ILLEGAL
        }
    }

    /**
     * Checks whether the piece can perform the enpassant move or not
     * En passant can only happen after the piece to be captured has moved for the first time 2 tiles
     * @param board        the board to check the movement on
     * @param pos    the piece to movement to check
     * @return a [Boolean] value indicating whether the piece can perform enpassant
     */
    private fun canEnPassant(board: Board, pos: PieceMove): Boolean{

        val rowAdd = if(player.isWhite()) UP else DOWN
        val leftPos = pos.startSquare.addDirection(Direction(LEFT,0))
        val rightPos = pos.startSquare.addDirection(Direction(RIGHT,0))
        if(leftPos != null){
            val leftPiece = board.getPiece(leftPos)

            val squareAfterLeftPiece = leftPos.addDirection(Direction(0,rowAdd))
            if(pos.endSquare == squareAfterLeftPiece)
                return (leftPiece is Pawn && leftPiece.moveCount == 1 && leftPiece.player != player)
        }
        if(rightPos != null){
            val rightPiece = board.getPiece(rightPos)

            val bellowRightPiece = rightPos.addDirection(Direction(0,rowAdd))
            if(pos.endSquare == bellowRightPiece)
                return (rightPiece is Pawn && rightPiece.moveCount == 1 && rightPiece.player != player)
        }

        return false
    }

    /**
     * Checks whether the piece can promote or not
     * @param pieceInfo    the piece to movement to check
     * @return a [Boolean] value indicating whether the piece can promote
     */
    private fun canPromote(pieceInfo: PieceMove): Boolean{
        if(pieceInfo.endSquare.row.number == PAWN_PROMOTION_ROW_BLACK || pieceInfo.endSquare.row.number == PAWN_PROMOTION_ROW_WHITE )
            return true
        return false
    }

    /**
     * Adds the ability to copy a piece
     */
    override fun copy() : Pawn {
        val newPawn = Pawn(player)
        if(this.hasMoved()) newPawn.setAsMoved()
        for(count in 0 until this.moveCount){
            newPawn.increaseMoveCounter()
        }
        return newPawn
    }

}

/**
 * Represents a king piece
 * @property player    the player that owns this piece
 */
data class King (override val player: Player) : Piece {
    private var moved = false
    /**
     * @return a [Boolean] value indicating whether the piece has moved or not
     */
    private fun hasMoved(): Boolean {
        return moved
    }

    /**
     * Sets a piece as moved
     */
    fun setAsMoved() {
        moved = true
    }



    /**
     * The possible offset this piece can move to
     */
    private val possibleDirections = listOf(
        Direction(RIGHT,0),
        Direction(LEFT ,0),
        Direction(0    , DOWN),
        Direction(0    , UP),
        Direction(RIGHT, DOWN),
        Direction(RIGHT, UP),
        Direction(LEFT , DOWN),
        Direction(LEFT , UP)
    )

    /**
     * @return the piece as a string with the correspondent color
     */
    override fun toString(): String {
        return if (player.isWhite()) "K" else "k"
    }

    /**
     * Gets the piece possible moves
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @param verifyForCheck a [Boolean] indicating wheter the possible moves should take into account the fact that the king is on check(to be able to protect him)
     * @return the list of possible [PieceMove] for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean ): List<PieceMove> {
        var moves = getMovesByAddingDirection(possibleDirections, pos, board)

        if ( !this.hasMoved() && canCastle(board, PieceMove(pos, pos.addDirectionNotNull(Direction(2 * RIGHT, 0))))) {
            moves = moves + (PieceMove(pos, pos.addDirectionNotNull(Direction(2 * RIGHT, 0))))
        }
        if( !this.hasMoved() && canCastle(board, PieceMove(pos, pos.addDirectionNotNull(Direction(2 * LEFT, 0))))) {
            moves = moves + (PieceMove(pos, pos.addDirectionNotNull(Direction(2 * LEFT, 0))))
        }
        if(verifyForCheck) moves = moves.filter { !isKingInCheckPostMove(board,it,board.player)  }


        return moves
    }


    /**
     * Gets the [MoveType] of the movement received
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        val pieceAtEndSquare = board.getPiece(pieceInfo.endSquare)

        return when ( getPossibleMoves(board, pieceInfo.startSquare, true ).contains(pieceInfo)) {
            false -> MoveType.ILLEGAL
            isCheckMateAfterMove(board,pieceInfo) -> MoveType.CHECKMATE
            isKingInCheckPostMove(board,pieceInfo,!board.player) -> MoveType.CHECK
            isStalemateAfterMove(board,pieceInfo) -> MoveType.STALEMATE
            canCastle(board, pieceInfo) -> MoveType.CASTLE
            pieceAtEndSquare == null -> MoveType.REGULAR
            pieceAtEndSquare != null && pieceAtEndSquare.player != this.player -> MoveType.CAPTURE

            else -> MoveType.ILLEGAL
        }
    }

    /**
     * Checks if the king can perform castle
     * @param board         the board to check the castle movement on
     * @param pieceInfo     the piece to movement to check
     * @return a [Boolean] value if the king can castle
     */
    private fun canCastle(board: Board, pieceInfo: PieceMove): Boolean {
        if (hasMoved()) return false
        val possibleDirections = listOf(
            Direction(RIGHT, 0),
            Direction(LEFT, 0)
        )
        val possibleMovesUnfiltered = getMoves(possibleDirections, pieceInfo.startSquare, board,false)
        val possibleCastleMoves = possibleMovesUnfiltered.filter { kotlin.math.abs(it.endSquare.column.number - it.startSquare.column.number) == 2 }

        if (!possibleCastleMoves.contains(pieceInfo)) return false
        if (possibleCastleMoves.isEmpty()) return false

        for (move in possibleCastleMoves) {
            val newSquare: Square =
                if (move.endSquare.column.number == G_COLUMN_NUMBER) {
                    move.endSquare.addDirection(Direction(RIGHT, 0)) ?: return false
                } else {
                    move.endSquare.addDirection(Direction(2 * LEFT, 0)) ?: return false
                }
            val rook = board.getPiece(newSquare) ?: return false
            if (rook is Rook && !rook.hasMoved() && rook.player == this.player) {
                return true
            }
        }
        return false
    }

    /**
     * Adds the ability to copy a piece
     */
    override fun copy(): Piece {
        val newKing = King(player)
        if(this.hasMoved()) newKing.setAsMoved()
        return newKing
    }

}

/**
 * Represents a queen piece
 * @property player    the player that owns this piece
 */
data class Queen (override val player: Player) : Piece {

    /**
     * The possible offset this piece can move to
     */
    private val possibleDirections = listOf(
        Direction(RIGHT, 0),
        Direction(0    , DOWN),
        Direction(LEFT , 0),
        Direction(0    , UP),
        Direction(RIGHT, DOWN),
        Direction(RIGHT, UP),
        Direction(LEFT , DOWN),
        Direction(LEFT , UP)
    )

    /**
     * @return the piece as a string with the correspondent color
     */
    override fun toString(): String {
        return if (player.isWhite()) "Q" else "q"
    }

    /**
     * Gets the piece possible moves
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @param verifyForCheck a [Boolean] indicating wheter the possible moves should take into account the fact that the king is on check(to be able to protect him)
     * @return the list of possible [PieceMove] for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean): List<PieceMove> = getMoves(possibleDirections, pos, board,verifyForCheck)

}

/**
 * @property player    the player that owns this piece
 * Represents a rook piece
 */
data class Rook (override val player: Player) : Piece {
    private var moved = false

    /**
     * The possible offset this piece can move to
     */
    private val possibleDirections = listOf(
        Direction(RIGHT, 0),
        Direction(0    , DOWN),
        Direction(LEFT , 0),
        Direction(0    , UP)
    )

    /**
     * Sets a piece as moved
     */
    fun setAsMoved() {
        moved = true
    }

    /**
     * @return a [Boolean] value indicating whether the piece has moved or not
     */
    fun hasMoved(): Boolean {
        return moved
    }

    /**
     * @return the piece as a string with the correspondent color
     */
    override fun toString(): String {
        return if (player.isWhite()) "R" else "r"
    }

    /**
     * Gets the piece possible moves
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @param verifyForCheck a [Boolean] indicating wheter the possible moves should take into account the fact that the king is on check(to be able to protect him)
     * @return the list of possible [PieceMove] for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean ): List<PieceMove> = getMoves(possibleDirections, pos, board,verifyForCheck)

    /**
     * Adds the ability to copy a piece
     */
    override fun copy(): Piece {
        val newRook = Rook(player)
        if(this.hasMoved()) newRook.setAsMoved()
        return newRook
    }
}

/**
 * @property player    the player that owns this piece
 * Represents a knight piece
 */
data class Knight (override val player: Player) : Piece {

    /**
     * The possible offset this piece can move to
     */
    private val possibleDirections = listOf(
        Direction(RIGHT, 2 * DOWN),
        Direction(2 * RIGHT, DOWN),
        Direction(2 * RIGHT, UP),
        Direction(RIGHT, 2 * UP),
        Direction(LEFT, 2 * UP),
        Direction(2 * LEFT, UP),
        Direction(2 * LEFT, DOWN),
        Direction(LEFT, 2 * DOWN)
    )

    /**
     * @return the piece as a string with the correspondent color
     */
    override fun toString(): String {
        return if (player.isWhite()) "N" else "n"
    }

    /**
     * Gets the piece possible moves
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @param verifyForCheck a [Boolean] indicating wheter the possible moves should take into account the fact that the king is on check(to be able to protect him)
     * @return the list of possible [PieceMove] for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean): List<PieceMove> {
        var moves = getMovesByAddingDirection(possibleDirections, pos, board)
        if(verifyForCheck) moves = moves.filter { !isKingInCheckPostMove(board,it,board.player) }
        return moves
    }


}

/**
 * @property player    the player that owns this piece
 * Represents a bishop piece
 */
data class Bishop (override val player: Player) : Piece {
    /**
     * The possible offset this piece can move to
     */
    private val possibleDirections = listOf(
        Direction(RIGHT, DOWN),
        Direction(RIGHT, UP),
        Direction(LEFT, DOWN),
        Direction(LEFT, UP)
    )

    /**
     * @return the piece as a string with the correspondent color
     */
    override fun toString(): String {
        return if (player.isWhite()) "B" else "b"
    }

    /**
     * Gets the piece possible moves
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @param verifyForCheck a [Boolean] indicating wheter the possible moves should take into account the fact that the king is on check(to be able to protect him)
     * @return the list of possible [PieceMove] for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean): List<PieceMove> = getMoves(possibleDirections, pos, board,verifyForCheck)

}