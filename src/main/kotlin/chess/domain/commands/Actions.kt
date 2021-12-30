package chess.domain.commands

import Board
import chess.Chess
import chess.GameName
import chess.Storage.DataBase
import chess.Storage.Move
import chess.domain.*
import chess.domain.board_components.toSquare
import doCastling
import promotePiece

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

    //if (chess.board.getPlayerColor() != chess.currentPlayer) return ERROR()
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

        }
        MoveType.PROMOTION -> {


            if (filteredInput.databaseMove.contains("=")) {
                newBoard = chess.board.promotePiece(
                        filteredInput.filteredMove.substring(1, 2).toSquare(),
                      filteredInput.filteredMove.last()
                      )
            }



        }

        MoveType.REGULAR -> {
            newBoard = chess.board.makeMove(filteredInput.filteredMove)
            chess.dataBase.addMoveToDb(Move(filteredInput.databaseMove),gameId )
        }
        MoveType.CAPTURE -> {
            newBoard = chess.board.makeMove(filteredInput.filteredMove)
            chess.dataBase.addMoveToDb(Move(filteredInput.databaseMove),gameId )
        }
    }







    return CONTINUE(Chess(newBoard,chess.dataBase,chess.currentGameId,chess.currentPlayer))
}

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
        val moveFiltered = filterInput(move.move,board)
        if(moveFiltered != null) {
            acc.makeMove(moveFiltered.filteredMove)
        }else acc
    }


/**
 * @param input          the input to filter
 * @param board          the board to filter the input on
 * @return  A moves data class with the filtered input and the database move
 * Allows only the moves that can be played on the board.
 */
private fun filterInput(input: String, board: Board): Moves? {
    val filter = Regex("([RNBQKPrnbqkp])([abcdefgh])([12345678])x?([abcdefgh])([12345678])=?([NBQR])?")
    val filterForPawn = Regex("([abcdefgh])([12345678])")
    val filteredForNoPieceName = Regex("([abcdefgh])([12345678])([abcdefgh])([12345678])")
    val removableInput = Regex("x?(=([NBQR]))?")
    val filteredMove = input.replace(removableInput,"")

    if(!filter.matches(filteredMove)  && filteredMove.length == NO_PIECE_INPUT && filteredForNoPieceName.matches(filteredMove)) {
        val piece = board.getPiece(input.substring(0, 2).toSquare())
        if(piece != null) {
            val filteredInput = piece.toString() + filteredMove
            val databaseMove = piece.toString() + input
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
