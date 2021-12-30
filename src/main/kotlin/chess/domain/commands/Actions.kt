package chess.domain.commands

import Board
import chess.Chess
import chess.GameName
import chess.Storage.DataBase
import chess.Storage.Move
import chess.domain.*
import chess.domain.board_components.toSquare
import doCastling
import doEnpassant
import promotePieceAndMove

private const val PAWN_INPUT = 2
private const val NO_PIECE_INPUT = 4

/**
 * @param filteredMove       the Filteres move to be sent to makeMove
 * @param databaseMove       the Filtered Move to be added to the database
 */
private data class Moves(val filteredMove: String, val databaseMove: String)

/**
 * Function to open a game as a player with the color WHITE and the game name received.
 */
fun openAction(gameName: String,chess: Chess): Result {
    if (gameName == "") {
        return ERROR()
    }

    val gameId = GameName(gameName)
    val gameExists = chess.dataBase.createGameDocumentIfItNotExists(gameId)

    val board = if (gameExists) {
        updateNewBoard(chess.dataBase, gameId, Board())
    }else{
        Board()
    }

    return CONTINUE(Chess(board,chess.dataBase,gameId,Player.WHITE))

}

/**
 * Function to join a game as a player with the color BLACK and the game name received.
 */
fun joinAction(gameName: String,chess: Chess): Result {
    if (gameName == "" ){  // PODE SE ADICIONAR  MAIS VERIFICAÇOES
        return ERROR()
    }

    val gameId = GameName(gameName)
    if(!chess.dataBase.doesGameExist(gameId)) return ERROR()

    val board = updateNewBoard(chess.dataBase, gameId, Board())

    return CONTINUE(Chess(board,chess.dataBase,gameId,Player.BLACK))
}


/**
 * Function to move a piece on the board and update the database with the new move.
 */
