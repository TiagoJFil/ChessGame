import chess.Storage.Move
import chess.domain.Player
import chess.domain.board_components.Square
import chess.domain.board_components.toSquare


private const val BOARD_SIZE = 8
const val MIN_X_LETTER = 'a'
const val MAX_X_LETTER = 'h'
const val MIN_Y_NUMBER = 1
const val MAX_Y_NUMBER = 8

typealias BoardMap = Array<Array<Piece?>>


class Board() {
    private val board: BoardMap = defaultBoard()
    private var player = Player(Colors.WHITE)



    /**
     * Converts the board into a String representation.
     */
    override fun toString(): String {
        var string = ""
        board.forEach { it ->
            it.forEach {
                string += it?.toString() ?: " "
            }
        }
        return string
    }

    /**
     * changes the player turn on the board.
     */
    private fun changePlayerTurn() {
        player = if (player.isWhite()) Player(Colors.BLACK)
        else Player(Colors.WHITE)
    }


    /**
     * @return the current player color.
     */
    fun getPlayerColor(): Colors {
        return player.color
    }

    /**
     * @param startPos the [Square] of the piece
     * @return the piece on the given coordinates or null if there is no piece at the given coordinates.
     */
    fun getPieceAt(position: Square): Piece? {
        val row = position.row.value()
        val column = position.column.value()
        return board[position.row.value()][position.column.value()]

    }




    /**
     * @param coordinates the [Coordinates] where we check if there is a piece
     * @return true if there is a piece on the given coordinates, false otherwise
     */
    fun hasPieceAt(square: Square): Boolean {
        return board[square.row.value()][square.column.value()] != null
    }

    /**
     * @param currentPlayer color of the king we are looking for
     * @return the square were the king is positioned
     * loops through the map looking for the player's king
     */

    fun getKingSquare(playerColor: Colors): Square? {
        var k = '8'
        for (idx in board) {
            var j = 'a'
            for (piece in idx) {
                if (piece != null)
                    if ((piece is King ) && piece.player.color == playerColor) return "$j$k".toSquare()
                j++
            }
            k--
        }
        return null //this should never occur if checkmate is well implemented
    }


    /**
     * put the pieces on the default position on the board
     * @return [BoardMap] which is an array of array of pieces
     */
    private fun defaultBoard(): BoardMap {
        val whitePlayer = Player(Colors.WHITE)
        val blackPlayer = Player(Colors.BLACK)


        val newBoard: BoardMap = arrayOf(
            arrayOf(
                Rook(blackPlayer),
                Knight(blackPlayer),
                Bishop(blackPlayer),
                Queen(blackPlayer),
                King(blackPlayer),
                Bishop(blackPlayer),
                Knight(blackPlayer),
                Rook(blackPlayer)
            ),
            arrayOf(
                Pawn(blackPlayer),
                Pawn(blackPlayer),
                Pawn(blackPlayer),
                Pawn(blackPlayer),
                Pawn(blackPlayer),
                Pawn(blackPlayer),
                Pawn(blackPlayer),
                Pawn(blackPlayer)
            ),
            arrayOfNulls(BOARD_SIZE),
            arrayOfNulls(BOARD_SIZE),
            arrayOfNulls(BOARD_SIZE),
            arrayOfNulls(BOARD_SIZE),
            arrayOf(
                Pawn(whitePlayer),
                Pawn(whitePlayer),
                Pawn(whitePlayer),
                Pawn(whitePlayer),
                Pawn(whitePlayer),
                Pawn(whitePlayer),
                Pawn(whitePlayer),
                Pawn(whitePlayer),

            ),
            arrayOf(
                Rook(whitePlayer),
                Knight(whitePlayer),
                Bishop(whitePlayer),
                Queen(whitePlayer),
                King(whitePlayer),
                Bishop(whitePlayer),
                Knight(whitePlayer),
                Rook(whitePlayer)
            )
        )

        return newBoard
    }


    /**
     * @param move the [Move] we want to make
     * @return the [Board] after the move
     * Makes a move on the board and changes the player turn
     */
    fun makeMove(move: String): Board {

        val pieceMovement = move.formatToPieceMove()

        val piece = board[pieceMovement.startSquare.row.value()][pieceMovement.startSquare.column.value()]
            ?: throw IllegalStateException("no piece at startingSquare")
        board[pieceMovement.startSquare.row.value()][pieceMovement.startSquare.column.value()] = null
        board[pieceMovement.endSquare.row.value()][pieceMovement.endSquare.column.value()] = piece

        when(piece) {
            is Pawn -> piece.setAsMoved()
            is King -> piece.setAsMoved()
            is Rook -> piece.setAsMoved()
        }

        changePlayerTurn()

        return this
    }

    fun isPlayerMovingOwnPieces(move: String): Boolean {
        val pieceMovement = move.formatToPieceMove()
        val startingSquare = Square(pieceMovement.startSquare.column, pieceMovement.startSquare.row)
        val pieceAtStart = getPieceAt(startingSquare) ?: return false

        return pieceAtStart.player.color == getPlayerColor()
    }

    /**
     *
     */
    fun isPlayerMovingTheRightPieces(move: String): Boolean {
        val pieceMovement = move.formatToPieceMove()
        val startingSquare = Square(pieceMovement.startSquare.column, pieceMovement.startSquare.row)
        val pieceAtStart = getPieceAt(startingSquare) ?: return false

        return pieceAtStart.toString().equals((move[0]).toString(), ignoreCase = true)
    }


    /**
     * @param board         The board to perform the castling on
     * @param move          The move received from the user to perform the castling.
     * @returns     a List of Moves that happened during the castling.
     * Moves the king and the rook to the correct position.
     */
    fun doCastling(move: Move): List<Move> {

        val rookColumnLetter = if (move.move[3] == 'c') MIN_X_LETTER else MAX_X_LETTER


        val rookStartPos = move.move.substring(1..2)
        val kingStartPos = rookColumnLetter + move.move[2].toString()
        val newRookPos = if (rookColumnLetter == MIN_X_LETTER) "d" + move.move[2] else "f" + move.move[2]
        val newKingPos = if (rookColumnLetter == MIN_X_LETTER) "c" + move.move[2] else "g" + move.move[2]

        val kingMove = "K$kingStartPos$newKingPos"
        val rookMove = "R$rookStartPos$newRookPos"

        makeMove(kingMove)
        makeMove(rookMove)
        //changes the player turn twice so we need to  change it again

        changePlayerTurn()
        val moves = mutableListOf<Move>()
        moves.add(Move(kingMove))
        moves.add(Move(rookMove))

        return moves
    }


    fun doEnpassant(pieceMove: PieceMove) {
        TODO()
    }

    /**
     * @param square the Square where the piece to be promoted is placed
     * @return true if the piece is a promoted or false if it isn't
     */
    fun promotePiece(square: Square, promotionType: Char = 'q'): Boolean {
        val piece = getPieceAt(square) ?: return false
        if (piece !is Pawn) return false
        else {
            board[square.row.value()][square.column.value()] = when(promotionType.toLowerCase()) {
                'r' -> {
                   val res = Rook(piece.player)
                       res.setAsMoved()
                       res
                }
                'b' -> Bishop(piece.player)
                'n' -> Knight(piece.player)
                'q' -> Queen(piece.player)
                else -> throw IllegalArgumentException("Invalid promotion type")
            }


        }
        return true
    }
}




