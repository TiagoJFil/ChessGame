import chess.Storage.Move
import chess.domain.Player
import org.junit.Test


private const val BOARD_SIZE = 8
private const val MIN_X_LETTER = 'a'
private const val MAX_X_LETTER = 'h'
private const val MIN_Y_NUMBER = 1
private const val MAX_Y_NUMBER = 8

typealias BoardMap = Array<Array<Piece?>>



/**
 * Represents the Board position that uses columns and rows as coordinates.
 * @property column    the Column associated with the Square
 * @property row       the row associated with the Square
 * Only letters ranging from a to h and numbers from 1 to 8 are allowed.
 */
data class Square(val column: Column, val row: Row){
    override fun toString(): String {
        return "${column.letter}${MAX_Y_NUMBER - row.value()}"
    }
}

/**
 * Convert a string into A Square Type
 */
fun String.toSquare(): Square {
    val column = this[0].lowercaseChar()
    val row = this[1].code.toChar()
    return Square(findColumn(column),findRow(row) )
}

/**
 * Represents the Column that is used to represent the X axis on the board.
 * @property letter    the letter associated with the Column
 */
enum class Column(val letter: Char) { //ordem normal
    A('a'),
    B('b'),
    C('c'),
    D('d'),
    E('e'),
    F('f'),
    G('g'),
    H('h');

    fun value() = letter - MIN_X_LETTER // ex: 'a' - 'a' == 0

}


fun findColumn(c : Char): Column {
    require(c.isAColumn()) {
        "Char is not valid"
    }
    val index = c - MIN_X_LETTER
    return Column.values()[index]
}

/**
 * Represents the Row that is used to represent the Y axis on the board.
 * @property number    the number associated with the row
 */
enum class Row(val number: Int) {  //BoardMap[row][column]
    Eight(0),
    Seven(1),
    Six(2),
    Five(3),
    Four(4),
    Three(5),
    Two(6),
    One(7);

    fun value() = number

}

fun findRow(c : Char) : Row{  // '1' passa pra int e depois é que fazemos a operação
    require(c.isARow())
    return Row.values()[8 - c.toString().toInt()]
}

fun Char.isAColumn() = this in MIN_X_LETTER..MAX_X_LETTER
fun Char.isARow() = this.toString().toInt() in MIN_Y_NUMBER..MAX_Y_NUMBER



class Board() {
    private val board: BoardMap = defaultBoard()
    private var player = Player(Colors.WHITE)
    private var lastMove = ""


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
    private fun changePlayerTurn(){
        player = if(player.isWhite()) Player(Colors.BLACK)
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
    fun getPieceAt(position: Square):Piece?{
        val row = position.row.value()
        val column = position.column.value()
        return board[position.row.value()][position.column.value()]

    }

    /**
     * @return last move made on the board
     */
    fun getLastMove() = lastMove


    /**
     * @param coordinates the [Coordinates] where we check if there is a piece
     * @return true if there is a piece on the given coordinates, false otherwise
     */
    fun hasPieceAt(square: Square): Boolean{
        return board[square.row.value()][square.column.value()] != null
    }

    /**
     * @param currentPlayer color of the king we are looking for
     * @return the square were the king is positioned
     * loops through the map looking for the player's king
     */

    fun getKingSquare(playerColor:Colors):Square?{
        var k = '8'
        for(idx in board){
            var j = 'a'
            for(piece in idx){
                if(piece != null)
                    if((piece.type == PieceType.KING) && piece.player.color == playerColor) return "$j$k".toSquare()
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
            arrayOf(Piece(PieceType.ROOK, blackPlayer), Piece(PieceType.KNIGHT,blackPlayer), Piece(PieceType.BISHOP, blackPlayer), Piece(PieceType.QUEEN, blackPlayer), Piece(PieceType.KING, blackPlayer), Piece(PieceType.BISHOP, blackPlayer), Piece(PieceType.KNIGHT,blackPlayer), Piece(PieceType.ROOK, blackPlayer)),
            arrayOf(Piece(PieceType.PAWN, blackPlayer), Piece(PieceType.PAWN, blackPlayer), Piece(PieceType.PAWN, blackPlayer), Piece(PieceType.PAWN, blackPlayer), Piece(PieceType.PAWN, blackPlayer), Piece(PieceType.PAWN, blackPlayer), Piece(PieceType.PAWN, blackPlayer), Piece(PieceType.PAWN, blackPlayer)),
            arrayOfNulls(BOARD_SIZE),
            arrayOfNulls(BOARD_SIZE),
            arrayOfNulls(BOARD_SIZE),
            arrayOfNulls(BOARD_SIZE),
            arrayOf(Piece(PieceType.PAWN, whitePlayer), Piece(PieceType.PAWN, whitePlayer), Piece(PieceType.PAWN, whitePlayer), Piece(PieceType.PAWN, whitePlayer), Piece(PieceType.PAWN, whitePlayer), Piece(PieceType.PAWN, whitePlayer), Piece(PieceType.PAWN, whitePlayer), Piece(PieceType.PAWN, whitePlayer)),
            arrayOf(Piece(PieceType.ROOK, whitePlayer), Piece(PieceType.KNIGHT, whitePlayer), Piece(PieceType.BISHOP, whitePlayer), Piece(PieceType.QUEEN, whitePlayer), Piece(PieceType.KING, whitePlayer), Piece(PieceType.BISHOP, whitePlayer), Piece(PieceType.KNIGHT, whitePlayer), Piece(PieceType.ROOK, whitePlayer))
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

        val piece = board[pieceMovement.startSquare.row.value()][pieceMovement.startSquare.column.value()] ?: throw IllegalStateException("no piece at startingSquare")
        board[pieceMovement.startSquare.row.value()][pieceMovement.startSquare.column.value()] = null
        board[pieceMovement.endSquare.row.value()][pieceMovement.endSquare.column.value()] = piece

        lastMove = move
        piece.setAsMoved()
        changePlayerTurn()

        return this
    }

    fun isPlayerMovingOwnPieces(move: String):Boolean{
        val pieceMovement = move.formatToPieceMove()
        val startingSquare = Square(pieceMovement.startSquare.column, pieceMovement.startSquare.row)
        val pieceAtStart = getPieceAt(startingSquare) ?: return false

        return pieceAtStart.player.color == getPlayerColor()
    }

    /**
     *
     */
    fun isPlayerMovingTheRightPieces(move: String):Boolean{
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

        val rookColumnLetter = if(move.move[3] == 'c') MIN_X_LETTER else MAX_X_LETTER


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


    fun doEnpassant(pieceMove: PieceMove){
        TODO()
    }
    /**
     * @param square the Square where the piece to be promoted is placed
     * @return true if the piece is a promoted or false if it isn't
     */
    fun promotePiece(square: Square,promotionType: PieceType ): Boolean{
        val piece = getPieceAt(square) ?: return false
        if(piece.type != PieceType.PAWN) return false
        else{
            board[square.row.value()][square.column.value()] = Piece(promotionType, piece.player)
            board[square.row.value()][square.column.value()]?.setAsMoved()

        }
        return true
    }
}



