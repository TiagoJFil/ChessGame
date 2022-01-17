import chess.domain.*
import chess.domain.board_components.*

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
 * @param type    the [PieceType] of the piece
 * @param player  the [Player] corresponding of the piece
 * Represents a chess piece.
 */
sealed interface Piece {
    val player: Player

    /**
     * @return the piece as a string with the correspondent color
     */
    override fun toString(): String

    /**
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @return the list of possible directions for the piece
     */
    fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean): List<PieceMove>

    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    fun canMove(board: Board, pieceInfo: PieceMove): MoveType



    /**
     * @return a [Boolean] value if the piece belongs to the white player(true) or false if it belongs to the black player
     */
    fun belongsToWhitePlayer(): Boolean {
        return player.isWhite()
    }

    fun copy() : Piece {
        return when(this) {
            is Pawn -> {
                this.copy()
            }
            is Rook -> {
                val newRook = Rook(player)
                if(this.hasMoved()) newRook.setAsMoved()
                newRook
            }
            is Knight -> Knight(player)
            is Bishop -> Bishop(player)
            is Queen -> Queen(player)
            is King -> {
                val newKing = King(player)
                if(this.hasMoved()) newKing.setAsMoved()
                newKing
            }
            else -> throw IllegalArgumentException("Piece type not supported")
        }
    }




}

/**
 * @property player    the player that owns this piece
 * Represents a pawn piece
 */
class Pawn (override val player: Player) : Piece  {

    private var moveCount: Int = 0
    private var moved: Boolean = false



    /**
     * Sets a piece as moved
     */
    fun setAsMoved() {
        this.moved = true
    }

    fun moveCounter(){
        if(this.moved) this.moveCount++
    }


    /**
     * @return a [Boolean] value indicating whether the piece has moved or not
     */
     fun hasMoved(): Boolean {
        return this.moved
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
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @return the list of possible directions for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean): List<PieceMove> {
        var moves = listOf<PieceMove>()
        val colorDirection = if (belongsToWhitePlayer())  UP else DOWN
        if(!hasMoved() && board.getPiece(pos.addDirectionNotNull(Direction(0, 1 * colorDirection))) == null && board.getPiece(pos.addDirectionNotNull(Direction(0, 2 * colorDirection))) == null){
            moves += (PieceMove(pos, pos.addDirectionNotNull(Direction(0, 2 * colorDirection))))
        }
        val squareToDiagonalRight = pos.addDirection(Direction(RIGHT,colorDirection))
        val squareToDiagonalLeft = pos.addDirection(Direction(LEFT,colorDirection))

        if(squareToDiagonalLeft != null){
            val piece = board.getPiece(squareToDiagonalLeft)
            if(piece != null && piece.player != this.player){
                moves += (PieceMove(pos, squareToDiagonalLeft))
            }
            if(canEnPassant(board,PieceMove(pos, squareToDiagonalLeft))){
                moves += (PieceMove(pos, squareToDiagonalLeft))
            }
        }

        if(squareToDiagonalRight != null ){
            val piece = board.getPiece(squareToDiagonalRight)
            if(piece != null && piece.player != this.player){
                moves += (PieceMove(pos, squareToDiagonalRight))
            }
            if(canEnPassant(board,PieceMove(pos,squareToDiagonalRight)))
                moves += (PieceMove(pos, squareToDiagonalRight))
        }
        if(pos.addDirection(possibleDirections[0]) != null && board.getPiece(pos.addDirectionNotNull(possibleDirections[0])) == null){
            moves += (PieceMove(pos, pos.addDirectionNotNull(possibleDirections[0])))
        }

        if(verifyForCheck) moves = moves.filter { it.endSquare in board.counterCheckMoves(!board.player)  && !isMyKingInCheckPostMove(board,it) }

        return moves
    }





    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        val pieceAtEndSquare = board.getPiece(pieceInfo.endSquare)


        return when(getPossibleMoves(board, pieceInfo.startSquare,isKingInCheck(board,board.player) ||  isMyKingInCheckPostMove(board,pieceInfo) ).contains(pieceInfo)){
            false -> MoveType.ILLEGAL
            isStalemateAfterMove(board,pieceInfo) -> MoveType.STALEMATE
            canPromote(pieceInfo) -> MoveType.PROMOTION
            isCheckMateAfterMove(board,pieceInfo) -> MoveType.CHECKMATE
            isOpponentKingInCheckAfterMove(board,pieceInfo) -> MoveType.CHECK
            pieceAtEndSquare == null && canEnPassant(board,pieceInfo) -> MoveType.ENPASSANT
            pieceAtEndSquare == null -> MoveType.REGULAR
            pieceAtEndSquare != null && pieceAtEndSquare.player != this.player -> MoveType.CAPTURE

            else -> MoveType.ILLEGAL
        }
    }

    /**
     * Checks whether the piece can perform the enpassant move or not
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
            newPawn.moveCounter()
        }
        return newPawn
    }

}

/**
 * @property player    the player that owns this piece
 * Represents a king piece
 */
