import chess.domain.*
import chess.domain.board_components.*

private const val LEFT = -1
private const val RIGHT = 1
private const val UP = -1
private const val DOWN = 1


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
    fun getPossibleMoves(board: Board, pos: Square): List<PieceMove>

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


}

/**
 * @property player    the player that owns this piece
 * Represents a pawn piece
 */
class Pawn (override val player: Player) : Piece  {

    private var moveCount = 0
    private var moved = false

    /**
     * Sets a piece as moved
     */
    fun setAsMoved() {
        moved = true
        moveCount++
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
    private val possibleDirections = if(this.player.isWhite()) listOf(Direction(0, UP))
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
    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> {
        val moves = mutableListOf<PieceMove>()
        val colorDirection = if (belongsToWhitePlayer())  UP else DOWN
        if(!hasMoved() && board.getPiece(pos.addDirectionNotNull(Direction(0, 2 * colorDirection))) == null){
            moves.add(PieceMove(pos, pos.addDirectionNotNull(Direction(0, 2 * colorDirection))))
        }
        val squareToDiagonalRight = pos.addDirection(Direction(LEFT,colorDirection))
        val squareToDiagonalLeft = pos.addDirection(Direction(RIGHT,colorDirection))

        if(squareToDiagonalLeft != null ){
            val piece = board.getPiece(squareToDiagonalLeft)
            if(piece != null && piece.player != this.player){
                moves.add(PieceMove(pos, squareToDiagonalLeft))
            }
            if(canEnpassant(board,PieceMove(pos, squareToDiagonalLeft))){
                moves.add(PieceMove(pos, squareToDiagonalLeft))
            }
        }

        if(squareToDiagonalRight != null ){
            val piece = board.getPiece(squareToDiagonalRight)
            if(piece != null && piece.player != this.player){
                moves.add(PieceMove(pos, squareToDiagonalRight))
            }
            if(canEnpassant(board,PieceMove(pos,squareToDiagonalRight )))
                moves.add(PieceMove(pos, squareToDiagonalRight))
        }

        return moves + getMovesByAddingDirection(possibleDirections, pos, board)
    }


    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        val pieceAtEndSquare = board.getPiece(pieceInfo.endSquare)
        if(isCheckMate(board,player)) return MoveType.CHECKMATE
        return when(getPossibleMoves(board, pieceInfo.startSquare).contains(pieceInfo)){
            false -> MoveType.ILLEGAL
            canPromote(pieceInfo) -> MoveType.PROMOTION
            pieceAtEndSquare == null && canEnpassant(board,pieceInfo) -> MoveType.ENPASSANT
            pieceAtEndSquare == null -> MoveType.REGULAR
            pieceAtEndSquare != null && pieceAtEndSquare.player != this.player -> MoveType.CAPTURE
            else -> MoveType.ILLEGAL
        }
    }

