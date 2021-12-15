package chess.domain.commands
import Board
import chess.domain.MoveType
import chess.domain.canPieceMoveTo
import chess.Chess
import chess.GameName
import chess.Storage.ChessDataBase
import chess.Storage.DataBase
import chess.Storage.Move
import chess.domain.Player
import chess.domain.traceBackPawn
import isel.leic.tds.storage.DbMode
import isel.leic.tds.storage.getDBConnectionInfo
import isel.leic.tds.storage.mongodb.createMongoClient
import org.junit.Test

private const val PAWN_INPUT = 2

/**
 * @param filteredMove       the Filteres move to be sent to makeMove
 * @param databaseMove       the Filtered Move to be added to the database
 */
private data class Moves(val filteredMove: String, val databaseMove: String)

/**
 * Contract to be supported by all commands
 */
interface Commands {
    /**
     * Executes this command passing it the given parameter
     * @param parameter the commands parameter, or null, if no parameter has been passed
     */
    fun execute(parameter: String?): Result
    /**
     * Overload of invoke operator, for convenience.
     */
    operator fun invoke(parameter: String?) = execute(parameter)
}



/**
 * Command to open a game as a player with the color WHITE.
 */
class OpenCommand(private val chess: Chess) : Commands {
    override fun execute(parameter: String?): Result {
        if (parameter == null) {
            return ERROR("ERROR: Missing game name.")
        }
        val gameId = GameName(parameter)

        val gameExists = chess.dataBase.createGameDocumentIfItNotExists(gameId)


        val board = if (gameExists) {
             updateNewBoard(chess.dataBase, gameId, Board())
        }else{
            Board()
        }



        chess.currentPlayer = Player.WHITE
        chess.board = board
        chess.currentGameId = gameId

        //Se inicializarmos um jogo novo não há lastMove logo dá erro. Temos de arranjar isso
//        if(chess.domain.getCheck(chess.board)) return ERROR("Your king is in check please protect or move the king")
        return CONTINUE(Pair(chess.board,"Game $parameter opened. Play with white pieces."))
    }
}

/**
 * Command to join a game as a player with the color BLACK.
 */
class JoinCommand(private val chess: Chess) : Commands {
    override fun execute(parameter: String?): Result {
        if (parameter == null) return ERROR("ERROR: Missing game name.")
        val gameId = GameName(parameter)



        if(!chess.dataBase.doesGameExist(gameId)) return ERROR("ERROR: Game $parameter does not exist.")

        chess.currentPlayer = Player.BLACK
        chess.board =  updateNewBoard(chess.dataBase, gameId, Board())
        chess.currentGameId = gameId
        //Se inicializarmos um jogo novo não há lastMove logo dá erro. Temos de arranjar isso
        //if(chess.domain.getCheck(chess.board)) return ERROR("Your king is in check please protect or move the king")
        return CONTINUE(Pair(chess.board,"Join to game $parameter. Play with black pieces."))
    }
}





/**
 * Command to move a piece on the board and update the database with the new move.
 */
class PlayCommand(private val chess: Chess) : Commands {

