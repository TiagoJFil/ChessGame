package chess.domain.commands

import Board
import chess.Chess
import chess.GameName
import chess.domain.Player

fun openAction(gameName: String,chess: Chess): Chess {
    val gameId = GameName(gameName)
    val gameExists = chess.dataBase.createGameDocumentIfItNotExists(gameId)

    val board = if (gameExists) {
        updateNewBoard(chess.dataBase, gameId, Board())
    }else{
        Board()
    }


    return Chess(board,chess.dataBase,gameId,Player.WHITE)

}


fun joinAction(gameName: String,chess: Chess): Chess {
    val gameId = GameName(gameName)
    val board = updateNewBoard(chess.dataBase, gameId, Board())

    return Chess(board,chess.dataBase,gameId,Player.BLACK)
}