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
        updateNewBoard(chess.dataBase, gameId)
    }else{
        Board()
    }
    val moves = getMovesAction(gameId, chess.dataBase)

    return CONTINUE(Chess(board,chess.dataBase,gameId,Player.WHITE),moves)

}

/**
 * Function to join a game as a player with the color BLACK and the game name received.
 */
fun joinAction(gameId: GameName,chess: Chess): Result {
    if(!chess.dataBase.doesGameExist(gameId)) return ERROR

    val board = updateNewBoard(chess.dataBase, gameId)

    val moves = getMovesAction(gameId, chess.dataBase)

    return CONTINUE(Chess(board,chess.dataBase,gameId,Player.BLACK),moves)
}


/**
 * Function to move a piece on the board and update the database with the new move.
 */
fun playAction(move: String, chess: Chess): Result {

    if (chess.board.getPlayerColor() != chess.currentPlayer) return ERROR
    val gameId = chess.currentGameId
    require(gameId != null)

    val filteredInput = filterInput(move, chess.board) ?: return ERROR
    var newBoard : Board = chess.board

    val movement = getMoveType(filteredInput.filteredMove, chess.board)
    when (movement) {
        MoveType.ILLEGAL -> return ERROR;
        MoveType.CASTLE -> {
            val updatedBoard = chess.board.doCastling(filteredInput.filteredMove.formatToPieceMove())
            newBoard = updatedBoard
        }
        MoveType.PROMOTION -> {
            if (filteredInput.databaseMove.contains("=")) {
                newBoard =
                    chess.board.promotePieceAndMove(filteredInput.filteredMove, filteredInput.databaseMove.last())
                //val dbMove = filterToDbString(filteredInput.filteredMove,MoveType.PROMOTION)
            }else{
                return ERROR
            }
        }
        MoveType.ENPASSANT -> {
            newBoard = chess.board.doEnpassant(filteredInput.filteredMove.formatToPieceMove())
        }
        MoveType.REGULAR -> {
            newBoard = chess.board.makeMove(filteredInput.filteredMove)
        }
        MoveType.CAPTURE -> {
            newBoard = chess.board.makeMove(filteredInput.filteredMove)
        }
        MoveType.CHECK -> {
            return CHECK
        }
        MoveType.CHECKMATE -> {
            return CHECKMATE
        }

    }

    val dbMove = filterToDbString(filteredInput,movement)
    chess.dataBase.addMoveToDb(dbMove,gameId)




    return CONTINUE(Chess(newBoard,chess.dataBase,chess.currentGameId,chess.currentPlayer),null)
}


fun refreshBoardAction(chess: Chess): Chess {
    require(chess.currentGameId != null)
    val board = updateAnExistentBoard(chess.dataBase, chess.currentGameId, chess.board)
    return Chess(board,chess.dataBase,chess.currentGameId,chess.currentPlayer)
}

fun getMovesAction(gameId: GameName, database: ChessDataBase) : Iterable<Move> {
    return database.getAllMoves(gameId)
}


/**
 * Receives a string and returns a [Move] to insert into the database
 */
private fun filterToDbString(filteredMove: Moves, type: MoveType): Move {
    //TODO("when all moves are implemented, change the when to have a else and not repeat code")
    when(type){
        MoveType.CASTLE -> return Move(filteredMove.filteredMove)
        MoveType.REGULAR -> return Move(filteredMove.filteredMove)
        MoveType.PROMOTION -> return Move(filteredMove.databaseMove)
        MoveType.ENPASSANT ->{
            return Move((filteredMove.filteredMove.substring(0,3) + "x" + filteredMove.filteredMove.substring(3,5) + ".ep"))
        }
        MoveType.CAPTURE -> {
            return Move((filteredMove.filteredMove.substring(0, 3) + "x" + filteredMove.filteredMove.substring(3, 5)))
        }

    }
    //it will never return Move("") otherwise the function is used in the wrong place
    return Move("")
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
private fun updateNewBoard(dataBase: DataBase, gameId: GameName): Board =
    dataBase.getAllMoves(gameId).fold(Board()) {
            acc, move ->

        val filteredInput = filterInput(move.move, acc) ?: throw IllegalArgumentException("Invalid move from db")

        evaluateMoveAndUpdateBoard(filteredInput, acc)
}

/**
 * @param dataBase      the database to use
 * @param gameId        the id of the game to update from
 * @param board         the board to update
 * Updates a board with the moves from the DataBase with the given gameId.
 */
private fun updateAnExistentBoard(dataBase: DataBase, gameId: GameName,board: Board): Board {
    val dbMove = dataBase.getLastMove(gameId) ?: return board

    val filteredInput = filterInput(dbMove.move, board) ?: return board

    return evaluateMoveAndUpdateBoard(filteredInput, board)
}

/**
 * Added to avoid code duplication
 * @param filteredInput the input to be evaluated
 * @param board         the board to update
 * @return              the updated board
 */
private fun evaluateMoveAndUpdateBoard(filteredInput : Moves, board: Board): Board {
    return when (getMoveType(filteredInput.filteredMove, board)) {
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
        else -> board
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