    override fun execute(parameter: String?): Result {
        if (parameter == null || parameter.isEmpty()) return ERROR("ERROR: Missing move.")

        val gameId = chess.currentGameId ?: return ERROR("ERROR: Can't play without a game: try open or join commands.")

        if(chess.board.getPlayerColor() != chess.currentPlayer) return ERROR("ERROR: Wait for your turn: try refresh command.")

        val filteredInput = filterInput(parameter,chess.board) ?: return ERROR("Illegal move $parameter. Unrecognized Play. Use format: [<piece>][<from>][x]<to>[=<piece>].")

        if(!chess.board. isPlayerMovingOwnPieces(filteredInput.filteredMove)) {
            return ERROR("You can't move the other player's pieces")
        }
        if(!chess.board.isPlayerMovingTheRightPieces(filteredInput.filteredMove)) {
            return ERROR("Piece in input doesn't match the piece at the position")
        }

        val movement = canPieceMoveTo(filteredInput.filteredMove, chess.board)
        when(movement){
            MoveType.ILLEGAL -> return ERROR("Illegal move $parameter. Illegal move.");

            MoveType.CASTLE -> {
                if(filteredInput.databaseMove.contains("x") || filteredInput.databaseMove.contains("="))
                    return ERROR("Illegal move $parameter. Unrecognized Play. Use format: [<piece>][<from>][x]<to>[=<piece>].")
                val moves = chess.board.doCastling( Move(filteredInput.filteredMove))
                moves.forEach {
                    chess.dataBase.addMoveToDb(it, gameId)
                }
                return CONTINUE(Pair(chess.board,null))
            }
            MoveType.PROMOTION -> {
                if(filteredInput.databaseMove.contains("x"))
                    return ERROR("Illegal move $parameter. Unrecognized Play. Use format: [<piece>][<from>][x]<to>[=<piece>].")
                if (filteredInput.databaseMove.contains("=")) {
            //        chess.board.promotePiece(
                //        filteredInput.filteredMove.substring(1, 2).toSquare(),
                  //      filteredInput.filteredMove.last()
              //      )
                }else{
              //      chess.board.promotePiece(
                 //       filteredInput.filteredMove.substring(1, 2).toSquare()
                //    )

                }

            }

            MoveType.REGULAR ->{ if(filteredInput.databaseMove.contains("x") || filteredInput.databaseMove.contains("="))
               return ERROR("Illegal move $parameter. Unrecognized Play. Use format: [<piece>][<from>][x]<to>[=<piece>].")
            }
        }



        chess.board.makeMove(filteredInput.filteredMove)
        chess.dataBase.addMoveToDb(Move(filteredInput.databaseMove),gameId)





        return CONTINUE(Pair(chess.board,null))
    }
}




/**
 * Command to get the current board.
 */
class RefreshCommand(private val chess: Chess) : Commands {

    override fun execute(parameter: String?): Result {


        val gameId = chess.currentGameId ?: return ERROR("ERROR: Can't refresh without a game: try open or join commands.")

        if(chess.currentPlayer == chess.board.getPlayerColor()) return ERROR("ERROR: It's your turn: try play.")

        val board = updateNewBoard(chess.dataBase, gameId, Board())



        if (board.toString() != chess.board.toString()){
            chess.board = board
        }
       // if(chess.domain.getCheck(chess.board)) return ERROR("Your king is in check please protect or move the king")
        return CONTINUE(Pair(board,null))
    }
}

/**
 * Command to show the previous moves played.
 */
class MovesCommand(private val chess: Chess) : Commands {
    override fun execute(parameter: String?): Result {
        val gameId = chess.currentGameId ?: return ERROR("No game, no moves.")
        val moves = chess.dataBase.getAllMoves(gameId)
        if (moves.count() == 0) return ERROR("No moves have been made yet.")


        return CONTINUE(moves)
    }
}
/**
 * Command to quit the game.
 */
class ExitCommand: Commands {
    override fun execute(parameter: String?) = EXIT("BYE.")
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
            board.makeMove(moveFiltered.filteredMove)
        }else acc
    }


/**
 * @param input          the input to filter
 * @param board          the board to filter the input on
 * @return  A moves data class with the filtered input and the database move
 * Allows only the moves that can be played on the board.
 */
private fun filterInput(input: String, board: Board): Moves? {
    val filtered = Regex("([RNBQKPrnbqkp])([abcdefgh])([12345678])x?([abcdefgh])([12345678])=?([NBQR])?")
    val filteredPawn = Regex("([abcdefgh])([12345678])")
    val removableInput = Regex("x?(=([NBQR]))?")
    val filteredMove = input.replace(removableInput,"")


    if(!filtered.matches(input)  && (input.length == PAWN_INPUT && !filteredPawn.matches(input))) return null

    if(input.length == PAWN_INPUT && filteredPawn.matches(input)){
        val tracePawn = traceBackPawn(input, board)
        return if(tracePawn == null) null
        else Moves(tracePawn,tracePawn)
    }
    if(!filtered.matches(filteredMove)) return null
    return Moves(filteredMove,input)

}



