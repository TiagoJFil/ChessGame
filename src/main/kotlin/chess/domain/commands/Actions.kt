package chess.domain.commands

import Board
import chess.Chess
import chess.GameName
import chess.Storage.ChessDataBase
import chess.Storage.DataBase
import chess.Storage.Move
import chess.domain.*
import chess.domain.board_components.toSquare
import doCastling
import doEnpassant
import promotePieceAndMove

private const val NO_PIECE_INPUT = 4

/**
 * @param filteredMove       the Filteres move to be sent to makeMove
 * @param databaseMove       the Filtered Move to be added to the database
 */
private data class Moves(val filteredMove: String, val databaseMove: String)

/**
 * Function to open a game as a player with the color WHITE and the game name received.
 */
fun openAction(gameId: GameName,chess: Chess): Result {
    val gameExists = chess.dataBase.createGameDocumentIfItNotExists(gameId)

    val board = if (gameExists) {
        updateBoardUntilLastMove(chess.dataBase, gameId)
    }else{
        Board()
    }
    val moves = getMovesAction(gameId, chess.dataBase)

//    refreshBoardAction
    if(gameExists){
        val movement = getMoveTypeFromLastDbPlay(gameId, chess.dataBase,chess.board)

        return when(movement){
            MoveType.CHECKMATE ->  CHECKMATE(Chess(board,chess.dataBase,gameId,Player.WHITE), board.player)
            MoveType.CHECK ->  CHECK(Chess(board,chess.dataBase,gameId,Player.WHITE), board.player)
            else ->  OK(Chess(board,chess.dataBase,gameId,Player.WHITE),moves)
        }
    }else{
        return OK(Chess(board,chess.dataBase,gameId,Player.WHITE),moves)
    }
}

/**
 * Function to join a game as a player with the color BLACK and the game name received.
 */
fun joinAction(gameId: GameName,chess: Chess): Result {
    if(!chess.dataBase.doesGameExist(gameId)) return NONE()

    val board = updateBoardUntilLastMove(chess.dataBase, gameId)
    //TODO: to fix the bug of check , we can create a function to update all the moves but the last then update the last manually and return it
    //TODO: or change play action and use in the update board function
    val moves = getMovesAction(gameId, chess.dataBase)

    return OK(Chess(board,chess.dataBase,gameId,Player.BLACK),moves)
}


/**
 * Function to move a piece on the board and update the database with the new move.
 */
fun playAction(move: String, chess: Chess): Result {

    if (chess.board.getPlayerColor() != chess.currentPlayer) return NONE()
    val gameId = chess.currentGameId
    require(gameId != null)

    val filteredInput = filterInput(move, chess.board) ?: return NONE()

    val movement = getMoveType(filteredInput.filteredMove, chess.board)
    val newBoard = dealWithMovement(movement, chess.board,filteredInput) ?: return NONE()

    val dbMove = filterToDbString(filteredInput,movement)
    chess.dataBase.addMoveToDb(dbMove,gameId)

   return when(movement){
        MoveType.CHECKMATE ->  CHECKMATE(Chess(newBoard,chess.dataBase,chess.currentGameId,chess.currentPlayer), newBoard.player)
        MoveType.CHECK ->  CHECK(Chess(newBoard,chess.dataBase,chess.currentGameId,chess.currentPlayer), newBoard.player)
        else ->  OK(Chess(newBoard,chess.dataBase,chess.currentGameId,chess.currentPlayer),null)
    }
}

/**
 * Added to avoid code duplication
 * @param movement      the movement type to be checked
 * @param board         the board to update
 * @param filteredInput the filtered move to be sent to makeMove
 * @return              the updated board or null if the movement is invalid
 */
private fun dealWithMovement(movement: MoveType, board: Board,filteredInput: Moves): Board? =
    when (movement) {
        MoveType.ILLEGAL -> null
        MoveType.CASTLE -> {
            board.doCastling(filteredInput.filteredMove.formatToPieceMove())
        }
        MoveType.PROMOTION -> {
            board.promotePieceAndMove(filteredInput.filteredMove, filteredInput.databaseMove.last())
        }
        MoveType.ENPASSANT -> {
            board.doEnpassant(filteredInput.filteredMove.formatToPieceMove())
        }
        MoveType.REGULAR -> {
            board.makeMove(filteredInput.filteredMove)
        }
        MoveType.CAPTURE -> {
            board.makeMove(filteredInput.filteredMove)
        }
        MoveType.CHECK -> {
            board.makeMove(filteredInput.filteredMove)
        }
        MoveType.CHECKMATE -> {
            board.makeMove(filteredInput.filteredMove)
        }

    }


