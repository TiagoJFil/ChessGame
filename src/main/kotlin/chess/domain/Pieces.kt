import chess.GameName
import chess.Storage.DataBase
import chess.domain.*
import chess.domain.board_components.*
import java.lang.Math.abs

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
        return player.color == Colors.WHITE
    }


}

/**
 * @property player    the player that owns this piece
 * Represents a pawn piece
 */
class Pawn (override val player: Player) : Piece  {

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
    private val possibleDirections = if(this.player.color == Colors.WHITE) listOf(Pair(0, -1))
                                        else listOf(Pair(0, 1))

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
        moves += if (this.hasMoved()) possibleDirections.mapNotNull {
            val newPos = pos.addDirection(it)
            if (newPos != null) PieceMove(pos, newPos)
            else null
        } else{
            val tempDir = possibleDirections + possibleDirections.map { it.first * 2 to it.second * 2 }
            tempDir.mapNotNull {
                val newPos = pos.addDirection(it)
                if (newPos != null) PieceMove(pos, newPos)
                else null
            }
        }

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

        return moves
    }


    /**
     * @param board         the board to check the movement on
     * @param pieceInfo     the piece to movement to check
     * @return the [MoveType] of the piece
     */
    override fun canMove(board: Board, pieceInfo: PieceMove, lastMove: PieceMove): MoveType {
        val pieceAtEndSquare = board.getPieceAt(pieceInfo.endSquare)

        if(canEnPassant(board = board, startSquare = pieceInfo.startSquare, lastMove).first ||
            canEnPassant(board = board, startSquare = pieceInfo.startSquare, lastMove).second)
            return MoveType.ENPASSANT

        return when( getPossibleMoves(board, pieceInfo.startSquare).contains(pieceInfo) ){
            false -> MoveType.ILLEGAL
            pieceAtEndSquare == null -> MoveType.REGULAR
            pieceAtEndSquare != null && pieceAtEndSquare.player.color != this.player.color -> MoveType.CAPTURE
            else -> MoveType.ILLEGAL
        }
    }

    //Par de bools, first - enpassant para esquerda, second - enpassant para direita
    fun canEnPassant(board: Board, startSquare: Square, lastMove: PieceMove): Pair<Boolean,Boolean> {
        val player = board.getPlayerColor()

        val columnToRight = (startSquare.column.value()+1).toColumn()
        val columnToLeft = (startSquare.column.value()-1).toColumn()

        val rightSquare =  Square(column = columnToRight,row = startSquare.row)
        val leftSquare = Square(column = columnToLeft,row = startSquare.row)

        val pieceAtRight = board.getPieceAt(position = rightSquare) == Pawn(player = Player(color = !player))
        val pieceAtLeft = board.getPieceAt(position = leftSquare) == Pawn(player = Player(color = !player))

        val enPassantRight = (kotlin.math.abs(lastMove.startSquare.column.value() - rightSquare.column.value()) == 0) &&
                (kotlin.math.abs(lastMove.startSquare.row.value()-rightSquare.row.value()) == 2)
        val enPassantLeft = (kotlin.math.abs(lastMove.startSquare.column.value() - leftSquare.column.value()) == 0) &&
                (kotlin.math.abs(lastMove.startSquare.row.value()-leftSquare.row.value()) == 2)

        return Pair(enPassantLeft && pieceAtLeft, enPassantRight && pieceAtRight)
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

        var moves = possibleDirections.mapNotNull {
            val newPos = pos.addDirection(it)
            if (newPos != null) PieceMove(pos, newPos)
            else null
        }
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
        val pieceAtEndSquare = board.getPieceAt(pieceInfo.endSquare)
        if (!hasMoved() && canCastle(board, pieceInfo)) return MoveType.CASTLE
        return when (getPossibleMoves(board, pieceInfo.startSquare).contains(pieceInfo)) {
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
    private fun canCastle(board: Board, pieceInfo: PieceMove): Boolean {
        val possibleDirections = listOf(
            Pair(1, 0),
            Pair(-1, 0)
        )
        val movesUnfiltered = getMoves(board, pieceInfo.startSquare, possibleDirections)
        val castlemoves = movesUnfiltered.filter { it.endSquare.column.value() - it.startSquare.column.value() == 2 }

        if (!castlemoves.contains(pieceInfo)) return false
        if (castlemoves.isEmpty()) return false

        for (move in castlemoves) {
            val newSquare: Square = if (move.endSquare.column.value() == 6) {
                move.endSquare.addDirection(Pair(1, 0)) ?: return false
            } else {
                move.endSquare.addDirection(Pair(-1, 0)) ?: return false
            }
            val rook = board.getPieceAt(newSquare) ?: return false
            if (rook is Rook && !rook.hasMoved() && rook.player.color == this.player.color) {
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
