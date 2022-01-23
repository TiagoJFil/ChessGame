package chess.domain.commands

import Board
import chess.Chess
import chess.GameName
import chess.storage.ChessRepository
import chess.storage.DatabaseMove
import chess.domain.*
import chess.domain.board_components.toSquare



/**
 * @param filteredMove       the Filteres move to be sent to makeMove
 * @param databaseMove       the Filtered Move to be added to the database
 */
private data class Moves(val filteredMove: Move, val databaseMove: DatabaseMove)


/**
 * Represents an action that can be performed on a game.
 */
class GameActions : Actions{

    /**
     * Opens a game idetified by the [gameId] and atributes the user the WHITE [Player]
     * @param gameId        the id of the game to open
     * @param chess         the chess game to use to make the new one
     * @return a [Result] with the new [Chess] and played [DatabaseMove]
     */
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
     * Joins a game idetified by the [gameId] received and atributes the user the BLACK [Player]
     * @param gameId        the id of the game to open
     * @param chess          the chess game to use to make the new one
     * @return a [Result] with the new [Chess] and played [DatabaseMove]s
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
     * @param move          the move to be performed
     * @param chess          the chess game to use to make the new one
     * @return a [Result] with the new [Chess] and played [DatabaseMove]s
     */
    override suspend fun play(move: String, chess: Chess): Result {
        if (chess.board.player != chess.localPlayer) return EMPTY()
        val gameId = chess.currentGameId
        require(gameId != null)
        val filteredInput = filterInputToMoves(move, chess.board) ?: return EMPTY()

        val movement = getMoveType(filteredInput.filteredMove, chess.board)
        val newBoard = dealWithMovement(movement, chess.board, filteredInput) ?: return EMPTY()

        val dbMove = filterToDbString(filteredInput, movement,chess.board)
        chess.database.addMoveToDb(dbMove, gameId)
        return handleMoveType(movement, chess,newBoard)
    }


    /**
     * Function to refresh a board received in the [Chess] object with the last move played.
     * @param chess          the chess game to use to make the new one
     * @return a [Result] with the new [Chess] and played [DatabaseMove]s
     */
    override suspend fun refreshBoard(chess: Chess): Result {
        require(chess.currentGameId != null)
        val dbMove = chess.database.getLastMove(chess.currentGameId) ?: return EMPTY()

        val filteredInput = filterInputToMoves(dbMove.move, chess.board) ?: return EMPTY()

        val movement = getMoveType(filteredInput.filteredMove, chess.board)
        val newBoard = dealWithMovement(movement, chess.board, filteredInput) ?: return EMPTY()
        return handleMoveType(movement, chess,newBoard)
    }

}

/**
 * Added to avoid code repetition
 * Receives a move type and returns a Result according to the type.
 * @param movement the type of movement
 * @param chess the chess object
 * @param newBoard the new board after the movement
 */
private suspend fun handleMoveType(movement: MoveType, chess: Chess, newBoard: Board ): Result{
    require(chess.currentGameId != null)
    val moves = chess.database.getAllMoves(chess.currentGameId)
    return when (movement) {
        MoveType.CHECKMATE -> CHECKMATE(
            Chess(newBoard, chess.database, chess.currentGameId, chess.localPlayer),
            newBoard.player,
            moves
        )
        MoveType.CHECK -> CHECK(
            Chess(newBoard, chess.database, chess.currentGameId, chess.localPlayer),
            newBoard.player,
            moves
        )
        MoveType.STALEMATE -> STALEMATE(
            Chess(newBoard, chess.database, chess.currentGameId, chess.localPlayer),
            moves
        )
        else -> OK(Chess(newBoard, chess.database, chess.currentGameId, chess.localPlayer), moves)
    }
}


/**
 * Added to avoid code duplication
 * Updates a board according to a moveType
 * @param movement      the movement type to be checked
 * @param board         the board to update
 * @param filteredInput the filtered move to be sent to makeMove
 * @return              the updated board or null if the movement is invalid
 */
