import chess.Storage.Move
import chess.domain.PieceMove
import chess.domain.Player
import chess.domain.board_components.Square
import chess.domain.board_components.toRow
import chess.domain.board_components.toSquare
import chess.domain.formatToPieceMove


const val BOARD_SIZE = 8
const val MIN_X_LETTER = 'a'
const val MAX_X_LETTER = 'h'
const val MIN_Y_NUMBER = 1
const val MAX_Y_NUMBER = 8
const val MIN_ROW_NUMBER = 0
const val MAX_ROW_NUMBER = 7
private const val PAWN_PROMOTION_DEFAULT_PIECE_LETTER = 'q'
private val PAWN_POSSIBLE_PROMOTIONS = listOf('q', 'r', 'b', 'n')

typealias BoardList = List<Piece?>

data class Board internal constructor(

    private val board: BoardList = List<Piece?>(BOARD_SIZE * BOARD_SIZE) {
        when(it){
            in 0..7 -> makePiecesRow(Player.BLACK)[it]
            in 8..15 -> makePawnRow(Player.BLACK)[it - 8]
            in 48..55 -> makePawnRow(Player.WHITE)[it - 48]
            in 56..63 -> makePiecesRow(Player.WHITE)[it - 56]
            else -> null
        }
                                                                         },
    val player: Player = Player.WHITE
) {
    companion object {
        operator fun invoke() = Board()
    }

    /**
     * Converts the board into a String representation.
     */
    override fun toString(): String {
        var string = ""
        board.forEach {
            string += it?.toString() ?: " "
        }
        return string
    }

    /**
     * @return the current player color.
     */
    fun getPlayerColor(): Player {
        return player
    }

    /**
     * @param startPos the [Square] of the piece
     * @return the piece on the given coordinates or null if there is no piece at the given coordinates.
     */
    fun getPiece(at: Square): Piece? = board[at.toIndex()]


    /**
     * @param coordinates the [Coordinates] where we check if there is a piece
     * @return true if there is a piece on the given coordinates, false otherwise.
     */
    fun hasPiece(at: Square): Boolean = board[at.toIndex()] != null

    /**
     * @param currentPlayer color of the king we are looking for
     * @return the square were the king is positioned
     * Finds the king of the given color
     */
    fun getKingSquare(player: Player): Square {
        return board.indexOfFirst{ it is King && it.player == player }.toSquare()
    }

    /**
     * @param player current player's color
     * @return the king piece
     */
    fun getKingPiece(player: Player):Piece{
        val king = board.find {it is King && it.player == player}
        checkNotNull(king)
        return king
    }

    /**
     * @param move the [Move] we want to make
     * @return the [Board] after the move
     * Makes a move on the board and changes the player turn
     */
    fun makeMove(move: String): Board {
        val pieceMovement = move.formatToPieceMove()
        val piece =getPiece(pieceMovement.startSquare) ?: throw IllegalStateException("no piece at startingSquare")
        when(piece) {
            is Pawn -> piece.setAsMoved()
            is King -> piece.setAsMoved()
            is Rook -> piece.setAsMoved()
        }
        val newBoard : BoardList = board.mapIndexed{index, it ->
            when(index){
                pieceMovement.startSquare.toIndex() -> null
                pieceMovement.endSquare.toIndex() -> piece
                else -> it
            }
        }

        return Board(newBoard, !player)
    }




    fun asList(): List<Piece?> {
        return board
    }

}


/**
 * @param square the Square where the piece to be promoted is placed
 * @return true if the piece is a promoted or false if it isn't
 */
fun Board.promotePieceAndMove(move: String, promotionType: Char = PAWN_PROMOTION_DEFAULT_PIECE_LETTER) : Board{
    val square = move.substring(1,2).toSquare()
    val piece = getPiece(square) ?: throw IllegalStateException("No piece at $square")
    if (piece !is Pawn) throw IllegalStateException("Can't promote a non pawn piece")
    else {
        val newPiece = when (promotionType.toLowerCase()) {
            'q' -> Queen(piece.player)
            'r' -> Rook(piece.player)
            'b' -> Bishop(piece.player)
            'n' -> Knight(piece.player)
            else -> throw IllegalStateException("Invalid promotion type")
        }
        val newBoardList = this.asList().mapIndexed { index, it ->
            if (index == square.toIndex()) newPiece
            else it
        }
        return Board(newBoardList, player).makeMove(move)
    }
}