    /**
     * Checks whether the piece can perform the enpassant move or not
     * @param board        the board to check the movement on
     * @param pieceInfo    the piece to movement to check
     * @return a [Boolean] value indicating whether the piece can perform enpassant
     */
    private fun canEnpassant(board: Board, pos: PieceMove): Boolean{

        val rowAdd = if(player.isWhite()) UP else DOWN
        val leftPos = pos.startSquare.addDirection(Direction(LEFT,0))
        val rightPos = pos.startSquare.addDirection(Direction(RIGHT,0))
        if(leftPos != null){
            val leftPiece = board.getPiece(leftPos)

            val bellowLeftPiece = leftPos.addDirection(Direction(0,rowAdd))
            if(pos.endSquare == bellowLeftPiece)
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
        if(pieceInfo.endSquare.row.number == PAWN_PROMOTION_ROW_BLACK || pieceInfo.endSquare.row.number == PAWN_PROMOTION_ROW_WHITE  )
            return true
        return false
    }

    fun traceBackPawn(endPos:String, board: Board){
        TODO("Not yet implemented")
    }


}

/**
 * @property player    the player that owns this piece
 * Represents a king piece
 */
class King (override val player: Player) : Piece {

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
        Pair(RIGHT,0),
        Pair(LEFT ,0),
        Pair(0    ,DOWN),
        Pair(0    ,UP),
        Pair(RIGHT,DOWN),
        Pair(RIGHT,UP),
        Pair(LEFT ,DOWN),
        Pair(LEFT ,UP)
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
    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> {
        var moves = getMovesByAddingDirection(possibleDirections, pos, board)
        if (canCastle(board, PieceMove(pos, pos.addDirectionNotNull(Pair(2 * RIGHT, 0))))) {
            moves += listOf(PieceMove(pos, pos.addDirectionNotNull(Pair(2 * RIGHT, 0))), PieceMove(pos, pos.addDirectionNotNull(Pair(2 * LEFT, 0))))
        }
        if(canCastle(board, PieceMove(pos, pos.addDirectionNotNull(Pair(2 * LEFT, 0))))) {
            moves += PieceMove(pos, pos.addDirectionNotNull(Pair(RIGHT, 0)))
        }
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
        return when (getPossibleMoves(board, pieceInfo.startSquare).contains(pieceInfo)) {
            false -> MoveType.ILLEGAL
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
            Pair(RIGHT, 0),
            Pair(LEFT, 0)
        )
        val possibleMovesUnfiltered = getMoves(board, pieceInfo.startSquare, possibleDirections)
        val possibleCastleMoves = possibleMovesUnfiltered.filter { it.endSquare.column.number - it.startSquare.column.number == 2 }

        if (!possibleCastleMoves.contains(pieceInfo)) return false
        if (possibleCastleMoves.isEmpty()) return false

        for (move in possibleCastleMoves) {
            val newSquare: Square =
                if (move.endSquare.column.number == G_COLUMN_NUMBER) {
                    move.endSquare.addDirection(Pair(RIGHT, 0)) ?: return false
                } else {
                    move.endSquare.addDirection(Pair(LEFT, 0)) ?: return false
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
class Queen (override val player: Player) : Piece {

    /**
     * The possible offset this piece can move to
     */
    private val possibleDirections = listOf(
        Pair(RIGHT, 0),
        Pair(0    , DOWN),
        Pair(LEFT , 0),
        Pair(0    , UP),
        Pair(RIGHT, DOWN),
        Pair(RIGHT, UP),
        Pair(LEFT , DOWN),
        Pair(LEFT , UP)
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
    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> = getMoves(board, pos, possibleDirections)

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
class Rook (override val player: Player) : Piece {

    private var moved = false

    /**
     * The possible offset this piece can move to
     */
    private val possibleDirections = listOf(
        Pair(RIGHT, 0),
        Pair(0    , DOWN),
        Pair(LEFT , 0),
        Pair(0    , UP)
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
    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> = getMoves(board, pos, possibleDirections)

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
class Knight (override val player: Player) : Piece {

    /**
     * The possible offset this piece can move to
     */
    private val possibleDirections = listOf(
        Pair(RIGHT, 2 * DOWN),
        Pair(2 * RIGHT, DOWN),
        Pair(2 * RIGHT, UP),
        Pair(RIGHT, 2 * UP),
        Pair(LEFT, 2 * UP),
        Pair(2 * LEFT, UP),
        Pair(2 * LEFT, DOWN),
        Pair(LEFT, 2 * DOWN)
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
    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> =  getMovesByAddingDirection(possibleDirections, pos, board)

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
class Bishop (override val player: Player) : Piece {
    /**
     * The possible offset this piece can move to
     */
    private val possibleDirections = listOf(
        Pair(RIGHT, DOWN),
        Pair(RIGHT, UP),
        Pair(LEFT, DOWN),
        Pair(LEFT, UP)
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
    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> = getMoves(board, pos, possibleDirections)


    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType = canNormalPieceMove(board, pieceInfo)

}