private fun dealWithMovement(movement: MoveType, board: Board,filteredInput: Moves): Board? =
    when (movement) {
        MoveType.ILLEGAL -> null
        MoveType.CASTLE -> {
            board.doCastling(filteredInput.filteredMove.toString().formatToPieceMove() )
        }
        MoveType.PROMOTION -> {
            board.moveAndPromotePiece(filteredInput.filteredMove.toString(), filteredInput.databaseMove.toString().last())
        }
        MoveType.ENPASSANT -> {
            board.doEnpassant(filteredInput.filteredMove.toString().formatToPieceMove() )
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
 * @param moves     the [Moves] object with the filtered move
 * @param type      the type of the move
 * @param board     utilitary board for extracting the information if a piece eats other
 * Receives a filteredMove and a type, associates them and returns a [DatabaseMove] to insert into the database
 */
private fun filterToDbString(moves: Moves, type: MoveType, board: Board): DatabaseMove {
    val endPieceSquare = moves.filteredMove.move.substring(3,5).toSquare()
    val pieceAtEnd = board.getPiece(endPieceSquare)
    val captures = if(pieceAtEnd != null) "x" else ""
    val rook = if(board.player.isWhite()) "O" else "o"
    return when(type){
        MoveType.CASTLE ->
            if(moves.filteredMove.move[3] == 'c')
                DatabaseMove("$rook-$rook-$rook")
            else
                DatabaseMove("$rook-$rook")
        MoveType.REGULAR ->
            DatabaseMove(moves.filteredMove.move)
        MoveType.PROMOTION ->
            DatabaseMove(moves.databaseMove.move.substring(0,3) + captures + moves.databaseMove.move.substring(3,7))      //Pa2a4=Q or //Pa2xa4=Q
        MoveType.ENPASSANT ->
            DatabaseMove(moves.filteredMove.move.substring(0,3) + "x" + moves.filteredMove.move.substring(3,5) + ".ep")   //Pa2xa4.ep
        MoveType.CAPTURE ->
            DatabaseMove(moves.filteredMove.move.substring(0, 3) + "x" + moves.filteredMove.move.substring(3, 5))         //Pa2xa4
        MoveType.CHECK ->
            DatabaseMove(moves.databaseMove.move.substring(0,3) + captures + moves.filteredMove.move.substring(3,5))      //Pa2xa4 or //Pa2a4
        MoveType.CHECKMATE ->
            DatabaseMove(moves.databaseMove.move.substring(0,3) + captures + moves.filteredMove.move.substring(3,5))      //Pa2xa4 or //Pa2a4
        MoveType.STALEMATE -> {
            DatabaseMove(moves.databaseMove.move.substring(0,3) + captures + moves.filteredMove.move.substring(3,5))      //Pa2xa4 or //Pa2a4
        }
        MoveType.ILLEGAL -> throw IllegalArgumentException("Illegal move")
    }
}

/**
 * Updates a board with the moves from the DataBase with the given gameId.
 * @param dataBase      the database to use
 * @param gameId        the id of the game to update from
 */
private suspend fun updateBoardUntilLastMove(dataBase: ChessRepository, gameId: GameName): Board {
    val lastMove = dataBase.getLastMove(gameId)
    require(lastMove != null) { "No moves found for game $gameId" }
    return dataBase.getAllMoves(gameId).fold(Board()) {
            acc, move ->
        val filteredInput = filterInputToMoves(move.move, acc) ?: throw IllegalArgumentException("Invalid move from db")
        val moveType = getMoveType(filteredInput.filteredMove, acc)
        if(move.move == lastMove.move){acc}
        else {
            dealWithMovement(moveType, acc, filteredInput) ?: throw IllegalArgumentException("Invalid move from db")
        }
    }
}

/**
 * Filters the input to a [Moves] object containing a [Move] and a [DatabaseMove]
 * @param input          the input to filter
 * @param board          the board to filter the input on
 * @return  A [Moves] data class with a playable move and the database move
 * Allows only the moves that can be played on the board.
 */
private fun filterInputToMoves(input: String, board: Board): Moves? {
    val filter = Regex("([RNBQKPrnbqkp])([abcdefgh])([12345678])x?([abcdefgh])([12345678])=?([NBQR])?(.ep)?")
    val removableInput = Regex("x?(=([NBQR]))?(.ep)?")
    val filteredMove = input.replace(removableInput,"")

    val castleLeft = if(board.player.isWhite())"Ke1c1".toMove(board) else "ke8c8".toMove(board)
    val castleRight = if(board.player.isWhite()) "Ke1g1".toMove(board) else "ke8g8".toMove(board)
    val rook = if(board.player.isWhite()) "O" else "o"

    val castle = Regex("([Oo]-[Oo]-?[Oo]?)")
    if(castle.matches(input)){
        if(input.length > 3 && castleLeft != null) return Moves(castleLeft, DatabaseMove("$rook-$rook-$rook"))
        if(castleRight != null) return Moves(castleRight,DatabaseMove("$rook-$rook"))
    }

    val filterForNoPieceName = Regex("([abcdefgh])([12345678])([abcdefgh])([12345678])")
    if(filterForNoPieceName.matches(filteredMove) ) {
        val simpleMove = filteredMove.toMove(board)
        if (simpleMove != null ) {
            //put the piece in the idx 0 and the rest of the input
            val databaseMove = DatabaseMove(simpleMove.move[0] + input)
            return Moves(simpleMove, databaseMove)
        }
    }
    if(!filter.matches(filteredMove)) return null
    return Moves(Move(filteredMove),DatabaseMove(input))
}
