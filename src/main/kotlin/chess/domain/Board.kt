import chess.domain.PieceMove
import chess.domain.Player
import chess.domain.board_components.*
import chess.domain.formatToPieceMove



const val BOARD_SIZE = 8
const val MIN_X_LETTER = 'a'
const val MAX_X_LETTER = 'h'
const val MIN_Y_NUMBER = 1
const val MAX_Y_NUMBER = 8
const val MIN_ROW_NUMBER = 0
const val MAX_ROW_NUMBER = 7
private const val PAWN_PROMOTION_DEFAULT_PIECE_LETTER = 'q'

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
     * @param startPos the [Square] of the piece
     * @return the piece on the given coordinates or null if there is no piece at the given coordinates.
     */
    fun getPiece(at: Square): Piece? = board[at.toIndex()]


    /**
     * @param currentPlayer color of the king we are looking for
     * @return the square were the king is positioned
     * Finds the king of the given color
     */
    fun getKingSquare(player: Player): Square {
        val idx = this.board.indexOfFirst { it is King && it.player == player }
        if(idx == -1) throw IllegalStateException("No king found for player $player")
        return idx.toSquare()
    }

    /**
     * @param player current player's color
     * @return the king piece
     */
    fun getKingPiece(player: Player): Piece {
        val king = board.find { it is King && it.player == player }
        checkNotNull(king)
        return king
    }


    /**
     * @param move the movement we want to make
     * @return the [Board] after the move
     * Makes a move on the board and changes the player turn
     */
    fun makeMove(move: String): Board {
        val pieceMovement = move.formatToPieceMove()
        val oldPiece = getPiece(pieceMovement.startSquare) ?: throw IllegalStateException("no piece at startingSquare")
        val newPiece = oldPiece.copy()

        when (newPiece) {
            is Pawn -> {
                newPiece.setAsMoved()
                newPiece.increaseMoveCounter()  //its needed here because this piece wont get affected by the if on the map
            }
            is King -> newPiece.setAsMoved()
            is Rook -> newPiece.setAsMoved()
        }

        val newBoard: BoardList = board.mapIndexed { index, it ->

            when (index) {
                pieceMovement.startSquare.toIndex() -> null
                pieceMovement.endSquare.toIndex() -> newPiece
                else -> {
                    val piece = it?.copy()
                    if (piece is Pawn) piece.increaseMoveCounter()
                    piece
                }
            }
        }

        return Board(newBoard, !player)
    }


    /**
     * @param move      the movement we want to make
     * @param promotionType  the type of the promotion
     * @return the [Board] after the promotion
     */
    fun moveAndPromotePiece(move: String, promotionType: Char = PAWN_PROMOTION_DEFAULT_PIECE_LETTER) : Board{
        val square = move.substring(1,3).toSquare()
        val piece = getPiece(square) ?: throw IllegalStateException("No piece at $square")
        if (piece !is Pawn) throw IllegalStateException("Can't promote a non pawn piece")
        else {
            val newPiece = when (promotionType.lowercaseChar()) {
                'q' -> Queen(piece.player)
                'r' -> Rook(piece.player)
                'b' -> Bishop(piece.player)
                'n' -> Knight(piece.player)
                else -> throw IllegalArgumentException("Invalid promotion type")
            }
            val newBoardList = this.asList().mapIndexed { index, it ->
                if (index == square.toIndex()) newPiece
                else it
            }
            return Board(newBoardList, player).makeMove(move)
        }
    }

    /**
     * @param move          The move received from the user to perform the castling.
     * @returns            The [Board] after the castling.
     * Moves the king and the rook to the correct position.
     */
    fun doCastling(move: PieceMove): Board {

        val rookColumnLetter = if ( move.endSquare.column == Column.C ) MIN_X_LETTER else MAX_X_LETTER

        val rookStartPos = Square( (rookColumnLetter).toColumn(), move.endSquare.row )
        val kingStartPos = move.startSquare
        val kingRow = kingStartPos.row.toString()
        val newRookPos = if (rookColumnLetter == MIN_X_LETTER) "d$kingRow" else "f$kingRow"
        val newKingPos = if (rookColumnLetter == MIN_X_LETTER) "c$kingRow" else "g$kingRow"

        val kingMove = "K$kingStartPos$newKingPos"
        val rookMove = "R$rookStartPos$newRookPos"

        val newBoard = this.makeMove(kingMove).makeMove(rookMove).asList()

        return Board(newBoard, !player)
    }

    /**
     * Does en passant movement from the given [PieceMove]
     * @param pieceMove     The piece movement to be performed
     * @return              The new [Board] after the movement
     */
    fun doEnpassant(pieceMove: PieceMove): Board {
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
     * Returns the board as a list of pieces
     * Usefull if we change the board to a different type
     */
    private fun asList(): List<Piece?> {
        return board
    }


    /**
     * Accumulates value starting with a
     * @param initial value and applying an
     * @param operation from right to left to each element with its index from the [Board]
     * @Return the specified initial value if the list is empty or the result of the operation.
     */
    fun <T>foldRightIndexed(initial: T , operation: (Int, Piece?, T) -> T) =
        this.asList().foldRightIndexed(initial, operation)


}



/**
 * Creates a list with all the possible moves of the pieces in the board
 * @param player the color of the player
 * @param inCheck if the player is in check or not
 * @return the list of pieces of the given player
 */
fun Board.getAllBoardMovesFrom(player: Player,inCheck: Boolean ): List<PieceMove> =
    foldRightIndexed(listOf<PieceMove>()){
            idx, p, acc ->
        if (p != null) {
            if (p.player == player) acc + p.getPossibleMoves(this, idx.toSquare(),inCheck)
            else acc
        }
        else acc
    }



/**
 * Creates a list with all the possible king moves that match the given player
 * @param player the player that corresponds to the king
 * @return a list of possible king moves
 */
fun Board.getKingPossibleMoves(player: Player,verifyForCheck: Boolean): List<PieceMove> {
    val king = getKingPiece(player)
    val kingPos = getKingSquare(player)
    val kingMoves = king.getPossibleMoves(this, kingPos, verifyForCheck )
    return kingMoves
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



