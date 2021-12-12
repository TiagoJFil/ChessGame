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
interface Piece {
    val player: Player
    //var moved: Boolean

    /**
     * @return the piece as a string with the correspondent color
     */
    override fun toString(): String

    fun getPossibleMoves(board: Board, pos: Square): List<PieceMove>

    fun canMove(board: Board, pieceMove: PieceMove): MoveType

    /*
    override fun toString(): String {
        val pieceString = when (this.type) {
            PieceType.ROOK -> "r"
            PieceType.KNIGHT -> "n"
            PieceType.BISHOP -> "b"
            PieceType.QUEEN -> "q"
            PieceType.KING -> "k"
            PieceType.PAWN -> "p"

        }
        return if (player.color == Colors.WHITE) pieceString.toUpperCase()
        else pieceString
    }
*/
    /**
     * @return a [Boolean] value if the piece belongs to the white player(true) or false if it belongs to the black player
     */
    fun belongsToWhitePlayer(): Boolean {
        return player.color == Colors.WHITE
    }

    /*
    /**
     * @return a [Boolean] value indicating if the piece has moved(true) or false if it hasn't
     */
    fun hasMoved(): Boolean {
        return moved
    }

    /**
     * Sets the [moved] property of a piece as moved.
     */
    fun setAsMoved() {
        moved = true
    }

*/
}







