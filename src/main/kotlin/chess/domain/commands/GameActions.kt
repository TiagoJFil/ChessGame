package chess.domain.commands

import Board
import chess.Chess
import chess.GameName
import chess.Storage.ChessRepository
import chess.Storage.DatabaseMove
import chess.Storage.toDataBaseMove
import chess.domain.*




/**
 * @param filteredMove       the Filteres move to be sent to makeMove
 * @param databaseMove       the Filtered Move to be added to the database
 */
private data class Moves(val filteredMove: Move, val databaseMove: DatabaseMove)



class GameActions : ActionInterface{

    override suspend fun openGame(gameId: GameName, chess: Chess): Result {
        val board: Board
        val gameExists = chess.database.doesGameExist(gameId)

        val moves = chess.database.getAllMoves(gameId)
        if (gameExists) {
            //if gameExists and there is no lastMove then return an OK result with a clean board
            chess.database.getLastMove(gameId) ?: return OK(Chess(Board(), chess.database, gameId, Player.WHITE), moves)

            board = updateBoardUntilLastMove(chess.database, gameId)


            return refreshBoard(Chess(board, chess.database, gameId, Player.WHITE))
        } else {
            chess.database.createGameDocumentIfItNotExists(gameId)

            board = Board()

            return OK(Chess(board, chess.database, gameId, Player.WHITE), moves)
        }
    }


    /**
     * Function to join a game as a player with the color BLACK and the game name received.
     */
    override suspend fun joinGame(gameId: GameName, chess: Chess): Result {
        val gameExists = chess.database.doesGameExist(gameId)
        if (!gameExists) return EMPTY()

        val moves = chess.database.getAllMoves(gameId)
        //if gameExists and there is no lastMove then return an OK result with a clean board
        chess.database.getLastMove(gameId) ?: return OK(Chess(Board(), chess.database, gameId, Player.BLACK), moves)


        val board = updateBoardUntilLastMove(chess.database, gameId)

        return refreshBoard(Chess(board, chess.database, gameId, Player.BLACK))
    }

    /**
     * Function to move a piece on the board and update the database with the new move.
     */
    override suspend fun play(move: String, chess: Chess): Result {

        if (chess.board.player != chess.localPlayer) return EMPTY()
        val gameId = chess.currentGameId
        require(gameId != null)

        val filteredInput = filterInputToMoves(move, chess.board) ?: return EMPTY()

        val movement = getMoveType(filteredInput.filteredMove, chess.board)
        val newBoard = dealWithMovement(movement, chess.board, filteredInput) ?: return EMPTY()

        val dbMove = filterToDbString(filteredInput, movement)
        chess.database.addMoveToDb(dbMove, gameId)
        val moves = chess.database.getAllMoves(gameId)
        return when (movement) {
            MoveType.CHECKMATE -> CHECKMATE(
                Chess(newBoard, chess.database, chess.currentGameId, chess.localPlayer),
                newBoard.player
            )
            MoveType.CHECK -> CHECK(
                Chess(newBoard, chess.database, chess.currentGameId, chess.localPlayer),
                newBoard.player
            )
            MoveType.STALEMATE -> STALEMATE(
                Chess(newBoard, chess.database, chess.currentGameId, chess.localPlayer)

            )
            else -> OK(Chess(newBoard, chess.database, chess.currentGameId, chess.localPlayer), moves)
        }
    }