fun playAction(move: String, chess: Chess): Result {

    if (chess.board.getPlayerColor() != chess.currentPlayer) return ERROR()
    val gameId = chess.currentGameId
    require(gameId != null)

    val filteredInput = filterInput(move, chess.board) ?: return ERROR()
    var newBoard : Board = chess.board

    val movement = canPieceMoveTo(filteredInput.filteredMove, chess.board)
    when (movement) {
        MoveType.ILLEGAL -> return ERROR();
        MoveType.CASTLE -> {
            val (updatedBoard, moves) = chess.board.doCastling(Move(filteredInput.filteredMove))
            moves.forEach {
                chess.dataBase.addMoveToDb(it, gameId)
            }
            newBoard = updatedBoard
            return CONTINUE(Chess(newBoard, chess.dataBase, chess.currentGameId, chess.currentPlayer))
        }
        MoveType.PROMOTION -> {
            if (filteredInput.databaseMove.contains("=")) {
                newBoard =
                    chess.board.promotePieceAndMove(filteredInput.filteredMove, filteredInput.databaseMove.last())
                //val dbMove = filterToDbString(filteredInput.filteredMove,MoveType.PROMOTION)
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
            newBoard = chess.board.makeMove(filteredInput.filteredMove)
        }

    }

    val dbMove = filterToDbString(filteredInput,movement)
    chess.dataBase.addMoveToDb(dbMove,gameId)




    return CONTINUE(Chess(newBoard,chess.dataBase,chess.currentGameId,chess.currentPlayer))
}

/**
 * Receives a string and returns a [Move] to insert into the database
 */
private fun filterToDbString(filteredMove: Moves, type: MoveType): Move {
    when(type){
        MoveType.REGULAR -> return Move(filteredMove.filteredMove)
        MoveType.PROMOTION -> return Move(filteredMove.databaseMove)
        MoveType.ENPASSANT ->{
            return Move((filteredMove.filteredMove.substring(0,3) + "x" + filteredMove.filteredMove.substring(3,5) + ".ep"))
        }
        MoveType.CAPTURE -> {
            return Move((filteredMove.filteredMove.substring(0, 2) + "x" + filteredMove.filteredMove.substring(3, 4)))
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
    return canPieceMoveTo(filteredInput.filteredMove, this) == MoveType.PROMOTION
}


/**
 * @param dataBase      the database to use
 * @param gameId        the id of the game to update from
 * @param board         the board to update
 * Updates a board with the moves from the DataBase with the given gameId.
 */
private fun updateNewBoard(dataBase: DataBase, gameId: GameName, board: Board): Board =
    dataBase.getAllMoves(gameId).fold(board) {
            acc, move ->

        val filteredInput = filterInput(move.move, acc) ?: return acc

         when (canPieceMoveTo(filteredInput.filteredMove, acc)) {
            MoveType.CASTLE -> {
                val (updatedBoard, moves) = acc.doCastling(Move(filteredInput.filteredMove))
                updatedBoard
            }
            MoveType.PROMOTION -> {
                acc.promotePieceAndMove(filteredInput.filteredMove, filteredInput.databaseMove.last())
            }
            MoveType.ENPASSANT -> {
                acc.doEnpassant(filteredInput.filteredMove.formatToPieceMove())
            }
            MoveType.REGULAR -> {
                acc.makeMove(filteredInput.filteredMove)
            }
            MoveType.CAPTURE -> {
                acc.makeMove(filteredInput.filteredMove)
            }
            MoveType.CHECK -> {
                acc.makeMove(filteredInput.filteredMove)
            }
            MoveType.CHECKMATE -> {
                acc.makeMove(filteredInput.filteredMove)
            }
            else -> acc
        }



        /*

        val moveFiltered = filterInput(move.move,board)
        if(moveFiltered != null) {
            if(moveFiltered.databaseMove.contains("=")) {
                acc.promotePieceAndMove(moveFiltered.filteredMove, moveFiltered.databaseMove.last())
            }
            else if(moveFiltered.databaseMove.contains(".ep")) {
                acc.doEnpassant(moveFiltered.filteredMove.formatToPieceMove())
            }
            else {
                acc.makeMove(moveFiltered.filteredMove)
            }

        }else acc
         */
}

/*
/**
 * @param dataBase      the database to use
 * @param gameId        the id of the game to update from
 * @param board         the board to update
 * Updates a board with the moves from the DataBase with the given gameId.
 */
private fun updateNewBoard(dataBase: DataBase, gameId: GameName, board: Board): Board =
    dataBase.getAllMoves(gameId).fold(board) {
            acc, move ->
        val moveFiltered = filterInput(move.move,board)
        if(moveFiltered != null) {
            if(moveFiltered.databaseMove.contains("=")) {
                acc.promotePieceAndMove(moveFiltered.filteredMove, moveFiltered.databaseMove.last())
            }
            else if(moveFiltered.databaseMove.contains(".ep")) {
                acc.doEnpassant(moveFiltered.filteredMove.formatToPieceMove())
            }
            else {
                acc.makeMove(moveFiltered.filteredMove)
            }

        }else acc
    }


 */

/**
 * @param input          the input to filter
 * @param board          the board to filter the input on
 * @return  A moves data class with the filtered input and the database move
 * Allows only the moves that can be played on the board.
 */
private fun filterInput(input: String, board: Board): Moves? {
    val filter = Regex("([RNBQKPrnbqkp])([abcdefgh])([12345678])x?([abcdefgh])([12345678])=?([NBQR])?(.ep)?")
    val filterForPawn = Regex("([abcdefgh])([12345678])")
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

    if(!filter.matches(input)  && input.length == PAWN_INPUT && !filterForPawn.matches(input)) return null

    if(input.length == PAWN_INPUT && filterForPawn.matches(input)){
        val tracePawn = traceBackPawn(input, board)
        return if(tracePawn == null) null
        else Moves(tracePawn,tracePawn)
    }
    if(!filter.matches(filteredMove)) return null
    return Moves(filteredMove,input)

}
