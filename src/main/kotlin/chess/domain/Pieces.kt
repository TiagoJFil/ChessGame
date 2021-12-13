import chess.domain.MoveType
import chess.domain.Player
import chess.domain.board_components.Square
import chess.domain.board_components.findColumn
import chess.domain.board_components.findRow

/**
 * Represents the possible piece types.
 */
enum class PieceType {
    ROOK,
    KNIGHT,
    BISHOP,
    QUEEN,
    KING,
    PAWN
}

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



/**
 * Converts a char into a PieceType.
 */
fun Char.toPieceType():PieceType{
    return when(this.toLowerCase()){
        'r' -> PieceType.ROOK
        'n' -> PieceType.KNIGHT
        'b' -> PieceType.BISHOP
        'q' -> PieceType.QUEEN
        'k' -> PieceType.KING
        'p' -> PieceType.PAWN
        else -> throw IllegalArgumentException("$this is not a valid piece type")
    }
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

    fun getPossibleMoves(board: Board, pos: Square): List<PieceMove>

    fun canMove(board: Board, pieceInfo: PieceMove): MoveType

    /**
     * @return a [Boolean] value if the piece belongs to the white player(true) or false if it belongs to the black player
     */
    fun belongsToWhitePlayer(): Boolean {
        return player.color == Colors.WHITE
    }
}


class Pawn (override val player: Player) : Piece  {
    private var moved = false

    fun setAsMoved() {
        moved = true
    }

    fun hasMoved(): Boolean {
        return moved
    }

    override fun toString(): String {
        TODO("Not yet implemented")
    }

    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> {
        TODO("Not yet implemented")
    }

    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        TODO("Not yet implemented")
    }
}



class King (override val player: Player) : Piece {
    private var moved = false

    fun setAsMoved() {
        moved = true
    }

    fun hasMoved(): Boolean {
        return moved
    }
    override fun toString(): String {
        TODO("Not yet implemented")
    }

    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> {
        TODO("Not yet implemented")
    }

    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        TODO("Not yet implemented")
    }

}

class Queen (override val player: Player) : Piece {

    override fun toString(): String {
        TODO("Not yet implemented")
    }

    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> {
        TODO("Not yet implemented")
    }

    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        TODO("Not yet implemented")
    }

}

class Rook (override val player: Player) : Piece {
    private var moved = false

    fun setAsMoved() {
        moved = true
    }

    fun hasMoved(): Boolean {
        return moved
    }

    override fun toString(): String {
        TODO("Not yet implemented")
    }

    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> {
        TODO("Not yet implemented")
    }

    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        TODO("Not yet implemented")
    }
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
        return if (player.isWhite()) "n" else "N"
    }

    /**
     * @param pos      the position of the piece
     * @param board    the board where the piece is
     * @return the list of possible directions for the piece
     */
    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> = possibleDirections.mapNotNull {
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
        return when( getPossibleMoves(board, pieceInfo.startSquare).contains(pieceInfo) ){
            pieceAtEndSquare == null -> MoveType.REGULAR
            pieceAtEndSquare != null && pieceAtEndSquare.player.color == !board.getPlayerColor() -> MoveType.CAPTURE
            else -> MoveType.ILLEGAL
        }
    }

}

class Bishop (override val player: Player) : Piece {

    override fun toString(): String {
        TODO("Not yet implemented")
    }

    override fun getPossibleMoves(board: Board, pos: Square): List<PieceMove> {
        TODO("Not yet implemented")
    }

    override fun canMove(board: Board, pieceInfo: PieceMove): MoveType {
        TODO("Not yet implemented")
    }

}