    /**
     * Function to get update the board with the last move played.
     */
    override suspend fun refreshBoard(chess: Chess): Result {
        require(chess.currentGameId != null)
        val dbMove = chess.database.getLastMove(chess.currentGameId) ?: return EMPTY()

        val filteredInput = filterInputToMoves(dbMove.move, chess.board) ?: return EMPTY()

        val movement = getMoveType(filteredInput.filteredMove, chess.board)
        val newBoard = dealWithMovement(movement, chess.board, filteredInput) ?: return EMPTY()
        val moves = chess.database.getAllMoves(chess.currentGameId)
        return when (movement) {
            MoveType.CHECKMATE -> CHECKMATE(
                Chess(newBoard, chess.database, chess.currentGameId, chess.localPlayer),
                newBoard.player
            )
            MoveType.CHECK -> CHECK(
                Chess(newBoard, chess.database, chess.currentGameId, chess.localPlayer),
                newBoard.player
            )
            MoveType.STALEMATE -> STALEMATE(
                Chess(newBoard, chess.database, chess.currentGameId, chess.localPlayer)
            )
            else -> OK(Chess(newBoard, chess.database, chess.currentGameId, chess.localPlayer), moves)
        }
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
            board.doCastling(filteredInput.filteredMove.toString().formatToPieceMove())
        }
        MoveType.PROMOTION -> {
            board.promotePieceAndMove(filteredInput.filteredMove.toString(), filteredInput.databaseMove.toString().last())
        }
        MoveType.ENPASSANT -> {
            board.doEnpassant(filteredInput.filteredMove.toString().formatToPieceMove())
        }
        MoveType.REGULAR -> {
            board.makeMove(filteredInput.filteredMove.toString())
        }
        MoveType.CAPTURE -> {
            board.makeMove(filteredInput.filteredMove.toString())
        }
        MoveType.CHECK -> {
            board.makeMove(filteredInput.filteredMove.toString())
        }
        MoveType.CHECKMATE -> {
            board.makeMove(filteredInput.filteredMove.toString())
        }
        MoveType.STALEMATE -> {
            board.makeMove(filteredInput.filteredMove.toString())
        }

    }


/**
 * @param moves    the [Moves] object with the filtered move
 * @param type          the type of the move
 * Receives a filteredMove and a type, associates them and returns a [Move] to insert into the database
 */
private fun filterToDbString(moves: Moves, type: MoveType): DatabaseMove {
    when(type){
        MoveType.CASTLE -> return moves.filteredMove.toDataBaseMove()
        MoveType.REGULAR -> return moves.filteredMove.toDataBaseMove()
        MoveType.PROMOTION -> return moves.databaseMove
        MoveType.ENPASSANT ->
            return DatabaseMove((moves.filteredMove.move.substring(0,3) + "x" + moves.filteredMove.move.substring(3,5) + ".ep"))

        MoveType.CAPTURE ->
            return DatabaseMove((moves.filteredMove.move.substring(0, 3) + "x" + moves.filteredMove.move.substring(3, 5)))

        MoveType.CHECK ->
            return moves.databaseMove

        MoveType.CHECKMATE ->
            return moves.databaseMove
        MoveType.STALEMATE -> {
            return moves.databaseMove
        }

        else ->
            throw IllegalArgumentException("Move type not implemented")
    }
}


/**
 * @param dataBase      the database to use
 * @param gameId        the id of the game to update from
 * Updates a board with the moves from the DataBase with the given gameId.
 */
private suspend fun updateBoardUntilLastMove(dataBase: ChessRepository, gameId: GameName): Board {
    val lastMove = dataBase.getLastMove(gameId)

    return dataBase.getAllMoves(gameId).fold(Board()) {
            acc, move ->

        val filteredInput = filterInputToMoves(move.move, acc) ?: throw IllegalArgumentException("Invalid move from db")
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
private fun filterInputToMoves(input: String, board: Board): Moves? {
    val filter = Regex("([RNBQKPrnbqkp])([abcdefgh])([12345678])x?([abcdefgh])([12345678])=?([NBQR])?(.ep)?")
    val removableInput = Regex("x?(=([NBQR]))?(.ep)?")
    val filteredMove = input.replace(removableInput,"")

    val filterForNoPieceName = Regex("([abcdefgh])([12345678])([abcdefgh])([12345678])")
    if( filterForNoPieceName.matches(filteredMove) ) {
        val simpleMove = filteredMove.toMove(board)
        if (simpleMove != null ) {
            val filteredInput = simpleMove
            //put the piece in the idx 0 and the rest of the input
            val databaseMove = DatabaseMove(simpleMove.move[0] + input)
            return Moves(filteredInput, databaseMove)
        }
    }

    if(!filter.matches(filteredMove)) return null
    return Moves(Move(filteredMove),DatabaseMove(input))
}