package chess.domain.commands

import Board
import chess.Chess
import chess.GameName
import chess.Storage.ChessRepository
import chess.Storage.ChessDatabase
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


//class Action : ActionInterface{
//TODO("CHOOSE IF WE ARE USING A ACTION CLASS OR NOT")
suspend fun openGame(gameId: GameName, chess: Chess): Result {
    val board: Board
    val gameExists = chess.dataBase.doesGameExist(gameId)

    val moves =  getPlayedMoves(gameId, chess.dataBase)
    if (gameExists) {
        //if gameExists and there is no lastMove then return an OK result with a clean board
        chess.dataBase.getLastMove(gameId) ?: return OK(Chess(Board(), chess.dataBase, gameId, Player.WHITE),moves)

        board = updateBoardUntilLastMove(chess.dataBase, gameId)


        return refreshBoard(Chess(board, chess.dataBase, gameId, Player.WHITE))
    } else {
        chess.dataBase.createGameDocumentIfItNotExists(gameId)

        board = Board()

        return OK(Chess(board, chess.dataBase, gameId, Player.WHITE),moves)
    }
}


/**
 * Function to join a game as a player with the color BLACK and the game name received.
 */
suspend fun joinGame(gameId: GameName, chess: Chess): Result {
    val gameExists = chess.dataBase.doesGameExist(gameId)
    if (!gameExists) return NONE()

    val moves = getPlayedMoves(gameId, chess.dataBase)
    //if gameExists and there is no lastMove then return an OK result with a clean board
    chess.dataBase.getLastMove(gameId) ?: return OK(Chess(Board(), chess.dataBase, gameId, Player.BLACK),moves)


    val board = updateBoardUntilLastMove(chess.dataBase, gameId)

    return refreshBoard(Chess(board, chess.dataBase, gameId, Player.BLACK))
}

/**
 * Function to move a piece on the board and update the database with the new move.
 */
suspend fun play(move: String, chess: Chess): Result {

    if (chess.board.getPlayerColor() != chess.localPlayer) return NONE()
    val gameId = chess.currentGameId
    require(gameId != null)

    val filteredInput = filterInput(move, chess.board) ?: return NONE()

    val movement = getMoveType(filteredInput.filteredMove, chess.board)
    val newBoard = dealWithMovement(movement, chess.board, filteredInput) ?: return NONE()

    val dbMove = filterToDbString(filteredInput, movement)
    chess.dataBase.addMoveToDb(dbMove, gameId)
    val moves = getPlayedMoves(gameId, chess.dataBase)
    return when (movement) {
        MoveType.CHECKMATE -> CHECKMATE(
            Chess(newBoard, chess.dataBase, chess.currentGameId, chess.localPlayer),
            newBoard.player
        )
        MoveType.CHECK -> CHECK(
            Chess(newBoard, chess.dataBase, chess.currentGameId, chess.localPlayer),
            newBoard.player
        )
        MoveType.STALEMATE -> STALEMATE(
            Chess(newBoard, chess.dataBase, chess.currentGameId, chess.localPlayer)

        )
        else -> OK(Chess(newBoard, chess.dataBase, chess.currentGameId, chess.localPlayer),moves)
    }
}

/**
 * Function to get update the board with the last move played.
 */
suspend fun refreshBoard(chess: Chess): Result {
    require(chess.currentGameId != null)
    val dbMove = chess.dataBase.getLastMove(chess.currentGameId) ?: return NONE()

    val filteredInput = filterInput(dbMove.move, chess.board) ?: return NONE()

    val movement = getMoveType(filteredInput.filteredMove, chess.board)
    val newBoard = dealWithMovement(movement, chess.board, filteredInput) ?: return NONE()
    val moves = getPlayedMoves(chess.currentGameId, chess.dataBase)
    return when (movement) {
        MoveType.CHECKMATE -> CHECKMATE(
            Chess(newBoard, chess.dataBase, chess.currentGameId, chess.localPlayer),
            newBoard.player
        )
        MoveType.CHECK -> CHECK(
            Chess(newBoard, chess.dataBase, chess.currentGameId, chess.localPlayer),
            newBoard.player
        )
        MoveType.STALEMATE -> STALEMATE(
            Chess(newBoard, chess.dataBase, chess.currentGameId, chess.localPlayer)
        )
        else -> OK(Chess(newBoard, chess.dataBase, chess.currentGameId, chess.localPlayer),moves)
    }
}


//}


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
        MoveType.STALEMATE -> {
            board.makeMove(filteredInput.filteredMove)
        }

    }



/**
 * Middleware function to communicate with the database.
 */
suspend fun getPlayedMoves(gameId: GameName, database: ChessRepository) : Iterable<Move> {
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
        MoveType.STALEMATE -> {
            return Move(filteredMove.databaseMove)
        }

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
private suspend fun updateBoardUntilLastMove(dataBase: ChessDatabase, gameId: GameName): Board {
    val lastMove = dataBase.getLastMove(gameId)

    return dataBase.getAllMoves(gameId).fold(Board()) {
            acc, move ->

        val filteredInput = filterInput(move.move, acc) ?: throw IllegalArgumentException("Invalid move from db")
        val moveType = getMoveType(filteredInput.filteredMove, acc)
        if(move == lastMove){acc}
            else {
            dealWithMovement(moveType, acc, filteredInput) ?: throw IllegalArgumentException("Invalid move from db")
        }
    }
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
