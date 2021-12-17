import chess.domain.*
import chess.domain.board_components.*



/**
 * @param startSquare       the square where the piece is placed
 * @param endSquare         the square where the piece is moved to.
 * Represents the piece movement.
 */
data class PieceMove(val startSquare: Square, val endSquare: Square)


private enum class MoveInput(val index : Int){
    POSITION_FROM_LETTER(1),
    POSITION_FROM_NUMBER(2),
    POSITION_TO_LETTER(3),
    POSITION_TO_NUMBER(4)
}

/**
 * receives the move input and transforms to a data class of coordinates
 * @param String  input to make move ex: Pe2e4
 * @return  the Movement as a data class [PieceMove]
 **/
fun String.formatToPieceMove(): PieceMove{

    val startSquare = Square((this[MoveInput.POSITION_FROM_LETTER.index]).toColumn(), (this[MoveInput.POSITION_FROM_NUMBER.index]).toRow())

    val endSquare = Square((this[MoveInput.POSITION_TO_LETTER.index]).toColumn(), (this[MoveInput.POSITION_TO_NUMBER.index]).toRow())

    return PieceMove(startSquare,endSquare)
}


/**
 *
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

    private var count = 0

    private var moved = false

    /**
     * Sets a piece as moved
     */
    fun setAsMoved() {
        moved = true
        count++
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
    private val possibleDirections = if(this.player.isWhite()) listOf(Direction(0, -1))
                                        else listOf(Direction(0, 1))


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
        var moves = listOf<PieceMove>()
        val colorDirection =if (this.belongsToWhitePlayer())  -1 else 1
        if(!this.hasMoved() ){
            moves = moves.plus(PieceMove(pos, pos.addDirectionNotNull(Direction(0, 2 * colorDirection))))
        }
        val squareToDiagonalRight = pos.addDirection(Direction(1,colorDirection))
        val squareToDiagonalLeft = pos.addDirection(Direction(-1,colorDirection))

        if(squareToDiagonalLeft != null ){
            val piece = board.getPiece(squareToDiagonalLeft)
            if(piece != null && piece.player != this.player){
                moves = moves.plus(PieceMove(pos, squareToDiagonalLeft))
            }
            if(checkEnpassant(board,PieceMove(pos, squareToDiagonalLeft))){
                moves = moves.plus(PieceMove(pos, squareToDiagonalLeft))
            }
        }

        if(squareToDiagonalRight != null ){
            val piece = board.getPiece(squareToDiagonalRight)
            if(piece != null && piece.player != this.player){
                moves = moves.plus(PieceMove(pos, squareToDiagonalRight))
            }
            if(checkEnpassant(board,PieceMove(pos,squareToDiagonalRight )))
                moves = moves.plus(PieceMove(pos, squareToDiagonalRight))
        }

        return moves + getMovesByAddingDirection(possibleDirections, pos)


        /*
        var moves = listOf<PieceMove>()
        if (this.hasMoved()) moves += possibleDirections.mapNotNull {
            val newPos = pos.addDirection(it)
            if (newPos != null) PieceMove(pos, newPos)
            else null
        }else{
            val tempDir = possibleDirections + possibleDirections.map { it.first * 2 to it.second * 2 }
            moves += tempDir.mapNotNull {
                val newPos = pos.addDirection(it)
                if (newPos != null) PieceMove(pos, newPos)
                else null
            }
        }
        */
        //FALTA ADICIONAR OS MOVIMENTOS DE ATAQUE ______________________________________________________________________________
        /*
        moves += possibleDirections.mapNotNull {

            if (it.second == -1) {
                val newPos = pos.addDirection(Direction(it.first - 1, it.second))
                val pieceAtPos: Piece?
                if (newPos != null) {
                    pieceAtPos = board.getPieceAt(newPos)
                    if (pieceAtPos != null && pieceAtPos.player.color != this.player.color) {
                        PieceMove(pos, newPos)
                    }
                } else null

            }
        }*/

    }


    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        TODO("MISSING PROMOTION FUNCTIONS")
        val pieceAtEndSquare = board.getPiece(pieceInfo.endSquare)

        return when(getPossibleMoves(board, pieceInfo.startSquare).contains(pieceInfo)){
            false -> MoveType.ILLEGAL
            pieceAtEndSquare == null && checkEnpassant(board,pieceInfo) -> MoveType.ENPASSANT
            pieceAtEndSquare == null -> MoveType.REGULAR
            pieceAtEndSquare != null && pieceAtEndSquare.player != this.player -> MoveType.CAPTURE
            else -> MoveType.ILLEGAL
        }
    }

    private fun checkEnpassant(board: Board, pos: PieceMove ): Boolean{

        val rowAdd = if(player.isWhite()) -1 else 1
        val leftPos = pos.startSquare.addDirection(Direction(-1,0))
        val rightPos = pos.startSquare.addDirection(Direction(1,0))
        if(leftPos != null){
            val leftPiece = board.getPiece(leftPos)

            val bellowLeftPiece = leftPos.addDirection(Direction(0,rowAdd))
            if(pos.endSquare == bellowLeftPiece)
                return (leftPiece is Pawn && leftPiece.count == 1 && leftPiece.player != player)
        }
        if(rightPos != null){
            val rightPiece = board.getPiece(rightPos)

            val bellowRightPiece = rightPos.addDirection(Direction(0,rowAdd))
            if(pos.endSquare == bellowRightPiece)
                return (rightPiece is Pawn && rightPiece.count == 1 && rightPiece.player != player)
        }

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
        Pair( 1, 0),
        Pair( 0, 1),
        Pair(-1, 0),
        Pair( 0, -1),
        Pair( 1, 1),
        Pair( 1,-1),
        Pair(-1, 1),
        Pair(-1,-1)
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


        var moves = getMovesByAddingDirection(possibleDirections, pos)
        if (!hasMoved() && canCastle(board, PieceMove(pos, pos.addDirectionNotNull(Pair(2, 0))))) {
            moves += listOf(PieceMove(pos, pos.addDirectionNotNull(Pair(2, 0))), PieceMove(pos, pos.addDirectionNotNull(Pair(-2, 0))))
        }
        if(!hasMoved() && canCastle(board, PieceMove(pos, pos.addDirectionNotNull(Pair(-2, 0))))) {
            moves += PieceMove(pos, pos.addDirectionNotNull(Pair(1, 0)))
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
        if (!hasMoved() && canCastle(board, pieceInfo)) return MoveType.CASTLE
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
        val possibleDirections = listOf(
            Pair(1, 0),
            Pair(-1, 0)
        )
        val movesUnfiltered = getMoves(board, pieceInfo.startSquare, possibleDirections)
        val castlemoves = movesUnfiltered.filter { it.endSquare.column.number - it.startSquare.column.number == 2 }

        if (!castlemoves.contains(pieceInfo)) return false
        if (castlemoves.isEmpty()) return false

        for (move in castlemoves) {
            val newSquare: Square = if (move.endSquare.column.number == 6) {
                move.endSquare.addDirection(Pair(1, 0)) ?: return false
            } else {
                move.endSquare.addDirection(Pair(-1, 0)) ?: return false
            }
            val rook = board.getPiece(newSquare) ?: return false
            if (rook is Rook && !rook.hasMoved() && rook.player == this.player) {
                println("as")
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
        Pair(1, 0),
        Pair(0, 1),
        Pair(-1, 0),
        Pair(0, -1),
        Pair( 1, 1),
        Pair( 1,-1),
        Pair(-1, 1),
        Pair(-1,-1)

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
        Pair(1, 0),
        Pair(0, 1),
        Pair(-1, 0),
        Pair(0, -1)
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
        return if (player.isWhite()) "N" else "n"
    }

    /**
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @return the list of possible directions for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> =  getMovesByAddingDirection(possibleDirections, pos)
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
        Pair( 1, 1),
        Pair( 1,-1),
        Pair(-1, 1),
        Pair(-1,-1)
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