data class King (override val player: Player) : Piece {

    private var moved = false

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
     * The possible offset this piece can move to
     */
    private val possibleDirections = listOf(
        Direction(RIGHT,0),
        Direction(LEFT ,0),
        Direction(0    ,DOWN),
        Direction(0    ,UP),
        Direction(RIGHT,DOWN),
        Direction(RIGHT,UP),
        Direction(LEFT ,DOWN),
        Direction(LEFT ,UP)
    )

    /**
     * @return the piece as a string with the correspondent color
     */
    override fun toString(): String {
        return if (player.isWhite()) "K" else "k"
    }


    /**
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @return the list of possible directions for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean ): List<PieceMove> {
        var moves = getMovesByAddingDirection(possibleDirections, pos, board)

        if ( !this.hasMoved() && canCastle(board, PieceMove(pos, pos.addDirectionNotNull(Direction(2 * RIGHT, 0))))) {
            moves = moves + (PieceMove(pos, pos.addDirectionNotNull(Direction(2 * RIGHT, 0))))
        }
        if( !this.hasMoved() && canCastle(board, PieceMove(pos, pos.addDirectionNotNull(Direction(2 * LEFT, 0))))) {
            moves = moves + (PieceMove(pos, pos.addDirectionNotNull(Direction(LEFT, 0))))
        }

        if(verifyForCheck) moves = moves.filter { !isMyKingInCheckPostMove(board,it) }



        return moves
    }


    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        val pieceAtEndSquare = board.getPiece(pieceInfo.endSquare)
        if (canCastle(board, pieceInfo)) return MoveType.CASTLE

        return when ( getPossibleMoves(board, pieceInfo.startSquare, true ).contains(pieceInfo)) {
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
     * @param board         the board to check the castle movement on
     * @param pieceInfo     the piece to movement to check
     * Checks if the king can castle
     * @return a [Boolean] value if the king can castle
     */
    private fun canCastle(board: Board, pieceInfo: PieceMove): Boolean {

        if (hasMoved()) return false
        val possibleDirections = listOf(
            Direction(RIGHT, 0),
            Direction(LEFT, 0)
        )
        val possibleMovesUnfiltered = getMoves(possibleDirections, pieceInfo.startSquare, board,false)
        val possibleCastleMoves = possibleMovesUnfiltered.filter { it.endSquare.column.number - it.startSquare.column.number == 2 }

        if (!possibleCastleMoves.contains(pieceInfo)) return false
        if (possibleCastleMoves.isEmpty()) return false

        for (move in possibleCastleMoves) {
            val newSquare: Square =
                if (move.endSquare.column.number == G_COLUMN_NUMBER) {
                    move.endSquare.addDirection(Direction(RIGHT, 0)) ?: return false
                } else {
                    move.endSquare.addDirection(Direction(LEFT, 0)) ?: return false
                }
            val rook = board.getPiece(newSquare) ?: return false
            if (rook is Rook && !rook.hasMoved() && rook.player == this.player) {
                return true
            }
        }
        return false
    }




}

/**
 * @property player    the player that owns this piece
 * Represents a queen piece
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
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @return the list of possible directions for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean): List<PieceMove> = getMoves(possibleDirections, pos, board,verifyForCheck)

    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType = canNormalPieceMove(board, pieceInfo)

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
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @return the list of possible directions for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean ): List<PieceMove> = getMoves(possibleDirections, pos, board,verifyForCheck)

    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType = canNormalPieceMove(board, pieceInfo)

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
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @return the list of possible directions for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean): List<PieceMove> {

        var moves = getMovesByAddingDirection(possibleDirections, pos, board)
        if(verifyForCheck) moves = moves.filter {  it.endSquare in board.counterCheckMoves(!board.player)  && !isMyKingInCheckPostMove(board,it) }
        return moves
    }

    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType = canNormalPieceMove(board, pieceInfo)

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
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @return the list of possible moves for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square, verifyForCheck : Boolean): List<PieceMove> = getMoves(possibleDirections, pos, board,verifyForCheck)


    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType = canNormalPieceMove(board, pieceInfo)

}

