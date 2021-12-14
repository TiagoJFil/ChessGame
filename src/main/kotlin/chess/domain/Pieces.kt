import chess.domain.*
import chess.domain.board_components.*

/**
 * Represents the possible Colors a player can take
 */
enum class Colors {
    WHITE,
    BLACK;

    operator fun not(): Colors {
        return when (this) {
            WHITE -> BLACK
            BLACK -> WHITE
        }
    }
}

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

    val startSquare = Square(
        findColumn(this[MoveInput.POSITION_FROM_LETTER.index]),
        findRow(this[MoveInput.POSITION_FROM_NUMBER.index])
    )

    val endSquare = Square(
        findColumn(this[MoveInput.POSITION_TO_LETTER.index]),
        findRow(this[MoveInput.POSITION_TO_NUMBER.index])
    )

    return PieceMove(startSquare,endSquare)
}



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
        return player.color == Colors.WHITE
    }


}

/**
 * @param player    the player that owns this piece
 * Represents a pawn piece
 */
class Pawn (override val player: Player) : Piece  {
    private var moved = false

    fun setAsMoved() {
        moved = true
    }

     fun hasMoved(): Boolean {
        return moved
    }

    /**
     * The possible offset this piece can move to
     */
    private val possibleDirections = if(this.player.color == Colors.WHITE) listOf(Pair(0, 1))
                                        else listOf(Pair(0, -1))

    /**
     * @return the piece as a string with the correspondent color
     */
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
    /**
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @return the list of possible directions for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> {
        TODO("Not yet implemented")
    }

    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        TODO("Not yet implemented")
    }
}


/**
 * @param player    the player that owns this piece
 * Represents a king piece
 */
class King (override val player: Player) : Piece {
    private var moved = false

    fun setAsMoved() {
        moved = true
    }

     fun hasMoved(): Boolean {
        return moved
    }

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
        return if (player.isWhite()) "K" else "k"
    }

    /**
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @return the list of possible directions for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> =possibleDirections.mapNotNull {
        val newPos = pos.addDirection(it)
        if (newPos != null) PieceMove(pos, newPos)
        else null
    }

    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        val pieceAtEndSquare = board.getPieceAt(pieceInfo.endSquare)
        if(!hasMoved() && canCastle(board, pieceInfo)) return MoveType.CASTLE
        return when( getPossibleMoves(board, pieceInfo.startSquare).contains(pieceInfo) ){
            false -> MoveType.ILLEGAL
            pieceAtEndSquare == null -> MoveType.REGULAR
            pieceAtEndSquare != null && pieceAtEndSquare.player.color != this.player.color -> MoveType.CAPTURE
            else -> MoveType.ILLEGAL
        }
    }

    /**
     * @param board         the board to check the castle movement on
     * @param pieceInfo     the piece to movement to check
     * Checks if the king can castle
     * @return a [Boolean] value if the king can castle
     */
    private fun canCastle(board: Board,pieceInfo: PieceMove): Boolean {
        val possibleDirections = listOf(
            Pair(1, 0),
            Pair(-1, 0)
        )
        val movesUnfiltered = getMoves(board, pieceInfo.startSquare, possibleDirections)
        val castlemoves = movesUnfiltered.filter { it.endSquare.column.value() - it.startSquare.column.value() == 2 }

        if(!castlemoves.contains(pieceInfo)) return false
        if (castlemoves.isEmpty()) return false

        for (move in castlemoves) {
            val newSquare : Square = if(move.endSquare.column.value() == 6) {
                move.endSquare.addDirection(Pair(1, 0)) ?: return false
            } else {
                move.endSquare.addDirection(Pair(-1, 0)) ?: return false
            }
            val rook = board.getPieceAt(newSquare) ?: return false
            if ( rook is Rook && !rook.hasMoved() && rook.player.color == this.player.color) {
                println("as")
                return true
            }
        }
        return false
    }

}

/**
 * @param player    the player that owns this piece
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
 * @param player    the player that owns this piece
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
 * @param player    the player that owns this piece
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
    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> =  possibleDirections.mapNotNull {
        val newPos = pos.addDirection(it)
        if (newPos != null) PieceMove(pos, newPos)
        else null
    }
    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType = canNormalPieceMove(board, pieceInfo)

}

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