fun refreshBoardAction(chess: Chess): Result {
    require(chess.currentGameId != null)
    val dbMove = chess.dataBase.getLastMove(chess.currentGameId) ?: return NONE()

    val filteredInput = filterInput(dbMove.move, chess.board) ?: return NONE()

    val movement = getMoveType(filteredInput.filteredMove, chess.board)
    val newBoard = dealWithMovement(movement,chess.board,filteredInput) ?: return NONE()

    return when(movement){
        MoveType.CHECKMATE ->  CHECKMATE(Chess(newBoard,chess.dataBase,chess.currentGameId,chess.currentPlayer), newBoard.player)
        MoveType.CHECK ->  CHECK(Chess(newBoard,chess.dataBase,chess.currentGameId,chess.currentPlayer), newBoard.player)
        else ->  OK(Chess(newBoard,chess.dataBase,chess.currentGameId,chess.currentPlayer),null)
    }
}

private fun getMoveTypeFromLastDbPlay(gameId: GameName, dataBase: DataBase, board: Board): MoveType {
    val dbMove = dataBase.getLastMove(gameId) ?: return MoveType.ILLEGAL
    val filteredInput = filterInput(dbMove.move, board) ?: return MoveType.ILLEGAL
    return getMoveType(filteredInput.filteredMove, board)
}

/**
 * Middleware function to communicate with the database.
 */
fun getMovesAction(gameId: GameName, database: ChessDataBase) : Iterable<Move> {
    return database.getAllMoves(gameId)
}


/**
 * @param filteredMove    the [Moves] object with the filtered move
 * @param type          the type of the move
 * Receives a filteredMove and a type, associates them and returns a [Move] to insert into the database
 */
private fun filterToDbString(filteredMove: Moves, type: MoveType): Move {
    when(type){
        MoveType.CASTLE -> return Move(filteredMove.filteredMove)
        MoveType.REGULAR -> return Move(filteredMove.filteredMove)
        MoveType.PROMOTION -> return Move(filteredMove.databaseMove)
        MoveType.ENPASSANT ->
            return Move((filteredMove.filteredMove.substring(0,3) + "x" + filteredMove.filteredMove.substring(3,5) + ".ep"))

        MoveType.CAPTURE ->
            return Move((filteredMove.filteredMove.substring(0, 3) + "x" + filteredMove.filteredMove.substring(3, 5)))

        MoveType.CHECK ->
            return Move(filteredMove.databaseMove)

        MoveType.CHECKMATE ->
            return Move(filteredMove.databaseMove)

        else ->
            throw IllegalArgumentException("Move type not implemented")
    }
}

/**
 * Checks whether the move receives as a [String] is a valid promotable move.
 * @param move           The move to be checked.
 * @return a [Boolean]   value indicating whether the move is promotable or not.
 */
fun Board.isTheMovementPromotable(move: String): Boolean {
    val filteredInput = filterInput(move, this) ?: return false
    return getMoveType(filteredInput.filteredMove, this) == MoveType.PROMOTION
}


/**
 * @param dataBase      the database to use
 * @param gameId        the id of the game to update from
 * Updates a board with the moves from the DataBase with the given gameId.
 */
private fun updateBoardUntilLastMove(dataBase: DataBase, gameId: GameName): Board {

    //TODO
}

/**
 * @param dataBase      the database to use
 * @param gameId        the id of the game to update from
 * Updates a board with the moves from the DataBase with the given gameId.
 */
private fun updateNewBoard(dataBase: DataBase, gameId: GameName): Board =
    dataBase.getAllMoves(gameId).fold(Board()) {
            acc, move ->

        val filteredInput = filterInput(move.move, acc) ?: throw IllegalArgumentException("Invalid move from db")
        val moveType = getMoveType(filteredInput.filteredMove, acc)

        dealWithMovement(moveType,acc,filteredInput) ?: acc
    }


/**
 * @param input          the input to filter
 * @param board          the board to filter the input on
 * @return  A moves data class with the filtered input and the database move
 * Allows only the moves that can be played on the board.
 */
private fun filterInput(input: String, board: Board): Moves? {
    val filter = Regex("([RNBQKPrnbqkp])([abcdefgh])([12345678])x?([abcdefgh])([12345678])=?([NBQR])?(.ep)?")
    val filteredForNoPieceName = Regex("([abcdefgh])([12345678])([abcdefgh])([12345678])")
    val removableInput = Regex("x?(=([NBQR]))?(.ep)?")
    val filteredMove = input.replace(removableInput,"")

    if(!filter.matches(filteredMove)  && filteredMove.length == NO_PIECE_INPUT && filteredForNoPieceName.matches(filteredMove)) {
        val piece = board.getPiece(input.substring(0, 2).toSquare())
        if(piece != null) {
            val filteredInput = piece.toString().uppercase() + filteredMove
            val databaseMove = piece.toString().uppercase() + input
            return Moves(filteredInput,databaseMove)
        }
        else return null
    }

    if(!filter.matches(filteredMove)) return null
    return Moves(filteredMove,input)

}