/**
 * @param board         The board to perform the castling on
 * @param move          The move received from the user to perform the castling.
 * @returns     a List of Moves that happened during the castling.
 * Moves the king and the rook to the correct position.
 */
fun Board.doCastling(move: Move): Pair<Board,List<Move>> {
//TODO("CHANGE THE MOVE RECEIVED TO BE A PIECEMOVE")
    val rookColumnLetter = if (move.move[3] == 'c') MIN_X_LETTER else MAX_X_LETTER


    val rookStartPos = move.move.substring(1..2)
    val kingStartPos = rookColumnLetter + move.move[2].toString()
    val newRookPos = if (rookColumnLetter == MIN_X_LETTER) "d" + move.move[2] else "f" + move.move[2]
    val newKingPos = if (rookColumnLetter == MIN_X_LETTER) "c" + move.move[2] else "g" + move.move[2]

    val kingMove = "K$kingStartPos$newKingPos"
    val rookMove = "R$rookStartPos$newRookPos"

    val newBoard = this.makeMove(kingMove).makeMove(rookMove).asList()


    val moves = mutableListOf<Move>()
    moves.add(Move(kingMove))
    moves.add(Move(rookMove))

    return Pair(Board(newBoard, !player),moves)
}


/**
 * Does en passant movement from the given [PieceMove]
 * @param pieceMove     The piece movement to be performed
 * @return              The new board after the movement
 */
fun Board.doEnpassant(pieceMove: PieceMove): Board {
    val piece = getPiece(pieceMove.startSquare) ?: throw IllegalStateException("No piece at ${pieceMove.startSquare}")

    val dir = if (piece.player == Player.WHITE) UP else DOWN
    val eatenPieceEndSquare = (pieceMove.endSquare.row.number - dir).toRow()
    val endSquareColumn = pieceMove.endSquare.column

    val eatenPieceSquare = Square(endSquareColumn,eatenPieceEndSquare)
    val newBoard = this.asList().mapIndexed{index, it ->
        when(index){
            eatenPieceSquare.toIndex() -> null
            pieceMove.startSquare.toIndex() -> null
            pieceMove.endSquare.toIndex() -> piece

            else -> it
        }
    }
    return Board(newBoard, !player)
}

/**
 * Checks whether the player is moving the own piece or the opponent's
 * @param move the [Move] we want to make
 * @return true if the player is moving his own piece, false otherwise
 */
fun Board.isPlayerMovingOwnPieces(move: PieceMove): Boolean {

    val startingSquare = Square(move.startSquare.column, move.startSquare.row)
    val pieceAtStart = getPiece(startingSquare) ?: return false

    return pieceAtStart.player == getPlayerColor()
}

/**
 * Checks whether the player is moving the the same piece time of piece as he is trying to move
 *
 */
fun Board.isPlayerMovingTheRightPieces(move: String): Boolean {
    val pieceMovement = move.formatToPieceMove()
    val startingSquare = Square(pieceMovement.startSquare.column, pieceMovement.startSquare.row)
    val pieceAtStart = getPiece(startingSquare) ?: return false

    return pieceAtStart.toString().equals((move[0]).toString(), ignoreCase = true)
}


fun Board.getAllMoves(player: Player): List<PieceMove> {
    val list = mutableListOf<PieceMove>()
    this.asList().forEachIndexed { idx, p ->
        if(p != null && p.player == player)
            list.addAll(p.getPossibleMoves(this,idx.toSquare()))
    }
    return list
}

/**
 * Checks whether the square is occupied by a piece of the player given at a square
 */
fun Square.doesBelongTo(player: Player,board: Board): Boolean {

    val piece = board.getPiece(this)

    return (piece != null) && (piece.player == player)
}

/**
 * Checks whether the square is not occupied by a piece of the player given at a square
 */
fun Square.doesNotBelongTo(player: Player, board: Board): Boolean {
    return !this.doesBelongTo(player,board)
}


/**
 * @param player    The player that owns the pieces
 * Creates a list with the first 8 pieces of the player
 */
private fun makePiecesRow(player: Player) = listOf(
    Rook(player),
    Knight(player),
    Bishop(player),
    Queen(player),
    King(player),
    Bishop(player),
    Knight(player),
    Rook(player)
)

/**
 * @param player    The player that owns the pieces
 * Creates a list with the player's pawns
 */
private fun makePawnRow(player: Player) = listOf(
    Pawn(player),
    Pawn(player),
    Pawn(player),
    Pawn(player),
    Pawn(player),
    Pawn(player),
    Pawn(player),
    Pawn(player)
)



